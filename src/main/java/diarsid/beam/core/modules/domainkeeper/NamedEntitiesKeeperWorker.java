/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.domainkeeper;

import java.util.List;
import java.util.Set;

import diarsid.beam.core.base.analyze.variantsweight.Variants;
import diarsid.beam.core.base.analyze.variantsweight.WeightAnalyze;
import diarsid.beam.core.base.control.flow.ValueFlow;
import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.control.io.base.interaction.Answer;
import diarsid.beam.core.base.control.io.base.interaction.Help;
import diarsid.beam.core.base.control.io.base.interaction.Message;
import diarsid.beam.core.base.control.io.commands.CommandType;
import diarsid.beam.core.base.control.io.commands.executor.InvocationCommand;
import diarsid.beam.core.domain.entities.NamedEntity;
import diarsid.beam.core.modules.responsivedata.ResponsiveDaoNamedEntities;

import static diarsid.beam.core.base.control.flow.Flows.valueFlowDoneEmpty;
import static diarsid.beam.core.base.control.flow.Flows.valueFlowDoneWith;
import static diarsid.beam.core.base.control.flow.Flows.valueFlowStopped;
import static diarsid.beam.core.base.control.io.base.interaction.Messages.entitiesToOptionalMessageWithHeader;
import static diarsid.beam.core.base.control.io.base.interaction.VariantConversions.entitiesToVariants;
import static diarsid.beam.core.base.control.io.commands.CommandType.EXECUTOR_DEFAULT;
import static diarsid.beam.core.base.util.CollectionsUtils.getOne;
import static diarsid.beam.core.base.util.CollectionsUtils.hasMany;
import static diarsid.beam.core.base.util.CollectionsUtils.hasOne;
import static diarsid.beam.core.base.util.CollectionsUtils.toSet;

/**
 *
 * @author Diarsid
 */
class NamedEntitiesKeeperWorker implements NamedEntitiesKeeper<NamedEntity> {
    
    private final InnerIoEngine ioEngine;
    private final ResponsiveDaoNamedEntities namedEntitiesDao;
    private final WeightAnalyze analyze;
    private final Set<CommandType> subjectedCommandTypes;
    private final Help chooseOneEntityHelp;

    NamedEntitiesKeeperWorker(
            InnerIoEngine ioEngine, ResponsiveDaoNamedEntities dao, WeightAnalyze analyze) {
        this.ioEngine = ioEngine;
        this.namedEntitiesDao = dao;
        this.analyze = analyze;
        this.subjectedCommandTypes = toSet(EXECUTOR_DEFAULT);
        this.chooseOneEntityHelp = this.ioEngine.addToHelpContext(
                "Choose one variant.",
                "Use:",
                "   - variant number to choose it",
                "   - part of variant to choose it",
                "   - n/no to see other, less relevant variants",
                "   - dot to break"
        );
    }

    @Override
    public boolean isSubjectedTo(InvocationCommand command) {
        return this.subjectedCommandTypes.contains(command.type());
    }

    @Override
    public ValueFlow<NamedEntity> findByExactName(
            Initiator initiator, String name) {
        return valueFlowDoneWith(this.namedEntitiesDao.getByExactName(initiator, name));
    }

    @Override
    public ValueFlow<NamedEntity> findByNamePattern(
            Initiator initiator, String pattern) {
        List<NamedEntity> entities = 
                this.namedEntitiesDao.getEntitiesByNamePattern(initiator, pattern);        
        if ( hasOne(entities) ) {
            NamedEntity entity = getOne(entities);
            if ( this.analyze.isSatisfiable(pattern, entity.name()) ) {
                return valueFlowDoneWith(entity);
            } else {
                return valueFlowDoneEmpty();
            }            
        } else if ( hasMany(entities) ) {
            return this.manageWithMultipleEntities(initiator, pattern, entities);
        } else {
            return valueFlowDoneEmpty();
        }
    }
    
    private ValueFlow<NamedEntity> manageWithMultipleEntities(
            Initiator initiator, String pattern, List<NamedEntity> entities) {
        Variants variants = this.analyze.weightVariants(
                pattern, entitiesToVariants(entities));
        if ( variants.isEmpty() ) {
            return valueFlowDoneEmpty();
        }
        Answer answer = this.ioEngine.ask(
                initiator, variants, this.chooseOneEntityHelp);
        if ( answer.isGiven() ) {
            return valueFlowDoneWith(entities.get(answer.index()));
        } else {
            if ( answer.isRejection() ) {                
                return valueFlowStopped();
            } else if ( answer.variantsAreNotSatisfactory() ) {
                return valueFlowDoneEmpty();
            } else {
                return valueFlowDoneEmpty();
            }
        }
    } 

    @Override
    public ValueFlow<Message> findAll(Initiator initiator) {
        return valueFlowDoneWith(entitiesToOptionalMessageWithHeader(
                    "all named entities:", this.namedEntitiesDao.getAll(initiator)));
    }
}
