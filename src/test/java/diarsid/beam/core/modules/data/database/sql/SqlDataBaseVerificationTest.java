/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.data.database.sql;


import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import testing.embedded.base.h2.H2TestDataBase;
import testing.embedded.base.h2.TestDataBase;

import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.data.DataBaseActuator;
import diarsid.beam.core.base.data.DataBaseModel;
import diarsid.beam.core.base.data.SqlDataBaseModel;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import static diarsid.beam.core.base.data.DataBaseActuator.getActuatorFor;

/**
 *
 * @author Diarsid
 */
public class SqlDataBaseVerificationTest {
    
    private static final Logger logger = LoggerFactory.getLogger(SqlDataBaseVerificationTest.class);
    
    private static TestDataBase dataBase;
    private static InnerIoEngine ioEngine;    
    
    public SqlDataBaseVerificationTest() {
    }
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        dataBase = new H2TestDataBase("data_verification");
        ioEngine = mock(InnerIoEngine.class);
        
        DataBaseModel dataBaseModel = new H2DataBaseModel();
        
        DataBaseActuator actuator = getActuatorFor(dataBase, dataBaseModel);
        
        List<String> reports = actuator.actuateAndGetReport();
        reports.stream().forEach(report -> logger.info(report));
        assertEquals(reports.size(), ((SqlDataBaseModel) dataBaseModel).tables().size());
    }
    
    @AfterClass
    public static void clean() {
        dataBase.disconnect();
    }
    
    @Test
    public void queryLocations() {
        int zero = dataBase.countRowsInTable("locations");
        assertEquals(zero, 0);
    }
    
    @Test
    public void queryBatches() {
        int zero = dataBase.countRowsInTable("batches");
        assertEquals(zero, 0);
    }
}
