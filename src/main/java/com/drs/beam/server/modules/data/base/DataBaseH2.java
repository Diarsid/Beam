/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam.server.modules.data.base;

import java.sql.Connection;
import java.sql.SQLException;
import org.h2.jdbcx.JdbcConnectionPool;

/*
 * Connects with H2 database using org.h2.jdbcx.JdbcConnectionPool pool.
 * Is used by TasksDAO to get connections froom connection pool.
 */
public class DataBaseH2 implements DataBase{
    // Fields =============================================================================
    private final JdbcConnectionPool conPool;
    private final String dataBaseName = "H2";
            
    // Constructors =======================================================================
    public DataBaseH2(String url){
        String user = "BeamServer";
        String pass = "admin";
        this.conPool = JdbcConnectionPool.create(url, user, pass);
        this.conPool.setMaxConnections(3);
    } 
    
    // Methods ============================================================================
    @Override
    public Connection connect() throws SQLException{
        return this.conPool.getConnection();
    }
    
    @Override
    public String getName(){
        return this.dataBaseName;
    }
}
