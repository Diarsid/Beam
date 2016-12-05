/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.domain.commands.exceptions;

/**
 *
 * @author Diarsid
 */
public class UndefinedOperationTypeException extends RuntimeException {

    /**
     * Creates a new instance of <code>UndefinedOperationTypeException</code> without detail
     * message.
     */
    public UndefinedOperationTypeException() {
    }

    /**
     * Constructs an instance of <code>UndefinedOperationTypeException</code> with the specified
     * detail message.
     *
     * @param msg the detail message.
     */
    public UndefinedOperationTypeException(String msg) {
        super(msg);
    }
}
