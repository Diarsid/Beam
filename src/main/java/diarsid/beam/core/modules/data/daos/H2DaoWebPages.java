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
import java.util.Random;

import diarsid.beam.core.domain.entities.WebPage;
import diarsid.beam.core.domain.entities.WebDirectory;
import diarsid.beam.core.domain.entities.WebPlacement;

import old.diarsid.beam.core.modules.IoInnerModule;

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
    private final Random random;

    H2DaoWebPages(IoInnerModule io, DataBase data) {
        this.data = data;
        this.ioEngine = io;
        this.random = new Random();
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
            "SELECT "
            + "page_name, page_order, dir_order, page_shortcuts, "
            + "page_url, page_placement, page_directory, page_browser " +
            "FROM web_pages p " +
            "INNER JOIN directories d ON "
            + "(p.page_directory = d.dir_name) "
            + "AND "
            + "(p.page_placement = d.dir_placement) " +
            "WHERE page_placement LIKE ? " +
            "ORDER BY dir_order, page_order";
    private final String SELECT_ALL_PAGES_JOIN_DIRS_WHERE_DIRECTORY_AND_PLACEMENT_LIKE = 
            "SELECT "
            + "page_name, page_order, dir_order, page_shortcuts, "
            + "page_url, page_placement, page_directory, page_browser " +
            "FROM web_pages p " +
            "INNER JOIN directories d ON "
            + "(p.page_directory = d.dir_name) "
            + "AND "
            + "(p.page_placement = d.dir_placement) " +
            "WHERE (page_directory LIKE ? ) AND (page_placement LIKE ? ) " +
            "ORDER BY dir_order, page_order";
    private final String SELECT_PAGES_JOIN_DIRS_WHERE_NAME_OR_SHORT_LIKE = 
            "SELECT "
            + "page_name, page_order, dir_order, page_shortcuts, "
            + "page_url, page_placement, page_directory, page_browser " +
            "FROM web_pages p " +
            "INNER JOIN directories d ON "
            + "(p.page_directory = d.dir_name) "
            + "AND "
            + "(p.page_placement = d.dir_placement) " +
            "WHERE (page_name LIKE ?) OR (page_shortcuts LIKE ?) " + 
            "ORDER BY dir_order, page_order ";
    private final String SELECT_PAGES_JOIN_DIRS_WHERE_PAGE_AND_DIR_AND_PLACE_IS = 
            "SELECT "
            + "page_name, page_order, dir_order, page_shortcuts, "
            + "page_url, page_placement, page_directory, page_browser " +
            "FROM web_pages p " +
            "INNER JOIN directories d ON "
            + "(p.page_directory = d.dir_name) "
            + "AND "
            + "(p.page_placement = d.dir_placement) " +
            "WHERE (page_name IS ? ) "
            + "AND (page_directory IS ? ) "
            + "AND (page_placement IS ? ) ";
    private final String SELECT_PAGES_JOIN_DIRS_WHERE = 
            "SELECT "
            + "page_name, page_order, dir_order, page_shortcuts, "
            + "page_url, page_placement, page_directory, page_browser " +
            "FROM web_pages p " +
            "INNER JOIN directories d ON "
            + "(p.page_directory = d.dir_name) "
            + "AND "
            + "(p.page_placement = d.dir_placement) " +
            "WHERE ";
    private final String NAME_OR_SHORTCUT_LIKE_NAMEPART = 
            " ( ( page_name LIKE ? ) OR ( page_shortcuts LIKE ? ) ) ";
    private final String AND = 
            " AND ";
    private final String ORDER_BY_DIR_AND_PAGE_ORDERS = 
            "ORDER BY dir_order, page_order ";
    private final String INSERT_NEW_PAGE = 
            "INSERT INTO web_pages "
            + "(page_name, page_shortcuts, page_url, page_placement, "
            + "page_directory, page_browser, page_order, page_id) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";           
    private final String SELECT_MAX_PAGE_ORDER_IN_PLACE_AND_DIR = 
            "SELECT MAX(page_order) " +
            "FROM web_pages " +
            "WHERE (page_placement IS ? ) AND (page_directory IS ?)";   
    private final String SELECT_PAGE_ORDER_WHERE_PAGE_NAME_DIR_PLACE_IS = 
            "SELECT page_order " + 
            "FROM web_pages " + 
            "WHERE ( page_name IS ? ) "
            + "AND ( page_directory IS ? ) "
            + "AND ( page_placement IS ? ) ";
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
            "WHERE ( page_name IS ? ) "
            + "AND ( page_directory IS ? ) "
            + "AND ( page_placement IS ? ) ";   
    private final String UPDATE_PAGE_ORDER_WHERE_PAGE_NAME_DIR_PLACE_IS = 
            "UPDATE web_pages " +
            "SET page_order = ? " +
            "WHERE ( page_name IS ? ) "
            + "AND ( page_directory IS ? ) "
            + "AND ( page_placement IS ? ) ";
    private final String UPDATE_DECREMENT_PAGES_ORDERS_WHERE_ORDER_HIGHER_THAN = 
            "UPDATE web_pages " + 
            "SET page_order = page_order-1 " +
            "WHERE ( page_order > ? ) "
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
    private final String UPDATE_DIRS_DECREMENT_ORDER_WHERE_ORDER_HIGHER_THAN = 
            "UPDATE directories " +
            "SET dir_order = dir_order-1 " +
            "WHERE ( dir_order > ? ) AND ( dir_placement IS ? ) ";
            
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
            if ( existResult.getInt(1) == 0 ) {
                
                PreparedStatement maxDirOrderStmnt = 
                        transact.getPreparedStatement(
                                SELECT_MAX_DIR_ORDER_IN_PLACE);
                maxDirOrderStmnt.setString(1, page.getPlacement().name());
                ResultSet maxOrderResultSet = 
                        transact.executePreparedQuery(maxDirOrderStmnt);
                maxOrderResultSet.first();
                int maxDirOrder = maxOrderResultSet.getInt(1);
                
                PreparedStatement insertNewDir = transact.getPreparedStatement(
                        INSERT_NEW_DIR);
                insertNewDir.setString(1, page.getDirectory());
                insertNewDir.setString(2, page.getPlacement().name());
                insertNewDir.setInt(3, maxDirOrder+1);
                transact.executePreparedUpdate(insertNewDir);
            }
            
            PreparedStatement maxPageOrder = transact
                    .getPreparedStatement(SELECT_MAX_PAGE_ORDER_IN_PLACE_AND_DIR);
            maxPageOrder.setString(1, page.getPlacement().name());
            maxPageOrder.setString(2, page.getDirectory());
            ResultSet maxPageOrderRs = transact.executePreparedQuery(maxPageOrder);
            maxPageOrderRs.first();
            int pageOrder = maxPageOrderRs.getInt(1);
            
            int newPageId;
            synchronized ( this ) {
                newPageId = this.random.nextInt();
            }
            PreparedStatement insertPage = transact
                    .getPreparedStatement(INSERT_NEW_PAGE);            
            insertPage.setString(1, page.getName());
            insertPage.setString(2, page.getShortcuts());
            insertPage.setString(3, page.getUrlAddress());
            insertPage.setString(4, page.getPlacement().name());
            insertPage.setString(5, page.getDirectory());
            insertPage.setString(6, page.getBrowser());
            insertPage.setInt(7, pageOrder+1);
            insertPage.setInt(8, newPageId);
            int qty = transact.executePreparedUpdate(insertPage);
            
            transact.commitThemAll();
            return ( qty > 0 );
        } catch (HandledTransactSQLException e) {
            if ( e.causedByPrimaryKeyViolation() ) {
                this.ioEngine.reportMessage("Such page already exists.");
            } else {
                this.ioEngine.reportException(e, "SQLException: web page saving.");
            }            
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
    public boolean deleteWebPage(String name, String dir, WebPlacement place) {
        JdbcTransaction transact = this.data.beginTransaction();
        try {
            int qty = 0;
            int deletedOrder = -1;
            PreparedStatement selectOrderSt = transact.getPreparedStatement(
                    SELECT_PAGE_ORDER_WHERE_PAGE_NAME_DIR_PLACE_IS);
            selectOrderSt.setString(1, name);
            selectOrderSt.setString(2, dir);
            selectOrderSt.setString(3, place.name());
            ResultSet orderRs = transact.executePreparedQuery(selectOrderSt);
            if ( orderRs.first() ) {
                deletedOrder = Integer.parseInt(orderRs.getString("page_order"));
            } else {
                transact.commitThemAll();
                return false;
            }
            
            PreparedStatement delPageSt = transact.getPreparedStatement(
                    DELETE_PAGES_WHERE_NAME_DIR_PLACE_IS);
            delPageSt.setString(1, name);
            delPageSt.setString(2, dir);
            delPageSt.setString(3, place.name());
            qty = qty + transact.executePreparedUpdate(delPageSt);
            
            PreparedStatement decrementOrdersSt = transact.getPreparedStatement(
                    UPDATE_DECREMENT_PAGES_ORDERS_WHERE_ORDER_HIGHER_THAN);
            decrementOrdersSt.setInt(1, deletedOrder);
            decrementOrdersSt.setString(2, dir);
            decrementOrdersSt.setString(3, place.name());
            qty = qty + transact.executePreparedUpdate(decrementOrdersSt);
            
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
    public List<WebPage> getAllWebPagesInPlacement(WebPlacement placement) {
        ResultSet rs = null;
        try(Connection con = data.connect();
            PreparedStatement ps = con.prepareStatement(
                    SELECT_ALL_PAGES_JOIN_DIRS_WHERE_PLACEMENT)) {
            
            ps.setString(1, placement.name());
            
            rs = ps.executeQuery();
            List<WebPage> pages = new ArrayList<>();
            while ( rs.next() ) {
                pages.add(WebPage.restorePage(rs.getString("page_name"),
                        rs.getString("page_shortcuts"), 
                        rs.getString("page_url"),
                        WebPlacement.valueOf(rs.getString("page_placement")),
                        rs.getString("page_directory"),
                        rs.getInt("page_order"),
                        rs.getInt("dir_order"),
                        rs.getString("page_browser")
                ));
            }
            
            return pages;
        } catch (SQLException e) {
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
            PreparedStatement ps = con.prepareStatement(
                    SELECT_PAGES_JOIN_DIRS_WHERE_NAME_OR_SHORT_LIKE)) {
            
            ps.setString(1, "%"+name+"%");
            ps.setString(2, "%"+name+"%");
            
            rs = ps.executeQuery();
            List<WebPage> pages = new ArrayList<>();
            while ( rs.next() ) {
                pages.add(WebPage.restorePage(rs.getString("page_name"),
                        rs.getString("page_shortcuts"), 
                        rs.getString("page_url"),
                        WebPlacement.valueOf(rs.getString("page_placement")),
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
    public List<WebPage> getWebPagesByNameParts(List<String> nameParts){
        int partsQty = nameParts.size();
        if ( partsQty > 0 ) {
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder
                    .append(SELECT_PAGES_JOIN_DIRS_WHERE)
                    .append(NAME_OR_SHORTCUT_LIKE_NAMEPART);
            for (int i = 1; i < partsQty; i++){
                queryBuilder
                        .append(AND)
                        .append(NAME_OR_SHORTCUT_LIKE_NAMEPART);
            }
            queryBuilder.append(ORDER_BY_DIR_AND_PAGE_ORDERS);
            
            ResultSet rs = null;
            try(Connection con = data.connect();
               PreparedStatement ps = con.prepareStatement(queryBuilder.toString())) {
                for (int j = 0; j < partsQty; j++) {
                    ps.setString( (j * 2) + 1, "%"+nameParts.get(j)+"%");
                    ps.setString( (j * 2) + 2, "%"+nameParts.get(j)+"%");
                }
                rs = ps.executeQuery();
                List<WebPage> pages = new ArrayList<>();
                while ( rs.next() ) {
                    pages.add(WebPage.restorePage(rs.getString("page_name"),
                            rs.getString("page_shortcuts"), 
                            rs.getString("page_url"),
                            WebPlacement.valueOf(rs.getString("page_placement")),
                            rs.getString("page_directory"),
                            rs.getInt("page_order"),
                            rs.getInt("dir_order"),
                            rs.getString("page_browser")
                    ));
                }
                return pages;
            } catch (SQLException e) {
                this.ioEngine.reportException(e, "SQLException: get web page by name parts.");
                return Collections.emptyList();
            } finally {
                if ( rs != null ) {
                    try { 
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
            String name, String dir, WebPlacement place) {
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
                    pages.add(WebPage.restorePage(dirRs.getString("page_name"),
                            dirRs.getString("page_shortcuts"), 
                            dirRs.getString("page_url"),
                            WebPlacement.valueOf(dirRs.getString("page_placement")),
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
            String dir, WebPlacement placement, boolean dirStrictMatch) {
        ResultSet rs = null;
        try (Connection con = data.connect();
            PreparedStatement ps = con.prepareStatement(
                    SELECT_ALL_PAGES_JOIN_DIRS_WHERE_DIRECTORY_AND_PLACEMENT_LIKE)) {
            
            if ( dirStrictMatch ) {
                ps.setString(1, dir);
            } else {
                if (placement.equals(WebPlacement.BOOKMARKS)) {
                    // if BOOKMARKS than full name of directory 
                    // must be specified: 
                    // "java" -> "blogs/java" NOT "blogs/java/another/subdir"
                    ps.setString(1, "%"+dir);
                } else {
                    // if WEBPANEL than only part of directory
                    // name is sufficient to select: 
                    // "com" -> "common"
                    ps.setString(1, "%"+dir+"%");
                }
            }
            ps.setString(2, placement.name());
            
            rs = ps.executeQuery();
            List<WebPage> pages = new ArrayList<>();
            while ( rs.next() ) {
                pages.add(WebPage.restorePage(rs.getString("page_name"),
                        rs.getString("page_shortcuts"), 
                        rs.getString("page_url"),
                        WebPlacement.valueOf(rs.getString("page_placement")),
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
            if ( rs != null ) {
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
    public List<String> getAllDirectoriesInPlacement(WebPlacement placement) {
        ResultSet rs = null;
        try(Connection con = data.connect();
            PreparedStatement ps = con.prepareStatement(
                    SELECT_ALL_DIRECTORIES_WHERE_PLACEMENT);) {
            
            ps.setString(1, placement.name());
            rs = ps.executeQuery();
            List<String> directories = new ArrayList<>();
            while ( rs.next() ) {
                directories.add(rs.getString(1));
            }
            return directories;
        } catch (SQLException e) {
            this.ioEngine.reportException(e, "SQLException: get all web page categories.");
            return Collections.emptyList();
        } finally {
            if ( rs != null ) {
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
        try (Connection con = data.connect();
            PreparedStatement ps = con.prepareStatement(UPDATE_PAGE_NAME)) {
            
            ps.setString(1, newName);
            ps.setString(2, name);
            int qty = ps.executeUpdate();

            return ( qty > 0 );            
        } catch (SQLException e) {
            if ( e.getSQLState().startsWith("23") ) {
                this.ioEngine.reportMessage(
                        "Web page with such name already exists in this directory.");
            } else {
                this.ioEngine.reportException(e, "SQLException: update web page name.");
            }            
            return false;
        }
    }
    
    @Override
    public boolean editWebPageShortcuts(String name, String newShortcuts) {
        try (Connection con = data.connect();
            PreparedStatement ps = con.prepareStatement(UPDATE_PAGE_SHORTCUTS)) {
            
            ps.setString(1, newShortcuts);
            ps.setString(2, name);
            int qty = ps.executeUpdate();

            return ( qty > 0 );            
        } catch (SQLException e) {
            this.ioEngine.reportException(e, 
                    "SQLException: update web page shortcuts.");
            return false;
        }
    }
    
    @Override
    public boolean editWebPageUrl(String name, String newUrl){
        try (Connection con = data.connect();
            PreparedStatement ps = con.prepareStatement(
                    UPDATE_PAGE_URL_WHERE_PAGE_NAME)) {
            
            ps.setString(1, newUrl);
            ps.setString(2, name);
            int qty = ps.executeUpdate();

            return ( qty > 0 ); 
        } catch (SQLException e) {
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
        try (Connection con = data.connect();
            PreparedStatement ps = con.prepareStatement(
                    UPDATE_PAGE_BROWSER_WHERE_PAGE_NAME)) {
            
            ps.setString(1, newBrowser);
            ps.setString(2, name);
            int qty = ps.executeUpdate();

            return (qty > 0); 
        } catch (SQLException e) {
            this.ioEngine.reportException(e, "SQLException: update web page browser.");
            return false;
        }
    }
    
    @Override
    public boolean renameDirectoryInPlacement(
            String directory, String newDirectory, WebPlacement placement) {
        JdbcTransaction transact = this.data.beginTransaction();
        try {         
            int qty = 0;
            PreparedStatement updateDirInPage = transact.getPreparedStatement(
                    UPDATE_PAGE_DIRECTORY_WHERE_DIRECTORY_AND_PLACEMENT);
            updateDirInPage.setString(1, newDirectory);
            updateDirInPage.setString(2, directory);
            updateDirInPage.setString(3, placement.name());
            qty = qty + transact.executePreparedUpdate(updateDirInPage);
            
            PreparedStatement updateDirInDirs = transact.getPreparedStatement(
                        UPDATE_DIR_NAME_WHERE_DIR_AND_PLACE_IS);
            updateDirInDirs.setString(1, newDirectory);
            updateDirInDirs.setString(2, directory);
            updateDirInDirs.setString(3, placement.name());
            qty = qty + transact.executePreparedUpdate(updateDirInDirs);
            
            transact.commitThemAll();
            
            return ( qty > 0 ); 
        } catch (HandledTransactSQLException e) {
            if ( e.causedByPrimaryKeyViolation() ) {
                this.ioEngine.reportMessage(
                        "Such directory already exists in " + 
                        placement.name().toLowerCase() + ".");
            } else {
                this.ioEngine.reportException(e, "SQLException: rename directory.");
            }            
        } catch (SQLException e) {
            transact.rollbackAllAndReleaseResources();
            this.ioEngine.reportException(e, "SQLException: rename directory.");
        } catch (NullPointerException e) {
            transact.rollbackAllAndReleaseResources();
            this.ioEngine.reportError(
                    "Incorrect usage of JdbcTransaction: " +
                    "database connection is NULL.");
        }
        return false;
    }
    
    @Override
    public boolean editWebPageOrder(
            String name, String dir, WebPlacement place, int newOrder) {
        
        JdbcTransaction transact = this.data.beginTransaction();
        try {
            PreparedStatement ps = transact.getPreparedStatement(
                    SELECT_ALL_PAGES_JOIN_DIRS_WHERE_DIRECTORY_AND_PLACEMENT_LIKE);            
            ps.setString(1, dir);
            ps.setString(2, place.name());            
            ResultSet pagesRs = transact.executePreparedQuery(ps);
            List<WebPage> pages = new ArrayList<>();
            
            while ( pagesRs.next() ) {
                pages.add(WebPage.restorePage(pagesRs.getString("page_name"),
                        pagesRs.getString("page_shortcuts"), 
                        pagesRs.getString("page_url"),
                        WebPlacement.valueOf(pagesRs.getString("page_placement")),
                        pagesRs.getString("page_directory"),
                        pagesRs.getInt("page_order"),
                        pagesRs.getInt("dir_order"),
                        pagesRs.getString("page_browser")
                ));
            }
            
            if ( newOrder > pages.size() ) {
                newOrder = pages.size();
            }
            WebPage movedPage = null;
            for ( WebPage page : pages ) {
                if ( page.getName().equals(name) ) {
                    movedPage = page;
                    break;
                }
            }
            if ( movedPage == null ) {
                transact.commitThemAll();
                return false;
            }
            int oldOrder = movedPage.getPageOrder();
            if ( newOrder == oldOrder ) {
                transact.commitThemAll();
                return true;
            }
            int movedPageIndex = pages.indexOf(movedPage);            
            
            this.changePagesOrderAccordingToNewOrder(
                    oldOrder, newOrder, movedPageIndex, pages);
            
            PreparedStatement updatePageOrderSt = transact.getPreparedStatement(
                    UPDATE_PAGE_ORDER_WHERE_PAGE_NAME_DIR_PLACE_IS);
            
            for ( WebPage updPage : pages ) {
                updatePageOrderSt.setInt(1, updPage.getPageOrder());
                updatePageOrderSt.setString(2, updPage.getName());
                updatePageOrderSt.setString(3, updPage.getDirectory());
                updatePageOrderSt.setString(4, updPage.getPlacement().name()); 
                updatePageOrderSt.addBatch();
            }
            int qty = transact.executeBatchPreparedUpdate(updatePageOrderSt);
            
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
    
    private void changePagesOrderAccordingToNewOrder(
            int oldOrder, int newOrder, int movedPageIndex, List<WebPage> pages) {
        
        if ( newOrder < oldOrder ) {                
            for (int i = newOrder - 1; i < movedPageIndex; i++) {
                pages.get(i).setOrder(pages.get(i).getPageOrder() + 1);
            }                
            pages.get(movedPageIndex).setOrder(newOrder);
        } else if ( oldOrder < newOrder ) {
            for (int i = movedPageIndex + 1; i < newOrder; i++) {
                pages.get(i).setOrder(pages.get(i).getPageOrder() - 1);
            }                
            pages.get(movedPageIndex).setOrder(newOrder);
        } 
        Collections.sort(pages);
        for (int i = 0; i < pages.size(); i++) {
            pages.get(i).setOrder(i + 1);
        }
    }
    
    @Override
    public boolean moveWebPageToPlacementAndDirectory(
            String pageName, 
            String oldDirectory, 
            WebPlacement oldPlacement, 
            String newDirectory, 
            WebPlacement newPlacement) {
        
        JdbcTransaction transact = this.data.beginTransaction();
        try {
            PreparedStatement oldOrderSt = transact.getPreparedStatement(
                    SELECT_PAGES_JOIN_DIRS_WHERE_PAGE_AND_DIR_AND_PLACE_IS);
            oldOrderSt.setString(1, pageName);
            oldOrderSt.setString(2, oldDirectory);
            oldOrderSt.setString(3, oldPlacement.name());
            ResultSet oldOrderRs = transact.executePreparedQuery(oldOrderSt); 
            if ( ! oldOrderRs.first() ) {
                transact.commitThemAll();
                this.ioEngine.reportMessage("This page does not exists.");
                return false;
            }
            int movedPageOldOrder = oldOrderRs.getInt("page_order");
            
            PreparedStatement dirExists = transact.getPreparedStatement(
                    SELECT_COUNT_DIR_IN_DIRS_WHERE_NAME_AND_PLACE_IS);
            dirExists.setString(1, newDirectory);
            dirExists.setString(2, newPlacement.name());
            ResultSet existResult = transact.executePreparedQuery(dirExists);
            existResult.first();
            if ( existResult.getInt(1) == 0 ) {
                PreparedStatement maxDirOrderStmnt = 
                        transact.getPreparedStatement(
                                SELECT_MAX_DIR_ORDER_IN_PLACE);
                maxDirOrderStmnt.setString(1, newPlacement.name());
                ResultSet maxOrderResultSet = transact
                        .executePreparedQuery(maxDirOrderStmnt);
                maxOrderResultSet.first();
                int maxDirOrder = maxOrderResultSet.getInt(1);
                
                PreparedStatement insertNewDir = transact.getPreparedStatement(
                        INSERT_NEW_DIR);
                insertNewDir.setString(1, newDirectory);
                insertNewDir.setString(2, newPlacement.name());
                insertNewDir.setInt(3, maxDirOrder+1);
                transact.executePreparedUpdate(insertNewDir);
            }
            
            PreparedStatement maxPageOrder = transact.getPreparedStatement(
                    SELECT_MAX_PAGE_ORDER_IN_PLACE_AND_DIR);
            maxPageOrder.setString(1, newPlacement.name());
            maxPageOrder.setString(2, newDirectory);
            ResultSet maxPageOrderRs = transact.executePreparedQuery(maxPageOrder);
            maxPageOrderRs.first();
            int pageOrder = maxPageOrderRs.getInt(1);
            
            PreparedStatement updatePage = transact.getPreparedStatement(
                    UPDATE_PAGE_DIRECTORY_AND_PLACEMENT_AND_ORDER_WHERE_PAGE_NAME_IS);            
            updatePage.setString(1, newDirectory);
            updatePage.setString(2, newPlacement.name());
            updatePage.setInt(3, pageOrder+1);
            updatePage.setString(4, pageName);
            updatePage.setString(5, oldDirectory);
            updatePage.setString(6, oldPlacement.name());
            int qty = transact.executePreparedUpdate(updatePage);
            
            PreparedStatement decrementPagesOrder = transact
                    .getPreparedStatement(
                            UPDATE_DECREMENT_PAGES_ORDERS_WHERE_ORDER_HIGHER_THAN);
            decrementPagesOrder.setInt(1, movedPageOldOrder);
            decrementPagesOrder.setString(2, oldDirectory);
            decrementPagesOrder.setString(3, oldPlacement.name());
            transact.executePreparedUpdate(decrementPagesOrder);
            
            transact.commitThemAll();
            
            return ( qty > 0 ); 
        } catch (HandledTransactSQLException e) { 
            if ( e.causedByPrimaryKeyViolation() ) {
                this.ioEngine.reportMessage(
                        "Such page already exists in target directory.");
            } else {
                this.ioEngine.reportException(e, "SQLException: move web page.");
            }            
        } catch (SQLException e) {
            transact.rollbackAllAndReleaseResources();
            this.ioEngine.reportException(e, "SQLException: move web page.");
        } catch (NullPointerException e) {
            transact.rollbackAllAndReleaseResources();
            this.ioEngine.reportError(
                    "Incorrect usage of JdbcTransaction: " +
                    "database connection is NULL.");
        }
        return false;
    }
    
    @Override
    public boolean deleteDirectoryAndPages(WebDirectory dir) {
        JdbcTransaction transact = this.data.beginTransaction();
        try {
            int qty = 0;
            int deletedOrder = -1;
            PreparedStatement getDirOrder = transact.getPreparedStatement(
                    SELECT_DIR_IN_DIRS_WHERE_NAME_AND_PLACE_IS);
            getDirOrder.setString(1, dir.getName());
            getDirOrder.setString(2, dir.getPlacement().name());
            ResultSet orderRs = transact.executePreparedQuery(getDirOrder);
            if ( orderRs.first() ) {
                deletedOrder = Integer.parseInt(orderRs.getString("dir_order"));
            } else {
                transact.commitThemAll();
                return false;
            }  
            
            PreparedStatement deleteFromDirs = transact.getPreparedStatement(
                    DELETE_FROM_DIRS_WHERE_DIR_NAME_AND_PLACEMENT_IS);
            deleteFromDirs.setString(1, dir.getName());
            deleteFromDirs.setString(2, dir.getPlacement().name());
            qty = qty + transact.executePreparedUpdate(deleteFromDirs);
            
            PreparedStatement deletePagesInDir = transact.getPreparedStatement(
                    DELETE_PAGES_WHERE_DIR_NAME_AND_PLACEMENT_IS);
            deletePagesInDir.setString(1, dir.getName());
            deletePagesInDir.setString(2, dir.getPlacement().name());
            qty = qty + transact.executePreparedUpdate(deletePagesInDir);            
            
            PreparedStatement decrementOtherDirsOrder = transact
                    .getPreparedStatement(
                            UPDATE_DIRS_DECREMENT_ORDER_WHERE_ORDER_HIGHER_THAN);
            decrementOtherDirsOrder.setInt(1, deletedOrder);
            decrementOtherDirsOrder.setString(2, dir.getPlacement().name());            
            qty = qty + transact.executePreparedUpdate(decrementOtherDirsOrder);
            
            transact.commitThemAll();
            
            return ( qty > 0 );
            
        } catch (HandledTransactSQLException e) { 
            this.ioEngine.reportException(e, "SQLException: delete directory.");
            return false;
        } catch (SQLException e) {
            transact.rollbackAllAndReleaseResources();
            this.ioEngine.reportException(e, "SQLException: delete directory.");
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
    public boolean createEmptyDirectory(WebPlacement place, String name) {
        JdbcTransaction transact = this.data.beginTransaction();
        try {
            PreparedStatement maxDirOrderStmnt = 
                        transact.getPreparedStatement(SELECT_MAX_DIR_ORDER_IN_PLACE);
                maxDirOrderStmnt.setString(1, place.name());
                ResultSet maxOrderResultSet = transact
                        .executePreparedQuery(maxDirOrderStmnt);
                maxOrderResultSet.first();
                int maxDirOrder = maxOrderResultSet.getInt(1);
            
            PreparedStatement inserDir = transact.getPreparedStatement(INSERT_NEW_DIR);
            inserDir.setString(1, name);
            inserDir.setString(2, place.name());
            inserDir.setInt(3, maxDirOrder+1);
            int changed = transact.executePreparedUpdate(inserDir);
            
            transact.commitThemAll();
            
            return ( changed > 0 ) ;
        } catch (HandledTransactSQLException e) {
            if ( e.causedByPrimaryKeyViolation() ) {
                this.ioEngine.reportMessage("Such directory already exists.");  
            } else {
                this.ioEngine.reportException(e, 
                        "SQLException: empty directory creation failure.");  
            }
        } catch (SQLException e) {
            transact.rollbackAllAndReleaseResources();
            this.ioEngine.reportException(e, 
                    "SQLException: empty directory creation failure.");
        } catch (NullPointerException e) {
            transact.rollbackAllAndReleaseResources();
            this.ioEngine.reportError(
                    "Incorrect usage of JdbcTransaction: " +
                    "database connection is NULL.");
        }
        return false;
    }
    
    @Override
    public WebDirectory getDirectoryExact(WebPlacement place, String name) {
        ResultSet rs = null;
        try (Connection con = this.data.connect();
                PreparedStatement ps = con.prepareStatement(
                        SELECT_DIR_IN_DIRS_WHERE_NAME_AND_PLACE_IS)) {
            
            ps.setString(1, name);
            ps.setString(2, place.name());
            rs = ps.executeQuery();
            if ( rs.first() ) {
                return new WebDirectory(
                        rs.getString("dir_name"), 
                        WebPlacement.valueOf(rs.getString("dir_placement")), 
                        Integer.parseInt(rs.getString("dir_order"))
                );
            } else {
                return null;
            }
        } catch (SQLException e) {
            this.ioEngine.reportError("Directory selection failure.");
            return null;
        } finally {
            if ( rs != null ) {
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
    public List<WebDirectory> getAllDirectoriesIn(WebPlacement place) {
        ResultSet rs = null;
        try (Connection con = this.data.connect();
                PreparedStatement ps = con.prepareStatement(
                        SELECT_ALL_DIRS_IN_DIRS_WHERE_PLACE_IS);) {
            
            ps.setString(1, place.name());
            rs = ps.executeQuery();
            List<WebDirectory> dirs = new ArrayList<>();
            
            while ( rs.next() ) {
                dirs.add(new WebDirectory(
                        rs.getString("dir_name"), 
                        WebPlacement.valueOf(rs.getString("dir_placement")), 
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
            if ( rs != null ) {
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
    public boolean editDirectoryOrder(WebPlacement place, String name, int newOrder) {
        JdbcTransaction transact = this.data.beginTransaction();
        try {      
            
            PreparedStatement ps = transact.getPreparedStatement(
                    SELECT_ALL_DIRS_IN_DIRS_WHERE_PLACE_IS);
            ps.setString(1, place.name());            
            ResultSet dirsRs = transact.executePreparedQuery(ps);
            List<WebDirectory> dirs = new ArrayList<>();
            
            while ( dirsRs.next() ) {
                dirs.add(new WebDirectory(
                        dirsRs.getString("dir_name"), 
                        WebPlacement.valueOf(dirsRs.getString("dir_placement")), 
                        Integer.parseInt(dirsRs.getString("dir_order"))
                )
                );
            }
            
            if (newOrder > dirs.size()) {
                newOrder = dirs.size();
            }
            WebDirectory movedDir = null;
            for (WebDirectory dir : dirs) {
                if (dir.getName().equals(name)) {
                    movedDir = dir;
                    break;
                }
            }
            if ( movedDir == null ) {
                transact.commitThemAll();
                return false;
            }
            int oldOrder = movedDir.getOrder();
            if ( newOrder == oldOrder ) {
                transact.commitThemAll();
                return true;
            }
            int movedDirIndex = dirs.indexOf(movedDir);            
            
            this.changeDirectoriesOrder(newOrder, oldOrder, dirs, movedDirIndex);
            
            PreparedStatement updatePageOrderSt = transact.getPreparedStatement(
                    UPDATE_DIR_ORDER_WHERE_NAME_AND_PLACE_IS);
            for (WebDirectory updDir : dirs) {
                updatePageOrderSt.setInt(1, updDir.getOrder());
                updatePageOrderSt.setString(2, updDir.getName());
                updatePageOrderSt.setString(3, updDir.getPlacement().name());
                updatePageOrderSt.addBatch();
            }
            int qty = transact.executeBatchPreparedUpdate(updatePageOrderSt);
            
            transact.commitThemAll();
            return ( qty > 0 );
            
        } catch (HandledTransactSQLException e) {
            this.ioEngine.reportException(e, "SQLException: edit directory order.");            
        } catch (SQLException e) {            
            transact.rollbackAllAndReleaseResources();
            this.ioEngine.reportException(e, "SQLException: edit directory order.");
        } catch (NullPointerException e) {
            transact.rollbackAllAndReleaseResources();
            this.ioEngine.reportError(
                    "Incorrect usage of JdbcTransaction: " +
                    "database connection is NULL.");
        }
        return false;
    }
    
    private void changeDirectoriesOrder(
            int newOrder, int oldOrder, List<WebDirectory> dirs, int movedDirIndex) {
        
        if (newOrder < oldOrder) {                
            for (int i = newOrder - 1; i < movedDirIndex; i++) {
                dirs.get(i).setOrder(dirs.get(i).getOrder() + 1);
            }                
            dirs.get(movedDirIndex).setOrder(newOrder);
        } else if (oldOrder < newOrder) {
            for (int i = movedDirIndex + 1; i < newOrder; i++) {
                dirs.get(i).setOrder(dirs.get(i).getOrder() - 1);
            }                
            dirs.get(movedDirIndex).setOrder(newOrder);
        }
        Collections.sort(dirs);
        for (int i = 0; i < dirs.size(); i++) {
            dirs.get(i).setOrder(i + 1);
        }
    }
}