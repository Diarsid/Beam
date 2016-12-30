/*
 * project: Beam
 * author: Diarsid
 */
package old.diarsid.beam.core.modules.data;

import java.sql.Connection;
import java.sql.SQLException;


/**
 * Interface representing DataBase abstraction.
 * Hides concrete implementation of connecting with particular database.
 * 
 * @author Diarsid
 */
public interface DataBase {
    
    Connection connect() throws SQLException;
    
    void disconnect();
    
    JdbcTransaction beginTransaction();
    
    String getName();
}
