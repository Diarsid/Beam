/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.drs.beam.core.modules.tasks.exceptions;

/**
 *
 * @author Diarsid
 */
public class TaskTypeInvalidException extends RuntimeException {

    public TaskTypeInvalidException(String msg) {
        super(msg);
    }
}
