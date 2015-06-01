/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam.util.data;

import java.sql.Connection;
import java.sql.SQLException;
import org.h2.jdbcx.JdbcConnectionPool;

/*
 * Connects with H2 database using org.h2.jdbcx.JdbcConnectionPool pool.
 * Is used by TasksDAO to get connections froom connection pool.
 */
public class DataBaseH2Pooled implements DataBase{
    // Fields =============================================================================
    private final JdbcConnectionPool conPool;
            
    // Constructors =======================================================================
    DataBaseH2Pooled(String url, String user, String pass, int maxConnections){
        this.conPool = JdbcConnectionPool.create(url, user, pass);
        this.conPool.setMaxConnections(maxConnections);
    } 
    
    // Methods ============================================================================
    @Override
    public Connection connect() throws SQLException{
        return conPool.getConnection();
    }
}
