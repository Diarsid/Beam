/*
 * project: Beam
 * author: Diarsid
 */

package com.drs.beam.core.modules.data.daos;

import com.drs.beam.core.modules.data.DaoWebPages;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.drs.beam.core.entities.WebPage;
import com.drs.beam.core.modules.InnerIOModule;
import com.drs.beam.core.modules.data.DataBase;

/**
 *
 * @author Diarsid
 */
class H2DaoWebPages implements DaoWebPages {
    
    private final DataBase data;
    private final InnerIOModule ioEngine;

    public H2DaoWebPages(InnerIOModule io, DataBase data) {
        this.data = data;
        this.ioEngine = io;
    }
    
    /* 
     * SQL Table illustration for webPages entities.
     *  +----------------------------------------------------------------------------------------+
     *  | web_pages                                                                              |
     *  +----------+------------+-------------------------------+----------------+---------------+
     *  | page_id  | page_name  | page_url                      | page_caterory  | page_browser  |
     *  +----------+------------+-------------------------------+----------------+---------------+
     *  | 123      | facebook   | https://www.facebook.com...   | social         | default       |
     *  +----------+------------+-------------------------------+----------------+---------------+
     *  | 451      | wiki       | https://en.wikipedia.org      | common         | opera         |
     *  +----------+------------+-------------------------------+----------------+---------------+
     */
    
    private final String SELECT_ALL_PAGES = 
            "SELECT page_name, page_url, page_category, page_browser " +
            "FROM web_pages " +
            "ORDER BY page_category, page_name";
    private final String SELECT_ALL_PAGES_WHERE_CATEGORY = 
            "SELECT page_name, page_url, page_category, page_browser " +
            "FROM web_pages " +
            "WHERE page_category LIKE ? " +
            "ORDER BY page_name";
    private final String SELECT_PAGES_WHERE_NAME_LIKE = 
            "SELECT page_name, page_url, page_category, page_browser " +
            "FROM web_pages " +
            "WHERE page_name LIKE ? " + 
            "ORDER BY page_category, page_name ";
    private final String SELECT_PAGES_WHERE = 
            "SELECT page_name, page_url, page_category, page_browser " +
            "FROM web_pages " +
            "WHERE ";
    private final String NAME_LIKE_NAMEPART = 
            "page_name LIKE ? ";
    private final String AND = 
            " AND ";
    private final String ODER_BY_CATEGORY_AND_NAME = 
            "ORDER BY page_category, page_name ";
    private final String INSERT_NEW_PAGE = 
            "INSERT INTO web_pages (page_name, page_url, page_category, page_browser) " +
            "VALUES (?, ?, ?, ?)";
    private final String DELETE_PAGES_WHERE_NAME_LIKE = 
            "DELETE FROM web_pages " +
            "WHERE page_name LIKE ?";
    private final String SELECT_ALL_CATEGORIES = 
            "SELECT DISTINCT page_category "+
            "FROM web_pages " +
            "ORDER BY page_category ";
    private final String UPDATE_PAGE_NAME = 
            "UPDATE web_pages " +
            "SET page_name = ? " +
            "WHERE page_name = ?";
    private final String UPDATE_PAGE_URL_WHERE_PAGE_NAME = 
            "UPDATE web_pages " +
            "SET page_url = ? " + 
            "WHERE page_name = ?";
    private final String UPDATE_PAGE_CATEGORY_WHERE_PAGE_NAME = 
            "UPDATE web_pages " +
            "SET page_category = ?" + 
            "WHERE page_name = ?";
    private final String UPDATE_PAGE_BROWSER_WHERE_PAGE_NAME = 
            "UPDATE web_pages " +
            "SET page_browser = ?" + 
            "WHERE page_name = ?";
    private final String UPDATE_PAGE_CATEGORY_WHERE_CATEGORY = 
            "UPDATE web_pages " +
            "SET page_category = ?" + 
            "WHERE page_category = ?";
    
    // Methods ============================================================================
    @Override
    public void saveWebPage(WebPage page){
        try(Connection con = data.connect();
           PreparedStatement ps = con.prepareStatement(INSERT_NEW_PAGE)) {
            
            ps.setString(1, page.getName());
            ps.setString(2, page.getUrlAddress());
            ps.setString(3, page.getCategory());
            ps.setString(4, page.getBrowser());
            ps.executeUpdate();
            
        } catch (SQLException e){
            this.ioEngine.reportException(e, "SQLException: web page saving.");
        }
    }
    
    @Override
    public boolean deleteWebPage(String name){
        try(Connection con = data.connect();
            PreparedStatement ps = con.prepareStatement(DELETE_PAGES_WHERE_NAME_LIKE);) {
            
            ps.setString(1, name);
            int qty = ps.executeUpdate();

            return (qty > 0);
        } catch (SQLException e){
            this.ioEngine.reportException(e, "SQLException: web page deleting.");
            return false;
        }
    }
    
    @Override
    public List<WebPage> getAllWebPages(){
        try(Connection con = data.connect();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(SELECT_ALL_PAGES)) {
            
            List<WebPage> pages = new ArrayList<>();
            while (rs.next()){
                pages.add(new WebPage(
                        rs.getString(1),
                        rs.getString(2), 
                        rs.getString(3),
                        rs.getString(4)));
            }
            
            return pages;
        } catch (SQLException e){
            this.ioEngine.reportException(e, "SQLException: get all web pages.");
            return Collections.emptyList();
        }
    }
    
    @Override
    public List<WebPage> getWebPagesByName(String name){
        ResultSet rs = null;
        try(Connection con = data.connect();
            PreparedStatement ps = con.prepareStatement(SELECT_PAGES_WHERE_NAME_LIKE)) {
            
            ps.setString(1, "%"+name+"%");
            rs = ps.executeQuery();
            List<WebPage> pages = new ArrayList<>();
            while (rs.next()){
                pages.add(new WebPage(
                        rs.getString(1),
                        rs.getString(2), 
                        rs.getString(3),
                        rs.getString(4)));
            }
            return pages;
        } catch (SQLException e){
            this.ioEngine.reportException(e, "SQLException: get web pages by name.");            
            return Collections.emptyList();
        } finally {
            if (rs != null){
                try{
                    rs.close();
                } catch (SQLException se){
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
            queryBuilder.append(ODER_BY_CATEGORY_AND_NAME);
            ResultSet rs = null;
            try(Connection con = data.connect();
               PreparedStatement ps = con.prepareStatement(queryBuilder.toString())) {
                for (int j = 0; j < partsQty; j++){
                    ps.setString(j+1, "%"+nameParts[j]+"%");
                }
                rs = ps.executeQuery();
                List<WebPage> pages = new ArrayList<>();
                while (rs.next()){
                    pages.add(new WebPage(
                            rs.getString(1),
                            rs.getString(2), 
                            rs.getString(3),
                            rs.getString(4)));
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
    public List<WebPage> getAllWebPagesOfCategory(String category){
        ResultSet rs = null;
        try(Connection con = data.connect();
            PreparedStatement ps = con.prepareStatement(SELECT_ALL_PAGES_WHERE_CATEGORY)) {
            
            ps.setString(1, "%"+category+"%");
            rs = ps.executeQuery();
            List<WebPage> pages = new ArrayList<>();
            while (rs.next()){
            pages.add(new WebPage(
                    rs.getString(1),
                    rs.getString(2), 
                    rs.getString(3),
                    rs.getString(4)));
            }
            return pages;
        } catch (SQLException e){
          this.ioEngine.reportException(e, "SQLException: get web pages of category.");
            return Collections.emptyList();
        } finally {
            if (rs != null){
                try{
                    rs.close();
                } catch (SQLException se){
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
    public List<String> getAllCategories(){
        try(Connection con = data.connect();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(SELECT_ALL_CATEGORIES)) {
            
            List<String> categories = new ArrayList<>();
            while (rs.next()){
                categories.add(rs.getString(1));
            }
            return categories;
        } catch (SQLException e){
            this.ioEngine.reportException(e, "SQLException: get all web page categories.");
            return Collections.emptyList();
        }
    }   
    
    @Override
    public boolean editWebPageName(String name, String newName){
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
    public boolean editWebPageCategory(String name, String newCategory){
        try(Connection con = data.connect();
            PreparedStatement ps = con.prepareStatement(UPDATE_PAGE_CATEGORY_WHERE_PAGE_NAME)) {
            
            ps.setString(1, newCategory);
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
    public boolean renameCategory(String category, String newCategory){
        try(Connection con = data.connect();
            PreparedStatement ps = con.prepareStatement(UPDATE_PAGE_CATEGORY_WHERE_CATEGORY)) {
            
            ps.setString(1, newCategory);
            ps.setString(2, category);
            int qty = ps.executeUpdate();

            return (qty > 0); 
        } catch (SQLException e){
            this.ioEngine.reportException(e, "SQLException: update web page browser.");
            return false;
        }
    }
}