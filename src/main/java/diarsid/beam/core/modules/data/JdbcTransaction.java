/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author Diarsid
 */
public interface JdbcTransaction {

    /*
     * Let's try to commit all changes, restore auto commit,
     * release all resources (ResultSets and Statements previously
     * created through this JdbcTransactionWrapper) and close connection.
     */
    void commitThemAll();

    /*
     * Execute the query represented by specified PreparedStatement.
     * It is implied that it has been set with all necessary
     * parameters.
     *
     * If operation fails - make rollback, close all resources
     * and rethrow SQLException into main execution method to stop it.
     */
    ResultSet executePreparedQuery(PreparedStatement ps) 
            throws HandledTransactSQLException;

    /*
     * Execute the update represented by specified PreparedStatement.
     * It is implied that it has been set with all necessary
     * parameters.
     *
     * If operation fails - make rollback, close all resources
     * and rethrow SQLException into main execution method to stop it.
     */
    int executePreparedUpdate(PreparedStatement ps) 
            throws HandledTransactSQLException;

    /*
     * Execute given query and save corresponding Statement and
     * ResultSet objects to close them later.
     *
     * If operation fails - make rollback, close all resources
     * and rethrow SQLException into main execution method to stop it.
     */
    ResultSet executeQuery(String sql) throws HandledTransactSQLException;
    
    /*
     * Execute given update, save corresponding Statement to 
     * close it later.
     *
     * If operation fails - make rollback, close all resources
     * and rethrow SQLException into main execution method to stop it.
     */
    int executeUpdate(String sql) throws HandledTransactSQLException;

    /*
     * Create and save PreparedStatement.
     *
     * If operation fails - make rollback, close all resources
     * and rethrow SQLException into main execution method to stop it.
     */
    PreparedStatement getPreparedStatement(String sql) 
            throws HandledTransactSQLException;

    void rollbackAllAndReleaseResources();
    
}
