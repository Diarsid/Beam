/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.data;

import java.util.List;

import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.domain.entities.NamedEntity;

/**
 *
 * @author Diarsid
 */
public interface DaoNamedEntities {
    
    List<NamedEntity> getEntitiesByNamePattern(
            Initiator initiator, String namePattern);
    
    List<NamedEntity> getEntitiesByNamePatternParts(
            Initiator initiator, List<String> namePatternParts);
}
