/*
 * project: Beam
 * author: Diarsid
 */

package com.drs.beam.modules.data.dao.locations;

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
public class LocationsDaoH2 implements LocationsDao{
    // Fields =============================================================================
    private final DataBase data;
    private final InnerIOInterface ioEngine;
    
    /* 
     * SQL Table description for locations entities.
     *  +--------------------------------------------------------------+
     *  | locations                                                    |
     *  +--------------------------------------------------------------+
     *  | location_name  | location_path                               |
     *  +----------------+---------------------------------------------+
     *  | javaBooks      | /path/to/my/library/with/books/about/java   |
     *  +----------------+---------------------------------------------+
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
    private final String INSERT_NEW_LOCATION = 
            "INSERT INTO locations VALUES (?, ?)";   
    private final String DELETE_LOCATION_WHERE_NAME_lIKE = 
            "DELETE "+
            "FROM locations " +
            "WHERE location_name LIKE ?";
    private final String SELECT_ALL_LOCATIONS = 
            "SELECT * "+
            "FROM locations";
    
    // Notifications
    private final String MORE_THAN_ONE_LOCATION = 
            "There are more than one location with such name.";    
    private final String NO_SUCH_LOCATION = 
            "Couldn`t find location with such name.";   
    private final String LOCATION_ALREADY_EXISTS = 
            "Such location name already exists.";    
    private final String NO_STORED_LOCATIONS = "There aren`t any locations.";
    
    // Constructors =======================================================================
    public LocationsDaoH2(DataBase data, InnerIOInterface io) {
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
    public Map<String, String> getLocationsByName(String locationName){
        Map<String, String> locations = new HashMap<>();
        try(    Connection con = this.data.connect();
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
            this.ioEngine.informAboutException(e, false);
            return locations;
        }
    }
    
    @Override
    public Map<String, String> getLocationsByNameParts(String[] locationNameParts){
        Map<String, String> locations = new HashMap<>();
        try(    Connection con = this.data.connect())
        {               
            if (locationNameParts.length > 0){
                StringBuilder statementBuild = new StringBuilder();
                statementBuild.append(SELECT_LOCATIONS_WHERE_NAME).append(NAME_LIKE_NAMEPART);
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
            this.ioEngine.informAboutException(e, false);
            return locations;
        }
    }
        
    @Override
    public void saveNewLocation(String locationPath, String locationName){
        try(    Connection con = this.data.connect();
                PreparedStatement ps = con.prepareStatement(INSERT_NEW_LOCATION);)
        {
            ps.setString(1, locationName);
            ps.setString(2, locationPath.replace("\\", "/"));
            ps.executeUpdate();
        } catch(SQLException e){
            if (e.getSQLState().startsWith("23")){
                this.ioEngine.informAboutError(LOCATION_ALREADY_EXISTS, false);
            } else {
                this.ioEngine.informAboutException(e, false);
            }            
        }   
    }
    
    @Override
    public boolean removeLocation(String locationName){
        try(    Connection con = this.data.connect();
                PreparedStatement ps = con.prepareStatement(DELETE_LOCATION_WHERE_NAME_lIKE);)
        {
            ps.setString(1, locationName);
            int qty = ps.executeUpdate();
            return (qty > 0);
        } catch(SQLException e){
            this.ioEngine.informAboutException(e, false);
            return false;
        } 
    }
    
    @Override
    public Map<String, String> getLocations(){
        Map<String, String> locations = new HashMap<>();
        try(    Connection con = this.data.connect();
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery(SELECT_ALL_LOCATIONS);)
        {            
            while(rs.next()){
                locations.put(rs.getString(1), rs.getString(2));
            } 
            return locations;
        } catch(SQLException e){
            this.ioEngine.informAboutException(e, false);
            return locations;
        }     
    } 
}
