/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.data;

import java.util.Optional;

import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.domain.entities.Picture;

/**
 *
 * @author Diarsid
 */
public interface DaoPictures {
    
    Optional<Picture> getByName(Initiator initiator, String name);
    
    boolean save(Initiator initiator, Picture image);
    
    boolean removeByName(Initiator initiator, String name);
    
    boolean remove(Initiator initiator, Picture image);
}
