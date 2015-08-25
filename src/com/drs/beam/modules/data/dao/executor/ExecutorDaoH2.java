/*
 * project: Beam
 * author: Diarsid
 */

package com.drs.beam.modules.data.dao.executor;

import com.drs.beam.modules.io.BeamIO;
import com.drs.beam.modules.io.InnerIOInterface;
import com.drs.beam.modules.data.base.DataBase;
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
    private final InnerIOInterface ioEngine = BeamIO.getInnerIO();
    
    /* 
     * SQL Tables description for locations and commands entities.
     * 
     *  Location`s table:
     *  | location_name  | location_path                               |
     *  |----------------+---------------------------------------------+
     *  | javaBooks      | C:/path/to/my/library/with/books/about/java |
     * 
     *  Command`s table:
     *  | command_name  | command_text                                                    |
     *  |---------------+-----------------------------------------------------------------+
     *  | work          | run netbeans::open myproj in projects::open Bloch in javaBooks  |
     */

    private final String SELECT_LOCATIONS_WHERE_NAME_LIKE = 
            "SELECT location_name, location_path "+
            "FROM locations "+
            "WHERE location_name LIKE ?";
    private final String SELECT_LOCATIONS_WHERE_NAME = 
            "SELECT location_name, location_path "+
            "FROM locations "+
            "WHERE ";
    private final String NAME_LIKE_NAMEPART = 
            " location_name LIKE ? ";
    private final String AND = 
            " AND ";
    private final String SELECT_COMMANDS_WHERE_NAME_LIKE = 
            "SELECT command_name, command_text " +
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
    public Map<String, String> getLocationsByName(String locationName){
        Map<String, String> locations = new HashMap<>();
        try(    Connection con = data.connect();
                PreparedStatement ps = con.prepareStatement(SELECT_LOCATIONS_WHERE_NAME_LIKE);)
        {
            ps.setString(1, "%"+locationName+"%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()){
                String locName = rs.getString(1);
                String locPath = rs.getString(2);
                locations.put(locName, locPath);
            }
            rs.close();
            return locations;
        } catch(SQLException e){
            ioEngine.informAboutException(e, false);
            return locations;
        }
    }
    
    @Override
    public Map<String, String> getLocationsByNameParts(String[] locationNameParts){
        Map<String, String> locations = new HashMap<>();
        try(    Connection con = data.connect())
        {   
            StringBuilder statementBuild = new StringBuilder();
            statementBuild.append(SELECT_LOCATIONS_WHERE_NAME);
            statementBuild.append(NAME_LIKE_NAMEPART);
            if (locationNameParts.length > 0){
                for(int i = 1; i < locationNameParts.length; i++){
                    statementBuild.append(AND).append(NAME_LIKE_NAMEPART);
                }
                PreparedStatement ps = con.prepareStatement(statementBuild.toString());
                for(int j = 0; j < locationNameParts.length; j++){
                    ps.setString(j+1, "%"+locationNameParts[j]+"%");
                }
                ResultSet rs = ps.executeQuery();
                while (rs.next()){
                    String locName = rs.getString(1);
                    String locPath = rs.getString(2);
                    locations.put(locName, locPath);
                }
                rs.close();
                ps.close();
                return locations;
            } else {
                return locations;
            }
        } catch(SQLException e){
            ioEngine.informAboutException(e, false);
            return locations;
        }
    }
    
    @Override
    public Map<String, List<String>>  getCommandsByName(String commandName){
        Map<String, List<String>> allCommands = new HashMap<>();
        try(    Connection con = data.connect();
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
            ioEngine.informAboutException(e, false);
            return allCommands;
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
    public boolean removeCommand(String commandName){
        try (   Connection con = data.connect();
                PreparedStatement ps = con.prepareStatement(DELETE_COMMAND_WHERE_NAME_lIKE);)
        {
            ps.setString(1, commandName);
            int qty = ps.executeUpdate();
            return (qty > 0);
        } catch(SQLException e){
            ioEngine.informAboutException(e, false);
            return false;
        }    
    }
    
    @Override
    public boolean removeLocation(String locationName){
        try(    Connection con = data.connect();
                PreparedStatement ps = con.prepareStatement(DELETE_LOCATION_WHERE_NAME_lIKE);)
        {
            ps.setString(1, locationName);
            int qty = ps.executeUpdate();
            return (qty > 0);
        } catch(SQLException e){
            ioEngine.informAboutException(e, false);
            return false;
        } 
    }
    
    @Override
    public Map<String, String> getLocations(){
        Map<String, String> locations = new HashMap<>();
        try(    Connection con = data.connect();
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery(SELECT_ALL_LOCATIONS);)
        {            
            while(rs.next()){
                locations.put(rs.getString(1), rs.getString(2));
            } 
            return locations;
        } catch(SQLException e){
            ioEngine.informAboutException(e, false);
            return locations;
        }     
    } 
    
    @Override
    public Map<String, List<String>> getCommands(){
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
            return commands;
        } catch(SQLException e){
            ioEngine.informAboutException(e, false);
            return commands;
        } 
    }

}
