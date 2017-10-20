/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.domainkeeper;

import java.util.List;
import java.util.Optional;

import diarsid.beam.core.base.analyze.variantsweight.WeightedVariants;
import diarsid.beam.core.base.control.flow.ValueFlow;
import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.control.io.base.interaction.Answer;
import diarsid.beam.core.base.control.io.base.interaction.Help;
import diarsid.beam.core.domain.entities.LocationSubPath;
import diarsid.beam.core.modules.data.DaoLocationSubPathChoices;
import diarsid.beam.core.modules.data.DaoLocationSubPaths;

import static java.util.stream.Collectors.toList;

import static diarsid.beam.core.base.analyze.variantsweight.Analyze.weightVariants;
import static diarsid.beam.core.base.control.flow.Flows.valueFlowCompletedEmpty;
import static diarsid.beam.core.base.control.flow.Flows.valueFlowCompletedWith;
import static diarsid.beam.core.base.control.flow.Flows.valueFlowStopped;
import static diarsid.beam.core.base.control.io.base.interaction.Variants.toVariants;
import static diarsid.beam.core.base.util.CollectionsUtils.getOne;
import static diarsid.beam.core.base.util.CollectionsUtils.hasMany;
import static diarsid.beam.core.base.util.CollectionsUtils.hasOne;
import static diarsid.beam.core.base.util.ConcurrencyUtil.asyncDo;


class LocationSubPathKeeperWorker implements LocationSubPathKeeper {

    private final DaoLocationSubPaths daoSubPaths;
    private final DaoLocationSubPathChoices daoSubPathChoices;
    private final InnerIoEngine ioEngine;
    private final Help chooseOneSubPathHelp;
    
    LocationSubPathKeeperWorker(
            DaoLocationSubPaths daoSubPaths,
            DaoLocationSubPathChoices daoSubPathChoices,
            InnerIoEngine ioEngine) {        
        this.daoSubPaths = daoSubPaths;
        this.daoSubPathChoices = daoSubPathChoices;
        this.ioEngine = ioEngine;
        this.chooseOneSubPathHelp = this.ioEngine.addToHelpContext(
                "Choose one Location subpath from given variants.",
                "Use:",
                "   - number to choose subpath",
                "   - unique part of subpath to choose it",
                "   - n/no to see other found subpaths if any",
                "   - dot to break"
        );
    }    
    
    private ValueFlow<LocationSubPath> getOneSubPathIfPointsToDirectory(List<LocationSubPath> subPaths) {
        LocationSubPath subPath = getOne(subPaths);
        if ( subPath.pointsToDirectory() ) {
            return valueFlowCompletedWith(subPath);
        } else {
            return valueFlowCompletedEmpty();
        }
    } 

    @Override
    public ValueFlow<LocationSubPath> findLocationSubPath(
            Initiator initiator, String pattern) {
        List<LocationSubPath> subPaths = this.daoSubPaths.getSubPathesByPattern(initiator, pattern);
        if ( hasOne(subPaths) ) {
            // TODO HIGH weight single variant or test for similarity
            return this.getOneSubPathIfPointsToDirectory(subPaths);
        } else if ( hasMany(subPaths) ) {
            return this.chooseOneSubPathsFromMany(initiator, pattern, subPaths);
        } else {
            return valueFlowCompletedEmpty();
        }    
    }   
    
    private ValueFlow<LocationSubPath> chooseOneSubPathsFromMany(
            Initiator initiator, String pattern, List<LocationSubPath> subPaths) {
        subPaths = subPaths
                .stream()
                .filter(subPath -> subPath.pointsToDirectory())
                .collect(toList());
        if ( hasOne(subPaths) ) {
            return this.getOneSubPathIfPointsToDirectory(subPaths);
        } else if ( hasMany(subPaths) ) {
            return this.resolveManySubPaths(initiator, pattern, subPaths);
        } else {
            return valueFlowCompletedEmpty();
        }    
    }    
    
    private ValueFlow<LocationSubPath> resolveManySubPaths(
            Initiator initiator, String pattern, List<LocationSubPath> subPaths) {
        WeightedVariants variants = weightVariants(pattern, toVariants(subPaths));
        if ( variants.hasOne() ) {
            return valueFlowCompletedWith(subPaths.get(variants.best().index()));
        } else if ( variants.hasMany() ) {
            return this.resolveManySubPathsVariants(initiator, pattern, subPaths, variants);
        } else {
            return valueFlowCompletedEmpty();
        }
    }
    
    private ValueFlow<LocationSubPath> resolveManySubPathsVariants(
            Initiator initiator, 
            String pattern, 
            List<LocationSubPath> subPaths, 
            WeightedVariants variants) {
        Optional<LocationSubPath> chosenSubPath = this.daoSubPathChoices
                .getChoiceFor(initiator, pattern, variants);
        if ( chosenSubPath.isPresent() ) {
            return valueFlowCompletedWith(chosenSubPath.get());
        } else {
            return this.askToChooseOneSubPathAndSaveChoice(initiator, pattern, subPaths, variants);
        }     
    }
    
    private ValueFlow<LocationSubPath> askToChooseOneSubPathAndSaveChoice(
            Initiator initiator, 
            String pattern, 
            List<LocationSubPath> subPaths, 
            WeightedVariants variants) {
        Answer answer = this.ioEngine.chooseInWeightedVariants(
                initiator, variants, this.chooseOneSubPathHelp);
        if ( answer.isGiven() ) {
            LocationSubPath subPath = subPaths.get(answer.index());
            asyncDo(() -> {
                this.daoSubPathChoices.save(initiator, subPath, pattern, variants);
            });
            return valueFlowCompletedWith(subPath);
        } else if ( answer.isRejection() ) {
            return valueFlowStopped();
        } else if ( answer.variantsAreNotSatisfactory() ) {
            return valueFlowCompletedEmpty();
        } else {
            return valueFlowCompletedEmpty();
        }
    }
}
