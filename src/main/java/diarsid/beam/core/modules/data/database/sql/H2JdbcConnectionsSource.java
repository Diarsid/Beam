/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.data.database.sql;

import java.sql.Connection;
import java.sql.SQLException;

import org.h2.jdbcx.JdbcConnectionPool;

import diarsid.jdbc.transactions.JdbcConnectionsSource;

/**
 *
 * @author Diarsid
 */
class H2JdbcConnectionsSource implements JdbcConnectionsSource {
    
    private final JdbcConnectionPool conPool;
    
    H2JdbcConnectionsSource(String url, String user, String pass) {
        this.conPool = JdbcConnectionPool.create(url, user, pass);
        this.conPool.setMaxConnections(5);
    }

    @Override
    public Connection getConnection() throws SQLException {
        return this.conPool.getConnection();
    }

    @Override
    public void closeSource() {
        this.conPool.dispose();
    }
}
