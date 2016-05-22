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
import diarsid.beam.core.modules.IoInnerModule;
import diarsid.beam.core.modules.data.DaoExecutorIntelligentChoices;
import diarsid.beam.core.modules.data.DataBase;
import diarsid.beam.core.modules.executor.CommandChoice;
import diarsid.beam.core.modules.executor.CurrentCommandState;

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
                    H2DaoIntellChoice.class.getSimpleName(), 
                    IoInnerModule.class.getSimpleName());
        }
        if (data == null) {
            throw new NullDependencyInjectionException(
                    H2DaoIntellChoice.class.getSimpleName(), 
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
            "WHERE ( command IS ? ) " + 
            "  AND ( pattern_number IS ? ) " + 
            "  AND ( pattern IS ? )";
    private final String SELECT_ALL_COMMANDS = 
            "SELECT command, pattern, choice, pattern_number " +
            "FROM command_choices ";
    private final String SELECT_COMMANDS_WHERE_COMMAND_OR_CHOICE_OR_PATTERN_LIKE = 
            "SELECT command, pattern, choice, pattern_number " +
            "FROM command_choices " + 
            "WHERE ( command LIKE ? ) " +
            "   OR ( pattern LIKE ? ) " +
            "   OR ( choice LIKE ? ) ";
    private final String INSERT_COMMAND = 
            "INSERT INTO command_choices " + 
            "       (choice_id, command, pattern_number, pattern, choice)" +
            "VALUES (?, ?, ?, ?, ?) ";
    private final String DELETE_WHERE_COMMAND_IS = 
            "DELETE FROM command_choices " + 
            "WHERE command IS ? ";
    
    @Override
    public String getChoiceForCommandPart(
            String command, int attemptNumber, String pattern) {
        
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
        try (Connection con = this.data.connect();
                PreparedStatement ps = con.prepareStatement(INSERT_COMMAND)) {
            
            for (CommandChoice choice : commandState.getMadeChoices()) {
                ps.setInt(1, this.getRandomInt());
                ps.setString(2, commandState.getCommandString());
                ps.setInt(3, choice.getChoiceNumber());
                ps.setString(4, choice.getPattern());
                ps.setString(5, choice.getMadeChoice());
                ps.addBatch();
            }            
            
            ps.executeBatch();
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
