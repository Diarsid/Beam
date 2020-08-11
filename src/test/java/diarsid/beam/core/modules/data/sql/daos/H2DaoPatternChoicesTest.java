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

import diarsid.beam.core.base.analyze.variantsweight.Variants;
import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.control.io.commands.executor.InvocationCommand;
import diarsid.beam.core.base.data.DataBaseActuator;
import diarsid.beam.core.base.data.DataBaseModel;
import diarsid.beam.core.base.data.SqlDataBaseModel;
import diarsid.beam.core.modules.data.DaoPatternChoices;
import diarsid.beam.core.modules.data.sql.database.H2DataBaseModel;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import static diarsid.beam.core.base.analyze.variantsweight.WeightAnalyzeReal.weightVariants;
import static diarsid.beam.core.base.control.io.base.actors.OuterIoEngineType.IN_MACHINE;
import static diarsid.beam.core.base.control.io.base.interaction.VariantConversions.stringsToVariants;
import static diarsid.beam.core.base.control.io.commands.CommandType.OPEN_LOCATION_TARGET;
import static diarsid.beam.core.base.control.io.commands.Commands.createInvocationCommandFrom;
import static diarsid.beam.core.base.data.DataBaseActuator.getActuatorFor;
import static diarsid.beam.core.base.util.CollectionsUtils.arrayListOf;
import static diarsid.jdbc.transactions.core.Params.params;


/**
 *
 * @author Diarsid
 */
public class H2DaoPatternChoicesTest {
    
    private static final Logger logger = LoggerFactory.getLogger(H2DaoPatternChoicesTest.class);
    
    static DaoPatternChoices dao;
    static Initiator initiator;
    static TestDataBase dataBase;
    static InnerIoEngine ioEngine;
    
    Variants variants;
    
    public H2DaoPatternChoicesTest() {
    }
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        initiator = new Initiator(41, IN_MACHINE);
        dataBase = new H2TestDataBase();
        ioEngine = mock(InnerIoEngine.class);
        
        dao = new H2DaoPatternChoices(dataBase, ioEngine);
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
                        "INSERT INTO pattern_choices ( original, extended, variants_stamp ) " +
                        "VALUES ( ?, ?, ? )", 
                        params("beaproj", "c:/projects/netbeans/beam", "c:/projects/netbeans/beam;c:/projects/netbeans"),
                        params("beaporj", "c:/projects/netbeans/beam", "c:/projects/netbeans/beam;c:/projects/netbeans"),
                        params("fb", "facebook", "c:/books/library/common/author/book.fb2;facebook"));
        
        String pattern = "beaproj";
        List<String> variantsStrings = arrayListOf("C:/Projects/NetBeans", "C:/Projects/NetBeans/Beam");
        variants = weightVariants(pattern, stringsToVariants(variantsStrings));
    }
    
    @After
    public void tearDownCase() throws Exception {
        dataBase.transactionFactory()
                .createDisposableTransaction()
                .doUpdate("DELETE FROM pattern_choices");
    }

    @Test
    public void testIsChoiceDoneFor() {
        
        boolean isDone;
        
        isDone = dao.hasMatchOf("BeaProj", "c:/projects/netbeans/beam", variants);        
        assertEquals(true, isDone);
        
        isDone = dao.hasMatchOf("BeaPorj", "c:/projects/netbeans/beam", variants);        
        assertEquals(true, isDone);
        
        isDone = dao.hasMatchOf("nebeaproj", "c:/projects/netbeans", variants);        
        assertEquals(false, isDone);
        
        isDone = dao.hasMatchOf("beaproj", "c:/projects/netbeans", variants);        
        assertEquals(false, isDone);
    }
    
    @Test
    public void testIsTypeChoiceDoneFor() {
        String pattern = "fb";
        List<String> variantsStrings = arrayListOf("c:/books/library/common/author/book.fb2", "facebook");
        Variants fbVariants = weightVariants(pattern, stringsToVariants(variantsStrings));
        
        Optional<String> choice = dao.findChoiceFor("Fb", fbVariants);
        assertEquals(true, choice.isPresent());
        assertEquals("facebook", choice.get());
    }
    
    @Test
    public void testIsChoiceDoneForNegative() {
        String pattern = "beaproj";
        List<String> variantsStrings = arrayListOf(
                "C:/Projects/NetBeans", 
                "C:/Projects/NetBeans/Beam", 
                "C:/Projects/NetBeans/Beam.server");
        Variants negativeVariants = weightVariants(pattern, stringsToVariants(variantsStrings));
        
        boolean isDone;
        
        isDone = dao.hasMatchOf("BeaProj", "C:/Projects/NetBeans/Beam", negativeVariants);        
        assertEquals(false, isDone);
    }
    
    @Test
    public void testSaveRewrite() {
        int countBefore = dataBase.countRowsInTable("pattern_choices");
        
        InvocationCommand command = createInvocationCommandFrom(
                OPEN_LOCATION_TARGET, "beaproj", "C:/Projects/NetBeans/Beam");
        boolean rewrited = dao.save(command, variants);
        assertEquals(true, rewrited);
        
        int countAfter = dataBase.countRowsInTable("pattern_choices");
        assertEquals(countBefore, countAfter);
    }
    
    @Test
    public void testSave() {
        int countBefore = dataBase.countRowsInTable("pattern_choices");
        
        InvocationCommand command = createInvocationCommandFrom(
                OPEN_LOCATION_TARGET, "nebeaproj", "C:/Projects/NetBeans");
        boolean saved = dao.save(command, variants);
        assertEquals(true, saved);
        
        int countAfter = dataBase.countRowsInTable("pattern_choices");
        assertEquals(countBefore + 1, countAfter);
    }
    
    @Test
    public void testDelete() {
        int countBefore = dataBase.countRowsInTable("pattern_choices");
        
        boolean deleted = dao.delete("beaproj");
        assertEquals(true, deleted);
        
        int countAfter = dataBase.countRowsInTable("pattern_choices");
        assertEquals(countBefore - 1, countAfter);
    }
    
}
