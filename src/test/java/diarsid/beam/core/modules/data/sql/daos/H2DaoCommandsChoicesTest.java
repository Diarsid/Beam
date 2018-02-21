/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.data.sql.daos;

import java.util.List;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import testing.embedded.base.h2.H2TestDataBase;
import testing.embedded.base.h2.TestDataBase;

import diarsid.beam.core.base.analyze.variantsweight.WeightedVariants;
import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.control.io.commands.CommandType;
import diarsid.beam.core.base.control.io.commands.executor.InvocationCommand;
import diarsid.beam.core.base.data.DataBaseActuator;
import diarsid.beam.core.base.data.DataBaseModel;
import diarsid.beam.core.base.data.SqlDataBaseModel;
import diarsid.beam.core.modules.data.DaoCommandsChoices;
import diarsid.beam.core.modules.data.sql.database.H2DataBaseModel;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import static diarsid.beam.core.base.analyze.variantsweight.Analyze.weightVariants;
import static diarsid.beam.core.base.control.io.base.actors.OuterIoEngineType.IN_MACHINE;
import static diarsid.beam.core.base.control.io.base.interaction.Variants.stringsToVariants;
import static diarsid.beam.core.base.control.io.commands.CommandType.BROWSE_WEBPAGE;
import static diarsid.beam.core.base.control.io.commands.CommandType.OPEN_LOCATION_TARGET;
import static diarsid.beam.core.base.control.io.commands.Commands.createInvocationCommandFrom;
import static diarsid.beam.core.base.data.DataBaseActuator.getActuatorFor;
import static diarsid.beam.core.base.util.CollectionsUtils.arrayListOf;
import static diarsid.jdbc.transactions.core.Params.params;


/**
 *
 * @author Diarsid
 */
public class H2DaoCommandsChoicesTest {
    
    private static final Logger logger = LoggerFactory.getLogger(H2DaoCommandsChoicesTest.class);
    
    static DaoCommandsChoices dao;
    static Initiator initiator;
    static TestDataBase dataBase;
    static InnerIoEngine ioEngine;
    
    WeightedVariants variants;
    
    public H2DaoCommandsChoicesTest() {
    }
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        initiator = new Initiator(41, IN_MACHINE);
        dataBase = new H2TestDataBase("commands-choices-test");
        ioEngine = mock(InnerIoEngine.class);
        
        dao = new H2DaoCommandsChoices(dataBase, ioEngine);
        DataBaseModel dataBaseModel = new H2DataBaseModel();
        
        DataBaseActuator actuator = getActuatorFor(dataBase, dataBaseModel);
        
        List<String> reports = actuator.actuateAndGetReport();
        reports.stream().forEach(report -> logger.info(report));
        assertEquals(reports.size(), ((SqlDataBaseModel) dataBaseModel).objects().size());
    }
    
    @Before 
    public void setUpCase() throws Exception {
        dataBase.transactionFactory()
                .createDisposableTransaction()
                .doBatchUpdateVarargParams(
                        "INSERT INTO commands_choices ( com_original, com_type, com_variants_stamp ) " +
                        "VALUES ( ?, ?, ? )", 
                        params("beaproj", "OPEN_LOCATION_TARGET", "c:/projects/netbeans/beam;c:/projects/netbeans"),
                        params("beaporj", "OPEN_LOCATION_TARGET", "c:/projects/netbeans/beam;c:/projects/netbeans"),
                        params("fb", "BROWSE_WEBPAGE", "c:/books/library/common/author/book.fb2;facebook"));
        
        String pattern = "beaproj";
        List<String> variantsStrings = arrayListOf("C:/Projects/NetBeans", "C:/Projects/NetBeans/Beam");
        variants = weightVariants(pattern, stringsToVariants(variantsStrings));
    }
    
    @After
    public void teatDownCase() throws Exception {
        dataBase.transactionFactory()
                .createDisposableTransaction()
                .doUpdate("DELETE FROM commands_choices");
    }

    @Test
    public void testIsChoiceDoneFor() {
        
        boolean isDone;
        
        isDone = dao.isChoiceDoneFor("BeaProj", variants);        
        assertEquals(true, isDone);
        
        isDone = dao.isChoiceDoneFor("BeaPorj", variants);        
        assertEquals(true, isDone);
        
        isDone = dao.isChoiceDoneFor("nebeaproj", variants);        
        assertEquals(false, isDone);
    }
    
    @Test
    public void testIsTypeChoiceDoneFor() {
        String pattern = "fb";
        List<String> variantsStrings = arrayListOf("c:/books/library/common/author/book.fb2", "facebook");
        WeightedVariants fbVariants = weightVariants(pattern, stringsToVariants(variantsStrings));
        
        Optional<CommandType> type = dao.isTypeChoiceDoneFor("Fb", fbVariants);
        assertEquals(true, type.isPresent());
        assertEquals(BROWSE_WEBPAGE, type.get());
    }
    
    @Test
    public void testIsChoiceDoneForNegative() {
        String pattern = "beaproj";
        List<String> variantsStrings = arrayListOf(
                "C:/Projects/NetBeans", 
                "C:/Projects/NetBeans/Beam", 
                "C:/Projects/NetBeans/Beam.server");
        WeightedVariants negativeVariants = weightVariants(pattern, stringsToVariants(variantsStrings));
        
        boolean isDone;
        
        isDone = dao.isChoiceDoneFor("BeaProj", negativeVariants);        
        assertEquals(false, isDone);
    }
    
    @Test
    public void testSaveRewrite() {
        int countBefore = dataBase.countRowsInTable("commands_choices");
        
        InvocationCommand command = createInvocationCommandFrom(
                OPEN_LOCATION_TARGET, "beaproj", "C:/Projects/NetBeans/Beam");
        boolean rewrited = dao.save(command, variants);
        assertEquals(true, rewrited);
        
        int countAfter = dataBase.countRowsInTable("commands_choices");
        assertEquals(countBefore, countAfter);
    }
    
    @Test
    public void testSave() {
        int countBefore = dataBase.countRowsInTable("commands_choices");
        
        InvocationCommand command = createInvocationCommandFrom(
                OPEN_LOCATION_TARGET, "nebeaproj", "C:/Projects/NetBeans");
        boolean saved = dao.save(command, variants);
        assertEquals(true, saved);
        
        int countAfter = dataBase.countRowsInTable("commands_choices");
        assertEquals(countBefore + 1, countAfter);
    }
    
    @Test
    public void testDelete() {
        int countBefore = dataBase.countRowsInTable("commands_choices");
        
        boolean deleted = dao.delete("beaproj");
        assertEquals(true, deleted);
        
        int countAfter = dataBase.countRowsInTable("commands_choices");
        assertEquals(countBefore - 1, countAfter);
    }
    
}
