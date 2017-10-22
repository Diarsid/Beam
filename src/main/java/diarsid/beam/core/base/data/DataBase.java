/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.data;

import diarsid.jdbc.transactions.core.JdbcTransactionFactory;

/**
 *
 * @author Diarsid
 */
public interface DataBase {
    
    JdbcTransactionFactory transactionFactory();
    
    void disconnect();
    
    DataBaseType type();
    
}
