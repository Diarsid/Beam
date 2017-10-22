/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.data;

import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.data.DataBase;
import diarsid.jdbc.transactions.JdbcTransaction;
import diarsid.jdbc.transactions.exceptions.TransactionHandledSQLException;

/**
 *
 * @author Diarsid
 */
public abstract class BeamCommonDao {
    
    private final DataBase dataBase;
    private final InnerIoEngine ioEngine;
    
    public BeamCommonDao(DataBase dataBase, InnerIoEngine ioEngine) {
        this.dataBase = dataBase;
        this.ioEngine = ioEngine;
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
    
    protected InnerIoEngine ioEngine() {
        return this.ioEngine;
    }
}
