/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.domainkeeper;

import java.util.List;
import java.util.Optional;

import diarsid.beam.core.base.analyze.variantsweight.Variant;
import diarsid.beam.core.base.analyze.variantsweight.Variants;
import diarsid.beam.core.base.analyze.variantsweight.WeightAnalyzeReal;
import diarsid.beam.core.base.control.flow.ValueFlow;
import diarsid.beam.core.base.control.flow.VoidFlow;
import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.control.io.base.interaction.Answer;
import diarsid.beam.core.base.control.io.base.interaction.Choice;
import diarsid.beam.core.base.control.io.base.interaction.Help;
import diarsid.beam.core.domain.entities.Location;
import diarsid.beam.core.domain.entities.LocationSubPath;
import diarsid.beam.core.modules.responsivedata.ResponsiveDaoLocationSubPathChoices;
import diarsid.beam.core.modules.responsivedata.ResponsiveDaoLocationSubPaths;

import static java.util.stream.Collectors.toList;

import static diarsid.beam.core.base.control.flow.FlowResult.DONE;
import static diarsid.beam.core.base.control.flow.Flows.valueFlowDoneEmpty;
import static diarsid.beam.core.base.control.flow.Flows.valueFlowDoneWith;
import static diarsid.beam.core.base.control.flow.Flows.valueFlowFail;
import static diarsid.beam.core.base.control.flow.Flows.valueFlowStopped;
import static diarsid.beam.core.base.control.io.base.interaction.VariantConversions.toVariants;
import static diarsid.beam.core.base.util.CollectionsUtils.getOne;
import static diarsid.beam.core.base.util.CollectionsUtils.hasMany;
import static diarsid.beam.core.base.util.CollectionsUtils.hasOne;
import static diarsid.beam.core.base.util.ConcurrencyUtil.asyncDo;


class LocationSubPathKeeperWorker implements LocationSubPathKeeper {

    private final ResponsiveDaoLocationSubPaths daoSubPaths;
    private final ResponsiveDaoLocationSubPathChoices daoSubPathChoices;
    private final InnerIoEngine ioEngine;
    private final WeightAnalyzeReal analyze;
    private final Help getOneSubPathHelp;
    private final Help chooseOneSubPathHelp;
    
    LocationSubPathKeeperWorker(
            ResponsiveDaoLocationSubPaths daoSubPaths,
            ResponsiveDaoLocationSubPathChoices daoSubPathChoices,
            WeightAnalyzeReal analyze, 
            InnerIoEngine ioEngine) {        
        this.daoSubPaths = daoSubPaths;
        this.daoSubPathChoices = daoSubPathChoices;
        this.ioEngine = ioEngine;
        this.analyze = analyze;
        this.getOneSubPathHelp = this.ioEngine.addToHelpContext(
                "Choose if use this supbath to find specified target.",
                "Use: ",
                "   - n/no to reject",
                "   - y/yes to submit",
                "   - dot to break"
        );
        this.chooseOneSubPathHelp = this.ioEngine.addToHelpContext(
                "Choose one Location subpath from given variants.",
                "Use:",
                "   - number to choose subpath",
                "   - unique part of subpath to choose it",
                "   - n/no to see other found subpaths if any",
                "   - dot to break"
        );
    }  

    @Override
    public List<Variant> findAllLocationSubPaths(
            Initiator initiator, Location location, String pattern) {
        List<LocationSubPath> subPaths = this.daoSubPaths
                .getSubPathesByPattern(initiator, location, pattern);
        return this.analyze.weightVariantsList(pattern, toVariants(subPaths));
    }

    @Override
    public ValueFlow<LocationSubPath> findLocationSubPath(
            Initiator initiator, Location location, String pattern) {
        List<LocationSubPath> subPaths = this.daoSubPaths
                .getSubPathesByPattern(initiator, location, pattern);
        return this.resloveFoundSubPaths(initiator, location.relativePathTo(pattern), subPaths);
    }

    @Override
    public ValueFlow<LocationSubPath> findLocationSubPath(
            Initiator initiator, String pattern) {
        List<LocationSubPath> subPaths = this.daoSubPaths
                .getSubPathesByPattern(initiator, pattern);
        return this.resloveFoundSubPaths(initiator, pattern, subPaths);
    }     

    private ValueFlow<LocationSubPath> resloveFoundSubPaths(
            Initiator initiator, String pattern, List<LocationSubPath> subPaths) {
        if ( hasOne(subPaths) ) {
            LocationSubPath subPath = getOne(subPaths);
            if ( subPath.pattern().equalsIgnoreCase(pattern) ) {
                return valueFlowDoneWith(subPath);
            } else {
                return this.askAboutSubPathIfSatisfiable(initiator, pattern, subPath);
            }            
        } else if ( hasMany(subPaths) ) {
            return this.chooseOneSubPathsFromMany(initiator, pattern, subPaths);
        } else {
            return valueFlowDoneEmpty();
        }
    }     
    
    private ValueFlow<LocationSubPath> askAboutSubPath(
            Initiator initiator, LocationSubPath subPath, String pattern) {
        Choice choice = this.ioEngine.ask(
                initiator, subPath.variantDisplayName(), this.getOneSubPathHelp);
        if ( choice.isPositive() ) {
            asyncDo(() -> {
                this.daoSubPathChoices.saveSingle(initiator, subPath, pattern);
            });
            return valueFlowDoneWith(subPath);
        } else if ( choice.isNegative() ) {
            VoidFlow removeFlow = this.daoSubPathChoices.remove(initiator, subPath);
            if ( removeFlow.result().is(DONE) ) {
                return this.findLocationSubPath(initiator, subPath.pattern());
            } else {
                return valueFlowFail(removeFlow.message());
            }            
        } else if ( choice.isRejected() ) {
            return valueFlowStopped();
        } else {
            return valueFlowDoneEmpty();
        }
    }
    
    private ValueFlow<LocationSubPath> resolveOneSubPathFrom(
            Initiator initiator, String pattern, LocationSubPath subPath) {
        if ( subPath.pointsToDirectory() ) {
            if ( this.daoSubPathChoices.isChoiceExistsForSingle(initiator, subPath, pattern) ) {
                return valueFlowDoneWith(subPath);
            } else {
                return this.askAboutSubPathIfSatisfiable(initiator, pattern, subPath);
            }            
        } else {
            return valueFlowDoneEmpty();
        }
    } 

    private ValueFlow<LocationSubPath> askAboutSubPathIfSatisfiable(
            Initiator initiator, String pattern, LocationSubPath subPath) {
        if ( this.analyze.isSatisfiable(pattern, subPath.toSingleVariant()) ) {
            return this.askAboutSubPath(initiator, subPath, pattern);
        } else {
            return valueFlowDoneEmpty();
        }
    }
    
    private ValueFlow<LocationSubPath> chooseOneSubPathsFromMany(
            Initiator initiator, String pattern, List<LocationSubPath> subPaths) {
        subPaths = subPaths
                .stream()
                .filter(subPath -> subPath.pointsToDirectory())
                .collect(toList());
        if ( hasOne(subPaths) ) {
            return this.resolveOneSubPathFrom(initiator, pattern, getOne(subPaths));
        } else if ( hasMany(subPaths) ) {
            return this.resolveManySubPaths(initiator, pattern, subPaths);
        } else {
            return valueFlowDoneEmpty();
        }    
    }    
    
    private ValueFlow<LocationSubPath> resolveManySubPaths(
            Initiator initiator, String pattern, List<LocationSubPath> subPaths) {
        Variants variants = this.analyze.weightVariants(pattern, toVariants(subPaths));
        if ( variants.hasOne() ) {
            LocationSubPath chosen = subPaths.get(variants.best().index());
            return this.resolveOneSubPathFrom(initiator, pattern, chosen);
        } else if ( variants.hasMany() ) {
            return this.resolveManySubPathsVariants(initiator, pattern, subPaths, variants);
        } else {
            return valueFlowDoneEmpty();
        }
    }
    
    private ValueFlow<LocationSubPath> resolveManySubPathsVariants(
            Initiator initiator, 
            String pattern, 
            List<LocationSubPath> subPaths, 
            Variants variants) {
        Optional<LocationSubPath> chosenSubPath = this.daoSubPathChoices.getChoiceFor(
                initiator, pattern, variants);
        if ( chosenSubPath.isPresent() ) {
            if ( chosenSubPath.get().name().equalsIgnoreCase(variants.best().value()) ) {
                return valueFlowDoneWith(chosenSubPath);
            } else {
                return this.askAboutSubPath(initiator, chosenSubPath.get(), pattern);
            }            
        } else {
            return this.askToChooseOneSubPathAndSaveChoice(initiator, pattern, subPaths, variants);
        }     
    }
    
    private ValueFlow<LocationSubPath> askToChooseOneSubPathAndSaveChoice(
            Initiator initiator, 
            String pattern, 
            List<LocationSubPath> subPaths, 
            Variants variants) {
        Answer answer = this.ioEngine.ask(
                initiator, variants, this.chooseOneSubPathHelp);
        if ( answer.isGiven() ) {
            LocationSubPath subPath = subPaths.get(answer.index());
            asyncDo(() -> {
                this.daoSubPathChoices.saveWithVariants(initiator, subPath, pattern, variants);
            });
            return valueFlowDoneWith(subPath);
        } else if ( answer.isRejection() ) {
            return valueFlowStopped();
        } else if ( answer.variantsAreNotSatisfactory() ) {
            return valueFlowDoneEmpty();
        } else {
            return valueFlowDoneEmpty();
        }
    }
}
