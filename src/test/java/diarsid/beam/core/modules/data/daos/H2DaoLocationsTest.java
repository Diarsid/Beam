/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.data.daos;


import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import testing.embedded.base.h2.H2TestDataBase;
import testing.embedded.base.h2.TestDataBase;

import diarsid.beam.core.control.io.base.Initiator;
import diarsid.beam.core.control.io.base.InnerIoEngine;
import diarsid.beam.core.domain.entities.Location;
import diarsid.beam.core.modules.data.DaoLocations;
import diarsid.jdbc.transactions.exceptions.TransactionHandledSQLException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import static diarsid.beam.core.util.CollectionsUtils.containsOne;
import static diarsid.beam.core.util.CollectionsUtils.getOne;
import static diarsid.beam.core.util.StringUtils.splitByWildcard;
import static diarsid.jdbc.transactions.core.Params.params;

/**
 *
 * @author Diarsid
 */
public class H2DaoLocationsTest {
    
    private static final Logger logger = LoggerFactory.getLogger(H2DaoLocationsTest.class);
    private static DaoLocations daoLocations;
    private static TestDataBase testDataBase;
    private static Initiator initiator;

    public H2DaoLocationsTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        InnerIoEngine ioEngine = mock(InnerIoEngine.class);
        initiator = new Initiator();
        testDataBase = new H2TestDataBase("testBase");
        daoLocations = new H2DaoLocations(testDataBase, ioEngine);
        testDataBase.setupRequiredTable(
                "CREATE TABLE locations (" +
                "loc_name   VARCHAR(300)    NOT NULL PRIMARY KEY," +
                "loc_path   VARCHAR(300)    NOT NULL)");
    }
    
    private static void setupTestData() {
        try {
            testDataBase
                    .transactionFactory()
                    .createDisposableTransaction()
                    .doBatchUpdateVarargParams(
                            "INSERT INTO locations (loc_name, loc_path) " +
                            "VALUES ( ?, ? ) ",
                            params("books", "C:/my_doc/BOOKS"),
                            params("My_Projects", "D:/Tech/DEV/projects"),
                            params("java_projects", "D:/Tech/DEV/projects/java"),
                            params("js_projects", "D:/Tech/DEV/projects/js"));
        } catch (TransactionHandledSQLException ex) {
            
        }
    }
    
    private static void clearTestData() {
        try {
            testDataBase
                    .transactionFactory()
                    .createDisposableTransaction()
                    .doUpdate("DELETE FROM locations");
        } catch (TransactionHandledSQLException ex) {
            
        }
    }
    
    @Before
    public void setUpCase() {
        setupTestData();
    }
    
    @After
    public void clearCase() {
        clearTestData();
    }

    /**
     * Test of getLocationsByName method, of class H2DaoLocations.
     */
    @Test
    public void testGetLocationsByName() {
        List<Location> locations = daoLocations.getLocationsByName(initiator, "proj");
        assertTrue(locations.size() == 3);
        assertTrue(testDataBase.ifAllConnectionsReleased());
    }

    /**
     * Test of getLocationsByNameParts method, of class H2DaoLocations.
     */
    @Test
    public void testGetLocationsByNameParts() {
        List<Location> j_proj_locations = daoLocations
                .getLocationsByNameParts(initiator, splitByWildcard("j-proj"));
        assertTrue(j_proj_locations.size() == 3);
        assertTrue(testDataBase.ifAllConnectionsReleased());
        
        List<Location> js_proj_locations = daoLocations
                .getLocationsByNameParts(initiator, splitByWildcard("js-proj"));
        assertTrue(js_proj_locations.size() == 1);
        assertTrue(testDataBase.ifAllConnectionsReleased());
        
        List<Location> j_pr_oj_cts_locations = daoLocations
                .getLocationsByNameParts(initiator, splitByWildcard("pr-j-cts-oj"));
        assertTrue(j_pr_oj_cts_locations.size() == 3);
        assertTrue(testDataBase.ifAllConnectionsReleased());
    }

    /**
     * Test of saveNewLocation method, of class H2DaoLocations.
     */
    @Test
    public void testSaveNewLocation() {
        int qtyBefore = testDataBase.countRowsInTable("locations");
        
        Location location = new Location("films", "c:/content/media/movies");
        boolean created = daoLocations.saveNewLocation(initiator, location);
        assertTrue(testDataBase.ifAllConnectionsReleased());
        assertTrue(created);
        
        int qtyAfter = testDataBase.countRowsInTable("locations");
        
        assertEquals(qtyBefore + 1, qtyAfter);
        assertTrue(testDataBase.ifAllConnectionsReleased());
    }
    
    @Test
    public void testSaveNewLocation_notPermitted() {
        int qtyBefore = testDataBase.countRowsInTable("locations");
        
        Location location = new Location("films", "c:/content/media/movies");
        boolean created = daoLocations.saveNewLocation(initiator, location);
        assertTrue(testDataBase.ifAllConnectionsReleased());
        assertTrue(created);
        
        Location locationClone = new Location("films", "c:/content/media/movies/another");
        boolean createdClone = daoLocations.saveNewLocation(initiator, locationClone);
        assertTrue(testDataBase.ifAllConnectionsReleased());
        assertFalse(createdClone);
        
        int qtyAfter = testDataBase.countRowsInTable("locations");
        
        assertEquals(qtyBefore + 1, qtyAfter);
    }

    /**
     * Test of removeLocation method, of class H2DaoLocations.
     */
    @Test
    public void testRemoveLocation() {
        int qtyBefore = testDataBase.countRowsInTable("locations");
        
        boolean deleted = daoLocations.removeLocation(initiator, "books");
        assertTrue(testDataBase.ifAllConnectionsReleased());
        assertTrue(deleted);
        
        int qtyAfter = testDataBase.countRowsInTable("locations");
        
        assertEquals(qtyBefore - 1, qtyAfter);
    }

    /**
     * Test of editLocationPath method, of class H2DaoLocations.
     */
    @Test
    public void testEditLocationPath() {
        boolean edited = daoLocations.editLocationPath(initiator, "books", "c:/new/path/for/books");
        assertTrue(testDataBase.ifAllConnectionsReleased());
        assertTrue(edited);
        
        List<Location> locations = daoLocations.getLocationsByName(initiator, "boo");
        assertTrue(testDataBase.ifAllConnectionsReleased());
        assertTrue(containsOne(locations));
        Location loc = getOne(locations);
        assertEquals("books", loc.getName());        
        assertEquals("c:/new/path/for/books", loc.getPath());
    }

    /**
     * Test of editLocationName method, of class H2DaoLocations.
     */
    @Test
    public void testEditLocationName() {
        boolean edited = daoLocations.editLocationName(initiator, "books", "my_books");
        assertTrue(testDataBase.ifAllConnectionsReleased());
        assertTrue(edited);
        
        List<Location> locations = daoLocations.getLocationsByName(initiator, "boo");
        assertTrue(testDataBase.ifAllConnectionsReleased());
        assertTrue(containsOne(locations));
        Location loc = getOne(locations);
        assertEquals("my_books", loc.getName());
    }

    /**
     * Test of replaceInPaths method, of class H2DaoLocations.
     */
    @Test
    public void testReplaceInPaths() {
        List<Location> locationsBefore = daoLocations.getLocationsByName(initiator, "proj");
        assertTrue(testDataBase.ifAllConnectionsReleased());
        locationsBefore
                .stream()
                .forEach(location -> assertTrue(location.getPath().startsWith("D:/Tech/DEV")));
        
        boolean replaced = daoLocations.replaceInPaths(initiator, "d:/tech/dev", "C:/Dev");
        assertTrue(testDataBase.ifAllConnectionsReleased());
        assertTrue(replaced);
        
        List<Location> locationsAfter = daoLocations.getLocationsByName(initiator, "proj");
        assertTrue(testDataBase.ifAllConnectionsReleased());
        locationsAfter
                .stream()
                .forEach(location -> assertTrue(location.getPath().startsWith("C:/Dev")));
    }

    /**
     * Test of getAllLocations method, of class H2DaoLocations.
     */
    @Test
    public void testGetAllLocations() {
        List<Location> locations = daoLocations.getAllLocations(initiator);
        assertTrue(testDataBase.ifAllConnectionsReleased());
        assertTrue(locations.size() == 4);
    }

}