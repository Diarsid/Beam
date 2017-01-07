/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package testing.embedded.base.h2;

import java.sql.Connection;
import java.sql.SQLException;

import org.h2.jdbcx.JdbcConnectionPool;

import diarsid.jdbc.transactions.JdbcConnectionsSource;

/**
 *
 * @author Diarsid
 */
public class H2TestJdbcConnectionsSource implements JdbcConnectionsSource {
    
    private final JdbcConnectionPool connectionPool;
    
    public H2TestJdbcConnectionsSource(JdbcConnectionPool conPool) {
        this.connectionPool = conPool;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return this.connectionPool.getConnection();
    }

    @Override
    public void closeSource() {
        this.connectionPool.dispose();
    }
}
