/*
 * project: Beam
 * author: Diarsid
 */

package com.drs.beam.executor.dao;

import com.drs.beam.io.BeamIO;
import com.drs.beam.io.InnerIOIF;
import com.drs.beam.util.data.DataBase;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;

/**
 *
 * @author Diarsid
 */
public class ExecutorDaoH2 implements ExecutorDao{
    // Fields =============================================================================
    private final DataBase data;
    private final InnerIOIF ioEngine = BeamIO.getInnerIO();
    
    private final String CREATE_COMMANDS_TABLE = 
            "CREATE TABLE commands (" +
            "command_name   VARCHAR(20)     NOT NULL PRIMARY KEY, " +
            "command_text   VARCHAR(300)    NOT NULL";
    private final String CREATE_LOCATIONS_TABLE = 
            "CREATE TABLE locations (" +
            "location_name   VARCHAR(20)     NOT NULL PRIMARY KEY, " +
            "location_path   VARCHAR(300)    NOT NULL";
    private final String SELECT_LOCATIONS_WHERE_NAME_LIKE = 
            "SELECT location_path "+
            "FROM locations "+
            "WHERE location_name LIKE ?";
    private final String SELECT_COMMANDS_WHERE_NAME_LIKE = 
            "SELECT command_text " +
            "FROM commands " +
            "WHERE command_name LIKE ?";
    private final String INSERT_NEW_COMMAND = 
            "INSERT INTO commands VALUES (?, ?)";
    
    // Notifications
    private final String MORE_THAN_ONE_LOCATION = 
            "There are more than one location with such name.";
    private final String MORE_THAN_ONE_COMMAND = 
            "There are more than one command with such name.";
    private final String NO_SUCH_LOCATION = 
            "Couldn`t find location with such name.";
    private final String NO_SUCH_COMMAND = 
            "Couldn`t find command with such name.";   
    
    // Util strings
    private final String NO_RESULTS = "";
    private final String DELIMITER = "~}";
    private final String ANY_SYMBOLS = "%";
    private final String NATIVE_SEPARATOR = "/";
    private final String WIN_SEPARATOR = "\\";
    
    
    // Constructors =======================================================================
    public ExecutorDaoH2(DataBase data) {
        this.data = data;
    }
    
    // Methods ============================================================================
    
    private String convertInputCommandForStoring(List<String> commands){
        StringJoiner sj = new StringJoiner(DELIMITER);
        for(String command : commands){
            sj.add(command.replace(WIN_SEPARATOR, NATIVE_SEPARATOR));
        }
        return sj.toString();
    }
    
    private List<String> convertStoredCommandForUsing(String storedCommandsString){
        return Arrays.asList(storedCommandsString.split(DELIMITER));
    }
    
    @Override
    public String getLocationByName(String locationName){
        try(Connection con = data.getConnection();
            PreparedStatement ps = con.prepareStatement(SELECT_LOCATIONS_WHERE_NAME_LIKE);)
        {
            ps.setString(1, ANY_SYMBOLS+locationName+ANY_SYMBOLS);
            ResultSet rs = ps.executeQuery();
            if (rs.first()){
                String location = rs.getString(1);
                rs.last();
                int rowQty = rs.getRow();
                rs.close();
                if (rowQty > 1){
                    ioEngine.inform(MORE_THAN_ONE_LOCATION);
                    return NO_RESULTS;
                }
                return location;
            } else {
                ioEngine.inform(NO_SUCH_LOCATION);
                return NO_RESULTS;
            }
        } catch(SQLException e){
            ioEngine.informAboutException(e, false);
            return NO_RESULTS;
        }
    }
    
    @Override
    public List<String> getCommandsByNames(List<String> commandsNames){
        try(Connection con = data.getConnection();
            PreparedStatement ps = con.prepareStatement(SELECT_COMMANDS_WHERE_NAME_LIKE);)
        {
            List<String> unconvertedCommandsList = new ArrayList<>();
            for(String commandName : commandsNames){
                ps.setString(1, ANY_SYMBOLS+commandName+ANY_SYMBOLS);
                ResultSet rs = ps.executeQuery();
                if (rs.first()){
                    String command = rs.getString(1);
                    rs.last();
                    int rowQty = rs.getRow();
                    rs.close();
                    if (rowQty > 1){
                        ioEngine.inform(MORE_THAN_ONE_COMMAND);
                    } else {
                        unconvertedCommandsList.add(command);
                    }                    
                } else {
                    ioEngine.inform(NO_SUCH_COMMAND);
                } 
                ps.clearParameters();
            }
            List<String> readyCommands = new ArrayList<>(); 
            for (String unconvertedCommand : unconvertedCommandsList){
                readyCommands.addAll(convertStoredCommandForUsing(unconvertedCommand));
            }
            return readyCommands;
        } catch(SQLException e){
            ioEngine.informAboutException(e, false);
            return new ArrayList<>();
        }   
    }
    
    @Override
    public void saveNewCommand(List<String> command, String commandName){
        try(Connection con = data.getConnection();
            PreparedStatement ps = con.prepareStatement(INSERT_NEW_COMMAND);){
            ps.setString(1, commandName);
            ps.setString(2, convertInputCommandForStoring(command));
            ps.executeUpdate();
        } catch(SQLException e){
            ioEngine.informAboutException(e, false);
        }
    }
    
    @Override
    public void saveNewLocation(String location){
        
    }
    
    @Override
    public void removeCommand(String commandName){
        
    }
    
    @Override
    public void removeLocation(String LocationName){
        
    }

}
