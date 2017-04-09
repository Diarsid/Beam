/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.domainkeeper;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.control.io.base.interaction.Answer;
import diarsid.beam.core.base.control.io.base.interaction.Question;
import diarsid.beam.core.base.control.io.commands.CommandType;
import diarsid.beam.core.base.control.io.commands.EntityInvocationCommand;
import diarsid.beam.core.domain.entities.NamedEntity;
import diarsid.beam.core.modules.data.DaoNamedEntities;

import static diarsid.beam.core.base.control.io.base.interaction.Question.question;
import static diarsid.beam.core.base.control.io.commands.CommandType.EXECUTOR_DEFAULT;
import static diarsid.beam.core.base.control.io.interpreter.ControlKeys.hasWildcard;
import static diarsid.beam.core.base.util.CollectionsUtils.getOne;
import static diarsid.beam.core.base.util.CollectionsUtils.hasMany;
import static diarsid.beam.core.base.util.CollectionsUtils.hasOne;
import static diarsid.beam.core.base.util.CollectionsUtils.toSet;
import static diarsid.beam.core.base.util.StringUtils.splitByWildcard;

/**
 *
 * @author Diarsid
 */
class DefaultNamedEntitiesKeeper implements NamedEntitiesKeeper {
    
    private final InnerIoEngine ioEngine;
    private final DaoNamedEntities dao;
    private final Set<CommandType> subjectedCommandTypes;

    DefaultNamedEntitiesKeeper(InnerIoEngine ioEngine, DaoNamedEntities dao) {
        this.ioEngine = ioEngine;
        this.dao = dao;
        this.subjectedCommandTypes = toSet(EXECUTOR_DEFAULT);
    }

    @Override
    public boolean isSubjectedTo(EntityInvocationCommand command) {
        return this.subjectedCommandTypes.contains(command.type());
    }

    @Override
    public Optional<NamedEntity> findByExactName(Initiator initiator, String name) {
        return this.dao.getByExactName(initiator, name);
    }

    @Override
    public Optional<NamedEntity> findByNamePattern(Initiator initiator, String pattern) {
        List<NamedEntity> entities;
        if ( hasWildcard(pattern) ) {
            entities = this.dao.getEntitiesByNamePatternParts(initiator, splitByWildcard(pattern));
        } else {
            entities = this.dao.getEntitiesByNamePattern(initiator, pattern);
        }
        
        if ( hasOne(entities) ) {
            return Optional.of(getOne(entities));
        } else if ( hasMany(entities) ) {
            return this.manageWithMultipleEntities(initiator, entities);
        } else {
            return Optional.empty();
        }
    }
    
    private Optional<NamedEntity> manageWithMultipleEntities(
            Initiator initiator, List<NamedEntity> entities) {
        Question question = question("choose").withAnswerEntities(entities);
        Answer answer = this.ioEngine.ask(initiator, question);
        if ( answer.isGiven() ) {
            // TODO
            // employ more sofisticated algorithm
            return Optional.of(entities.get(answer.index()));
        } else {
            return Optional.empty();
        }
    }
    
}
