/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.data.sql.daos;

import diarsid.beam.core.modules.data.sql.daos.H2DaoKeyValueStorage;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import testing.embedded.base.h2.H2TestDataBase;
import testing.embedded.base.h2.TestDataBase;

import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.data.DataBaseActuator;
import diarsid.beam.core.base.data.DataBaseModel;
import diarsid.beam.core.base.data.SqlDataBaseModel;
import diarsid.beam.core.domain.entities.Attribute;
import diarsid.beam.core.modules.data.DaoKeyValueStorage;
import diarsid.beam.core.modules.data.sql.database.H2DataBaseModel;
import diarsid.jdbc.transactions.exceptions.TransactionHandledException;
import diarsid.jdbc.transactions.exceptions.TransactionHandledSQLException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import static diarsid.beam.core.base.data.DataBaseActuator.getActuatorFor;
import static diarsid.beam.core.base.util.Logs.logError;
import static diarsid.jdbc.transactions.core.Params.params;

/**
 *
 * @author Diarsid
 */
public class H2DaoKeyValueStorageTest {
    
    private static final Logger logger = LoggerFactory.getLogger(H2DaoKeyValueStorageTest.class);
    
    private static TestDataBase dataBase;
    private static InnerIoEngine ioEngine;
    private static DaoKeyValueStorage dao;
    private static Initiator initiator;

    public H2DaoKeyValueStorageTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        initiator = new Initiator(87);
        dataBase = new H2TestDataBase("key-value-test");
        
        ioEngine = mock(InnerIoEngine.class);
        dao = new H2DaoKeyValueStorage(dataBase, ioEngine);
        
        DataBaseModel dataBaseModel = new H2DataBaseModel();
        
        DataBaseActuator actuator = getActuatorFor(dataBase, dataBaseModel);
        
        List<String> reports = actuator.actuateAndGetReport();
        reports.stream().forEach(report -> logger.info(report));
        assertEquals(reports.size(), ((SqlDataBaseModel) dataBaseModel).objects().size());
    }
    
    @Before
    public void setupTestData() {
        try {
            dataBase.transactionFactory()
                    .createDisposableTransaction()
                    .doBatchUpdateVarargParams(
                            "INSERT INTO key_value (key, value) " +
                            "VALUES ( ?, ? ) ",
                            params("name", "beam"),
                            params("password", "1234"),
                            params("counter", "123"),
                            params("own_address", "127.0.0.1"),
                            params("user_name", "John Doe"));
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            logError(H2DaoKeyValueStorageTest.class, ex);
        }
    }
    
    @After
    public void clearTestData() {
        try {
            dataBase.transactionFactory()
                    .createDisposableTransaction()
                    .doUpdate(
                            "DELETE FROM key_value");
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            logError(H2DaoKeyValueStorageTest.class, ex);
        }
    }

    /**
     * Test of list method, of class H2DaoKeyValueStorage.
     */
    @Test
    public void testGet() {
        Optional<String> value = dao.get("name");
        assertTrue(value.isPresent());
        assertEquals("beam", value.get());
    }

    /**
     * Test of save method, of class H2DaoKeyValueStorage.
     */
    @Test
    public void testSave() {
        boolean saved = dao.save("tag", "content");
        assertTrue(saved);
        
        boolean rewrited = dao.save("user", "New User");
        assertTrue(rewrited);
        
        Optional<String> value = dao.get("tAG");
        assertTrue(value.isPresent());
        assertEquals("content", value.get());
        
        Optional<String> newUser = dao.get("user");
        assertTrue(newUser.isPresent());
        assertEquals("New User", newUser.get());
    }

    /**
     * Test of delete method, of class H2DaoKeyValueStorage.
     */
    @Test
    public void testDelete() {
        boolean removed = dao.delete("name");
        assertTrue(removed);
        
        boolean notRemoved = dao.delete("name");
        assertFalse(notRemoved);
        
        Optional<String> value = dao.get("name");
        assertFalse(value.isPresent());
    }

    /**
     * Test of getAll method, of class H2DaoKeyValueStorage.
     */
    @Test
    public void testGetAll() {
        Map<String, String> allPairs = dao.getAll();
        assertEquals(5, allPairs.size());
    }

    /**
     * Test of getAttribute method, of class H2DaoKeyValueStorage.
     */
    @Test
    public void testGetAttribute() {
        Optional<Attribute> attribute = dao.getAttribute("name");
        assertTrue(attribute.isPresent());
        assertEquals("beam", attribute.get().content());
    }

    /**
     * Test of saveAttribute method, of class H2DaoKeyValueStorage.
     */
    @Test
    public void testSaveAttribute() {
        Attribute attribute = new Attribute("key", "value");
        boolean saved = dao.saveAttribute(attribute);
        assertTrue(saved);
    }

    /**
     * Test of deleteAttribute method, of class H2DaoKeyValueStorage.
     */
    @Test
    public void testDeleteAttribute() {
        Attribute attribute = new Attribute("name", "beam");
        boolean removed = dao.deleteAttribute(attribute);
        assertTrue(removed);
    }

    /**
     * Test of getAllAttributes method, of class H2DaoKeyValueStorage.
     */
    @Test
    public void testGetAllAttributes() {
        Set<Attribute> attributes = dao.getAllAttributes();
        assertEquals(5, attributes.size());
    }

}