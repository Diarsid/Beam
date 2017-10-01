/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.domainkeeper;

import java.util.List;
import java.util.Set;

import diarsid.beam.core.base.analyze.variantsweight.WeightedVariants;
import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.control.io.base.interaction.Answer;
import diarsid.beam.core.base.control.io.base.interaction.Help;
import diarsid.beam.core.base.control.io.commands.CommandType;
import diarsid.beam.core.base.control.io.commands.executor.InvocationCommand;
import diarsid.beam.core.domain.entities.NamedEntity;
import diarsid.beam.core.modules.data.DaoNamedEntities;

import static diarsid.beam.core.base.analyze.variantsweight.Analyze.weightVariants;
import static diarsid.beam.core.base.control.io.base.interaction.Variants.entitiesToVariants;
import static diarsid.beam.core.base.control.io.commands.CommandType.EXECUTOR_DEFAULT;
import static diarsid.beam.core.base.util.CollectionsUtils.getOne;
import static diarsid.beam.core.base.util.CollectionsUtils.hasMany;
import static diarsid.beam.core.base.util.CollectionsUtils.hasOne;
import static diarsid.beam.core.base.util.CollectionsUtils.toSet;
import static diarsid.beam.core.base.util.Logs.debug;

import diarsid.beam.core.base.control.flow.ValueFlow;

import static diarsid.beam.core.base.control.flow.Flows.valueFlowCompletedWith;
import static diarsid.beam.core.base.control.flow.Flows.valueFlowCompletedWith;
import static diarsid.beam.core.base.control.flow.Flows.valueFlowCompletedWith;
import static diarsid.beam.core.base.control.flow.Flows.valueFlowCompletedWith;
import static diarsid.beam.core.base.control.flow.Flows.valueFlowCompletedEmpty;
import static diarsid.beam.core.base.control.flow.Flows.valueFlowStopped;

/**
 *
 * @author Diarsid
 */
class NamedEntitiesKeeperWorker implements NamedEntitiesKeeper {
    
    private final InnerIoEngine ioEngine;
    private final DaoNamedEntities namedEntitiesDao;
    private final Set<CommandType> subjectedCommandTypes;
    private final Help chooseOneEntityHelp;

    NamedEntitiesKeeperWorker(InnerIoEngine ioEngine, DaoNamedEntities dao) {
        this.ioEngine = ioEngine;
        this.namedEntitiesDao = dao;
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
    public ValueFlow<? extends NamedEntity> findByExactName(
            Initiator initiator, String name) {
        debug("[ALL ENTITIES KEEPER] [by exact name] " + name);
        return valueFlowCompletedWith(this.namedEntitiesDao.getByExactName(initiator, name));
    }

    @Override
    public ValueFlow<? extends NamedEntity> findByNamePattern(
            Initiator initiator, String pattern) {
        debug("[ALL ENTITIES KEEPER] [by name pattern] " + pattern);
        List<NamedEntity> entities = 
                this.namedEntitiesDao.getEntitiesByNamePattern(initiator, pattern);        
        if ( hasOne(entities) ) {
            debug("[ALL ENTITIES KEEPER] [by name pattern] one : " + getOne(entities).name());
            return valueFlowCompletedWith(getOne(entities));
        } else if ( hasMany(entities) ) {
            debug("[ALL ENTITIES KEEPER] [by name pattern] many : " + entities.size());
            return this.manageWithMultipleEntities(initiator, pattern, entities);
        } else {
            return valueFlowCompletedEmpty();
        }
    }
    
    private ValueFlow<? extends NamedEntity> manageWithMultipleEntities(
            Initiator initiator, String pattern, List<NamedEntity> entities) {
        WeightedVariants variants = weightVariants(pattern, entitiesToVariants(entities));
        if ( variants.isEmpty() ) {
            return valueFlowCompletedEmpty();
        }
        Answer answer = this.ioEngine.chooseInWeightedVariants(
                initiator, variants, this.chooseOneEntityHelp);
        if ( answer.isGiven() ) {
            return valueFlowCompletedWith(entities.get(answer.index()));
        } else {
            if ( answer.isRejection() ) {                
                return valueFlowStopped();
            } else if ( answer.variantsAreNotSatisfactory() ) {
                return valueFlowCompletedEmpty();
            } else {
                return valueFlowCompletedEmpty();
            }
        }
    }   
}
