/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.control.flow;

/**
 *
 * @author Diarsid
 */
public enum OperationResult {
    OK,
    FAIL,
    STOP;
    
    public boolean is(OperationResult result) {
        return this.equals(result);
    }
    
    public boolean isNot(OperationResult result) {
        return ! this.equals(result);
    }
}
