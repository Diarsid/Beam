/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.os.treewalking.advanced;

import diarsid.beam.core.base.control.io.base.actors.Initiator;

/**
 *
 * @author Diarsid
 */
public interface WalkingInPlace {
    
    WalkingByInitiator by(Initiator initiator);
}
