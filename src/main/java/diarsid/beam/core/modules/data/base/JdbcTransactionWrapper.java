/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.data.base;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

import diarsid.beam.core.exceptions.WorkflowBrokenException;
import diarsid.beam.core.modules.data.HandledTransactSQLException;
import diarsid.beam.core.modules.data.JdbcTransaction;

/**
 * Transaction wrapper class to tackle with JDBC transactions.
 * 
 * Wrap and hides the Connection to the database. Concerns about 
 * proper treatment with opened Connection, ResultSets and Statements.
 * 
 * It is implied that all Statements, PreparedStatements and ResultSets 
 * are obtained using this object methods.
 * 
 * @author Diarsid
 */
class JdbcTransactionWrapper implements JdbcTransaction {    
    
    private final Set<ResultSet> resultSets;
    private final Set<Statement> statements;    
    private Connection con;
    
    JdbcTransactionWrapper(Connection con) {
        this.resultSets = new HashSet<>();
        this.statements = new HashSet<>();
        try {
            this.con = con;
            this.con.setAutoCommit(false);
        } catch (SQLException e) {
            // There is no sense to do anything further 
            // if connection attempt or auto commit 
            // disabling failed.
            throw new WorkflowBrokenException(
                    "It is impossible to open the database connection. " + 
                    "Program will be closed.");
        }
    }
    
   /*
    * Let's try to commit all changes, restore auto commit,
    * release all resources (ResultSets and Statements previously 
    * created through this JdbcTransactionWrapper) and close connection. 
    */
    @Override
    public void commitThemAll() {
        try {
            this.con.commit();
        } catch (SQLException e) {
            try {
                this.con.rollback();
            } catch (SQLException e1) {
                // No actions after rollback has failed.
                // Go to finally block.
            }
        } finally {
            this.restoreAutoCommit();
            this.closeResultSets();
            this.closeStatements();
            this.closeConnectionAnyway();
        }
    }
    
    private void closeResultSets() {
        for (ResultSet set : this.resultSets) {
            try {
                set.close();
            } catch (SQLException e) {
                // have no idea what to do if ResultSet is
                // impossible to close.
                // Just continue closing other ResultSets.
            }
        }
    }
    
    private void closeStatements() {
        for (Statement statement : this.statements) {
            try {
                statement.close();
            } catch (SQLException e) {
                // have no idea what to do if Statement is
                // impossible to close.
                // Just continue closing other Statements.
            }
        }
    }
    
    private void restoreAutoCommit() {
        try {
            this.con.setAutoCommit(true);
        } catch (SQLException e) {
            // no actions, just proceed and try to close
            // connection.
        }
    }
    
    private void closeConnectionAnyway() {
        try {
            this.con.close();
            this.con = null;
        } catch (SQLException e) {
            throw new WorkflowBrokenException(
                    "It is impossible to close the database connection. " +
                    "Program will be closed");
        }
    }
    
   /*
    * Execute given query, save corresponding Statement and 
    * ResultSet objects to close them later.
    *
    * If operation fails - make rollback, close all resources
    * and rethrow SQLException into main execution method to stop it.
    */
    @Override
    public ResultSet executeQuery(String sql) 
            throws HandledTransactSQLException {   
        
        try {
            Statement st = this.con.createStatement();
            this.statements.add(st);
            ResultSet rs = st.executeQuery(sql);
            this.resultSets.add(rs);
            return rs;
        } catch (SQLException e) {
            this.rollbackAllAndReleaseResources();
            throw new HandledTransactSQLException(e);
        }
    }
    
   /*
    * Execute given update, save corresponding Statement to 
    * close it later.
    *
    * If operation fails - make rollback, close all resources
    * and rethrow SQLException into main execution method to stop it.
    */
    @Override
    public int executeUpdate(String sql) throws HandledTransactSQLException { 
        try {
            Statement st = this.con.createStatement();
            this.statements.add(st);
            return st.executeUpdate(sql);
        } catch (SQLException e) {
            this.rollbackAllAndReleaseResources();
            throw new HandledTransactSQLException(e);
        }
    }
    
   /*
    * Create and save PreparedStatement.
    *
    * If operation fails - make rollback, close all resources
    * and rethrow SQLException into main execution method to stop it.
    */
    @Override
    public PreparedStatement getPreparedStatement(String sql) 
            throws HandledTransactSQLException {
        
        try {
            PreparedStatement ps = this.con.prepareStatement(sql);
            this.statements.add(ps);
            return ps;
        } catch (SQLException e) {
            this.rollbackAllAndReleaseResources();
            throw new HandledTransactSQLException(e);
        }
    }
    
   /*
    * Execute the query represented by specified PreparedStatement.
    * It is implied that it has been set with all necessary 
    * parameters.
    *
    * If operation fails - make rollback, close all resources
    * and rethrow SQLException into main execution method to stop it.
    */
    @Override
    public ResultSet executePreparedQuery(PreparedStatement ps) 
            throws HandledTransactSQLException {
        
        try {
            this.statements.add(ps);
            ResultSet rs = ps.executeQuery();
            this.resultSets.add(rs);
            return rs;
        } catch (SQLException e) {
            this.rollbackAllAndReleaseResources();
            throw new HandledTransactSQLException(e);
        }
    }    
    
    /*
    * Execute the update represented by specified PreparedStatement.
    * It is implied that it has been set with all necessary 
    * parameters.
    *
    * If operation fails - make rollback, close all resources
    * and rethrow SQLException into main execution method to stop it.
    */
    @Override
    public int executePreparedUpdate(PreparedStatement ps) 
            throws HandledTransactSQLException {
        
        try {
            this.statements.add(ps);
            return ps.executeUpdate();
        } catch (SQLException e) {
            this.rollbackAllAndReleaseResources();
            throw new HandledTransactSQLException(e);
        }
    }
    
    
    /*
     * Execute the batch update represented by specified PreparedStatement.
     * It is implied that it has been set with all necessary
     * parameters inside of the loop earlier.
     *
     * If operation fails - make rollback, close all resources
     * and rethrow SQLException into main execution method to stop it.
     */
    @Override
    public int executeBatchPreparedUpdate(PreparedStatement ps) 
            throws HandledTransactSQLException {
        
        try {
            this.statements.add(ps);
            int qty = 0;
            for (Integer i : ps.executeBatch()) {
                qty += i;
            }
            return qty;
        } catch (SQLException e) {
            this.rollbackAllAndReleaseResources();
            throw new HandledTransactSQLException(e);
        }
    }
    
    @Override
    public void rollbackAllAndReleaseResources() {
        try {
            this.con.rollback();
        } catch (SQLException e) {
            this.restoreAutoCommit();
            this.closeResultSets();
            this.closeStatements();
            this.closeConnectionAnyway();
        }
    }
}
