/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam.util.data;

import java.sql.Connection;
import java.sql.SQLException;

/*
 * Interface for access to database from TasksDAO object.
 * Hides concrete implementation of connecting with particular database
 */
public interface DataBase {
    public Connection getConnection() throws SQLException;
}
