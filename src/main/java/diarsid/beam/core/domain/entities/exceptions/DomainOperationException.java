/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.domain.entities.exceptions;

/**
 *
 * @author Diarsid
 */
public class DomainOperationException extends RuntimeException {
    
    public DomainOperationException(String message) {
        super(message);
    }
}
