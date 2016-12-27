/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.exceptions;

/**
 *
 * @author Diarsid
 */
public class WorkflowBrokenException extends RuntimeException {

    public WorkflowBrokenException() {
    }
    
    public WorkflowBrokenException(Throwable t) {
        super(t);
    }

    public WorkflowBrokenException(String msg) {
        super(msg);
    }
}
