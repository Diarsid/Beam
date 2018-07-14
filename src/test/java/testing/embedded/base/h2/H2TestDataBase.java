/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package testing.embedded.base.h2;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.h2.jdbcx.JdbcConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import diarsid.beam.core.base.data.DataBaseType;
import diarsid.jdbc.transactions.JdbcConnectionsSource;
import diarsid.jdbc.transactions.core.JdbcTransactionFactory;

import static java.lang.String.format;
import static java.util.UUID.randomUUID;

import static diarsid.beam.core.application.environment.BeamEnvironment.configuration;
import static diarsid.beam.core.base.data.DataBaseType.SQL;
import static diarsid.jdbc.transactions.core.JdbcTransactionFactoryBuilder.buildTransactionFactoryWith;


/**
 *
 * @author Diarsid
 */
public class H2TestDataBase implements TestDataBase {
    
    private static final Logger logger = LoggerFactory.getLogger(H2TestDataBase.class);
    private static final String H2_IN_MEMORY_TEST_BASE_URL_TEMPLATE = 
            "jdbc:h2:mem:%s;DB_CLOSE_DELAY=-1";
    private static final int POOL_SIZE = 5;
    
    static {
        try {
            Class.forName("org.h2.Driver");
        } catch (Exception e) {     
            logger.error("", e);
        }
    }
    
    private final JdbcConnectionPool conPool;
    private final JdbcTransactionFactory jdbcTransactionFactory;
    
    public H2TestDataBase() {
        String name = randomUUID().toString();
        this.conPool = JdbcConnectionPool.create(
                format(H2_IN_MEMORY_TEST_BASE_URL_TEMPLATE, name), "test", "test");
        this.conPool.setMaxConnections(POOL_SIZE);
        logger.info(format("H2 embedded test based established with URL: %s", 
                           format(H2_IN_MEMORY_TEST_BASE_URL_TEMPLATE, name)));
        JdbcConnectionsSource source = new H2TestJdbcConnectionsSource(this.conPool);
        this.jdbcTransactionFactory = buildTransactionFactoryWith(source).done();
        this.jdbcTransactionFactory.logHistory(configuration().asBoolean("data.log"));
    }
        
    @Override
    public void setupRequiredTable(String tableCreationSQLScript) {
        try (Connection con = this.conPool.getConnection();
                Statement st = con.createStatement();) {
            st.executeUpdate(tableCreationSQLScript);
            logger.info(format("Test table setup with SQL: %s", tableCreationSQLScript));
        } catch (SQLException e) {
            logger.error("creation required table:");
            logger.error(tableCreationSQLScript);
            logger.error("", e);
            throw new RuntimeException();
        }
    }
    
    @Override
    public int countRowsInTable(String tableName) {
        try (   Connection con = this.conPool.getConnection();
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery("SELECT * FROM " + tableName);) {
            int rows = 0;
            while ( rs.next() ) {
                rows++;
            }
            return rows;
        } catch (SQLException e) {
            logger.error("Exception during counting rows in table: " + tableName);
            logger.error("", e);
            throw new RuntimeException();
        }
    }
    
    @Override
    public boolean ifAllConnectionsReleased() {
        return ( this.conPool.getActiveConnections() == 0 );
    }

    @Override
    public JdbcTransactionFactory transactionFactory() {
        return this.jdbcTransactionFactory;
    }

    @Override
    public void disconnect() {
        this.jdbcTransactionFactory.close();
    }

    @Override
    public DataBaseType type() {
        return SQL;
    }
}
