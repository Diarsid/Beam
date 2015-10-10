/*
 * project: Beam
 * author: Diarsid
 */

package com.drs.beam.server.modules.data.dao.locations;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.drs.beam.server.entities.location.Location;
import com.drs.beam.server.modules.data.base.DataBase;

/**
 *
 * @author Diarsid
 */
public class LocationsDaoH2 implements LocationsDao{
    // Fields =============================================================================
    private final DataBase data;
    
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
    private final String SELECT_LOCATIONS_WHERE = 
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
    public LocationsDaoH2(DataBase data) {
        this.data = data;
    }
    
    // Methods ============================================================================
    
    @Override
    public List<Location> getLocationsByName(String locationName) throws SQLException{
        List<Location> locations = new ArrayList<>();
        Connection con = this.data.connect();
        PreparedStatement ps = con.prepareStatement(SELECT_LOCATIONS_WHERE_NAME_LIKE);
        
        ps.setString(1, "%"+locationName+"%");
        ResultSet rs = ps.executeQuery();
        while (rs.next()){
            String locName = rs.getString(1);
            String locPath = rs.getString(2);
            locations.add(new Location(locName, locPath));
        }
        rs.close();
        ps.close();
        con.close();
        return locations;        
    }
    
    @Override
    public List<Location> getLocationsByNameParts(String[] locationNameParts) throws SQLException{        
        if (locationNameParts.length > 0){
            Connection con = this.data.connect();
            StringBuilder statementBuild = new StringBuilder();
            statementBuild.append(SELECT_LOCATIONS_WHERE).append(NAME_LIKE_NAMEPART);
            for(int i = 1; i < locationNameParts.length; i++){
                statementBuild.append(AND).append(NAME_LIKE_NAMEPART);
            }
            PreparedStatement ps = con.prepareStatement(statementBuild.toString());
            for(int j = 0; j < locationNameParts.length; j++){
                ps.setString(j+1, "%"+locationNameParts[j]+"%");
            }
            ResultSet rs = ps.executeQuery();
            List<Location> locations = new ArrayList<>();
            while (rs.next()){
                String locName = rs.getString(1);
                String locPath = rs.getString(2);
                locations.add(new Location(locName, locPath));
            }
            rs.close();
            ps.close();
            con.close();
            return locations;
        } else {
            return Collections.emptyList();
        }        
    }
        
    @Override
    public void saveNewLocation(Location location) throws SQLException{
        Connection con = this.data.connect();
        PreparedStatement ps = con.prepareStatement(INSERT_NEW_LOCATION);
        
        ps.setString(1, location.getName());
        ps.setString(2, location.getPath().replace("\\", "/"));
        ps.executeUpdate();            
            
        ps.close();
        con.close();
    }
    
    @Override
    public boolean removeLocation(String locationName) throws SQLException{
        Connection con = this.data.connect();
        PreparedStatement ps = con.prepareStatement(DELETE_LOCATION_WHERE_NAME_lIKE);
        
        ps.setString(1, locationName);
        int qty = ps.executeUpdate();
        
        ps.close();
        con.close();
        return (qty > 0);
    }
    
    @Override
    public List<Location> getAllLocations() throws SQLException{
        List<Location> locations = new ArrayList<>();
        Connection con = this.data.connect();
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(SELECT_ALL_LOCATIONS);          
        
        while(rs.next()){
            String locName = rs.getString(1);
            String locPath = rs.getString(2);
            locations.add(new Location(locName, locPath));
        }

        rs.close();
        st.close();
        con.close();
        return locations;           
    }    
}
