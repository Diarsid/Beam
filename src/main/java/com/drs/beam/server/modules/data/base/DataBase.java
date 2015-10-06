/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam.server.modules.data.base;

import java.sql.Connection;
import java.sql.SQLException;


/*
 * Interface to get access to database from DAO objects.
 * Hides concrete implementation of connecting with particular database.
 */
public interface DataBase {
    
    Connection connect() throws SQLException;
    
    String getName();
}
