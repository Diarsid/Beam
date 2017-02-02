/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.os.search.result;

/**
 *
 * @author Diarsid
 */
public class ResultOperationNotAllowedException extends RuntimeException {

    /**
     * Constructs an instance of <code>ResultOperationNotAllowedException</code> with the specified
     * detail message.
     *
     * @param msg the detail message.
     */
    public ResultOperationNotAllowedException(String msg) {
        super(msg);
    }
}
