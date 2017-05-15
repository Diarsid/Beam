/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.domainkeeper;

import java.util.List;
import java.util.Set;

import diarsid.beam.core.base.control.flow.ValueOperation;
import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.control.io.base.interaction.Answer;
import diarsid.beam.core.base.control.io.commands.CommandType;
import diarsid.beam.core.base.control.io.commands.executor.InvocationCommand;
import diarsid.beam.core.domain.entities.NamedEntity;
import diarsid.beam.core.modules.data.DaoNamedEntities;

import static diarsid.beam.core.base.control.flow.Operations.valueCompletedEmpty;
import static diarsid.beam.core.base.control.flow.Operations.valueCompletedWith;
import static diarsid.beam.core.base.control.flow.Operations.valueOperationStopped;
import static diarsid.beam.core.base.control.io.base.interaction.Variants.entitiesToVariants;
import static diarsid.beam.core.base.control.io.commands.CommandType.EXECUTOR_DEFAULT;
import static diarsid.beam.core.base.util.CollectionsUtils.getOne;
import static diarsid.beam.core.base.util.CollectionsUtils.hasMany;
import static diarsid.beam.core.base.util.CollectionsUtils.hasOne;
import static diarsid.beam.core.base.util.CollectionsUtils.toSet;
import static diarsid.beam.core.base.util.Logs.debug;
import static diarsid.beam.core.domain.patternsanalyze.Analyze.analyzeAndWeightVariants;

/**
 *
 * @author Diarsid
 */
class AllNamedEntitiesKeeper implements NamedEntitiesKeeper {
    
    private final InnerIoEngine ioEngine;
    private final DaoNamedEntities namedEntitiesDao;
    private final Set<CommandType> subjectedCommandTypes;

    AllNamedEntitiesKeeper(InnerIoEngine ioEngine, DaoNamedEntities dao) {
        this.ioEngine = ioEngine;
        this.namedEntitiesDao = dao;
        this.subjectedCommandTypes = toSet(EXECUTOR_DEFAULT);
    }

    @Override
    public boolean isSubjectedTo(InvocationCommand command) {
        return this.subjectedCommandTypes.contains(command.type());
    }

    @Override
    public ValueOperation<? extends NamedEntity> findByExactName(
            Initiator initiator, String name) {
        debug("[ALL ENTITIES KEEPER] [by exact name] " + name);
        return valueCompletedWith(this.namedEntitiesDao.getByExactName(initiator, name));
    }

    @Override
    public ValueOperation<? extends NamedEntity> findByNamePattern(
            Initiator initiator, String pattern) {
        debug("[ALL ENTITIES KEEPER] [by name pattern] " + pattern);
        List<NamedEntity> entities = 
                this.namedEntitiesDao.getEntitiesByNamePattern(initiator, pattern);        
        if ( hasOne(entities) ) {
            debug("[ALL ENTITIES KEEPER] [by name pattern] one : " + getOne(entities).name());
            return valueCompletedWith(getOne(entities));
        } else if ( hasMany(entities) ) {
            debug("[ALL ENTITIES KEEPER] [by name pattern] many : " + entities.size());
            return this.manageWithMultipleEntities(initiator, pattern, entities);
        } else {
            return valueCompletedEmpty();
        }
    }
    
    private ValueOperation<? extends NamedEntity> manageWithMultipleEntities(
            Initiator initiator, String pattern, List<NamedEntity> entities) {
        Answer answer = this.ioEngine.chooseInWeightedVariants(
                initiator, analyzeAndWeightVariants(pattern, entitiesToVariants(entities)));
        if ( answer.isGiven() ) {
            return valueCompletedWith(entities.get(answer.index()));
        } else {
            return valueOperationStopped();
        }
    }   
}
