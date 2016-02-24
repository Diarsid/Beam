/*
 * project: Beam
 * author: Diarsid
 */

package com.drs.beam.core.modules.data.daos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.drs.beam.core.entities.WebPage;
import com.drs.beam.core.entities.WebPageDirectory;
import com.drs.beam.core.entities.WebPagePlacement;
import com.drs.beam.core.modules.IoInnerModule;
import com.drs.beam.core.modules.data.DaoWebPages;
import com.drs.beam.core.modules.data.DataBase;
import com.drs.beam.core.modules.data.HandledTransactSQLException;
import com.drs.beam.core.modules.data.JdbcTransaction;

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
     *  +----------------------------------------------------------------------------------------------------------------------------+
     *  | web_pages                                                                                                                  |
     *  +----------+------------+-----------------+------------------------------+----------------+------------------+---------------+
     *  | page_id  | page_name  | page_shortcuts  | page_url                     | page_placement | page_directory   | page_browser  |
     *  +----------+------------+-----------------+------------------------------+----------------+------------------+---------------+
     *  | 123      | facebook   | fb fboo faceb   |https://www.facebook.com...   | webpanel       | social           | default       |
     *  +----------+------------+-----------------+------------------------------+----------------+------------------+---------------+
     *  | 451      | wiki       |                 |https://en.wikipedia.org      | webpanel       | common           | firefox       |
     *  +----------+------------+-----------------+------------------------------+----------------+------------------+---------------+
     *  | 324      | java_blog  | bl jbl          |https://some.java.blog...     | bookmarks      | java/blogs/misc  | chrome        |
     *  +----------+------------+-----------------+------------------------------+----------------+------------------+---------------+ 
     */
        
    private final String SELECT_ALL_PAGES_WHERE_PLACEMENT = 
            "SELECT page_name, page_shortcuts, page_url, page_placement, page_directory, page_browser " +
            "FROM web_pages " +
            "WHERE page_placement LIKE ? " +
            "ORDER BY page_directory, page_name";
    private final String SELECT_ALL_PAGES_WHERE_DIRECTORY_AND_PLACEMENT_LIKE = 
            "SELECT page_name, page_shortcuts, page_url, page_placement, page_directory, page_browser " +
            "FROM web_pages " +
            "WHERE (page_directory LIKE ? ) AND (page_placement LIKE ? ) " +
            "ORDER BY page_directory, page_name";
    private final String SELECT_PAGES_WHERE_NAME_OR_SHORT_LIKE = 
            "SELECT page_name, page_shortcuts, page_url, page_placement, page_directory, page_browser " +
            "FROM web_pages " +
            "WHERE (page_name LIKE ?) OR (page_shortcuts LIKE ?) " + 
            "ORDER BY page_directory, page_name ";
    private final String SELECT_PAGES_WHERE = 
            "SELECT page_name, page_shortcuts, page_url, page_placement, page_directory, page_browser " +
            "FROM web_pages " +
            "WHERE ";
    private final String NAME_LIKE_NAMEPART = 
            "page_name LIKE ? ";
    private final String AND = 
            " AND ";
    private final String ODER_BY_DIRECTORY_AND_NAME = 
            "ORDER BY page_directory, page_name ";
    private final String INSERT_NEW_PAGE = 
            "INSERT INTO web_pages (page_name, page_shortcuts, page_url, page_placement, page_directory, page_browser) " +
            "VALUES (?, ?, ?, ?, ?, ?)";
    private final String DELETE_PAGES_WHERE_NAME_LIKE = 
            "DELETE FROM web_pages " +
            "WHERE page_name LIKE ?";
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
    private final String UPDATE_PAGE_DIRECTORY_AND_PLACEMENT_WHERE_PAGE_NAME_IS = 
            "UPDATE web_pages " +
            "SET page_directory = ?, page_placement = ? " +
            "WHERE page_name IS ? ";
    private final String DELETE_FROM_DIRS_WHERE_DIR_NAME_AND_PLACEMENT_IS = 
            "DELETE FROM directories " +
            "WHERE ( dir_name IS ? ) AND ( dir_placement is ? ) ";
    private final String DELETE_PAGES_WHERE_DIR_NAME_AND_PLACEMENT_IS = 
            "DELETE FROM web_pages " +
            "WHERE ( page_directory IS ? ) AND ( page_placement IS ? )";
    private final String INSERT_NEW_DIR_IN_DIRS = 
            "INSERT INTO directories (dir_name, dir_placement, dir_order) " +
            "VALUES (?, ?, ("
            + "SELECT (MAX(dir_order)+1) "
            + "FROM directories "
            + "WHERE dir_placement IS ?) "
            + ") ";
    private final String SELECT_COUNT_DIR_IN_DIRS_WHERE_NAME_AND_PLACE_IS = 
            "SELECT COUNT(dir_name) " +
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
    private final String SELECT_MAX_ORDER_IN_PLACE = 
            "SELECT MAX(dir_name) " +
            "FROM directories " +
            "WHERE dir_placement IS ? ";
    private final String SELECT_ALL_DIRS_IN_DIRS_IN_PLACE_WHERE_ORDER_HIGHER_THAN = 
            "SELECT dir_name, dir_order " +
            "FROM directories " +
            "WHERE ( order >= ? ) AND ( dir_placement IS ? ) ";
    private final String UPDATE_DIR_ORDER_WHERE_NAME_AND_PLACE_IS = 
            "UPDATE directories " +
            "SET dir_order = ? " +
            "WHERE ( dir_name = ? ) AND ( dir_placement IS ? ) ";
    private final String UPDATE_DIR_NAME_WHERE_DIR_AND_PLACE_IS = 
            "UPDATE directories " +
            "SET dir_name = ? " +
            "WHERE ( dir_name = ? ) AND ( dir_placement IS ? ) ";
            
    @Override
    public void saveWebPage(WebPage page) {
        JdbcTransaction transact = this.data.beginTransaction();
        try {
            PreparedStatement insertPage = transact.getPreparedStatement(INSERT_NEW_PAGE);
            
            insertPage.setString(1, page.getName());
            insertPage.setString(2, page.getShortcuts());
            insertPage.setString(3, page.getUrlAddress());
            insertPage.setString(4, page.getPlacement().name());
            insertPage.setString(5, page.getDirectory());
            insertPage.setString(6, page.getBrowser());
            
            transact.executePreparedUpdate(insertPage);
            
            PreparedStatement dirExists = transact.getPreparedStatement(
                    SELECT_COUNT_DIR_IN_DIRS_WHERE_NAME_AND_PLACE_IS);
            dirExists.setString(1, page.getName());
            dirExists.setString(2, page.getPlacement().name());
            ResultSet existResult = transact.executePreparedQuery(dirExists);
            existResult.first();
            int exists = existResult.getInt(1);
            if (exists == 0) {
                PreparedStatement insertNewDir = transact.getPreparedStatement(
                        INSERT_NEW_DIR_IN_DIRS);
                insertNewDir.setString(1, page.getDirectory());
                insertNewDir.setString(2, page.getPlacement().name());
                insertNewDir.setString(3, page.getPlacement().name());
                transact.executePreparedUpdate(insertNewDir);
            }
            
            transact.commitThemAll();
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
    }
    
    @Override
    public boolean deleteWebPage(String name) {
        try (Connection con = data.connect();
            PreparedStatement ps = con.prepareStatement(DELETE_PAGES_WHERE_NAME_LIKE);) {
            
            ps.setString(1, name);
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
            PreparedStatement ps = con.prepareStatement(SELECT_ALL_PAGES_WHERE_PLACEMENT)) {
            
            ps.setString(1, placement.name());
            
            rs = ps.executeQuery();
            List<WebPage> pages = new ArrayList<>();
            while (rs.next()){
                pages.add(new WebPage(
                        rs.getString("page_name"),
                        rs.getString("page_shortcuts"), 
                        rs.getString("page_url"),
                        WebPagePlacement.valueOf(rs.getString("page_placement")),
                        rs.getString("page_directory"),
                        rs.getString("page_browser")
                ));
            }
            
            return pages;
        } catch (SQLException e){
            this.ioEngine.reportException(e, "SQLException: get all web pages.");
            e.printStackTrace();
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
            PreparedStatement ps = con.prepareStatement(SELECT_PAGES_WHERE_NAME_OR_SHORT_LIKE)) {
            
            ps.setString(1, "%"+name+"%");
            ps.setString(2, "%"+name+"%");
            
            rs = ps.executeQuery();
            List<WebPage> pages = new ArrayList<>();
            while (rs.next()) {
                pages.add(new WebPage(
                        rs.getString("page_name"),
                        rs.getString("page_shortcuts"), 
                        rs.getString("page_url"),
                        WebPagePlacement.valueOf(rs.getString("page_placement")),
                        rs.getString("page_directory"),
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
            queryBuilder.append(SELECT_PAGES_WHERE).append(NAME_LIKE_NAMEPART);
            for (int i = 1; i < partsQty; i++){
                queryBuilder.append(AND).append(NAME_LIKE_NAMEPART);
            }
            queryBuilder.append(ODER_BY_DIRECTORY_AND_NAME);
            ResultSet rs = null;
            try(Connection con = data.connect();
               PreparedStatement ps = con.prepareStatement(queryBuilder.toString())) {
                for (int j = 0; j < partsQty; j++){
                    ps.setString(j+1, "%"+nameParts[j]+"%");
                }
                rs = ps.executeQuery();
                List<WebPage> pages = new ArrayList<>();
                while (rs.next()) {
                    pages.add(new WebPage(
                            rs.getString("page_name"),
                            rs.getString("page_shortcuts"), 
                            rs.getString("page_url"),
                            WebPagePlacement.valueOf(rs.getString("page_placement")),
                            rs.getString("page_directory"),
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
    public List<WebPage> getAllWebPagesInDirectoryAndPlacement(
            String directory, WebPagePlacement placement) {
        ResultSet rs = null;
        try (Connection con = data.connect();
            PreparedStatement ps = con.prepareStatement(SELECT_ALL_PAGES_WHERE_DIRECTORY_AND_PLACEMENT_LIKE)) {
            
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
                pages.add(new WebPage(
                        rs.getString("page_name"),
                        rs.getString("page_shortcuts"), 
                        rs.getString("page_url"),
                        WebPagePlacement.valueOf(rs.getString("page_placement")),
                        rs.getString("page_directory"),
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
    public boolean renameDirectoryInPlacement(
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
                PreparedStatement insertNewDir = transact.getPreparedStatement(
                        INSERT_NEW_DIR_IN_DIRS);
                insertNewDir.setString(1, newDirectory);
                insertNewDir.setString(2, placement.name());
                insertNewDir.setString(3, placement.name());
                transact.executePreparedUpdate(insertNewDir);
            }
            
            PreparedStatement updatePage = transact.getPreparedStatement(
                    UPDATE_PAGE_DIRECTORY_AND_PLACEMENT_WHERE_PAGE_NAME_IS);
            
            updatePage.setString(1, newDirectory);
            updatePage.setString(2, placement.name());
            updatePage.setString(3, pageName);
            
            int qty = transact.executePreparedUpdate(updatePage);
            
            transact.commitThemAll();
            
            return ( qty > 0); 
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
    public boolean createEmptyDirectoryWithDefaultOrder(WebPagePlacement place, String name) {
        try (Connection con = this.data.connect();
                PreparedStatement ps = con.prepareStatement(INSERT_NEW_DIR_IN_DIRS)) {
            
            ps.setString(1, name);
            ps.setString(2, place.name());
            ps.setString(3, place.name());
            
            return ( ps.executeUpdate() > 0) ;
        } catch (SQLException e) {
            this.ioEngine.reportError("Directory creation failure.");
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
    public boolean changeDirectoryOrder(WebPagePlacement place, String name, int newOrder) {
        JdbcTransaction transact = this.data.beginTransaction();
        try {
            PreparedStatement maxOrderStmnt = transact.getPreparedStatement(SELECT_MAX_ORDER_IN_PLACE);
            maxOrderStmnt.setString(1, place.name());
            ResultSet maxOrderResultSet = transact.executePreparedQuery(maxOrderStmnt);
            maxOrderResultSet.first();
            int maxOrder = maxOrderResultSet.getInt(1);
            if ( maxOrder > newOrder ) {
                transact.rollbackAllAndReleaseResources();
                return false;
            }            
            
            PreparedStatement selectDirsWithHigherOrder = transact.getPreparedStatement(
                    SELECT_ALL_DIRS_IN_DIRS_IN_PLACE_WHERE_ORDER_HIGHER_THAN);
            selectDirsWithHigherOrder.setInt(1, newOrder);
            selectDirsWithHigherOrder.setString(2, place.name());
            ResultSet higherDirs = transact
                    .executePreparedQuery(selectDirsWithHigherOrder);
            Map<Integer, String> dirs = new HashMap<>();
            while (higherDirs.next()) {
                dirs.put(
                        higherDirs.getInt("dir_order"), 
                        higherDirs.getString("dir_name"));
            }
            PreparedStatement updateOrder;
            int qty = 0;
            for (Map.Entry<Integer, String> entry : dirs.entrySet()) {
                updateOrder = transact.getPreparedStatement(
                        UPDATE_DIR_ORDER_WHERE_NAME_AND_PLACE_IS);
                updateOrder.setInt(1, entry.getKey()+1);
                updateOrder.setString(2, entry.getValue());
                updateOrder.setString(3, place.name());
                qty = qty + transact.executePreparedUpdate(updateOrder);
            }
            updateOrder = transact.getPreparedStatement(
                    UPDATE_DIR_ORDER_WHERE_NAME_AND_PLACE_IS);
            updateOrder.setInt(1, newOrder);
            updateOrder.setString(2, name);
            updateOrder.setString(3, place.name());
            qty = qty + transact.executePreparedUpdate(updateOrder);
            
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