/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.systemconsole;

/**
 *
 * @author Diarsid
 */
public class StartupFailedException extends RuntimeException {

    public StartupFailedException(Throwable t) {
        super(t);
    }
    
    /**
     * Constructs an instance of <code>StartupFailedException</code> with the specified detail
     * message.
     *
     * @param msg the detail message.
     */
    public StartupFailedException(String msg) {
        super(msg);
    }
}
