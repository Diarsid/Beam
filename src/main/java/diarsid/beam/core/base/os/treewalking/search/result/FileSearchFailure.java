/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.os.treewalking.search.result;

/**
 *
 * @author Diarsid
 */
public interface FileSearchFailure {
    
    boolean locationNotFound();
    
    boolean targetNotFound();
    
    boolean targetNotAccessible();
    
    boolean hasTargetInvalidMessage();
    
    String getMessage();
}
