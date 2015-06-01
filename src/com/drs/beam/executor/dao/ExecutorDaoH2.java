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
import java.sql.Statement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.StringJoiner;

/**
 *
 * @author Diarsid
 */
public class ExecutorDaoH2 implements ExecutorDao{
    // Fields =============================================================================
    private final DataBase data;
    private final InnerIOIF ioEngine = BeamIO.getInnerIO();
    
    /* 
     * SQL Tables description for locations and commands entities.
     * 
     *  Location`s table:
     *  | location_name  | location_path                               |
     *  |----------------+---------------------------------------------+
     *  | javaBooks      | C:path/to/my/library/with/books/about/java  |
     * 
     *  Command`s table:
     *  | command_name  | command_text                                                    |
     *  |---------------+-----------------------------------------------------------------+
     *  | work          | run netbeans::open myproj in projects::open Bloch in javaBooks  |
     */

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
    private final String INSERT_NEW_LOCATION = 
            "INSERT INTO locations VALUES (?, ?)";
    private final String DELETE_COMMAND_WHERE_NAME_lIKE = 
            "DELETE "+
            "FROM commands " +
            "WHERE command_name LIKE ?";
    private final String DELETE_LOCATION_WHERE_NAME_lIKE = 
            "DELETE "+
            "FROM locations " +
            "WHERE location_name LIKE ?";
    private final String SELECT_ALL_LOCATIONS = 
            "SELECT * "+
            "FROM locations";
    private final String SELECT_ALL_COMMANDS = 
            "SELECT * "+
            "FROM commands";
    
    // Notifications
    private final String MORE_THAN_ONE_LOCATION = 
            "There are more than one location with such name.";
    private final String MORE_THAN_ONE_COMMAND = 
            "There are more than one command with such name.";
    private final String NO_SUCH_LOCATION = 
            "Couldn`t find location with such name.";
    private final String NO_SUCH_COMMAND = 
            "Couldn`t find command with such name.";  
    private final String LOCATION_ALREADY_EXISTS = 
            "Such location name already exists.";
    private final String COMMAND_ALREADY_EXISTS = 
            "Such command name already exists.";
    private final String NO_STORED_LOCATIONS = "There aren`t any locations.";
    private final String NO_STORED_COMMANDS = "There aren`t any commands.";
    
    // Constructors =======================================================================
    public ExecutorDaoH2(DataBase data) {
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
    public String getLocationByName(String locationName){
        try(    Connection con = data.connect();
                PreparedStatement ps = con.prepareStatement(SELECT_LOCATIONS_WHERE_NAME_LIKE);)
        {
            ps.setString(1, "%"+locationName+"%");
            ResultSet rs = ps.executeQuery();
            if (rs.first()){
                String location = rs.getString(1);
                rs.last();
                int rowQty = rs.getRow();
                rs.close();
                if (rowQty > 1){
                    ioEngine.inform(MORE_THAN_ONE_LOCATION);
                    return "";
                }
                return location;
            } else {
                ioEngine.inform(NO_SUCH_LOCATION);
                return "";
            }
        } catch(SQLException e){
            ioEngine.informAboutException(e, false);
            return "";
        }
    }
    
    @Override
    public List<String> getCommandsByNames(List<String> commandsNames){
        List<String> readyCommands = new ArrayList<>();
        try(    Connection con = data.connect();
                PreparedStatement ps = con.prepareStatement(SELECT_COMMANDS_WHERE_NAME_LIKE);)
        {
            List<String> unconvertedCommandsList = new ArrayList<>();
            for(String commandName : commandsNames){
                ps.setString(1, "%"+commandName+"%");
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
            for (String unconvertedCommand : unconvertedCommandsList){
                readyCommands.addAll(convertStoredCommandForUsing(unconvertedCommand));
            }
            return readyCommands;
        } catch(SQLException e){
            ioEngine.informAboutException(e, false);
            return readyCommands;
        }   
    }
    
    @Override
    public void saveNewCommand(List<String> command, String commandName){
        try(    Connection con = data.connect();
                PreparedStatement ps = con.prepareStatement(INSERT_NEW_COMMAND);){
            ps.setString(1, commandName);
            ps.setString(2, convertInputCommandForStoring(command));
            ps.executeUpdate();
        } catch(SQLException e){
            if (e.getSQLState().startsWith("23")){
                ioEngine.informAboutError(COMMAND_ALREADY_EXISTS, false);
            } else {
                ioEngine.informAboutException(e, false);
            }
        }
    }
    
    @Override
    public void saveNewLocation(String locationPath, String locationName){
        try(    Connection con = data.connect();
                PreparedStatement ps = con.prepareStatement(INSERT_NEW_LOCATION);)
        {
            ps.setString(1, locationName);
            ps.setString(2, locationPath.replace("\\", "/"));
            ps.executeUpdate();
        } catch(SQLException e){
            if (e.getSQLState().startsWith("23")){
                ioEngine.informAboutError(LOCATION_ALREADY_EXISTS, false);
            } else {
                ioEngine.informAboutException(e, false);
            }            
        }   
    }
    
    @Override
    public void removeCommand(String commandName){
        try (   Connection con = data.connect();
                PreparedStatement ps = con.prepareStatement(DELETE_COMMAND_WHERE_NAME_lIKE);)
        {
            ps.setString(1, commandName);
            ps.executeUpdate();
        } catch(SQLException e){
            ioEngine.informAboutException(e, false);
        }    
    }
    
    @Override
    public void removeLocation(String locationName){
        try(    Connection con = data.connect();
                PreparedStatement ps = con.prepareStatement(DELETE_LOCATION_WHERE_NAME_lIKE);)
        {
            ps.setString(1, locationName);
            ps.executeUpdate();
        } catch(SQLException e){
            ioEngine.informAboutException(e, false);
        } 
    }
    
    @Override
    public Map<String, String> viewLocations(){
        Map<String, String> locations = new HashMap<>();
        try(    Connection con = data.connect();
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery(SELECT_ALL_LOCATIONS);)
        {            
            while(rs.next()){
                locations.put(rs.getString(1), rs.getString(2));
            }
            if(locations.isEmpty()){
                ioEngine.inform(NO_STORED_LOCATIONS);
            } 
            return locations;
        } catch(SQLException e){
            ioEngine.informAboutException(e, false);
            return locations;
        }     
    } 
    
    @Override
    public Map<String, List<String>> viewCommands(){
        Map<String, List<String>> commands = new HashMap<>();
        try(    Connection con = data.connect();
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery(SELECT_ALL_COMMANDS);)
        {
            while(rs.next()){
                commands.put(
                        rs.getString(1), 
                        convertStoredCommandForUsing(rs.getString(2)));
            }
            if (commands.isEmpty()){
                ioEngine.inform(NO_STORED_COMMANDS);
            }
            return commands;
        } catch(SQLException e){
            ioEngine.informAboutException(e, false);
            return commands;
        } 
    }

}
