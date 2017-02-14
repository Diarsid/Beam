/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.control.io.commands.exceptions;

/**
 *
 * @author Diarsid
 */
public class ParsingSingleArgumentFromMultipleException extends RuntimeException {

    /**
     * Creates a new instance of <code>ParsingSingleArgumentFromMultipleException</code> without
     * detail message.
     */
    public ParsingSingleArgumentFromMultipleException() {
    }

    /**
     * Constructs an instance of <code>ParsingSingleArgumentFromMultipleException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public ParsingSingleArgumentFromMultipleException(String msg) {
        super(msg);
    }
}
