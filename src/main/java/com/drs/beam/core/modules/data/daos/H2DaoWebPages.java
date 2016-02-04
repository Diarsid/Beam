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
import java.util.List;

import com.drs.beam.core.entities.WebPage;
import com.drs.beam.core.entities.WebPagePlacement;
import com.drs.beam.core.modules.IoInnerModule;
import com.drs.beam.core.modules.data.DaoWebPages;
import com.drs.beam.core.modules.data.DataBase;

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
            "WHERE page_name LIKE ?";
    private final String UPDATE_PAGE_DIRECTORY_WHERE_PAGE_NAME = 
            "UPDATE web_pages " +
            "SET page_directory = ?" + 
            "WHERE page_name LIKE ? ";
    private final String UPDATE_PAGE_BROWSER_WHERE_PAGE_NAME = 
            "UPDATE web_pages " +
            "SET page_browser = ?" + 
            "WHERE page_name LIKE ?";
    private final String UPDATE_PAGE_DIRECTORY_WHERE_DIRECTORY_AND_PLACEMENT = 
            "UPDATE web_pages " +
            "SET page_directory = ?" + 
            "WHERE (page_directory LIKE ? ) AND (page_placement LIKE ? ) ";
    private final String UPDATE_PAGE_PLACEMENT_WHERE_PAGE_NAME = 
            "UPDATE web_pages " +
            "SET page_placement = ? " +
            "WHERE page_name LIKE ? ";
    
    @Override
    public void saveWebPage(WebPage page) {
        try (Connection con = data.connect();
           PreparedStatement ps = con.prepareStatement(INSERT_NEW_PAGE)) {
            
            ps.setString(1, page.getName());
            ps.setString(2, page.getShortcuts());
            ps.setString(3, page.getUrlAddress());
            ps.setString(4, page.getPlacement().name());
            ps.setString(5, page.getDirectory());
            ps.setString(6, page.getBrowser());
            ps.executeUpdate();
            
        } catch (SQLException e) {
            this.ioEngine.reportException(e, "SQLException: web page saving.");
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
            this.ioEngine.reportException(e, "SQLException: update web page name.");
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
            String category, String newCategory, WebPagePlacement placement) {
        try (Connection con = data.connect();
            PreparedStatement ps = con.prepareStatement(UPDATE_PAGE_DIRECTORY_WHERE_DIRECTORY_AND_PLACEMENT)) {
            
            ps.setString(1, newCategory);
            ps.setString(2, category);
            ps.setString(3, placement.name());
            int qty = ps.executeUpdate();

            return (qty > 0); 
        } catch (SQLException e){
            this.ioEngine.reportException(e, "SQLException: update web page browser.");
            return false;
        }
    }
    
    @Override
    public boolean moveWebPageToPlacementAndDirectory(
            String pageName, String newDirectory, WebPagePlacement placement) {
        
        try(Connection con = data.connect();
            PreparedStatement psPlacement = 
                    con.prepareStatement(UPDATE_PAGE_PLACEMENT_WHERE_PAGE_NAME);
            PreparedStatement psDir = 
                    con.prepareStatement(UPDATE_PAGE_DIRECTORY_WHERE_PAGE_NAME)) {
            
            con.setAutoCommit(false);
            
            psPlacement.setString(1, placement.name());
            psPlacement.setString(2, pageName);
            int qtyPlace = psPlacement.executeUpdate();
            
            psDir.setString(1, newDirectory);
            psDir.setString(2, pageName);
            int qtyDir = psDir.executeUpdate();
            
            con.commit();
            
            return ( (qtyPlace + qtyDir) > 0); 
        } catch (SQLException e) {
            this.ioEngine.reportException(e, "SQLException: update web page category.");
            return false;
        }
    }
}