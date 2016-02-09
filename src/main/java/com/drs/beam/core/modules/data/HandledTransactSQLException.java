/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.drs.beam.core.modules.data;

import java.sql.SQLException;

/**
 *
 * @author Diarsid
 */
public class HandledTransactSQLException extends Exception {

    public HandledTransactSQLException(SQLException e) {
        super(e);
    }
}
