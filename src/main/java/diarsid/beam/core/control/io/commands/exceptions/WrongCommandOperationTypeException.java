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
public class WrongCommandOperationTypeException extends RuntimeException {

    /**
     * Constructs an instance of <code>WrongCommandOperationTypeException</code> with the specified
     * detail message.
     *
     * @param msg the detail message.
     */
    public WrongCommandOperationTypeException(String msg) {
        super(msg);
    }
}
