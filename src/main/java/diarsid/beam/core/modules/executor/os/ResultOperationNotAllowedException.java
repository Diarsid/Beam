/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.executor.os;

/**
 *
 * @author Diarsid
 */
public class ResultOperationNotAllowedException extends RuntimeException {

    public ResultOperationNotAllowedException(String msg) {
        super(msg);
    }
}