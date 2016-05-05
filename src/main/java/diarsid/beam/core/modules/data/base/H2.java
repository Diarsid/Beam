/*
 * project: Beam
 * author: Diarsid
 */
package diarsid.beam.core.modules.data.base;


import java.sql.Connection;
import java.sql.SQLException;

import org.h2.jdbcx.JdbcConnectionPool;

import diarsid.beam.core.exceptions.WorkflowBrokenException;
import diarsid.beam.core.modules.data.DataBase;
import diarsid.beam.core.modules.data.JdbcTransaction;

/**
 * Represents embedded H2 Database Engine, implementing DataBase
 * interface.
 * 
 * @author Diarsid
 */
class H2 implements DataBase {
    
    private final JdbcConnectionPool conPool;
    private final String dataBaseName = "H2";
    private final Object connectingLock;
    
    H2(String url) {
        String user = "BeamServer";
        String pass = "admin";
        this.conPool = JdbcConnectionPool.create(url, user, pass);
        this.conPool.setMaxConnections(5);
        this.connectingLock = new Object();
    } 
    
    @Override
    public Connection connect() throws SQLException {
        synchronized ( this.connectingLock ) {
            return this.conPool.getConnection();
        }        
    }
    
    @Override 
    public void disconnect() {
        this.conPool.dispose();
    } 
    
    @Override
    public String getName() {
        return this.dataBaseName;
    }
    
    @Override
    public JdbcTransaction beginTransaction() {
        try {
            synchronized ( this.connectingLock ) {
                return new JdbcTransactionWrapper(this.conPool.getConnection());
            }
        } catch (SQLException e) {
            throw new WorkflowBrokenException(
                    "It is impossible to open the database connection. " + 
                    "Program will be closed.");
        }
    }
}
