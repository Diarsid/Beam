/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.data.sql.daos;

import java.util.List;
import java.util.Optional;

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import testing.embedded.base.h2.H2TestDataBase;
import testing.embedded.base.h2.TestDataBase;

import diarsid.beam.core.application.environment.ProgramsCatalog;
import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.data.DataBaseActuator;
import diarsid.beam.core.base.data.DataBaseModel;
import diarsid.beam.core.base.data.SqlDataBaseModel;
import diarsid.beam.core.domain.entities.NamedEntity;
import diarsid.beam.core.modules.data.DaoNamedEntities;
import diarsid.beam.core.modules.data.sql.database.H2DataBaseModel;
import diarsid.jdbc.transactions.JdbcTransaction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

import static diarsid.beam.core.base.control.io.base.actors.OuterIoEngineType.IN_MACHINE;
import static diarsid.beam.core.base.control.io.commands.CommandType.BATCH_PAUSE;
import static diarsid.beam.core.base.control.io.commands.CommandType.BROWSE_WEBPAGE;
import static diarsid.beam.core.base.control.io.commands.CommandType.OPEN_LOCATION;
import static diarsid.beam.core.base.control.io.commands.CommandType.OPEN_LOCATION_TARGET;
import static diarsid.beam.core.base.control.io.commands.CommandType.RUN_PROGRAM;
import static diarsid.beam.core.base.data.DataBaseActuator.getActuatorFor;
import static diarsid.beam.core.domain.entities.NamedEntityType.BATCH;
import static diarsid.beam.core.domain.entities.NamedEntityType.LOCATION;
import static diarsid.beam.core.domain.entities.WebPlace.WEBPANEL;
import static diarsid.jdbc.transactions.core.Params.params;

/**
 *
 * @author Diarsid
 */
public class H2DaoNamedEntitiesTest {
    
    private static final Logger logger = LoggerFactory.getLogger(H2DaoNamedEntitiesTest.class);
    
    private static TestDataBase dataBase;
    private static InnerIoEngine ioEngine;
    private static ProgramsCatalog programsCatalog;
    private static DaoNamedEntities dao;
    private static Initiator initiator;

    public H2DaoNamedEntitiesTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        prepareComponents();        
        prepareDataBase();
        setupTestData();
    }

    private static void prepareDataBase() throws Exception {
        DataBaseModel dataBaseModel = new H2DataBaseModel();
        
        DataBaseActuator actuator = getActuatorFor(dataBase, dataBaseModel);
        
        List<String> reports = actuator.actuateAndGetReport();
        reports.stream().forEach(report -> logger.info(report));
        assertEquals(reports.size(), ((SqlDataBaseModel) dataBaseModel).objects().size());
    }

    private static void prepareComponents() {
        initiator = new Initiator(36, IN_MACHINE);
        dataBase = new H2TestDataBase("testBase-gsdfjsqw");
        ioEngine = mock(InnerIoEngine.class);
        programsCatalog = mock(ProgramsCatalog.class);
        dao = new H2DaoNamedEntities(dataBase, ioEngine, programsCatalog);
    }
    
    private static void setupTestData() throws Exception {
        try (JdbcTransaction transact = dataBase.transactionFactory().createTransaction()) {
            
            transact
                    .doBatchUpdateVarargParams(
                            "INSERT INTO locations (loc_name, loc_path) " +
                            "VALUES ( ?, ? ) ",
                            params("books", "C:/my_doc/BOOKS"),
                            params("tomcat_deploy", "D:/tools/servers/web/tomcat/apps"),
                            params("java_projects", "D:/Tech/DEV/projects/java"),
                            params("js_projects", "D:/Tech/DEV/projects/js"),
                            params("paint_workspace", "D:/hobby/painting/workspace"));
            
            transact
                    .doUpdate(
                            "INSERT INTO web_directories ( name, ordering, place ) " +
                            "VALUES ( ?, ?, ? ) ", 
                            params("Common", 0, WEBPANEL));
            
            int dirId = transact
                    .doQueryAndConvertFirstRowVarargParams(
                            Integer.class, 
                            "SELECT id FROM web_directories WHERE name IS ? ", 
                            (row) -> {
                                return (Integer) row.get("id");
                            }, 
                            "Common")
                    .get();
            
            transact
                    .doBatchUpdateVarargParams(
                            "INSERT INTO web_pages (name, url, shortcuts, ordering, dir_id) " +
                            "VALUES ( ?, ?, ?, ?, ? )", 
                            params("tomcat_deploy", "http://some/fake/url", "tomcat server apps", 0, dirId));
            
            transact
                    .doBatchUpdateVarargParams(
                            "INSERT INTO batches (bat_name) " +
                            "VALUES ( ? )", 
                            params("workspace"), 
                            params("tomcat"), 
                            params("open_space"));
            
            int[] modified = transact
                    .doBatchUpdateVarargParams(
                            "INSERT INTO batch_commands (" +
                            "       bat_name, " +
                            "       bat_command_type, " +
                            "       bat_command_order, " +
                            "       bat_command_original ) " +
                            "VALUES ( ?, ?, ?, ? ) ",
                            params("workspace", RUN_PROGRAM,     0, "netbeans"),
                            params("workspace", OPEN_LOCATION,   1, "projects"),
                            params("workspace", BROWSE_WEBPAGE,     2, "google"),
                            params("tomcat", RUN_PROGRAM,    0, "mysql_server"),
                            params("tomcat", RUN_PROGRAM,    1, "tomcat"),
                            params("tomcat", BATCH_PAUSE,    2, "3 SECONDS"),
                            params("tomcat", BROWSE_WEBPAGE,    3, "tomcat_root"),
                            params("open_space", OPEN_LOCATION_TARGET,  0, "books/common"),
                            params("open_space", OPEN_LOCATION,         1, "projects"),
                            params("open_space", OPEN_LOCATION_TARGET,  2, "content/tech"),
                            params("open_space", OPEN_LOCATION,         3, "dev"));
            
            if ( modified.length != 11 ) {
                throw new IllegalArgumentException();
            }
            
        }       
    }

    /**
     * Test of getEntitiesByNamePattern method, of class H2DaoNamedEntities.
     */
    @Test
    public void testGetEntitiesByNamePattern() {
        List<NamedEntity> entities = dao.getEntitiesByNamePattern(initiator, "tomc");
        assertEquals(3, entities.size());
    }
    
    @Test
    public void testGetEntitiesByNamePattern_2() {
        List<NamedEntity> entities = dao.getEntitiesByNamePattern(initiator, "tocat");
        assertEquals(3, entities.size());
    }

    @Test
    public void testGetEntitiesByNamePattern_3() {
        List<NamedEntity> entities = dao.getEntitiesByNamePattern(initiator, "wrkspce");
        assertEquals(2, entities.size());
        assertTrue(entities
                .stream()
                .filter(entity -> entity.name().equals("workspace"))
                .findFirst()
                .get()
                .is(BATCH));
        assertTrue(entities
                .stream()
                .filter(entity -> entity.name().equals("paint_workspace"))
                .findFirst()
                .get()
                .is(LOCATION));
    }
    
    @Test
    public void testGetEntityByExactName_success() {
        Optional<? extends NamedEntity> entity = dao.getByExactName(initiator, "WORKspAce");
        assertTrue(entity.isPresent());
        assertEquals(BATCH, entity.get().type());
    }
    
    @Test
    public void testGetEntityByExactName_success_shouldBeLocation() {
        Optional<? extends NamedEntity> entity = dao.getByExactName(initiator, "tomcat_deploy");
        assertTrue(entity.isPresent());
        assertEquals(LOCATION, entity.get().type());
    }
    
    @Test
    public void testGetEntityByExactName_fail() {
        Optional<? extends NamedEntity> entity = dao.getByExactName(initiator, "space");
        assertFalse(entity.isPresent());
    }
}