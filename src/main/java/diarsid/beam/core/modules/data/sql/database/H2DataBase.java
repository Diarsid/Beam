/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.data.sql.database;

import diarsid.support.configuration.Configuration;
import diarsid.beam.core.base.data.DataBase;
import diarsid.beam.core.base.data.DataBaseType;
import diarsid.jdbc.transactions.JdbcConnectionsSource;
import diarsid.jdbc.transactions.core.JdbcTransactionFactory;

import static diarsid.beam.core.base.data.DataBaseType.SQL;
import static diarsid.jdbc.transactions.core.JdbcTransactionFactoryBuilder.buildTransactionFactoryWith;


public class H2DataBase implements DataBase {
    
    private final JdbcTransactionFactory transactionFactory;
    
    public H2DataBase(Configuration configuration) {
        JdbcConnectionsSource source = new H2JdbcConnectionsSource(
                "jdbc:h2:" + configuration.asString("data.store") + "/BeamData", 
                configuration.asString("data.user"), 
                configuration.asString("data.pass"));
        this.transactionFactory = buildTransactionFactoryWith(source)
//                .withGuardWaitingOnSeconds(1)
                .done();
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
    
    @Override
    public DataBaseType type() {
        return SQL;
    }
}
