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
public class DataExtractionException extends DataException {
    
    private final boolean isLogical;

    public DataExtractionException(String msg) {
        super(msg);
        this.isLogical = true;
    }    
    
    public DataExtractionException(Exception e) {
        super(e);
        this.isLogical = false;
    }
    
    public boolean isLogical() {
        return this.isLogical;
    }
    
}
