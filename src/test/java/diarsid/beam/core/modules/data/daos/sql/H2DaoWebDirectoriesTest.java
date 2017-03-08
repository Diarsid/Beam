/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.data.daos.sql;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import testing.embedded.base.h2.H2TestDataBase;
import testing.embedded.base.h2.TestDataBase;

import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.domain.entities.WebDirectory;
import diarsid.beam.core.domain.entities.WebDirectoryPages;
import diarsid.beam.core.modules.data.DataBaseVerifier;
import diarsid.beam.core.modules.data.database.sql.H2DataBaseInitializer;
import diarsid.beam.core.modules.data.database.sql.H2DataBaseModel;
import diarsid.beam.core.modules.data.database.sql.H2DataBaseVerifier;
import diarsid.beam.core.modules.data.database.sql.SqlDataBaseInitializer;
import diarsid.beam.core.modules.data.database.sql.SqlDataBaseModel;
import diarsid.jdbc.transactions.JdbcTransaction;

import static java.lang.Integer.MIN_VALUE;
import static java.util.stream.Collectors.toList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import static diarsid.beam.core.base.util.CollectionsUtils.nonEmpty;
import static diarsid.beam.core.domain.entities.WebDirectories.restoreDirectory;
import static diarsid.beam.core.domain.entities.WebPlace.BOOKMARKS;
import static diarsid.beam.core.domain.entities.WebPlace.WEBPANEL;
import static diarsid.beam.core.domain.entities.WebPlace.parsePlace;
import static diarsid.jdbc.transactions.core.Params.params;

/**
 *
 * @author Diarsid
 */
public class H2DaoWebDirectoriesTest {
    
    private static final Logger logger = LoggerFactory.getLogger(H2DaoWebDirectoriesTest.class);
    
    private static TestDataBase dataBase;
    private static InnerIoEngine ioEngine;
    private static H2DaoWebDirectories dao;
    private static Initiator initiator;

    public H2DaoWebDirectoriesTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        initiator = new Initiator(12);
        dataBase = new H2TestDataBase("web_dirs_test");
        ioEngine = mock(InnerIoEngine.class);
        dao = new H2DaoWebDirectories(dataBase, ioEngine);
        
        SqlDataBaseModel model = new H2DataBaseModel();
        SqlDataBaseInitializer initializer = new H2DataBaseInitializer(ioEngine, dataBase);
        DataBaseVerifier verifier = new H2DataBaseVerifier(initializer);
        List<String> reports = verifier.verify(dataBase, model); 
        reports.stream().forEach(report -> logger.info(report));
        
        JdbcTransaction transact = dataBase.transactionFactory().createTransaction();
        
        transact
                .doBatchUpdateVarargParams(
                        "INSERT INTO web_directories (name, place, ordering) " +
                        "VALUES ( ?, ?, ? ) ", 
                        params("Common", WEBPANEL, 0), 
                        params("Dev.Java.Tools", WEBPANEL, 1),
                        params("Media", WEBPANEL, 2), 
                        params("Common (2)", WEBPANEL, 3), 
                        params("Dev.Java.Docs", BOOKMARKS, 0),
                        params("Dev.JS.Docs", BOOKMARKS, 1),
                        params("Dev.Tech", BOOKMARKS, 2));
        
        List<WebDirectory> savedDirs = transact
                .doQueryAndStream(
                        WebDirectory.class, 
                        "SELECT id, name, place, ordering FROM web_directories", 
                        (row) -> {
                            return restoreDirectory(
                                    (int) row.get("id"), 
                                    (String) row.get("name"), 
                                    parsePlace((String) row.get("place")), 
                                    (int) row.get("ordering"));
                        })
                .collect(toList());
        
        Map<String, WebDirectory> dirs = new HashMap<>();
        savedDirs.forEach(dir -> dirs.put(dir.name(), dir));
        
        assertEquals(7, dirs.size());
        
        transact.doBatchUpdateVarargParams(
                "INSERT INTO web_pages (name, shortcuts, url, ordering, dir_id) " +
                "VALUES ( ?, ?, ?, ?, ? ) ", 
                params("Google", "ggl", "http://...", 0, dirs.get("Common").id()),
                params("Google (2)", "ggl", "http://...", 1, dirs.get("Common").id()),
                params("Gmail", "ml", "http://...", 2, dirs.get("Common").id()),
                params("YouTube", "ytube yt", "http://...", 3, dirs.get("Common").id()),
                
                params("Facebook", "fb fbook", "http://...", 0, dirs.get("Media").id()),
                params("UkrPravda", "up", "http://...", 1, dirs.get("Media").id()),
                params("LinkedIn", "ln li", "http://...", 2, dirs.get("Media").id()),
                
                params("Java SE 8 API", "jse8 japi seapi", "http://...", 0, dirs.get("Dev.Java.Docs").id()),
                params("Java EE 7 API", "jee7 eeapi", "http://...", 1, dirs.get("Dev.Java.Docs").id()),
                params("Spring Core docs", "", "http://...", 2, dirs.get("Dev.Java.Docs").id()),
                params("Spring Data JPA docs", "", "http://...", 3, dirs.get("Dev.Java.Docs").id())
                );
        
        transact.commit();
    }

    /**
     * Test of freeNameNextIndex method, of class H2DaoWebDirectories.
     */
    @Test
    public void testFreeNameNextIndex_0() {
        Optional<Integer> nextFreeIndex = dao.freeNameNextIndex(initiator, "English", WEBPANEL);
        assertTrue(nextFreeIndex.isPresent());
        assertEquals(0, (int) nextFreeIndex.get());
    }
    
    @Test
    public void testFreeNameNextIndex_3() {
        Optional<Integer> nextFreeIndex = dao.freeNameNextIndex(initiator, "commoN", WEBPANEL);
        assertTrue(nextFreeIndex.isPresent());
        assertEquals(3, (int) nextFreeIndex.get());
    }

    /**
     * Test of getAllDirectoriesPages method, of class H2DaoWebDirectories.
     */
    @Test
    public void testGetAllDirectoriesPages() {
        List<WebDirectoryPages> dirs = dao.getAllDirectoriesPages(initiator);
        assertEquals(7, dirs.size());
        long nonEmptyQty = dirs.stream().filter(dir -> nonEmpty(dir.pages())).count();
        assertEquals(3, nonEmptyQty);
        WebDirectoryPages dir = dirs.stream().filter(dirPages -> dirPages.name().equals("Common")).findFirst().get();
        assertEquals(4, dir.pages().size());
    }

    /**
     * Test of getAllDirectoriesPagesInPlace method, of class H2DaoWebDirectories.
     */
    @Test
    public void testGetAllDirectoriesPagesInPlace() {
        List<WebDirectoryPages> dirs = dao.getAllDirectoriesPagesInPlace(initiator, WEBPANEL);
        assertEquals(4, dirs.size());
        long nonEmptyQty = dirs.stream().filter(dir -> nonEmpty(dir.pages())).count();
        assertEquals(2, nonEmptyQty);
        WebDirectoryPages dir = dirs.stream().filter(dirPages -> dirPages.name().equals("Media")).findFirst().get();
        assertEquals(3, dir.pages().size());
    }

    /**
     * Test of getDirectoryPagesByNameInPlace method, of class H2DaoWebDirectories.
     */
    @Test
    public void testGetDirectoryPagesById_exists_notEmpty() {
        Optional<WebDirectory> optDir = dao.getDirectoryByNameAndPlace(initiator, "coMmOn", WEBPANEL);
        assertTrue(optDir.isPresent());
        Optional<WebDirectoryPages> optDirPages = dao.getDirectoryPagesById(initiator, optDir.get().id());
        assertTrue(optDirPages.isPresent());
        assertEquals(4, optDirPages.get().pages().size());
    }
    
    @Test
    public void testGetDirectoryPagesById_exists_empty() {
        Optional<WebDirectory> optDir = dao.getDirectoryByNameAndPlace(initiator, "dev.js.docs", BOOKMARKS);
        assertTrue(optDir.isPresent());
        Optional<WebDirectoryPages> optDirPages = dao.getDirectoryPagesById(initiator, optDir.get().id());
        assertTrue(optDirPages.isPresent());
        assertEquals(0, optDirPages.get().pages().size());
    }
    
    @Test
    public void testGetDirectoryPagesById_notexists() {
        Optional<WebDirectoryPages> optDirPages = dao.getDirectoryPagesById(initiator, MIN_VALUE);
        assertFalse(optDirPages.isPresent());
    }

    /**
     * Test of getDirectoryByNameInPlace method, of class H2DaoWebDirectories.
     */
    @Test
    public void testGetDirectoryByNameInPlace_present() {
        Optional<WebDirectory> optDir = dao.getDirectoryByNameAndPlace(initiator, "commOn", WEBPANEL);
        assertTrue(optDir.isPresent());
        assertEquals("Common", optDir.get().name());
    }
    
    @Test
    public void testGetDirectoryByNameInPlace_notPresent() {
        Optional<WebDirectory> optDir = dao.getDirectoryByNameAndPlace(initiator, "News", WEBPANEL);
        assertFalse(optDir.isPresent());
    }

    /**
     * Test of findDirectoriesByPatternInPlace method, of class H2DaoWebDirectories.
     */
    @Test
    public void testFindDirectoriesByPatternInAnyPlace_found() {
        List<WebDirectory> found = dao.findDirectoriesByPatternInAnyPlace(initiator, "dev");
        assertEquals(4, found.size());
    }
    
    @Test
    public void testFindDirectoriesByPatternInAnyPlace_notFound() {
        List<WebDirectory> found = dao.findDirectoriesByPatternInAnyPlace(initiator, "news");
        assertEquals(0, found.size());
    }
    
    @Test
    public void testFindDirectoriesByPatternInAnyPlace_wildcard_found() {
        List<WebDirectory> found = dao.findDirectoriesByPatternInAnyPlace(initiator, "dev-jav");
        assertEquals(2, found.size());
    }
    
    @Test
    public void testFindDirectoriesByPatternInAnyPlace_wildcard_notFound() {
        List<WebDirectory> found = dao.findDirectoriesByPatternInAnyPlace(initiator, "my-dir");
        assertEquals(0, found.size());
    }

    /**
     * Test of findDirectoriesByPatternInAnyPlace method, of class H2DaoWebDirectories.
     */
    @Test
    public void testFindDirectoriesByPatternInPlace_wildcard_found() {
        List<WebDirectory> found = dao.findDirectoriesByPatternInPlace(initiator, "j-dev", BOOKMARKS);
        assertEquals(2, found.size());
    }
    
    @Test
    public void testFindDirectoriesByPatternInPlace_wildcard_notFound() {
        List<WebDirectory> found = dao.findDirectoriesByPatternInPlace(initiator, "dev-js-reac", BOOKMARKS);
        assertTrue(found.isEmpty());
    }
    
    @Test
    public void testFindDirectoriesByPatternInPlace_found() {
        List<WebDirectory> found = dao.findDirectoriesByPatternInPlace(initiator, "java", BOOKMARKS);
        assertEquals(1, found.size());
    }
    
    @Test
    public void testFindDirectoriesByPatternInPlace_notFound() {
        List<WebDirectory> found = dao.findDirectoriesByPatternInPlace(initiator, "react", BOOKMARKS);
        assertTrue(found.isEmpty());
    }

    /**
     * Test of getAllDirectories method, of class H2DaoWebDirectories.
     */
    @Test
    public void testGetAllDirectories() {
        List<WebDirectory> found = dao.getAllDirectories(initiator);
        assertEquals(7, found.size());
    }

    /**
     * Test of getAllDirectoriesInPlace method, of class H2DaoWebDirectories.
     */
    @Test
    public void testGetAllDirectoriesInPlace() {
        List<WebDirectory> found = dao.getAllDirectoriesInPlace(initiator, WEBPANEL);
        assertEquals(4, found.size());
    }

    /**
     * Test of exists method, of class H2DaoWebDirectories.
     */
    @Test
    public void testExists() {
    }

    /**
     * Test of updateWebDirectoryOrders method, of class H2DaoWebDirectories.
     */
    @Test
    public void testUpdateWebDirectoryOrders() {
    }

    /**
     * Test of save method, of class H2DaoWebDirectories.
     */
    @Test
    public void testSave() {
    }

    /**
     * Test of remove method, of class H2DaoWebDirectories.
     */
    @Test
    public void testRemove() {
    }

    /**
     * Test of moveDirectoryToPlace method, of class H2DaoWebDirectories.
     */
    @Test
    public void testMoveDirectoryToPlace() {
    }

    /**
     * Test of editDirectoryName method, of class H2DaoWebDirectories.
     */
    @Test
    public void testEditDirectoryName() {
    }

}