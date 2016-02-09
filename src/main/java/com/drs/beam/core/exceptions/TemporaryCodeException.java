/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.drs.beam.core.exceptions;

/**
 *
 * @author Diarsid
 */
public class TemporaryCodeException extends RuntimeException {
    
    public TemporaryCodeException() {
        super();
    }
    
    public TemporaryCodeException(String message) {
        super(message);
    }
}
