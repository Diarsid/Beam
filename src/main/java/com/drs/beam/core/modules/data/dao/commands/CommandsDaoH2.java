/*
 * project: Beam
 * author: Diarsid
 */

package com.drs.beam.core.modules.data.dao.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;

import com.drs.beam.core.entities.StoredExecutorCommand;
import com.drs.beam.core.modules.data.base.DataBase;

/**
 *
 * @author Diarsid
 */
public class CommandsDaoH2 implements CommandsDao{
    // Fields =============================================================================
    private final DataBase data;
    
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
    public CommandsDaoH2(DataBase data) {
        this.data = data;
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
    public List<StoredExecutorCommand> getCommandsByName(String commandName) throws SQLException{
        List<StoredExecutorCommand> commands = new ArrayList<>();
        Connection con = this.data.connect();
        PreparedStatement ps = con.prepareStatement(SELECT_COMMANDS_WHERE_NAME_LIKE);

        ps.setString(1, "%"+commandName+"%");
        ResultSet rs = ps.executeQuery();
        while(rs.next()){
            String restoredCommandName = rs.getString(1);
            String restoredCommandText = rs.getString(2);
            StoredExecutorCommand command = new StoredExecutorCommand(
                    restoredCommandName, 
                    convertStoredCommandForUsing(restoredCommandText));
            commands.add(command);
        }
        rs.close();
        ps.close();   
        con.close();
        return commands;          
    }
    
    @Override
    public void saveNewCommand(StoredExecutorCommand command) throws SQLException{
        Connection con = this.data.connect();
        PreparedStatement ps = con.prepareStatement(INSERT_NEW_COMMAND);
        
        ps.setString(1, command.getName());
        ps.setString(2, convertInputCommandForStoring(command.getCommands()));
        ps.executeUpdate();
        
        ps.close();
        con.close();
    }
    
    @Override
    public boolean removeCommand(String commandName) throws SQLException{
        Connection con = this.data.connect();
        PreparedStatement ps = con.prepareStatement(DELETE_COMMAND_WHERE_NAME_lIKE);
        
        ps.setString(1, commandName);
        int qty = ps.executeUpdate();

        ps.close();
        con.close();
        return (qty > 0);           
    }
    
    @Override
    public List<StoredExecutorCommand> getAllCommands() throws SQLException{
        List<StoredExecutorCommand> commands = new ArrayList<>();
        Connection con = this.data.connect();
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(SELECT_ALL_COMMANDS);

        while(rs.next()){
            String restoredCommandName = rs.getString(1);
            String restoredCommandText = rs.getString(2);
            StoredExecutorCommand command = new StoredExecutorCommand(
                    restoredCommandName, 
                    convertStoredCommandForUsing(restoredCommandText));
            commands.add(command);
        }
        
        rs.close();
        st.close();
        con.close();
        return commands;         
    }
}
