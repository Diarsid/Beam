/*
 * project: Beam
 * author: Diarsid
 */

package com.drs.beam.util.data;

import com.drs.beam.io.InnerIOIF;
import com.drs.beam.io.BeamIO;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author Diarsid
 */
public class DBInitializer {    
    // Fields =============================================================================  
    private final InnerIOIF ioEngine = BeamIO.getInnerIO();
    
    private final String CHECK_TASKS_TABLE = 
            "SELECT TOP 1 * FROM tasks";
    private final String CHECK_LOCATIONS_TABLE = 
            "SELECT TOP 1 * FROM locations";
    private final String CHECK_COMMANDS_TABLE = 
            "SELECT TOP 1 * FROM commands";
    
    private final String CREATE_TASKS_TABLE = 
            "CREATE TABLE tasks (" +
            "t_id       INTEGER         NOT NULL PRIMARY KEY, " +
            "t_time     CHARACTER(16)   NOT NULL, " +
            "t_content  VARCHAR(200)    NOT NULL, " +
            "t_type     VARCHAR(5)      NOT NULL," +
            "t_status   BOOLEAN         NOT NULL)";
    private final String CREATE_COMMANDS_TABLE = 
            "CREATE TABLE commands (" +
            "command_name   VARCHAR(20)     NOT NULL PRIMARY KEY, " +
            "command_text   VARCHAR(300)    NOT NULL)";
    private final String CREATE_LOCATIONS_TABLE = 
            "CREATE TABLE locations (" +
            "location_name   VARCHAR(20)     NOT NULL PRIMARY KEY, " +
            "location_path   VARCHAR(300)    NOT NULL)";
    
    // Constructors =======================================================================
    public DBInitializer() {
    }

    // Methods ============================================================================    
    
    void initDataBase(DataBase db){
        try (   Connection con = db.connect();
                Statement st = con.createStatement();)
        {
            checkTable(st, "Tasks", CHECK_TASKS_TABLE, CREATE_TASKS_TABLE, 5);
            checkTable(st, "Locations", CHECK_LOCATIONS_TABLE, CREATE_LOCATIONS_TABLE, 2);
            checkTable(st, "Commands", CHECK_COMMANDS_TABLE, CREATE_COMMANDS_TABLE, 2);
            st.close();
        } catch (SQLException e) {
            ioEngine.informAboutException(e, true);
        }
    }
    
    
    private void checkTable(Statement st, String table, 
            String checkSQL, String createSQL, int correctColsQty){
        try(    ResultSet rs = st.executeQuery(checkSQL);)
        {
            ResultSetMetaData tableData = rs.getMetaData();
            if (tableData.getColumnCount() != correctColsQty){
                ioEngine.informAboutError(table + " table has wrong columns.", true);
            }            
        }catch (SQLException e){
            ioEngine.informAboutError(table + " table was not initialized.", false);          
            try{
                st.executeUpdate(createSQL);
                ioEngine.informAboutError(table + " table initialization successful.", false);
            } catch (SQLException ex){
                ioEngine.informAboutException(ex, true);
            }
        }
    }    
    
    
}
