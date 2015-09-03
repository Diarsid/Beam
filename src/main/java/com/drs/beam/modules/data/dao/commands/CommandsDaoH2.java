/*
 * project: Beam
 * author: Diarsid
 */

package com.drs.beam.modules.data.dao.commands;

import com.drs.beam.modules.io.InnerIOInterface;
import com.drs.beam.modules.data.base.DataBase;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.StringJoiner;

/**
 *
 * @author Diarsid
 */
public class CommandsDaoH2 implements CommandsDao{
    // Fields =============================================================================
    private final DataBase data;
    private final InnerIOInterface ioEngine;
    
    /* 
     * SQL Table description for commands entities.
     *
     *  +---------------------------------------------------------------------------------+
     *  | commands                                                                        |
     *  +---------------------------------------------------------------------------------+
     *  | command_name  | command_text                                                    |
     *  |---------------+-----------------------------------------------------------------+
     *  | work          | run netbeans::open myproj in projects::open Bloch in javaBooks  |
     *  +---------------------------------------------------------------------------------+
     */

    private final String SELECT_COMMANDS_WHERE_NAME_LIKE = 
            "SELECT command_name, command_text " +
            "FROM commands " +
            "WHERE command_name LIKE ?";
    private final String INSERT_NEW_COMMAND = 
            "INSERT INTO commands VALUES (?, ?)";    
    private final String DELETE_COMMAND_WHERE_NAME_lIKE = 
            "DELETE "+
            "FROM commands " +
            "WHERE command_name LIKE ?";    
    private final String SELECT_ALL_COMMANDS = 
            "SELECT * "+
            "FROM commands";
    
    // Notifications
    private final String MORE_THAN_ONE_COMMAND = 
            "There are more than one command with such name.";
    private final String NO_SUCH_COMMAND = 
            "Couldn`t find command with such name.";
    private final String COMMAND_ALREADY_EXISTS = 
            "Such command name already exists.";
    private final String NO_STORED_COMMANDS = "There aren`t any commands.";
    
    // Constructors =======================================================================
    public CommandsDaoH2(DataBase data, InnerIOInterface io) {
        this.data = data;
        this.ioEngine = io;
    }
    
    // Methods ============================================================================
    
    private String convertInputCommandForStoring(List<String> commands){
        StringJoiner sj = new StringJoiner("::");
        for(String command : commands){
            sj.add(command.replace("\\", "/"));
        }
        return sj.toString();
    }
    
    private List<String> convertStoredCommandForUsing(String storedCommandsString){
        return Arrays.asList(storedCommandsString.split("::"));
    }
    
    @Override
    public Map<String, List<String>>  getCommandsByName(String commandName){
        Map<String, List<String>> allCommands = new HashMap<>();
        try(    Connection con = this.data.connect();
                PreparedStatement ps = con.prepareStatement(SELECT_COMMANDS_WHERE_NAME_LIKE);)
        {   
            String restoredCommandName;
            String restoredCommandText;
            ps.setString(1, "%"+commandName+"%");
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                restoredCommandName = rs.getString(1);
                restoredCommandText = rs.getString(2);
                allCommands.put(
                        restoredCommandName, 
                        convertStoredCommandForUsing(restoredCommandText)
                );
            }
            rs.close();
            ps.close();            
            return allCommands;
        } catch(SQLException e){
            this.ioEngine.informAboutException(e, false);
            return allCommands;
        }   
    }
    
    @Override
    public void saveNewCommand(List<String> command, String commandName){
        try(    Connection con = this.data.connect();
                PreparedStatement ps = con.prepareStatement(INSERT_NEW_COMMAND);){
            ps.setString(1, commandName);
            ps.setString(2, convertInputCommandForStoring(command));
            ps.executeUpdate();
        } catch(SQLException e){
            if (e.getSQLState().startsWith("23")){
                this.ioEngine.informAboutError(COMMAND_ALREADY_EXISTS, false);
            } else {
                this.ioEngine.informAboutException(e, false);
            }
        }
    }
    
    @Override
    public boolean removeCommand(String commandName){
        try (   Connection con = this.data.connect();
                PreparedStatement ps = con.prepareStatement(DELETE_COMMAND_WHERE_NAME_lIKE);)
        {
            ps.setString(1, commandName);
            int qty = ps.executeUpdate();
            return (qty > 0);
        } catch(SQLException e){
            this.ioEngine.informAboutException(e, false);
            return false;
        }    
    }
    
    @Override
    public Map<String, List<String>> getCommands(){
        Map<String, List<String>> commands = new HashMap<>();
        try(    Connection con = this.data.connect();
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery(SELECT_ALL_COMMANDS);)
        {
            while(rs.next()){
                commands.put(
                        rs.getString(1), 
                        convertStoredCommandForUsing(rs.getString(2)));
            }
            return commands;
        } catch(SQLException e){
            this.ioEngine.informAboutException(e, false);
            return commands;
        } 
    }

}
