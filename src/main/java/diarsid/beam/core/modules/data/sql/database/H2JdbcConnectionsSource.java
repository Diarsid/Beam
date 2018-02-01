/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.data.sql.database;

import java.sql.Connection;
import java.sql.SQLException;

import org.h2.jdbcx.JdbcConnectionPool;

import diarsid.jdbc.transactions.JdbcConnectionsSource;

/**
 *
 * @author Diarsid
 */
class H2JdbcConnectionsSource implements JdbcConnectionsSource {
    
    private final JdbcConnectionPool connectionsPool;
    private final int connectionsQty;
    
    H2JdbcConnectionsSource(String url, String user, String pass) {
        this.connectionsPool = JdbcConnectionPool.create(url, user, pass);
        this.connectionsQty = 5;
        this.connectionsPool.setMaxConnections(this.connectionsQty);
    }

    @Override
    public Connection getConnection() throws SQLException {
        return this.connectionsPool.getConnection();
    }

    @Override
    public void closeSource() {
        this.connectionsPool.dispose();
    }

    @Override
    public int totalConnectionsQuantity() {
        return this.connectionsQty;
    }
}
