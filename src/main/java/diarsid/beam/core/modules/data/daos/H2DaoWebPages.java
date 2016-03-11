/*
 * project: Beam
 * author: Diarsid
 */

package diarsid.beam.core.modules.data.daos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import diarsid.beam.core.entities.WebPage;
import diarsid.beam.core.entities.WebPageDirectory;
import diarsid.beam.core.entities.WebPagePlacement;
import diarsid.beam.core.modules.IoInnerModule;
import diarsid.beam.core.modules.data.DaoWebPages;
import diarsid.beam.core.modules.data.DataBase;
import diarsid.beam.core.modules.data.HandledTransactSQLException;
import diarsid.beam.core.modules.data.JdbcTransaction;

/**
 *
 * @author Diarsid
 */
class H2DaoWebPages implements DaoWebPages {
    
    private final DataBase data;
    private final IoInnerModule ioEngine;

    H2DaoWebPages(IoInnerModule io, DataBase data) {
        this.data = data;
        this.ioEngine = io;
    }
    
    /* 
     * SQL Table illustration for webPages entities.
     *  +-----------------------------------------------------------------------------------------------------------------------------------------+
     *  | web_pages                                                                                                                               |
     *  +----------+------------+-----------------+------------------------------+----------------+------------------+------------+---------------+
     *  | page_id  | page_name  | page_shortcuts  | page_url                     | page_placement | page_directory   | page_order | page_browser  |
     *  +----------+------------+-----------------+------------------------------+----------------+------------------+------------+---------------+
     *  | 123      | facebook   | fb fboo faceb   |https://www.facebook.com...   | webpanel       | common           |     1      | default       |
     *  +----------+------------+-----------------+------------------------------+----------------+------------------+------------+---------------+
     *  | 451      | wiki       |                 |https://en.wikipedia.org      | webpanel       | common           |     2      | firefox       |
     *  +----------+------------+-----------------+------------------------------+----------------+------------------+------------+---------------+
     *  | 324      | java_blog  | bl jbl          |https://some.java.blog...     | bookmarks      | java/blogs/misc  |     1      | chrome        |
     *  +----------+------------+-----------------+------------------------------+----------------+------------------+------------+---------------+ 
     */
        
    private final String SELECT_ALL_PAGES_JOIN_DIRS_WHERE_PLACEMENT = 
            "SELECT page_name, page_order, dir_order, page_shortcuts, page_url, page_placement, page_directory, page_browser " +
            "FROM web_pages p " +
            "INNER JOIN directories d ON "
            + "(p.page_directory = d.dir_name) "
            + "AND "
            + "(p.page_placement = d.dir_placement) " +
            "WHERE page_placement LIKE ? " +
            "ORDER BY dir_order, page_order";
    private final String SELECT_ALL_PAGES_JOIN_DIRS_WHERE_DIRECTORY_AND_PLACEMENT_LIKE = 
            "SELECT page_name, page_order, dir_order, page_shortcuts, page_url, page_placement, page_directory, page_browser " +
            "FROM web_pages p " +
            "INNER JOIN directories d ON "
            + "(p.page_directory = d.dir_name) "
            + "AND "
            + "(p.page_placement = d.dir_placement) " +
            "WHERE (page_directory LIKE ? ) AND (page_placement LIKE ? ) " +
            "ORDER BY dir_order, page_order";
    private final String SELECT_PAGES_JOIN_DIRS_WHERE_NAME_OR_SHORT_LIKE = 
            "SELECT page_name, page_order, dir_order, page_shortcuts, page_url, page_placement, page_directory, page_browser " +
            "FROM web_pages p " +
            "INNER JOIN directories d ON "
            + "(p.page_directory = d.dir_name) "
            + "AND "
            + "(p.page_placement = d.dir_placement) " +
            "WHERE (page_name LIKE ?) OR (page_shortcuts LIKE ?) " + 
            "ORDER BY dir_order, page_order ";
    private final String SELECT_PAGES_JOIN_DIRS_WHERE_PAGE_AND_DIR_AND_PLACE_IS = 
            "SELECT page_name, page_order, dir_order, page_shortcuts, page_url, page_placement, page_directory, page_browser " +
            "FROM web_pages p " +
            "INNER JOIN directories d ON "
            + "(p.page_directory = d.dir_name) "
            + "AND "
            + "(p.page_placement = d.dir_placement) " +
            "WHERE (page_name IS ? ) AND (page_directory IS ? ) AND (page_placement IS ? ) ";
    private final String SELECT_PAGES_JOIN_DIRS_WHERE = 
            "SELECT page_name, page_order, dir_order, page_shortcuts, page_url, page_placement, page_directory, page_browser " +
            "FROM web_pages p " +
            "INNER JOIN directories d ON "
            + "(p.page_directory = d.dir_name) "
            + "AND "
            + "(p.page_placement = d.dir_placement) " +
            "WHERE ";
    private final String NAME_LIKE_NAMEPART = 
            " page_name LIKE ? ";
    private final String AND = 
            " AND ";
    private final String ORDER_BY_DIR_AND_PAGE_ORDERS = 
            "ORDER BY dir_order, page_order ";
    private final String INSERT_NEW_PAGE = 
            "INSERT INTO web_pages (page_name, page_shortcuts, page_url, page_placement, page_directory, page_browser, page_order) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?)";           
    private final String SELECT_MAX_PAGE_ORDER_IN_PLACE_AND_DIR = 
            "SELECT MAX(page_order) " +
            "FROM web_pages " +
            "WHERE (page_placement IS ? ) AND (page_directory IS ?)";    
    private final String DELETE_PAGES_WHERE_NAME_DIR_PLACE_IS = 
            "DELETE FROM web_pages " +
            "WHERE ( page_name IS ? ) "
            + "AND ( page_directory IS ? ) "
            + "AND ( page_placement IS ? ) ";
    private final String SELECT_ALL_DIRECTORIES_WHERE_PLACEMENT = 
            "SELECT DISTINCT page_directory "+
            "FROM web_pages " +
            "WHERE page_placement LIKE ? " +
            "ORDER BY page_directory ";
    private final String UPDATE_PAGE_NAME = 
            "UPDATE web_pages " +
            "SET page_name = ? " +
            "WHERE page_name LIKE ?";
    private final String UPDATE_PAGE_SHORTCUTS = 
            "UPDATE web_pages " +
            "SET page_shortcuts = ? " +
            "WHERE page_name LIKE ? ";
    private final String UPDATE_PAGE_URL_WHERE_PAGE_NAME = 
            "UPDATE web_pages " +
            "SET page_url = ? " + 
            "WHERE page_name LIKE ?";;
    private final String UPDATE_PAGE_BROWSER_WHERE_PAGE_NAME = 
            "UPDATE web_pages " +
            "SET page_browser = ?" + 
            "WHERE page_name LIKE ?";
    private final String UPDATE_PAGE_DIRECTORY_WHERE_DIRECTORY_AND_PLACEMENT = 
            "UPDATE web_pages " +
            "SET page_directory = ?" + 
            "WHERE (page_directory LIKE ? ) AND (page_placement LIKE ? ) ";
    private final String UPDATE_PAGE_DIRECTORY_AND_PLACEMENT_AND_ORDER_WHERE_PAGE_NAME_IS = 
            "UPDATE web_pages " +
            "SET page_directory = ?, page_placement = ?, page_order = ? " +
            "WHERE page_name IS ? ";    
    private final String UPDATE_PAGE_ORDER_WHERE_PAGE_NAME_DIR_PLACE_IS = 
            "UPDATE web_pages " +
            "SET page_order = ? " +
            "WHERE ( page_name IS ? ) "
            + "AND ( page_directory IS ? ) "
            + "AND ( page_placement IS ? ) ";
    private final String DELETE_FROM_DIRS_WHERE_DIR_NAME_AND_PLACEMENT_IS = 
            "DELETE FROM directories " +
            "WHERE ( dir_name IS ? ) AND ( dir_placement is ? ) ";
    private final String DELETE_PAGES_WHERE_DIR_NAME_AND_PLACEMENT_IS = 
            "DELETE FROM web_pages " +
            "WHERE ( page_directory IS ? ) AND ( page_placement IS ? )";
    private final String INSERT_NEW_DIR = 
            "INSERT INTO directories (dir_name, dir_placement, dir_order) " +
            "VALUES (?, ?, ?)";
    private final String SELECT_COUNT_DIR_IN_DIRS_WHERE_NAME_AND_PLACE_IS = 
            "SELECT COUNT(dir_name) as count " +
            "FROM directories " +
            "WHERE ( dir_name IS ? ) AND ( dir_placement IS ? )";
    private final String SELECT_DIR_IN_DIRS_WHERE_NAME_AND_PLACE_IS = 
            "SELECT dir_name, dir_order, dir_placement " +
            "FROM directories " + 
            "WHERE ( dir_name IS ? ) AND (dir_placement IS ? ) ";
    private final String SELECT_ALL_DIRS_IN_DIRS_WHERE_PLACE_IS = 
            "SELECT dir_name, dir_order, dir_placement " +
            "FROM directories " + 
            "WHERE dir_placement IS ? ";
    private final String SELECT_MAX_DIR_ORDER_IN_PLACE = 
            "SELECT MAX(dir_order) " +
            "FROM directories " +
            "WHERE dir_placement IS ? ";
    private final String UPDATE_DIR_ORDER_WHERE_NAME_AND_PLACE_IS = 
            "UPDATE directories " +
            "SET dir_order = ? " +
            "WHERE ( dir_name IS ? ) AND ( dir_placement IS ? ) ";
    private final String UPDATE_DIR_NAME_WHERE_DIR_AND_PLACE_IS = 
            "UPDATE directories " +
            "SET dir_name = ? " +
            "WHERE ( dir_name IS ? ) AND ( dir_placement IS ? ) ";
            
    @Override
    public boolean saveWebPage(WebPage page) {
        JdbcTransaction transact = this.data.beginTransaction();
        try { 
            PreparedStatement dirExists = transact.getPreparedStatement(
                    SELECT_COUNT_DIR_IN_DIRS_WHERE_NAME_AND_PLACE_IS);
            dirExists.setString(1, page.getDirectory());
            dirExists.setString(2, page.getPlacement().name());
            ResultSet existResult = transact.executePreparedQuery(dirExists);
            existResult.first();  
            if (existResult.getInt(1) == 0) {
                
                PreparedStatement maxDirOrderStmnt = 
                        transact.getPreparedStatement(SELECT_MAX_DIR_ORDER_IN_PLACE);
                maxDirOrderStmnt.setString(1, page.getPlacement().name());
                ResultSet maxOrderResultSet = transact.executePreparedQuery(maxDirOrderStmnt);
                maxOrderResultSet.first();
                int maxDirOrder = maxOrderResultSet.getInt(1);
                
                PreparedStatement insertNewDir = transact.getPreparedStatement(
                        INSERT_NEW_DIR);
                insertNewDir.setString(1, page.getDirectory());
                insertNewDir.setString(2, page.getPlacement().name());
                insertNewDir.setInt(3, maxDirOrder+1);
                transact.executePreparedUpdate(insertNewDir);
            }
            
            PreparedStatement maxPageOrder = transact.getPreparedStatement(SELECT_MAX_PAGE_ORDER_IN_PLACE_AND_DIR);
            maxPageOrder.setString(1, page.getPlacement().name());
            maxPageOrder.setString(2, page.getDirectory());
            ResultSet maxPageOrderRs = transact.executePreparedQuery(maxPageOrder);
            maxPageOrderRs.first();
            int pageOrder = maxPageOrderRs.getInt(1);
            
            PreparedStatement insertPage = transact.getPreparedStatement(INSERT_NEW_PAGE);            
            insertPage.setString(1, page.getName());
            insertPage.setString(2, page.getShortcuts());
            insertPage.setString(3, page.getUrlAddress());
            insertPage.setString(4, page.getPlacement().name());
            insertPage.setString(5, page.getDirectory());
            insertPage.setString(6, page.getBrowser());
            insertPage.setInt(7, pageOrder+1);            
            int qty = transact.executePreparedUpdate(insertPage);
            
            transact.commitThemAll();
            return (qty > 0);
        } catch (HandledTransactSQLException e) {
            this.ioEngine.reportException(e, "SQLException: web page saving.");
        } catch (SQLException e) {
            transact.rollbackAllAndReleaseResources();
            this.ioEngine.reportException(e, "SQLException: web page saving.");
        } catch (NullPointerException e) {
            transact.rollbackAllAndReleaseResources();
            this.ioEngine.reportError(
                    "Incorrect usage of JdbcTransaction: " +
                    "database connection is NULL.");
        }
        return false;
    }
    
    @Override
    public boolean deleteWebPage(String name, String dir, WebPagePlacement place) {
        try (Connection con = data.connect();
            PreparedStatement ps = con.prepareStatement(DELETE_PAGES_WHERE_NAME_DIR_PLACE_IS);) {
            
            ps.setString(1, name);
            ps.setString(2, dir);
            ps.setString(3, place.name());
            int qty = ps.executeUpdate();

            return (qty > 0);
        } catch (SQLException e) {
            this.ioEngine.reportException(e, "SQLException: web page deleting.");
            return false;
        }
    }
    
    @Override
    public List<WebPage> getAllWebPagesInPlacement(WebPagePlacement placement) {
        ResultSet rs = null;
        try(Connection con = data.connect();
            PreparedStatement ps = con.prepareStatement(SELECT_ALL_PAGES_JOIN_DIRS_WHERE_PLACEMENT)) {
            
            ps.setString(1, placement.name());
            
            rs = ps.executeQuery();
            List<WebPage> pages = new ArrayList<>();
            while (rs.next()){
                pages.add(WebPage.restorePage(
                        rs.getString("page_name"),
                        rs.getString("page_shortcuts"), 
                        rs.getString("page_url"),
                        WebPagePlacement.valueOf(rs.getString("page_placement")),
                        rs.getString("page_directory"),
                        rs.getInt("page_order"),
                        rs.getInt("dir_order"),
                        rs.getString("page_browser")
                ));
            }
            
            return pages;
        } catch (SQLException e){
            this.ioEngine.reportException(e, "SQLException: get all web pages.");
            return Collections.emptyList();
        } finally {
            if (rs != null) {
                try{
                    rs.close();
                } catch (SQLException se) {
                    this.ioEngine.reportExceptionAndExitLater(se, 
                        "Unknown problem in WebPagesDao.getWebPagesByName:",
                        "ResultSet close exception.",
                        " ",
                        "Program will be closed.");
                }
            }
        }
    }    
        
    @Override
    public List<WebPage> getWebPagesByName(String name) {
        ResultSet rs = null;
        try (Connection con = data.connect();
            PreparedStatement ps = con.prepareStatement(SELECT_PAGES_JOIN_DIRS_WHERE_NAME_OR_SHORT_LIKE)) {
            
            ps.setString(1, "%"+name+"%");
            ps.setString(2, "%"+name+"%");
            
            rs = ps.executeQuery();
            List<WebPage> pages = new ArrayList<>();
            while (rs.next()) {
                pages.add(WebPage.restorePage(
                        rs.getString("page_name"),
                        rs.getString("page_shortcuts"), 
                        rs.getString("page_url"),
                        WebPagePlacement.valueOf(rs.getString("page_placement")),
                        rs.getString("page_directory"),                        
                        rs.getInt("page_order"),
                        rs.getInt("dir_order"),
                        rs.getString("page_browser")
                ));
            }
            return pages;
        } catch (SQLException e) {
            this.ioEngine.reportException(e, "SQLException: get web pages by name.");            
            return Collections.emptyList();
        } finally {
            if (rs != null) {
                try{
                    rs.close();
                } catch (SQLException se) {
                    this.ioEngine.reportExceptionAndExitLater(se, 
                        "Unknown problem in WebPagesDao.getWebPagesByName:",
                        "ResultSet close exception.",
                        " ",
                        "Program will be closed.");
                }
            }
        }
    }
    
    @Override
    public List<WebPage> getWebPagesByNameParts(String[] nameParts){
        int partsQty = nameParts.length;
        if (partsQty > 0){
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append(SELECT_PAGES_JOIN_DIRS_WHERE).append(NAME_LIKE_NAMEPART);
            for (int i = 1; i < partsQty; i++){
                queryBuilder.append(AND).append(NAME_LIKE_NAMEPART);
            }
            queryBuilder.append(ORDER_BY_DIR_AND_PAGE_ORDERS);
            ResultSet rs = null;
            try(Connection con = data.connect();
               PreparedStatement ps = con.prepareStatement(queryBuilder.toString())) {
                for (int j = 0; j < partsQty; j++){
                    ps.setString(j+1, "%"+nameParts[j]+"%");
                }
                rs = ps.executeQuery();
                List<WebPage> pages = new ArrayList<>();
                while (rs.next()) {
                    pages.add(WebPage.restorePage(
                            rs.getString("page_name"),
                            rs.getString("page_shortcuts"), 
                            rs.getString("page_url"),
                            WebPagePlacement.valueOf(rs.getString("page_placement")),
                            rs.getString("page_directory"),
                            rs.getInt("page_order"),
                            rs.getInt("dir_order"),
                            rs.getString("page_browser")
                    ));
                }
                return pages;
            } catch (SQLException e){
                this.ioEngine.reportException(e, "SQLException: get web page by name parts.");
                return Collections.emptyList();
            } finally {
                if (rs != null){
                    try{
                        rs.close();
                    } catch (SQLException se){
                        this.ioEngine.reportExceptionAndExitLater(se, 
                        "Unknown problem in WebPagesDao.getWebPagesByNameParts:",
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
    public List<WebPage> getWebPagesByNameInDirAndPlace(
            String name, String dir, WebPagePlacement place) {
        JdbcTransaction transact = this.data.beginTransaction();
        try {
            PreparedStatement getDir = transact.getPreparedStatement(
                    SELECT_PAGES_JOIN_DIRS_WHERE_PAGE_AND_DIR_AND_PLACE_IS);
            getDir.setString(1, name);
            getDir.setString(2, dir);
            getDir.setString(3, place.name());
            ResultSet dirRs = transact.executePreparedQuery(getDir);
            
            List<WebPage> pages = new ArrayList<>();
            while ( dirRs.next() ) {
                    pages.add(WebPage.restorePage(
                            dirRs.getString("page_name"),
                            dirRs.getString("page_shortcuts"), 
                            dirRs.getString("page_url"),
                            WebPagePlacement.valueOf(dirRs.getString("page_placement")),
                            dirRs.getString("page_directory"),
                            dirRs.getInt("page_order"),
                            dirRs.getInt("dir_order"),
                            dirRs.getString("page_browser")
                    ));
            }
            
            transact.commitThemAll();
            
            return pages;
            
        } catch (HandledTransactSQLException e) { 
            this.ioEngine.reportException(e, "SQLException: .");
        } catch (SQLException e) {
            transact.rollbackAllAndReleaseResources();
            this.ioEngine.reportException(e, "SQLException: .");
        } catch (NullPointerException e) {
            transact.rollbackAllAndReleaseResources();
            this.ioEngine.reportError(
                    "Incorrect usage of JdbcTransaction: " +
                    "database connection is NULL.");            
        }
        return Collections.emptyList();
    }
    
    @Override
    public List<WebPage> getAllWebPagesInDirectoryAndPlacement(
            String directory, WebPagePlacement placement) {
        ResultSet rs = null;
        try (Connection con = data.connect();
            PreparedStatement ps = con.prepareStatement(SELECT_ALL_PAGES_JOIN_DIRS_WHERE_DIRECTORY_AND_PLACEMENT_LIKE)) {
            
            if (placement.equals(WebPagePlacement.BOOKMARKS)) {
                // if BOOKMARKS than full name of directory 
                // must be specified: 
                // "java" -> "blogs/java" NOT "blogs/java/another/subdir"
                ps.setString(1, "%"+directory);
            } else {
                // if WEBPANEL than only part of directory
                // name is sufficient to select: 
                // "com" -> "common"
                ps.setString(1, "%"+directory+"%");
            }
            ps.setString(2, placement.name());
            
            rs = ps.executeQuery();
            List<WebPage> pages = new ArrayList<>();
            while (rs.next()) {
                pages.add(WebPage.restorePage(
                        rs.getString("page_name"),
                        rs.getString("page_shortcuts"), 
                        rs.getString("page_url"),
                        WebPagePlacement.valueOf(rs.getString("page_placement")),
                        rs.getString("page_directory"),
                        rs.getInt("page_order"),
                        rs.getInt("dir_order"),
                        rs.getString("page_browser")
                ));
            }
            return pages;
        } catch (SQLException e) {
          this.ioEngine.reportException(e, "SQLException: get web pages of category.");
            return Collections.emptyList();
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException se) {
                    this.ioEngine.reportExceptionAndExitLater(se, 
                        "Unknown problem in WebPagesDao.getAllWebPagesOfCategory:",
                        "ResultSet close exception.",
                        " ",
                        "Program will be closed.");
                }
            }
        }
    }
    
    @Override
    public List<String> getAllDirectoriesInPlacement(WebPagePlacement placement) {
        ResultSet rs = null;
        try(Connection con = data.connect();
            PreparedStatement ps = con.prepareStatement(SELECT_ALL_DIRECTORIES_WHERE_PLACEMENT);) {
            
            ps.setString(1, placement.name());
            rs = ps.executeQuery();
            List<String> directories = new ArrayList<>();
            while (rs.next()) {
                directories.add(rs.getString(1));
            }
            return directories;
        } catch (SQLException e) {
            this.ioEngine.reportException(e, "SQLException: get all web page categories.");
            return Collections.emptyList();
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException se) {
                    this.ioEngine.reportExceptionAndExitLater(se, 
                        "Unknown problem in WebPagesDao.getAllWebPagesOfCategory:",
                        "ResultSet close exception.",
                        " ",
                        "Program will be closed.");
                }
            }
        }
    }   
    
    @Override
    public boolean editWebPageName(String name, String newName) {
        try(Connection con = data.connect();
            PreparedStatement ps = con.prepareStatement(UPDATE_PAGE_NAME)) {
            
            ps.setString(1, newName);
            ps.setString(2, name);
            int qty = ps.executeUpdate();

            return (qty > 0);            
        } catch (SQLException e){
            this.ioEngine.reportException(e, "SQLException: update web page name.");
            return false;
        }
    }
    
    @Override
    public boolean editWebPageShortcuts(String name, String newShortcuts) {
        try(Connection con = data.connect();
            PreparedStatement ps = con.prepareStatement(UPDATE_PAGE_SHORTCUTS)) {
            
            ps.setString(1, newShortcuts);
            ps.setString(2, name);
            int qty = ps.executeUpdate();

            return (qty > 0);            
        } catch (SQLException e){
            this.ioEngine.reportException(e, "SQLException: update web page shortcuts.");
            return false;
        }
    }
    
    @Override
    public boolean editWebPageUrl(String name, String newUrl){
        try(Connection con = data.connect();
            PreparedStatement ps = con.prepareStatement(UPDATE_PAGE_URL_WHERE_PAGE_NAME)) {
            
            ps.setString(1, newUrl);
            ps.setString(2, name);
            int qty = ps.executeUpdate();

            return (qty > 0); 
        } catch (SQLException e){
            this.ioEngine.reportException(e, "SQLException: update web page URL.");
            return false;
        }
    }
    
    /*
    @Override
    public boolean editWebPageDirectory(String name, String newDirectory) {
        try(Connection con = data.connect();
            PreparedStatement ps = con.prepareStatement(UPDATE_PAGE_DIRECTORY_WHERE_PAGE_NAME)) {
            
            ps.setString(1, newDirectory);
            ps.setString(2, name);
            int qty = ps.executeUpdate();

            return (qty > 0); 
        } catch (SQLException e){
            this.ioEngine.reportException(e, "SQLException: update web page category.");
            return false;
        }
    }
    */
    
    @Override
    public boolean editWebPageBrowser(String name, String newBrowser){
        try(Connection con = data.connect();
            PreparedStatement ps = con.prepareStatement(UPDATE_PAGE_BROWSER_WHERE_PAGE_NAME)) {
            
            ps.setString(1, newBrowser);
            ps.setString(2, name);
            int qty = ps.executeUpdate();

            return (qty > 0); 
        } catch (SQLException e){
            this.ioEngine.reportException(e, "SQLException: update web page browser.");
            return false;
        }
    }
    
    @Override
    public boolean editDirectoryNameInPlacement(
            String directory, String newDirectory, WebPagePlacement placement) {
        try (Connection con = data.connect();
                PreparedStatement updateDirInPage = con.prepareStatement(
                    UPDATE_PAGE_DIRECTORY_WHERE_DIRECTORY_AND_PLACEMENT);
                PreparedStatement updateDirInDirs = con.prepareStatement(
                        UPDATE_DIR_NAME_WHERE_DIR_AND_PLACE_IS)) {
            
            updateDirInPage.setString(1, newDirectory);
            updateDirInPage.setString(2, directory);
            updateDirInPage.setString(3, placement.name());
            int qty = updateDirInPage.executeUpdate();
            
            updateDirInDirs.setString(1, newDirectory);
            updateDirInDirs.setString(2, directory);
            updateDirInDirs.setString(3, placement.name());
            qty = qty + updateDirInDirs.executeUpdate();
            
            return (qty > 0); 
        } catch (SQLException e){
            this.ioEngine.reportException(e, "SQLException: update web page browser.");
            return false;
        }
    }
    
    @Override
    public boolean editWebPageOrder(
            String name, String dir, WebPagePlacement place, int newOrder) {
        
        JdbcTransaction transact = this.data.beginTransaction();
        try {
            PreparedStatement ps = transact.getPreparedStatement(
                    SELECT_ALL_PAGES_JOIN_DIRS_WHERE_DIRECTORY_AND_PLACEMENT_LIKE);            
            ps.setString(1, dir);
            ps.setString(2, place.name());            
            ResultSet pagesRs = transact.executePreparedQuery(ps);
            List<WebPage> pages = new ArrayList<>();
            while (pagesRs.next()) {
                pages.add(WebPage.restorePage(
                        pagesRs.getString("page_name"),
                        pagesRs.getString("page_shortcuts"), 
                        pagesRs.getString("page_url"),
                        WebPagePlacement.valueOf(pagesRs.getString("page_placement")),
                        pagesRs.getString("page_directory"),
                        pagesRs.getInt("page_order"),
                        pagesRs.getInt("dir_order"),
                        pagesRs.getString("page_browser")
                ));
            }
            
            if (newOrder > pages.size()) {
                newOrder = pages.size();
            }
            WebPage movedPage = null;
            for (WebPage page : pages) {
                if (page.getName().equals(name)) {
                    movedPage = page;
                    break;
                }
            }
            if ( movedPage == null ) {
                return false;
            }
            int oldOrder = movedPage.getPageOrder();
            if (newOrder < oldOrder) {
                int oldOrderPageIndex = pages.indexOf(movedPage);
                for (int i = newOrder - 1; i < oldOrderPageIndex; i++) {
                    pages.get(i).setOrder(pages.get(i).getPageOrder() + 1);
                }                
                pages.get(oldOrderPageIndex).setOrder(newOrder);
            } else if (oldOrder < newOrder) {
                int oldOrderPageIndex = pages.indexOf(movedPage);
                for (int i = oldOrderPageIndex + 1; i < newOrder; i++) {
                    pages.get(i).setOrder(pages.get(i).getPageOrder() - 1);
                }                
                pages.get(oldOrderPageIndex).setOrder(newOrder);
            } else {
                return true;
            }
            Collections.sort(pages);
            for (int i = 0; i < pages.size(); i++) {
                pages.get(i).setOrder(i + 1);
            }
            
            PreparedStatement updatePageOrderSt;
            int qty = 0;
            for (WebPage updPage : pages) {
                updatePageOrderSt = transact.getPreparedStatement(
                    UPDATE_PAGE_ORDER_WHERE_PAGE_NAME_DIR_PLACE_IS);
                updatePageOrderSt.setInt(1, updPage.getPageOrder());
                updatePageOrderSt.setString(2, updPage.getName());
                updatePageOrderSt.setString(3, updPage.getDirectory());
                updatePageOrderSt.setString(4, updPage.getPlacement().name());
                qty = qty + transact.executePreparedUpdate(updatePageOrderSt);
            }
            
            transact.commitThemAll();
            return ( qty > 0 );
            
        } catch (HandledTransactSQLException e) { 
            this.ioEngine.reportException(e, "SQLException: .");
        } catch (SQLException e) {
            transact.rollbackAllAndReleaseResources();
            this.ioEngine.reportException(e, "SQLException: .");
        } catch (NullPointerException e) {
            transact.rollbackAllAndReleaseResources();
            this.ioEngine.reportError(
                    "Incorrect usage of JdbcTransaction: " +
                    "database connection is NULL.");            
        }
        return false;
    }
    
    @Override
    public boolean moveWebPageToPlacementAndDirectory(
            String pageName, String newDirectory, WebPagePlacement placement) {
        
        JdbcTransaction transact = this.data.beginTransaction();
        try {
            PreparedStatement dirExists = transact.getPreparedStatement(
                    SELECT_COUNT_DIR_IN_DIRS_WHERE_NAME_AND_PLACE_IS);
            dirExists.setString(1, newDirectory);
            dirExists.setString(2, placement.name());
            ResultSet existResult = transact.executePreparedQuery(dirExists);
            existResult.first();
            int exists = existResult.getInt(1);
            if (exists == 0) {
                PreparedStatement maxDirOrderStmnt = 
                        transact.getPreparedStatement(SELECT_MAX_DIR_ORDER_IN_PLACE);
                maxDirOrderStmnt.setString(1, placement.name());
                ResultSet maxOrderResultSet = transact.executePreparedQuery(maxDirOrderStmnt);
                maxOrderResultSet.first();
                int maxDirOrder = maxOrderResultSet.getInt(1);
                
                PreparedStatement insertNewDir = transact.getPreparedStatement(
                        INSERT_NEW_DIR);
                insertNewDir.setString(1, newDirectory);
                insertNewDir.setString(2, placement.name());
                insertNewDir.setInt(3, maxDirOrder+1);
                transact.executePreparedUpdate(insertNewDir);
            }
            
            PreparedStatement maxPageOrder = transact.getPreparedStatement(SELECT_MAX_PAGE_ORDER_IN_PLACE_AND_DIR);
            maxPageOrder.setString(1, placement.name());
            maxPageOrder.setString(2, newDirectory);
            ResultSet maxPageOrderRs = transact.executePreparedQuery(maxPageOrder);
            maxPageOrderRs.first();
            int pageOrder = maxPageOrderRs.getInt(1);
            
            PreparedStatement updatePage = transact.getPreparedStatement(
                    UPDATE_PAGE_DIRECTORY_AND_PLACEMENT_AND_ORDER_WHERE_PAGE_NAME_IS);            
            updatePage.setString(1, newDirectory);
            updatePage.setString(2, placement.name());
            updatePage.setInt(3, pageOrder+1);
            updatePage.setString(4, pageName);
            
            int qty = transact.executePreparedUpdate(updatePage);
            
            transact.commitThemAll();
            
            return ( qty > 0); 
        } catch (HandledTransactSQLException e) { 
            this.ioEngine.reportException(e, "SQLException: .");
        } catch (SQLException e) {
            transact.rollbackAllAndReleaseResources();
            this.ioEngine.reportException(e, "SQLException: .");
        } catch (NullPointerException e) {
            transact.rollbackAllAndReleaseResources();
            this.ioEngine.reportError(
                    "Incorrect usage of JdbcTransaction: " +
                    "database connection is NULL.");
        }
        return false;
    }
    
    @Override
    public boolean deleteDirectoryAndPages(WebPageDirectory dir) {
        JdbcTransaction transact = this.data.beginTransaction();
        try {
            PreparedStatement deleteFromDirs = transact.getPreparedStatement(
                    DELETE_FROM_DIRS_WHERE_DIR_NAME_AND_PLACEMENT_IS);
            deleteFromDirs.setString(1, dir.getName());
            deleteFromDirs.setString(2, dir.getPlacement().name());
            int qty = transact.executePreparedUpdate(deleteFromDirs);
            
            PreparedStatement deletePagesInDir = transact.getPreparedStatement(
                    DELETE_PAGES_WHERE_DIR_NAME_AND_PLACEMENT_IS);
            deletePagesInDir.setString(1, dir.getName());
            deletePagesInDir.setString(2, dir.getPlacement().name());
            qty = qty + transact.executePreparedUpdate(deletePagesInDir);
            
            transact.commitThemAll();
            
            return ( qty > 0 );
            
        } catch (HandledTransactSQLException e) { 
            this.ioEngine.reportException(e, "SQLException: .");
            return false;
        } catch (SQLException e) {
            transact.rollbackAllAndReleaseResources();
            this.ioEngine.reportException(e, "SQLException: .");
            return false;
        } catch (NullPointerException e) {
            transact.rollbackAllAndReleaseResources();
            this.ioEngine.reportError(
                    "Incorrect usage of JdbcTransaction: " +
                    "database connection is NULL.");
            return false;
        }
    }
    
    @Override
    public boolean createEmptyDirectory(WebPagePlacement place, String name) {
        JdbcTransaction transact = this.data.beginTransaction();
        try {
            PreparedStatement maxDirOrderStmnt = 
                        transact.getPreparedStatement(SELECT_MAX_DIR_ORDER_IN_PLACE);
                maxDirOrderStmnt.setString(1, place.name());
                ResultSet maxOrderResultSet = transact.executePreparedQuery(maxDirOrderStmnt);
                maxOrderResultSet.first();
                int maxDirOrder = maxOrderResultSet.getInt(1);
            
            PreparedStatement inserDir = transact.getPreparedStatement(INSERT_NEW_DIR);
            inserDir.setString(1, name);
            inserDir.setString(2, place.name());
            inserDir.setInt(3, maxDirOrder+1);
            int changed = transact.executePreparedUpdate(inserDir);
            
            transact.commitThemAll();
            
            return ( changed > 0) ;
        } catch (HandledTransactSQLException e) { 
            this.ioEngine.reportException(e, "SQLException: Empty directory creation failure.");
            return false;
        } catch (SQLException e) {
            transact.rollbackAllAndReleaseResources();
            this.ioEngine.reportException(e, "SQLException: .");
            return false;
        } catch (NullPointerException e) {
            transact.rollbackAllAndReleaseResources();
            this.ioEngine.reportError(
                    "Incorrect usage of JdbcTransaction: " +
                    "database connection is NULL.");
            return false;
        }
    }
    
    @Override
    public WebPageDirectory getDirectoryExact(WebPagePlacement place, String name) {
        ResultSet rs = null;
        try (Connection con = this.data.connect();
                PreparedStatement ps = con.prepareStatement(
                        SELECT_DIR_IN_DIRS_WHERE_NAME_AND_PLACE_IS)) {
            
            ps.setString(1, name);
            ps.setString(2, place.name());
            rs = ps.executeQuery();
            if (rs.first()) {
                return new WebPageDirectory(
                        rs.getString("dir_name"), 
                        WebPagePlacement.valueOf(rs.getString("dir_placement")), 
                        Integer.parseInt(rs.getString("dir_order"))
                );
            } else {
                return null;
            }
        } catch (SQLException e) {
            this.ioEngine.reportError("Directory selection failure.");
            return null;
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException se) {
                    this.ioEngine.reportExceptionAndExitLater(se, 
                        "Unknown problem in WebPagesDao.getDirectoryExact:",
                        "ResultSet close exception.",
                        " ",
                        "Program will be closed.");
                }
            }
        }
    }
    
    @Override
    public List<WebPageDirectory> getAllDirectoriesIn(WebPagePlacement place) {
        ResultSet rs = null;
        try (Connection con = this.data.connect();
                PreparedStatement ps = con.prepareStatement(
                        SELECT_ALL_DIRS_IN_DIRS_WHERE_PLACE_IS);) {
            
            ps.setString(1, place.name());
            rs = ps.executeQuery();
            List<WebPageDirectory> dirs = new ArrayList<>();
            while (rs.next()) {
                dirs.add(new WebPageDirectory(
                        rs.getString("dir_name"), 
                        WebPagePlacement.valueOf(rs.getString("dir_placement")), 
                        Integer.parseInt(rs.getString("dir_order"))
                )
                );
            }
            Collections.sort(dirs);
            return dirs;
        } catch (SQLException e) {
            this.ioEngine.reportError("Directory selection failure.");
            return Collections.emptyList();
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException se) {
                    this.ioEngine.reportExceptionAndExitLater(se, 
                        "Unknown problem in WebPagesDao.getAllDirectoriesIn:",
                        "ResultSet close exception.",
                        " ",
                        "Program will be closed.");
                }
            }
        }
    }
    
    @Override
    public boolean editDirectoryOrder(WebPagePlacement place, String name, int newOrder) {
        JdbcTransaction transact = this.data.beginTransaction();
        try {      
            
            PreparedStatement ps = transact.getPreparedStatement(
                    SELECT_ALL_DIRS_IN_DIRS_WHERE_PLACE_IS);
            ps.setString(1, place.name());            
            ResultSet dirsRs = transact.executePreparedQuery(ps);
            List<WebPageDirectory> dirs = new ArrayList<>();
            while (dirsRs.next()) {
                dirs.add(new WebPageDirectory(
                        dirsRs.getString("dir_name"), 
                        WebPagePlacement.valueOf(dirsRs.getString("dir_placement")), 
                        Integer.parseInt(dirsRs.getString("dir_order"))
                )
                );
            }
            
            if (newOrder > dirs.size()) {
                newOrder = dirs.size();
            }
            WebPageDirectory movedDir = null;
            for (WebPageDirectory dir : dirs) {
                if (dir.getName().equals(name)) {
                    movedDir = dir;
                    break;
                }
            }
            if ( movedDir == null ) {
                return false;
            }
            int oldOrder = movedDir.getOrder();
            if (newOrder < oldOrder) {
                int oldOrderPageIndex = dirs.indexOf(movedDir);
                for (int i = newOrder - 1; i < oldOrderPageIndex; i++) {
                    dirs.get(i).setOrder(dirs.get(i).getOrder() + 1);
                }                
                dirs.get(oldOrderPageIndex).setOrder(newOrder);
            } else if (oldOrder < newOrder) {
                int oldOrderPageIndex = dirs.indexOf(movedDir);
                for (int i = oldOrderPageIndex + 1; i < newOrder; i++) {
                    dirs.get(i).setOrder(dirs.get(i).getOrder() - 1);
                }                
                dirs.get(oldOrderPageIndex).setOrder(newOrder);
            } else {
                return true;
            }
            Collections.sort(dirs);
            for (int i = 0; i < dirs.size(); i++) {
                dirs.get(i).setOrder(i + 1);
            }
            
            PreparedStatement updatePageOrderSt;
            int qty = 0;
            for (WebPageDirectory updDir : dirs) {
                updatePageOrderSt = transact.getPreparedStatement(
                    UPDATE_DIR_ORDER_WHERE_NAME_AND_PLACE_IS);
                updatePageOrderSt.setInt(1, updDir.getOrder());
                updatePageOrderSt.setString(2, updDir.getName());
                updatePageOrderSt.setString(3, updDir.getPlacement().name());
                qty = qty + transact.executePreparedUpdate(updatePageOrderSt);
            }
            
            transact.commitThemAll();
            return ( qty > 0 );
            
        } catch (HandledTransactSQLException e) {
            this.ioEngine.reportException(e, "SQLException: .");
            return false;
        } catch (SQLException e) {            
            transact.rollbackAllAndReleaseResources();
            this.ioEngine.reportException(e, "SQLException: .");
            return false;
        } catch (NullPointerException e) {
            transact.rollbackAllAndReleaseResources();
            this.ioEngine.reportError(
                    "Incorrect usage of JdbcTransaction: " +
                    "database connection is NULL.");
            return false;
        }
    }
}