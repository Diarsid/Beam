/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.data.sql.daos;

import diarsid.beam.core.base.data.DataBase;
import diarsid.beam.core.base.data.DataExtractionException;
import diarsid.beam.core.modules.data.Dao;
import diarsid.jdbc.transactions.JdbcTransaction;
import diarsid.jdbc.transactions.exceptions.TransactionHandledSQLException;

import static diarsid.support.log.Logging.logFor;

/**
 *
 * @author Diarsid
 */
abstract class BeamCommonDao implements Dao {
    
    private final DataBase dataBase;
    
    public BeamCommonDao(DataBase dataBase) {
        this.dataBase = dataBase;
    }    
    
    protected JdbcTransaction openDisposableTransaction() 
            throws TransactionHandledSQLException {
        return this.dataBase
                .transactionFactory()
                .createDisposableTransaction();
    }
    
    protected JdbcTransaction openTransaction() 
            throws TransactionHandledSQLException {
        return this.dataBase
                .transactionFactory()
                .createTransaction();
    }
    
    protected DataExtractionException logAndWrap(Exception e) {
        logFor(this).error(e.getMessage(), e);
        return new DataExtractionException(e);
    }
    
    protected DataExtractionException wrap(String message) {
        logFor(this).info(message);
        return new DataExtractionException(message);
    }
    
}
