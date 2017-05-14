/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.data.daos.sql;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import diarsid.beam.core.domain.entities.NamedEntity;
import diarsid.beam.core.domain.entities.NamedEntityType;

import static java.lang.Integer.MAX_VALUE;

import static diarsid.beam.core.domain.entities.NamedEntityType.BATCH;
import static diarsid.beam.core.domain.entities.NamedEntityType.LOCATION;
import static diarsid.beam.core.domain.entities.NamedEntityType.PROGRAM;
import static diarsid.beam.core.domain.entities.NamedEntityType.UNDEFINED_ENTITY;
import static diarsid.beam.core.domain.entities.NamedEntityType.WEBPAGE;

/**
 *
 * @author Diarsid
 */
class NamedEntityComparator implements Comparator<NamedEntity> {
    
    private static final Map<NamedEntityType, Integer> NAMED_ENTITY_PRIORITIES;
    static {
        NAMED_ENTITY_PRIORITIES = new HashMap<>();
        NAMED_ENTITY_PRIORITIES.put(LOCATION, 1);
        NAMED_ENTITY_PRIORITIES.put(WEBPAGE, 2);
        NAMED_ENTITY_PRIORITIES.put(PROGRAM, 3);
        NAMED_ENTITY_PRIORITIES.put(BATCH, 4);
        NAMED_ENTITY_PRIORITIES.put(UNDEFINED_ENTITY, MAX_VALUE);
    }

    NamedEntityComparator() {
    }
    
    private static int priorityOf(NamedEntity entity) {
        return NAMED_ENTITY_PRIORITIES.get(entity.type());
    }
    
    @Override
    public int compare(NamedEntity one, NamedEntity another) {
        int onePriority = priorityOf(one);
        int anotherPriority = priorityOf(another);
        if ( onePriority < anotherPriority ) {
            return -1;
        } else if ( onePriority > anotherPriority ) {
            return 1;
        } else {
            return 0;
        }
    }
}
