/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.data.database.sql;

import diarsid.beam.core.application.environment.Configuration;
import diarsid.beam.core.modules.data.DataBase;
import diarsid.jdbc.transactions.JdbcConnectionsSource;
import diarsid.jdbc.transactions.core.JdbcPreparedStatementSetter;
import diarsid.jdbc.transactions.core.JdbcTransactionFactory;
import diarsid.jdbc.transactions.core.JdbcTransactionGuard;


public class H2DataBase implements DataBase {
    
    private final JdbcTransactionFactory transactionFactory;
    
    public H2DataBase(Configuration configuration) {
        JdbcConnectionsSource source = new H2JdbcConnectionsSource(
                "jdbc:h2:" + configuration.asString("data.store") + "/BeamData", 
                configuration.asString("data.user"), 
                configuration.asString("data.pass"));
        JdbcTransactionGuard transactionGuard = new JdbcTransactionGuard(100500);
        JdbcPreparedStatementSetter paramsSetter = new JdbcPreparedStatementSetter();
        this.transactionFactory = new JdbcTransactionFactory(
                source, transactionGuard, paramsSetter);
        this.transactionFactory.logHistory(configuration.asBoolean("data.log"));
    }

    @Override
    public JdbcTransactionFactory transactionFactory() {
        return this.transactionFactory;
    }

    @Override
    public void disconnect() {
        this.transactionFactory.close();
    }
}
