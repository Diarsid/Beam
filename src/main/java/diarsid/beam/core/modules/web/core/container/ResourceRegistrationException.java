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
public class ResourceRegistrationException extends RuntimeException {

    /**
     * Creates a new instance of <code>ResourceCreationException</code> without detail message.
     */
    public ResourceRegistrationException() {
    }

    /**
     * Constructs an instance of <code>ResourceCreationException</code> with the specified detail
     * message.
     *
     * @param msg the detail message.
     */
    public ResourceRegistrationException(String msg) {
        super(msg);
    }
}
