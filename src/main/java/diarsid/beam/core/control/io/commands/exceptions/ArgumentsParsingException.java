/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.control.io.commands.exceptions;

/**
 *
 * @author Diarsid
 */
public class ArgumentsParsingException extends RuntimeException {

    /**
     * Creates a new instance of <code>ArgumentsParsingException</code> without detail message.
     */
    public ArgumentsParsingException() {
    }

    /**
     * Constructs an instance of <code>ArgumentsParsingException</code> with the specified detail
     * message.
     *
     * @param msg the detail message.
     */
    public ArgumentsParsingException(String msg) {
        super(msg);
    }
}
