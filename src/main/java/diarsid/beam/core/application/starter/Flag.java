/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.application.starter;

/**
 *
 * @author Diarsid
 */
public interface Flag {
    
    String text();
    
    FlagType type();
    
    String description();
}
