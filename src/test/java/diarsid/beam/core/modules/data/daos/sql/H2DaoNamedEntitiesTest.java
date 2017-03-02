/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.data.daos.sql;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import testing.embedded.base.h2.H2TestDataBase;
import testing.embedded.base.h2.TestDataBase;

import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.domain.entities.NamedEntity;
import diarsid.beam.core.modules.data.DaoNamedEntities;
import diarsid.beam.core.modules.data.DataBaseVerifier;
import diarsid.beam.core.modules.data.database.sql.H2DataBaseInitializer;
import diarsid.beam.core.modules.data.database.sql.H2DataBaseModel;
import diarsid.beam.core.modules.data.database.sql.H2DataBaseVerifier;
import diarsid.beam.core.modules.data.database.sql.SqlDataBaseInitializer;
import diarsid.beam.core.modules.data.database.sql.SqlDataBaseModel;
import diarsid.jdbc.transactions.JdbcTransaction;
import diarsid.jdbc.transactions.exceptions.TransactionHandledException;
import diarsid.jdbc.transactions.exceptions.TransactionHandledSQLException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import static diarsid.beam.core.base.control.io.commands.CommandType.BATCH_PAUSE;
import static diarsid.beam.core.base.control.io.commands.CommandType.OPEN_LOCATION;
import static diarsid.beam.core.base.control.io.commands.CommandType.OPEN_PATH;
import static diarsid.beam.core.base.control.io.commands.CommandType.RUN_PROGRAM;
import static diarsid.beam.core.base.control.io.commands.CommandType.SEE_WEBPAGE;
import static diarsid.beam.core.base.util.StringUtils.splitByWildcard;
import static diarsid.jdbc.transactions.core.Params.params;

/**
 *
 * @author Diarsid
 */
public class H2DaoNamedEntitiesTest {
    
    private static final Logger logger = LoggerFactory.getLogger(H2DaoNamedEntitiesTest.class);
    
    private static TestDataBase dataBase;
    private static InnerIoEngine ioEngine;
    private static DaoNamedEntities dao;
    private static Initiator initiator;

    public H2DaoNamedEntitiesTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        initiator = new Initiator(36);
        dataBase = new H2TestDataBase("testBase");
        ioEngine = mock(InnerIoEngine.class);
        dao = new H2DaoNamedEntities(dataBase, ioEngine);
        
        SqlDataBaseModel model = new H2DataBaseModel();
        SqlDataBaseInitializer initializer = new H2DataBaseInitializer(ioEngine, dataBase);
        DataBaseVerifier verifier = new H2DataBaseVerifier(initializer);
        List<String> reports = verifier.verify(dataBase, model); 
        reports.stream().forEach(report -> logger.info(report));
        
        dataBase.setupRequiredTable(
                "CREATE TABLE webpages (" +
                "page_name   VARCHAR(300)    NOT NULL PRIMARY KEY," +
                "page_path   VARCHAR(300)    NOT NULL)");
        
        setupTestData();
    }
    
    private static void setupTestData() {
        try (JdbcTransaction transact = dataBase.transactionFactory().createTransaction()) {
            
            transact
                    .doBatchUpdateVarargParams(
                            "INSERT INTO locations (loc_name, loc_path) " +
                            "VALUES ( ?, ? ) ",
                            params("books", "C:/my_doc/BOOKS"),
                            params("tomcat_deploy", "D:/tools/servers/web/tomcat/apps"),
                            params("java_projects", "D:/Tech/DEV/projects/java"),
                            params("js_projects", "D:/Tech/DEV/projects/js"));
            
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
                            "       bat_command_original, " +
                            "       bat_command_extended ) " +
                            "VALUES ( ?, ?, ?, ?, ? ) ",
                            params("workspace", RUN_PROGRAM.name(),     0, "netbeans", "netbeans"),
                            params("workspace", OPEN_LOCATION.name(),   1, "projects", "projects"),
                            params("workspace", SEE_WEBPAGE.name(),     2, "google", "google"),
                            params("tomcat", RUN_PROGRAM.name(),    0, "mysql_server", "mysql_server"),
                            params("tomcat", RUN_PROGRAM.name(),    1, "tomcat", "tomcat"),
                            params("tomcat", BATCH_PAUSE.name(),    2, "3 SECONDS", "3 SECONDS"),
                            params("tomcat", SEE_WEBPAGE.name(),    3, "tomcat_root", "tomcat_root"),
                            params("open_space", OPEN_PATH.name(),        0, "books/common", "books/common"),
                            params("open_space", OPEN_LOCATION.name(),    1, "projects", "projects"),
                            params("open_space", OPEN_PATH.name(),        2, "content/tech", "content/tech"),
                            params("open_space", OPEN_LOCATION.name(),    3, "dev", "dev"));
            
            if ( modified.length != 11 ) {
                throw new IllegalArgumentException();
            }
            
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            
        }        
    }

    /**
     * Test of getEntitiesByNamePattern method, of class H2DaoNamedEntities.
     */
    @Test
    public void testGetEntitiesByNamePattern() {
        List<NamedEntity> entities = dao.getEntitiesByNamePattern(initiator, "tomc");
        assertEquals(2, entities.size());
    }

    /**
     * Test of getEntitiesByNamePatternParts method, of class H2DaoNamedEntities.
     */
    @Test
    public void testGetEntitiesByNamePatternParts() {
        List<NamedEntity> entities = dao.getEntitiesByNamePatternParts(initiator, splitByWildcard("to-cat"));
        assertEquals(2, entities.size());
    }

}