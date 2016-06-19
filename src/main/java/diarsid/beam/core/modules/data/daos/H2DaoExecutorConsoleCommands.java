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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;
import java.util.StringJoiner;
import java.util.TreeMap;

import diarsid.beam.core.Logs;
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
            "WHERE [REPLACEABLE_CONDITION] ";
    private final String SELECT_JOIN_CHOICES_WHERE_COMMAND_LIKE = 
            "SELECT cons.command, ch.pattern, ch.pattern_number, ch.choice " +
            "FROM " +
            "    (SELECT command " +
            "    FROM console_commands " +
            "    WHERE [REPLACEABLE_CONDITION] ) cons " +
            "LEFT JOIN " +
            "    command_choices ch " +
            "ON (LOWER(cons.command) = LOWER(ch.command)) ";
    private final String COMMAND_LIKE = 
            " ( LOWER(command) LIKE ? ) ";
    private final String AND = " AND ";
    private final String REPLACEABLE_CONDITION = "[REPLACEABLE_CONDITION]";
    private final String INSERT_NEW_COMMAND = 
            "INSERT INTO console_commands (command_id, command) " +
            "VALUES ( ?, ? ) ";
    private final String DELETE_WHERE_COMMAND_IS = 
            "DELETE FROM console_commands " +
            "WHERE LOWER(command) IS ? ";
    
    
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
        Logs.debug("[COMMANDS CONSOLE DAO] save: " +command);
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
            this.ioEngine.reportException(e, "SQLException: save new console command.");
            return false;
        }
    }
    
    @Override
    public boolean remove(String command) {
        command = command.toLowerCase();
        Logs.debug("[COMMANDS CONSOLE DAO] delete " +command);
        boolean removed = false;
        try (Connection con = this.data.connect();
                PreparedStatement delete = con.prepareStatement(
                        DELETE_WHERE_COMMAND_IS)) {
            
            delete.setString(1, "call " + command);
            delete.addBatch();
            delete.setString(1, "exe " + command);
            delete.addBatch();
            removed = ( delete.executeBatch().length > 0 );
        } catch (SQLException e) {
            this.ioEngine.reportError("SQLException: delete command.");
            removed = false;
        }
        return removed;
    }
    
    @Override
    public SortedMap<String, String> getImprovedCommandsForPattern(String pattern) {
        Set<String> patternParts = this.splitPatternIfMultipart(pattern); 
        Map<String, Set<CommandChoice>> choices = new HashMap<>();
        SortedMap<String, String> found = new TreeMap<>();
        String statement = SELECT_JOIN_CHOICES_WHERE_COMMAND_LIKE.replace(
                REPLACEABLE_CONDITION, 
                this.prepareFullConditionExpression(patternParts.size()));
        
        try (Connection con = data.connect();
                PreparedStatement ps = con.prepareStatement(statement)) {
            int paramsCounter = 1;
            for (String patternPart : patternParts) {
                ps.setString(paramsCounter, "%"+patternPart+"%");
                paramsCounter++;
            }            
            ResultSet rs = ps.executeQuery();
            
            while ( rs.next() ) {
                String command = rs.getString("command");
                if ( rs.getString("pattern") == null ) {
                    found.put(command, command);
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
            String improvedCommand = entry.getKey();
            for (CommandChoice choice : entry.getValue()) {
                // patterns are being replaced with a whitespace behind them
                // to prevent an undesired accident replacing of an actual
                // part of real argument. Replacing " ja" to " java"
                // instead of "ja" ensures that any arbitraty argument 
                // coincidentally containing "ja" will be undamaged.
                improvedCommand = improvedCommand.replace(
                        " "+choice.getPattern(), " "+choice.getMadeChoice());                
            }
            found.put(entry.getKey(), improvedCommand);
        }
        
        return found;
    }
    
    @Override
    public Set<String> getRawCommandsForPattern(String pattern) {
        Set<String> patternParts = this.splitPatternIfMultipart(pattern);
        Set<String> found = new HashSet<>(); 
        String statement = SELECT_WHERE_COMMAND_LIKE.replace(
                REPLACEABLE_CONDITION, 
                this.prepareFullConditionExpression(patternParts.size()));
        
        try (Connection con = data.connect();
                PreparedStatement ps = con
                        .prepareStatement(statement)) {
            int paramsCounter = 1;
            for (String patternPart : patternParts) {
                ps.setString(paramsCounter, "%"+patternPart+"%");
                paramsCounter++;
            }
            ResultSet rs = ps.executeQuery();
            
            while ( rs.next() ) {
                found.add(rs.getString("command"));
            }            
        } catch (SQLException e) {
            this.ioEngine.reportError("SQLException: get commands for pattern.");
        }
        return found;
    }
    
    private String prepareFullConditionExpression(int qty) {
        StringJoiner joiner = new StringJoiner(AND);
        for (int i = 0; i < qty; i++) {
            joiner.add(COMMAND_LIKE);
        }
        return joiner.toString();
    }
    
    private Set<String> splitPatternIfMultipart(String pattern) {
        pattern = pattern.toLowerCase();
        if ( pattern.contains("-") ) {
            return new HashSet<>(Arrays.asList(pattern.split("-")));
        } else {
            return new HashSet<>(Arrays.asList(new String[] {pattern}));
        }
    }    
}
