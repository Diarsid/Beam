/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package old.diarsid.beam.core.modules.data.base;


import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.h2.jdbcx.JdbcConnectionPool;

import old.diarsid.beam.core.modules.data.JdbcTransaction;

import diarsid.beam.core.exceptions.ModuleInitializationException;
import diarsid.beam.core.exceptions.WorkflowBrokenException;

/**
 *
 * @author Diarsid
 */
public class H2TestDataBase implements TestDataBase {
    
    static {
        try {
            Class.forName("org.h2.Driver");
        } catch (Exception e) {           
            throw new ModuleInitializationException("H2 driver loading failure.");
        }
    }
    
    private final JdbcConnectionPool conPool;
    private final String dataBaseName = "H2_in_memory_test_base";
    private final Object connectingLock;
    private boolean isRequiredTableSet;
    
    public H2TestDataBase(String URL) {
        this.conPool = JdbcConnectionPool.create(URL, "test", "test");
        this.conPool.setMaxConnections(5);
        this.connectingLock = new Object();
        this.isRequiredTableSet = false;
    }    
    
    @Override
    public Connection connect() throws SQLException {
        if ( ! this.isRequiredTableSet ) {
            throw new ModuleInitializationException(
                    "Required SQL table has not been set for test database.");
        }
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
        if ( ! this.isRequiredTableSet ) {
            throw new ModuleInitializationException(
                    "Required SQL table has not been set for test database.");
        }
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
    
    @Override
    public void setupRequiredTable(String tableCreationSQLScript) {
        try (Connection con = this.conPool.getConnection();
                Statement st = con.createStatement();) {
            st.executeUpdate(tableCreationSQLScript);
            this.isRequiredTableSet = true;
        } catch (SQLException e) {
            throw new ModuleInitializationException(
                    "Requires table creation failure."); 
        }
    }
}
