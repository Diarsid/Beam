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
public class ModuleInitializationException extends RuntimeException {
    
    public ModuleInitializationException() {
        super();
    }

    public ModuleInitializationException(String message) {
        super(message);
    }
}