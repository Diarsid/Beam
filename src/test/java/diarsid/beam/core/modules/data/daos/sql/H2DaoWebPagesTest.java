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

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import testing.embedded.base.h2.H2TestDataBase;
import testing.embedded.base.h2.TestDataBase;

import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.domain.entities.WebDirectory;
import diarsid.beam.core.domain.entities.WebPage;
import diarsid.beam.core.modules.data.DaoWebPages;
import diarsid.beam.core.modules.data.DataBaseVerifier;
import diarsid.beam.core.modules.data.database.sql.H2DataBaseInitializer;
import diarsid.beam.core.modules.data.database.sql.H2DataBaseModel;
import diarsid.beam.core.modules.data.database.sql.H2DataBaseVerifier;
import diarsid.beam.core.modules.data.database.sql.SqlDataBaseInitializer;
import diarsid.beam.core.modules.data.database.sql.SqlDataBaseModel;
import diarsid.jdbc.transactions.JdbcTransaction;

import static java.util.stream.Collectors.toList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import static diarsid.beam.core.domain.entities.Orderables.reorderAccordingToNewOrder;
import static diarsid.beam.core.domain.entities.WebDirectories.restoreDirectory;
import static diarsid.beam.core.domain.entities.WebPages.newWebPage;
import static diarsid.beam.core.domain.entities.WebPlace.BOOKMARKS;
import static diarsid.beam.core.domain.entities.WebPlace.WEBPANEL;
import static diarsid.beam.core.domain.entities.WebPlace.parsePlace;
import static diarsid.jdbc.transactions.core.Params.params;


/**
 *
 * @author Diarsid
 */
public class H2DaoWebPagesTest {
    
    private static final Logger logger = LoggerFactory.getLogger(H2DaoWebPagesTest.class);
    
    private static TestDataBase dataBase;
    private static InnerIoEngine ioEngine;
    private static DaoWebPages dao;
    private static Initiator initiator;
    
    private Map<String, WebDirectory> dirs;
    
    public H2DaoWebPagesTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        initiator = new Initiator(12);
        dataBase = new H2TestDataBase("web_pages_test");
        ioEngine = mock(InnerIoEngine.class);
        dao = new H2DaoWebPages(dataBase, ioEngine);
        
        SqlDataBaseModel model = new H2DataBaseModel();
        SqlDataBaseInitializer initializer = new H2DataBaseInitializer(ioEngine, dataBase);
        DataBaseVerifier verifier = new H2DataBaseVerifier(initializer);
        List<String> reports = verifier.verify(dataBase, model); 
        reports.stream().forEach(report -> logger.info(report));
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() throws Exception {
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
        
        dirs = new HashMap<>();
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
    
    @After
    public void tearDown() throws Exception {
        JdbcTransaction transact = dataBase.transactionFactory().createTransaction();
        dirs.clear();
        transact.doUpdate("DELETE FROM web_pages");
        transact.doUpdate("DELETE FROM web_directories");
        transact.commit();
    }

    @Test
    public void testFreeNameNextIndex() {
        Optional<Integer> index = dao.findFreeNameNextIndex(initiator, "facebook");
        assertTrue(index.isPresent());
        assertEquals(2, (int) index.get());
    }

    @Test
    public void testGetByExactName() {
        Optional<WebPage> page = dao.getByExactName(initiator, "faceBooK");
        assertTrue(page.isPresent());
        
        Optional<WebPage> pageNotExist = dao.getByExactName(initiator, "sdsfdssdfsd");
        assertFalse(pageNotExist.isPresent());
    }

    @Test
    public void testFindByPattern() {
        List<WebPage> found1 = dao.findByPattern(initiator, "gogl");
        assertEquals(2, found1.size());
        
        List<WebPage> found2 = dao.findByPattern(initiator, "sprigdosc");
        assertEquals(2, found2.size());
        
        List<WebPage> found3 = dao.findByPattern(initiator, "Reacttutoril");
        assertEquals(0, found3.size());
    }

    @Test
    public void testAllFromDirectory() {
        List<WebPage> all = dao.allFromDirectory(initiator, dirs.get("Common").id());
        assertEquals(4, all.size());
    }

    @Test
    public void testSave() {
        int qtyBefore = dataBase.countRowsInTable("web_pages");
        WebPage page = newWebPage("Any", "", "http://fake", dirs.get("Common").id());
        boolean saved = dao.save(initiator, page);
        assertTrue(saved);
        int qtyAfter = dataBase.countRowsInTable("web_pages");
        assertEquals(qtyBefore + 1, qtyAfter);
        
        List<WebPage> all = dao.allFromDirectory(initiator, dirs.get("Common").id());
        int index = 0;
        for (WebPage wpage : all) {
            assertEquals(index, wpage.order());
            index++;
        }
        
        assertEquals(page.name(), all.get(all.size() - 1).name());
    }

    @Test
    public void testRemove() {
        int qtyBefore = dataBase.countRowsInTable("web_pages");
        boolean removed = dao.remove(initiator, "gMaiL");
        assertTrue(removed);
        int qtyAfter = dataBase.countRowsInTable("web_pages");
        assertEquals(qtyBefore - 1, qtyAfter);
        
        List<WebPage> all = dao.allFromDirectory(initiator, dirs.get("Common").id());
        int index = 0;
        for (WebPage wpage : all) {
            assertEquals(index, wpage.order());
            index++;
        }
        
        Optional<WebPage> removedPage = all
                .stream()
                .filter(page -> page.name().equalsIgnoreCase("GMaIl"))
                .findFirst();
        assertFalse(removedPage.isPresent());
    }

    @Test
    public void testEditName() {
        String oldName = "Google";
        Optional<WebPage> oldPage = dao.getByExactName(initiator, oldName);
        assertTrue(oldPage.isPresent());
        
        String newName = "any new name";
        Optional<WebPage> newPage = dao.getByExactName(initiator, newName);
        assertFalse(newPage.isPresent());
        
        boolean renamed = dao.editName(initiator, "Google", newName);
        assertTrue(renamed);
        
        Optional<WebPage> oldPageAfter = dao.getByExactName(initiator, oldName);
        assertFalse(oldPageAfter.isPresent());
        
        Optional<WebPage> newPageAfter = dao.getByExactName(initiator, newName);
        assertTrue(newPageAfter.isPresent());
    }

    @Test
    public void testEditShortcuts() {
        String newShortcuts = "any new alias";
        boolean changed = dao.editShortcuts(initiator, "google", newShortcuts);
        assertTrue(changed);
        
        Optional<WebPage> pageAfter = dao.getByExactName(initiator, "Google");
        assertEquals(newShortcuts, pageAfter.get().shortcuts());
    }

    @Test
    public void testEditUrl() {
        String newUrl = "http://any/new/url";
        boolean changed = dao.editUrl(initiator, "google", newUrl);
        assertTrue(changed);
        
        Optional<WebPage> pageAfter = dao.getByExactName(initiator, "Google");
        assertEquals(newUrl, pageAfter.get().url());
    }

    @Test
    public void testMovePageFromDirToDir() {
        int oldDirSizeBefore = dao.allFromDirectory(initiator, dirs.get("Common").id()).size();
        int newDirSizeBefore = dao.allFromDirectory(initiator, dirs.get("Media").id()).size();
        
        Optional<WebPage> movedPageBefore = dao.getByExactName(initiator, "Gmail");
        assertEquals(movedPageBefore.get().directoryId(), dirs.get("Common").id());
        boolean moved = dao.movePageFromDirToDir(initiator, movedPageBefore.get(), dirs.get("Media").id());
        assertTrue(moved);
        
        int oldDirSizeAfter = dao.allFromDirectory(initiator, dirs.get("Common").id()).size();
        int newDirSizeAfter = dao.allFromDirectory(initiator, dirs.get("Media").id()).size();
        assertEquals(oldDirSizeBefore - 1, oldDirSizeAfter);
        assertEquals(newDirSizeBefore + 1, newDirSizeAfter);
        
        Optional<WebPage> movedPageAfter = dao.getByExactName(initiator, "Gmail");
        assertEquals(movedPageAfter.get().directoryId(), dirs.get("Media").id());
        
        List<WebPage> allFromOld = dao.allFromDirectory(initiator, dirs.get("Common").id());
        int indexOldDir = 0;
        for (WebPage wpage : allFromOld) {
            assertEquals(indexOldDir, wpage.order());
            indexOldDir++;
        }
        
        List<WebPage> allFromNew = dao.allFromDirectory(initiator, dirs.get("Media").id());
        int indexNewDir = 0;
        for (WebPage wpage : allFromNew) {
            assertEquals(indexNewDir, wpage.order());
            indexNewDir++;
        }
    }

    @Test
    public void testUpdatePageOrdersInDir() {
        List<WebPage> pagesBefore = dao.allFromDirectory(initiator, dirs.get("Common").id());
        int oldOrder = 0;
        int newOrder = 2;
        WebPage movedPage = pagesBefore.get(oldOrder);
        reorderAccordingToNewOrder(pagesBefore, oldOrder, newOrder);
        boolean reordered = dao.updatePageOrdersInDir(initiator, pagesBefore);
        assertTrue(reordered);
        
        List<WebPage> pagesAfter = dao.allFromDirectory(initiator, dirs.get("Common").id());
        int index = 0;
        for (WebPage wpage : pagesAfter) {
            assertEquals(index, wpage.order());            
            if ( wpage.name().equals(movedPage.name()) ) {
                assertEquals(newOrder, wpage.order());
            }
            index++;
        }
    }    
}
