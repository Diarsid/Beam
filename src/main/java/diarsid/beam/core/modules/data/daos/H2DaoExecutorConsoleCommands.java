/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.data.daos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import diarsid.beam.core.exceptions.NullDependencyInjectionException;
import diarsid.beam.core.modules.IoInnerModule;
import diarsid.beam.core.modules.data.DaoExecutorConsoleCommands;
import diarsid.beam.core.modules.data.DataBase;
import diarsid.beam.core.modules.data.HandledTransactSQLException;
import diarsid.beam.core.modules.data.JdbcTransaction;
import diarsid.beam.core.modules.executor.workflow.CommandChoice;

/**
 *
 * @author Diarsid
 */
class H2DaoExecutorConsoleCommands implements DaoExecutorConsoleCommands {    
    
    private final IoInnerModule ioEngine;
    private final DataBase data;
    private final Random random;
    
    /* 
     * SQL Table illustration for Executor console commands storing.
     * 
     *  +------------------------------------------------------------------+
     *  | Console_commands                                                  |
     *  +------------+---------------+-------------------------------------+
     *  | command_id | command       | pattern_number | pattern | choice   |
     *  +------------+---------------+----------------+---------+----------+
     *  | 123        | see mai       | 0              | mai     | gmail    |
     *  +------------+---------------+----------------+---------+----------+
     *  | 451        | op j in eng   | 0              | j       | java     |
     *  +------------+---------------+----------------+---------+----------+
     *  | 347        | call apache   | 1              | eng     | engines  |
     *  +------------+---------------+----------------+---------+----------+
     */
    
    H2DaoExecutorConsoleCommands(IoInnerModule ioEngine, DataBase data) {
        if (ioEngine == null) {
            throw new NullDependencyInjectionException(
                    H2DaoExecutorConsoleCommands.class.getSimpleName(), 
                    IoInnerModule.class.getSimpleName());
        }
        if (data == null) {
            throw new NullDependencyInjectionException(
                    H2DaoExecutorConsoleCommands.class.getSimpleName(), 
                    DataBase.class.getSimpleName());
        }
        this.data = data;
        this.ioEngine = ioEngine;
        this.random = new Random();
    }
    
    private final String SELECT_ALL = 
            "SELECT command " +
            "FROM console_commands ";
    private final String SELECT_WHERE_COMMAND_IS = 
            "SELECT command " +
            "FROM console_commands " + 
            "WHERE LOWER(command) IS ? ";
    private final String SELECT_WHERE_COMMAND_LIKE = 
            "SELECT command " +
            "FROM console_commands " + 
            "WHERE ( LOWER(command) LIKE ? ) ";
    private final String SELECT_JOIN_CHOICES_WHERE_COMMAND_LIKE = 
            "SELECT cons.command, ch.pattern, ch.pattern_number, ch.choice " +
            "FROM " +
            "    (SELECT command " +
            "    FROM console_commands " +
            "    WHERE LOWER(command) LIKE ? ) cons " +
            "LEFT JOIN " +
            "    command_choices ch " +
            "ON (LOWER(cons.command) = LOWER(ch.command)) ";
    private final String INSERT_NEW_COMMAND = 
            "INSERT INTO console_commands (command_id, command) " +
            "VALUES ( ?, ? ) ";
    
    
    @Override
    public Set<String> getAllConsoleCommands() {
        try (Connection con = data.connect();
                Statement st = con.createStatement()) {
            
            ResultSet rs = st.executeQuery(SELECT_ALL);
            Set<String> found = new HashSet<>(); 
            while ( rs.next() ) {
                found.add(rs.getString("command"));
            }
            return found;
        } catch (SQLException e) {
            this.ioEngine.reportError("SQLException: get commands for pattern.");
            return Collections.emptySet();
        }
    }
    
    @Override
    public boolean saveNewConsoleCommand(String command) {        
        command = command.toLowerCase();
        System.out.println("[DAO CONSOLE DEBUG] save: " +command);
        JdbcTransaction transact = data.beginTransaction();
        try {
            PreparedStatement getCommand = 
                    transact.getPreparedStatement(SELECT_WHERE_COMMAND_IS);
            getCommand.setString(1, command);
            ResultSet rs = transact.executePreparedQuery(getCommand);
            boolean inserted = false;
            if ( ! rs.first() ) {
                PreparedStatement insert = transact
                        .getPreparedStatement(INSERT_NEW_COMMAND);
                insert.setInt(1, this.random.nextInt());
                insert.setString(2, command);
                inserted = ( transact.executePreparedUpdate(insert) > 0 );
            }             
            transact.commitThemAll();
            return inserted;
        } catch (HandledTransactSQLException e) {
            transact.rollbackAllAndReleaseResources();
            this.ioEngine.reportError(
                    "Exception during a JdbcTransaction execution: "
                            + "save new console command.");
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            this.ioEngine.reportError("SQLException: save new console command.");
            return false;
        }
    }
    
    @Override
    public Set<String> getImprovedCommandsForPattern(String pattern) {
        pattern = this.adjustPatternIfMultipart(pattern); 
        Map<String, Set<CommandChoice>> choices = new HashMap<>();
        Set<String> found = new HashSet<>();
        
        try (Connection con = data.connect();
                PreparedStatement ps = con.prepareStatement(
                        SELECT_JOIN_CHOICES_WHERE_COMMAND_LIKE)) {
            
            ps.setString(1, "%"+pattern+"%");
            ResultSet rs = ps.executeQuery();
            
            while ( rs.next() ) {
                String command = rs.getString("command");
                if ( rs.getString("pattern") == null ) {
                    found.add(command);
                } else {
                    CommandChoice choice = new CommandChoice(
                            rs.getString("pattern"), 
                            rs.getString("choice"),
                            rs.getInt("pattern_number"));
                    if ( choices.containsKey(command) ) {
                        choices.get(command).add(choice);
                    } else {
                        choices.put(command, new HashSet<>());
                        choices.get(command).add(choice);
                    }
                }                
            }
        } catch (SQLException e) {
            this.ioEngine.reportError("SQLException: get commands for pattern.");
        }
        
        for (Map.Entry<String, Set<CommandChoice>> entry : choices.entrySet() ) {
            String command = entry.getKey();
            for (CommandChoice choice : entry.getValue()) {
                // patterns are being replaced with a whitespace behind them
                // to prevent an undesired accident replacing of an actual
                // part of real argument. Replacing " ja" to " java"
                // instead of "ja" ensures that any arbitraty argument 
                // coincidentally containing "ja" will be undamaged.
                command = command.replace(
                        " "+choice.getPattern(), " "+choice.getMadeChoice());                
            }
            found.add(command);
        }
        
        return found;
    }
    
    @Override
    public Set<String> getRawCommandsForPattern(String pattern) {
        pattern = this.adjustPatternIfMultipart(pattern);
        Set<String> found = new HashSet<>(); 
        try (Connection con = data.connect();
                PreparedStatement ps = con
                        .prepareStatement(SELECT_WHERE_COMMAND_LIKE)) {
            ps.setString(1, "%"+pattern+"%");
            ResultSet rs = ps.executeQuery();
            
            while ( rs.next() ) {
                found.add(rs.getString("command"));
            }            
        } catch (SQLException e) {
            this.ioEngine.reportError("SQLException: get commands for pattern.");
        }
        return found;
    }
    
    private String adjustPatternIfMultipart(String pattern) {
        pattern = pattern.toLowerCase();
        if ( pattern.contains("-") ) {
            pattern = pattern.trim().replace("-", "%");
        }
        return pattern;
    }    
}
