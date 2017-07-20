/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.web.core.container;

/**
 *
 * @author Diarsid
 */
public class JsonConversionException extends RuntimeException {
    
    public JsonConversionException(String msg) {
        super(msg);
    }
    
    public JsonConversionException(Throwable t) {
        super(t);
    }
}
