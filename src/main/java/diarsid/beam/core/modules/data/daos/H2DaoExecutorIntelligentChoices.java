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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import diarsid.beam.core.exceptions.NullDependencyInjectionException;

import old.diarsid.beam.core.modules.IoInnerModule;

import diarsid.beam.core.modules.data.DaoExecutorIntelligentChoices;
import diarsid.beam.core.modules.data.DataBase;
import diarsid.beam.core.modules.executor.workflow.CommandChoice;
import diarsid.beam.core.modules.executor.workflow.CurrentCommandState;
import diarsid.beam.core.util.Logs;

/**
 *
 * @author Diarsid
 */
class H2DaoExecutorIntelligentChoices implements DaoExecutorIntelligentChoices {
    
    private final DataBase data;
    private final IoInnerModule ioEngine;
    private final Random random;
    
    H2DaoExecutorIntelligentChoices(final IoInnerModule io, final DataBase data) {
        if (io == null) {
            throw new NullDependencyInjectionException(
                    H2DaoExecutorIntelligentChoices.class.getSimpleName(), 
                    IoInnerModule.class.getSimpleName());
        }
        if (data == null) {
            throw new NullDependencyInjectionException(
                    H2DaoExecutorIntelligentChoices.class.getSimpleName(), 
                    DataBase.class.getSimpleName());
        }
        this.data = data;
        this.ioEngine = io;
        this.random = new Random();
    }
    
    /* 
     * SQL Table illustration for remembered command choices.
     *  +------------------------------------------------------------------+
     *  | command_choices                                                  |
     *  +------------+---------------+-------------------------------------+
     *  | choice_id  | command       | pattern_number | pattern | choice   |
     *  +------------+---------------+----------------+---------+----------+
     *  | 123        | see mai       | 0              | mai     | gmail    |
     *  +------------+---------------+----------------+---------+----------+
     *  | 451        | op j in eng   | 0              | j       | java     |
     *  +------------+---------------+----------------+---------+----------+
     *  | 347        | op j in eng   | 1              | eng     | engines  |
     *  +------------+---------------+----------------+---------+----------+
     */
    
    private final String SELECT_CHOICES_WHERE_COMMAND_NUBMER_PATTERN_IS = 
            "SELECT choice " +
            "FROM command_choices " +
            "WHERE ( LOWER(command) IS ? ) " + 
            "  AND ( LOWER(pattern_number) IS ? ) " + 
            "  AND ( LOWER(pattern) IS ? )";
    private final String SELECT_ALL_COMMANDS = 
            "SELECT command, pattern, choice, pattern_number " +
            "FROM command_choices ";
    private final String SELECT_COMMANDS_WHERE_COMMAND_OR_CHOICE_OR_PATTERN_LIKE = 
            "SELECT command, pattern, choice, pattern_number " +
            "FROM command_choices " + 
            "WHERE ( LOWER(command) LIKE ? ) " +
            "   OR ( LOWER(pattern) LIKE ? ) " +
            "   OR ( LOWER(choice) LIKE ? ) ";
    private final String INSERT_COMMAND = 
            "INSERT INTO command_choices " + 
            "       (choice_id, command, pattern_number, pattern, choice)" +
            "VALUES (?, ?, ?, ?, ?) ";
    private final String DELETE_WHERE_COMMAND_IS = 
            "DELETE FROM command_choices " + 
            "WHERE LOWER(command) IS ? ";
    private final String DELETE_FROM_CHOICES_WHERE_COMMAND_AND_PATTERN_OR_CHOICE_LIKE = 
            "DELETE FROM command_choices " +
            "WHERE ( LOWER(command) LIKE ? ) ";
    private final String DELETE_FROM_CHOICES_WHERE_COMMAND_OR_CHOICE_OR_PATTEN_LIKE = 
            "DELETE FROM command_choices " +
            "WHERE ( LOWER(command) LIKE ? ) " +
            "   OR ( LOWER(pattern) LIKE ? ) " +
            "   OR ( LOWER(choice) LIKE ? ) ";
    private final String DELETE_FROM_CHOICES_WHERE_LOCATION_IN_COMMAND_LIKE_AND_NOT_LIKE = 
            "DELETE FROM command_choices " +
            "WHERE  " +
            "   ( ( LOWER(command) LIKE ? ) OR ( LOWER(command) LIKE ? ) ) " +
            "       AND " +
            "   ( LOWER(command) NOT LIKE ? ) ";
    private final String DELETE_FROM_CHOICES_WHERE_TARGET_IN_COMMAND_LIKE = 
            "DELETE FROM command_choices " +
            "WHERE  ( LOWER(command) LIKE ? ) ";
    private final String DELETE_FROM_CHOICES_WHERE_COMMAND_AND_PATH_OR_CHOICE_OR_PATTEN_LIKE = 
            "DELETE FROM command_choices " +
            "WHERE ( ( LOWER(command) LIKE ? ) AND (LOWER(command) LIKE ? ) )" +
            "   OR ( ( LOWER(pattern) LIKE ? ) AND (LOWER(pattern) LIKE ? ) ) " +
            "   OR ( ( LOWER(choice) LIKE ? ) AND (LOWER(choice) LIKE ? ) ) ";
    private final String DELETE_FROM_CONSOLE_WHERE_COMMAND_LIKE = 
            "DELETE FROM console_commands " +
            "WHERE LOWER(command) LIKE ? ";
    private final String DELETE_FROM_CONSOLE_WHERE_LOCATION_IN_COMMAND_LIKE_AND_NOT_LIKE = 
            "DELETE FROM console_commands " +
            "WHERE " +
            "   ( ( LOWER(command) LIKE ? ) OR ( LOWER(command) LIKE ? ) ) " +
            "       AND " +
            "   ( LOWER(command) NOT LIKE ? )";
    private final String DELETE_FROM_CONSOLE_WHERE_TARGET_IN_COMMAND_LIKE = 
            "DELETE FROM console_commands " +
            "WHERE  ( LOWER(command) LIKE ? ) ";;
    
    @Override
    public String getChoiceForCommandPart(
            String command, int attemptNumber, String pattern) {
        
        command = command.toLowerCase();
        pattern = pattern.toLowerCase();
        
        try (Connection con = this.data.connect();
                PreparedStatement ps = con.prepareStatement(
                        SELECT_CHOICES_WHERE_COMMAND_NUBMER_PATTERN_IS)) {
            
            ps.setString(1, command);
            ps.setInt(2, attemptNumber);
            ps.setString(3, pattern);
            
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString(1);
            } else {
                return "";
            }
        } catch (SQLException e) {
            this.ioEngine.reportException(e, 
                    "SQLException: get choice for command: " + command);
            return "";
        }
    }
    
    @Override
    public boolean saveChoiceForCommandAndItsPart(CurrentCommandState commandState) {
        Logs.debug("[DAO EXECUTOR CHOICES] save: " + commandState.getCommandString()
                + " -> " + commandState.getMadeChoices());
        try (Connection con = this.data.connect();
                PreparedStatement insertInChoices = 
                        con.prepareStatement(INSERT_COMMAND);) {
            
            for (CommandChoice choice : commandState.getMadeChoices()) {
                insertInChoices.setInt(1, this.getRandomInt());
                insertInChoices.setString(2, commandState.getCommandString());
                insertInChoices.setInt(3, choice.getChoiceNumber());
                insertInChoices.setString(4, choice.getPattern());
                insertInChoices.setString(5, choice.getMadeChoice());
                insertInChoices.addBatch();
            }            
            
            insertInChoices.executeBatch();
            return true;
        } catch (SQLException e) {
            this.ioEngine.reportException(e, 
                    "SQLException: save choice for command: " + 
                            commandState.getCommandString());
            return false;
        }
    }
    
    @Override
    public boolean deleteChoicesForCommand(String command) {
        command = command.toLowerCase();
        try (Connection con = this.data.connect();
                PreparedStatement ps = con.prepareStatement(
                        DELETE_WHERE_COMMAND_IS)) {
            
            ps.setString(1, command);
            int qty = ps.executeUpdate();
            return ( qty > 0 );
        } catch (SQLException e) {
            this.ioEngine.reportException(e, 
                    "SQLException: delete choice for command: " + command);
            return false;
        }
    }
    
    @Override
    public boolean discardCommandByPattern(String pattern) {
        pattern = pattern.toLowerCase().replace("-", "%");
        try (Connection con = this.data.connect();
                PreparedStatement deleteFromChoices = con.prepareStatement(
                        DELETE_FROM_CHOICES_WHERE_COMMAND_OR_CHOICE_OR_PATTEN_LIKE);
                PreparedStatement deleteFromConsole = con.prepareStatement(
                        DELETE_FROM_CONSOLE_WHERE_COMMAND_LIKE)) {
            
            deleteFromChoices.setString(1, "% "+pattern+" %");
            deleteFromChoices.setString(2, "%"+pattern+"%");
            deleteFromChoices.setString(3, "%"+pattern+"%");
            int qty = deleteFromChoices.executeUpdate();
            
            deleteFromConsole.setString(1, "% "+pattern+" %");
            qty = qty + deleteFromConsole.executeUpdate();
            
            return ( qty > 0 );
        } catch (SQLException e) {
            this.ioEngine.reportException(e, 
                    "SQLException: delete choice for command: " + pattern);
            return false;
        }
    }
    
    @Override
    public boolean discardCommandByInvalidLocationInPath(String invalidLocationName) {
        invalidLocationName = invalidLocationName.replace("-", "%").toLowerCase();
        try (Connection con = this.data.connect();
                PreparedStatement deleteFromChoices = con.prepareStatement(
                        DELETE_FROM_CHOICES_WHERE_LOCATION_IN_COMMAND_LIKE_AND_NOT_LIKE);
                PreparedStatement deleteFromConsole = con.prepareStatement(
                        DELETE_FROM_CONSOLE_WHERE_LOCATION_IN_COMMAND_LIKE_AND_NOT_LIKE)) {
            
            // like: 
            deleteFromChoices.setString(1, "open % in %"+invalidLocationName+"%");
            // or like:
            deleteFromChoices.setString(2, "open %"+invalidLocationName+"%");
            // but not like:
            deleteFromChoices.setString(3, "open %"+invalidLocationName+"% in %");
            int qty = deleteFromChoices.executeUpdate();
            
            // like:
            deleteFromConsole.setString(1, "open % in %"+invalidLocationName+"%");
            // or like:
            deleteFromConsole.setString(2, "open %"+invalidLocationName+"%");
            // but not like:
            deleteFromConsole.setString(3, "open %"+invalidLocationName+"% in %");
            qty = qty + deleteFromConsole.executeUpdate();
            
            return ( qty > 0 );
        } catch (SQLException e) {
            this.ioEngine.reportException(e, 
                    "SQLException: delete command with invalid location: " + invalidLocationName);
            return false;
        }
    }    
    
    @Override
    public boolean discardCommandByInvalidTargetInPath(String target) {
        target = target.replace("-", "%").toLowerCase();
        try (Connection con = this.data.connect();
                PreparedStatement deleteFromChoices = con.prepareStatement(
                        DELETE_FROM_CHOICES_WHERE_TARGET_IN_COMMAND_LIKE);
                PreparedStatement deleteFromConsole = con.prepareStatement(
                        DELETE_FROM_CONSOLE_WHERE_TARGET_IN_COMMAND_LIKE)) {
            
            // like: 
            deleteFromChoices.setString(1, "open %"+target+"% in %");
            int qty = deleteFromChoices.executeUpdate();
            
            // like:
            deleteFromConsole.setString(1, "open %"+target+"% in %");
            qty = qty + deleteFromConsole.executeUpdate();
            
            return ( qty > 0 );
        } catch (SQLException e) {
            this.ioEngine.reportException(e, 
                    "SQLException: delete command with invalid location: " + target);
            return false;
        }
    }
    
    @Override
    public boolean discardCommandByPatternAndOperation(String operation, String pattern) {
        pattern = pattern.replace("-", "%").toLowerCase();
        try (Connection con = this.data.connect();
                PreparedStatement deleteFromChoices = con.prepareStatement(
                        DELETE_FROM_CHOICES_WHERE_COMMAND_AND_PATTERN_OR_CHOICE_LIKE);
                PreparedStatement deleteFromConsole = con.prepareStatement(
                        DELETE_FROM_CONSOLE_WHERE_COMMAND_LIKE)) {
            
            deleteFromChoices.setString(1, operation + " %"+pattern+"%");
            int qty = deleteFromChoices.executeUpdate();
            
            deleteFromConsole.setString(1, operation + " %"+pattern+"%");
            qty = qty + deleteFromConsole.executeUpdate();
            
            return ( qty > 0 );
        } catch (SQLException e) {
            this.ioEngine.reportException(e, 
                    "SQLException: delete choice for command: " + operation + " " + pattern);
            return false;
        }
    }
    
//    @Override
//    public boolean discardCommandByPathPatternAndOperation(String operation, String pathToTarget, String targetPattern) {
//        targetPattern = targetPattern.replace("-", "%").trim().toLowerCase();
//        pathToTarget = pathToTarget.replace("-", "%").trim().toLowerCase();
//        try (Connection con = this.data.connect();
//                PreparedStatement deleteFromChoices = con.prepareStatement(
//                        DELETE_FROM_CHOICES_WHERE_COMMAND_AND_PATH_OR_CHOICE_OR_PATTEN_LIKE);
//                PreparedStatement deleteFromConsole = con.prepareStatement(
//                        DELETE_FROM_CONSOLE_WHERE_COMMAND_AND_PATH_LIKE)) {
//            
//            deleteFromChoices.setString(1, operation + "% "+targetPattern+"%");
//            deleteFromChoices.setString(2, operation + "% "+pathToTarget+"%");
//            
//            deleteFromChoices.setString(3, "%"+targetPattern+"%");
//            deleteFromChoices.setString(4, "%"+pathToTarget+"%");
//            
//            deleteFromChoices.setString(5, "%"+targetPattern+"%");
//            deleteFromChoices.setString(6, "%"+pathToTarget+"%");
//            
//            int qty = deleteFromChoices.executeUpdate();
//            
//            deleteFromConsole.setString(1, operation + "% "+targetPattern+"%");
//            deleteFromConsole.setString(2, operation + "% "+pathToTarget+"%");
//            qty = qty + deleteFromConsole.executeUpdate();
//            
//            return ( qty > 0 );
//        } catch (SQLException e) {
//            this.ioEngine.reportException(e, 
//                    "SQLException: delete choice for command: " + 
//                            operation + " " + pathToTarget + " " + targetPattern);
//            return false;
//        }
//    }
    
    @Override
    public List<CurrentCommandState> getAllChoices() {
        try (Connection con = this.data.connect();
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery(SELECT_ALL_COMMANDS)) {
            
            return this.extractCommandsFromResultSet(rs);
        } catch (SQLException e) {
            this.ioEngine.reportException(e, 
                    "SQLException: select all choices");
            return Collections.emptyList();
        }
    }

    private List<CurrentCommandState> extractCommandsFromResultSet(
            final ResultSet rs)
                    throws SQLException {
        
        Map<String, List<CommandChoice>> commandsRaw = new HashMap<>();
        String command;
        CommandChoice newChoice;
        while ( rs.next() ) {
            command = rs.getString("command");
            if ( ! commandsRaw.containsKey(command) ) {
                commandsRaw.put(command, new ArrayList<>());
            }
            newChoice = new CommandChoice(
                    rs.getString("pattern"),
                    rs.getString("choice"),
                    rs.getInt("pattern_number")
            );
            commandsRaw.get(command).add(newChoice);
        }
        List<CurrentCommandState> commands = new ArrayList<>();
        for (Map.Entry<String, List<CommandChoice>> entry : commandsRaw.entrySet()) {
            commands.add(new CurrentCommandState(
                    entry.getKey(), entry.getValue()));
        }
        return commands;
    }
    
    @Override
    public List<CurrentCommandState> getChoicesWhereCommandLike(String part) {
        part = part.toLowerCase().replace("-", "%");
        try (Connection con = this.data.connect();
                PreparedStatement ps = con.prepareStatement(
                SELECT_COMMANDS_WHERE_COMMAND_OR_CHOICE_OR_PATTERN_LIKE)) {
            
            ps.setString(1, "%"+part+"%");
            ps.setString(2, "%"+part+"%");
            ps.setString(3, "%"+part+"%");
            ResultSet rs = ps.executeQuery();
            return extractCommandsFromResultSet(rs);
        } catch (SQLException e) {
            this.ioEngine.reportException(e, 
                    "SQLException: select all choices");
            return Collections.emptyList();
        }
    } 
    
    @Override
    public List<String> formatCommandsForOutput(
            List<CurrentCommandState> commands) {
        
        List<String> displayedCommands = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for (CurrentCommandState commandState : commands) {
            sb.append(commandState.getCommandString()).append(" -> ");
            for (CommandChoice choice : commandState.getMadeChoices()) {
                sb
                        .append(choice.getPattern())
                        .append("->")
                        .append(choice.getMadeChoice())
                        .append(" ");
            }
            displayedCommands.add(sb.toString());
            sb.delete(0, sb.length());
        }
        return displayedCommands;
    }
    
    private int getRandomInt() {
        synchronized ( this ) {
            return this.random.nextInt();
        }
    }
}
