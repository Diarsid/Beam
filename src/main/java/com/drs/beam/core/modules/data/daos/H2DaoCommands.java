/*
 * project: Beam
 * author: Diarsid
 */

package com.drs.beam.core.modules.data.daos;

import com.drs.beam.core.modules.data.DaoCommands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;

import com.drs.beam.core.exceptions.NullDependencyInjectionException;
import com.drs.beam.core.modules.IoInnerModule;
import com.drs.beam.core.modules.executor.StoredExecutorCommand;
import com.drs.beam.core.modules.data.DataBase;

/**
 *
 * @author Diarsid
 */
class H2DaoCommands implements DaoCommands{
    // Fields =============================================================================
    private final DataBase data;
    private final IoInnerModule ioEngine;
    
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
    H2DaoCommands(final IoInnerModule io, final DataBase data) {
        if (io == null){
            throw new NullDependencyInjectionException(
                    H2DaoCommands.class.getSimpleName(), 
                    IoInnerModule.class.getSimpleName());
        }
        if (data == null){
            throw new NullDependencyInjectionException(
                    H2DaoCommands.class.getSimpleName(), 
                    DataBase.class.getSimpleName());
        }
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
    public List<StoredExecutorCommand> getCommandsByName(String commandName){
        ResultSet rs = null;
        try(Connection con = this.data.connect();
            PreparedStatement ps = con.prepareStatement(SELECT_COMMANDS_WHERE_NAME_LIKE);){
            List<StoredExecutorCommand> commands = new ArrayList<>();

            ps.setString(1, "%"+commandName+"%");
            rs = ps.executeQuery();
            while(rs.next()){
                commands.add(new StoredExecutorCommand(
                        rs.getString(1), 
                        convertStoredCommandForUsing(rs.getString(2)))
                );
            }            
            return commands; 
        } catch (SQLException e){
            this.ioEngine.reportException(e, "SQLException: get commands by name.");
            return Collections.emptyList();
        } finally {
            if (rs != null){
                try{
                    rs.close();
                } catch (SQLException se){
                    this.ioEngine.reportExceptionAndExitLater(se, 
                        "Unknown problem in CommandsDao.getCommandsByName:",
                        "ResultSet close exception.",
                        " ",
                        "Program will be closed.");
                }
            }
        }
    }
    
    @Override
    public void saveNewCommand(StoredExecutorCommand command){
        try(Connection con = this.data.connect();
            PreparedStatement ps = con.prepareStatement(INSERT_NEW_COMMAND);){
            
            ps.setString(1, command.getName());
            ps.setString(2, convertInputCommandForStoring(command.getCommands()));
            ps.executeUpdate();
            
        } catch (SQLException e){
            if (e.getSQLState().startsWith("23")){
                this.ioEngine.reportMessage("Such command name already exists.");
            } else {
                this.ioEngine.reportException(e, "SQLException: save command.");
            }
        }
    }
    
    @Override
    public boolean removeCommand(String commandName){
        try(Connection con = this.data.connect();
            PreparedStatement ps = con.prepareStatement(DELETE_COMMAND_WHERE_NAME_lIKE);){

            ps.setString(1, commandName);
            int qty = ps.executeUpdate();

            return (qty > 0); 
        } catch (SQLException e){
            this.ioEngine.reportException(e, "SQLException: remove command.");
            return false;
        } 
    }
    
    @Override
    public List<StoredExecutorCommand> getAllCommands(){
        try(Connection con = this.data.connect();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(SELECT_ALL_COMMANDS);){
            
            List<StoredExecutorCommand> commands = new ArrayList<>();
            while(rs.next()){
                commands.add(new StoredExecutorCommand(
                        rs.getString(1), 
                        convertStoredCommandForUsing(rs.getString(2)))
                );
            }

            return commands;  
        } catch (SQLException e){
            this.ioEngine.reportException(e, "SQLException: get commands.");
            return Collections.emptyList();
        }
    }
}