/*
 * project: Beam
 * author: Diarsid
 */

package com.drs.beam.core.modules.data.daos;

import com.drs.beam.core.modules.data.DaoLocations;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.drs.beam.core.exceptions.NullDependencyInjectionException;
import com.drs.beam.core.entities.Location;
import com.drs.beam.core.modules.IoInnerModule;
import com.drs.beam.core.modules.data.DataBase;

/**
 *
 * @author Diarsid
 */
class H2DaoLocations implements DaoLocations{
    // Fields =============================================================================
    private final DataBase data;
    private final IoInnerModule ioEngine;
    
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
            "WHERE location_name LIKE ? " +
            "ORDER BY location_name";
    private final String SELECT_LOCATIONS_WHERE = 
            "SELECT location_name, location_path "+
            "FROM locations "+
            "WHERE ";
    private final String NAME_LIKE_NAMEPART = 
            " location_name LIKE ? ";
    private final String AND = 
            " AND ";
    private final String ORDER_BY_NAME_AND_PATH = 
            "ORDER BY location_name ";
    private final String INSERT_NEW_LOCATION = 
            "INSERT INTO locations VALUES (?, ?)";   
    private final String DELETE_LOCATION_WHERE_NAME_lIKE = 
            "DELETE FROM locations " +
            "WHERE location_name LIKE ?";
    private final String SELECT_ALL_LOCATIONS = 
            "SELECT location_name, location_path "+
            "FROM locations " +
            "ORDER BY location_name ";
    private final String UPDATE_PATH_WHERE_NAME_LIKE = 
            "UPDATE locations " +
            "SET location_path = ? " +
            "WHERE location_name = ? ";    
    
    // Notifications
    private final String MORE_THAN_ONE_LOCATION = 
            "There are more than one location with such name.";    
    private final String NO_SUCH_LOCATION = 
            "Couldn`t find location with such name.";   
    private final String LOCATION_ALREADY_EXISTS = 
            "Such location name already exists.";    
    private final String NO_STORED_LOCATIONS = "There aren`t any locations.";
    
    // Constructors =======================================================================
    H2DaoLocations(final IoInnerModule io, final DataBase data) {
        if (io == null){
            throw new NullDependencyInjectionException(
                    H2DaoLocations.class.getSimpleName(), IoInnerModule.class.getSimpleName());
        }
        if (data == null){
            throw new NullDependencyInjectionException(
                    H2DaoLocations.class.getSimpleName(), DataBase.class.getSimpleName());
        }
        this.data = data;
        this.ioEngine = io;
    }
    
    // Methods ============================================================================    
        
    @Override
    public boolean saveNewLocation(Location location){
        try(Connection con = this.data.connect();
            PreparedStatement ps = con.prepareStatement(INSERT_NEW_LOCATION);){
            
            ps.setString(1, location.getName());
            ps.setString(2, location.getPath().replace("\\", "/"));
            int qty = ps.executeUpdate();
            
            return ( qty > 0 );
        } catch (SQLException e) {
            if (e.getSQLState().startsWith("23")){
                this.ioEngine.reportMessage("Such location name already exists.");
            } else {
                this.ioEngine.reportException(e, "SQLException: save location.");
            }
            return false;
        }    
    }
    
    @Override
    public List<Location> getLocationsByName(String locationName){
        ResultSet rs = null;
        try(Connection con = this.data.connect();
            PreparedStatement ps = con.prepareStatement(SELECT_LOCATIONS_WHERE_NAME_LIKE);){
            List<Location> locations = new ArrayList<>();
            
            ps.setString(1, "%"+locationName+"%");
            rs = ps.executeQuery();
            while (rs.next()){
                locations.add(new Location(
                        rs.getString(1), 
                        rs.getString(2)));
            }
            return locations;
        } catch (SQLException e) {
            this.ioEngine.reportException(e, "SQLException: get locations by name.");            
            return Collections.emptyList();
        } finally {
            if (rs != null){
                try{
                    rs.close();
                } catch (SQLException se){
                    this.ioEngine.reportExceptionAndExitLater(se, 
                        "Unknown problem in LocationsDao.getLocationsByName:",
                        "ResultSet close exception.",
                        " ",
                        "Program will be closed.");
                }
            }
        }
    }
    
    @Override
    public List<Location> getLocationsByNameParts(String[] locationNameParts){ 
        if(locationNameParts.length > 0){
            
            StringBuilder statementBuild = new StringBuilder();
            statementBuild.append(SELECT_LOCATIONS_WHERE).append(NAME_LIKE_NAMEPART);
            for(int i = 1; i < locationNameParts.length; i++){
                statementBuild.append(AND).append(NAME_LIKE_NAMEPART);
            }
            statementBuild.append(ORDER_BY_NAME_AND_PATH);
            ResultSet rs = null;
            try(Connection con = this.data.connect();
               PreparedStatement ps = con.prepareStatement(statementBuild.toString());){
                for(int j = 0; j < locationNameParts.length; j++){
                    ps.setString(j+1, "%"+locationNameParts[j]+"%");
                }
                rs = ps.executeQuery();
                List<Location> locations = new ArrayList<>();
                while (rs.next()){
                    locations.add(new Location(
                        rs.getString(1), 
                        rs.getString(2)));
                }               
                return locations;
            } catch (SQLException e){
                this.ioEngine.reportException(e, "SQLException: get locations by name parts.");                
                return Collections.emptyList();
            } finally {
                if (rs != null){
                    try {
                        rs.close();
                    } catch (SQLException se){
                        this.ioEngine.reportExceptionAndExitLater(se, 
                            "Unknown problem in LocationsDao.getLocationsByNameParts:",
                            "ResultSet close exception.",
                            " ",
                            "Program will be closed.");
                    }
                }
            }
        } else {
            return Collections.emptyList();
        } 
    }
    
    @Override
    public boolean editLocationPath(String locationName, String newPath) {
        try (Connection con = this.data.connect();
            PreparedStatement ps = con.prepareStatement(UPDATE_PATH_WHERE_NAME_LIKE);) {
            
            ps.setString(1, newPath);
            ps.setString(2, locationName.replace("\\", "/"));
            
            int qty = ps.executeUpdate();
            return (qty > 0);
        } catch (SQLException e) {
            this.ioEngine.reportException(e, "SQLException: edit location's path.");
            return false;
        }
    }    
    
    @Override
    public boolean removeLocation(String locationName){
        try(Connection con = this.data.connect();
            PreparedStatement ps = con.prepareStatement(DELETE_LOCATION_WHERE_NAME_lIKE);){
            
            ps.setString(1, locationName);
            int qty = ps.executeUpdate();

            return (qty > 0);
        } catch (SQLException e) {
            this.ioEngine.reportException(e, "SQLException: remove location.");
            return false;
        }    
    }
    
    @Override
    public List<Location> getAllLocations(){
        try(Connection con = this.data.connect();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(SELECT_ALL_LOCATIONS);){
            
            List<Location> locations = new ArrayList<>();
            while(rs.next()){
                locations.add(new Location(
                        rs.getString(1), 
                        rs.getString(2)));
            }
            return locations;   
        } catch (SQLException e) {
            this.ioEngine.reportException(e, "SQLException: get all locations.");
            return Collections.emptyList();
        }    
    }    
}
