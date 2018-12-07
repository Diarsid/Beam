/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.data;

/**
 *
 * @author Diarsid
 */
public abstract class DataException extends Exception {
    
    public DataException(String msg) {
        super(msg);
    }
    
    public DataException(Exception e) {
        super(e);
    }
    
}
