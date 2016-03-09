package diarsid.beam.core.entities;

import java.io.Serializable;
import java.util.Objects;

import static diarsid.beam.core.entities.WebPagePlacement.BOOKMARKS;
import static diarsid.beam.core.entities.WebPagePlacement.WEBPANEL;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Diarsid
 */
public class WebPage implements Serializable, Comparable<WebPage> {
    
    private final String name;
    private final String shortcuts;
    private final String urlAddress;
    private final WebPagePlacement placement;
    private final String directory;
    private final int pageInDirectoryOrder;
    private final int directoryOrder;
    private final String browser;
        
    private WebPage(
            String name, 
            String shortcuts,             
            String urlAddress, 
            WebPagePlacement placement,
            String directory, 
            int pageOrder,
            int dirOrder,
            String browser) {
        
        this.name = name;
        this.shortcuts = shortcuts;
        this.urlAddress = urlAddress;
        this.placement = placement;
        this.directory = directory;
        this.pageInDirectoryOrder = pageOrder;
        this.directoryOrder = dirOrder;
        this.browser = browser;
    }
    
    public static WebPage newPage(
            String name, 
            String shortcuts,             
            String urlAddress, 
            WebPagePlacement placement,
            String directory,
            String browser) {
        
        return new WebPage(name, shortcuts, urlAddress, placement, 
                directory, -1, -1, browser);
    }
    
    public static WebPage restorePage(
            String name, 
            String shortcuts,             
            String urlAddress, 
            WebPagePlacement placement,
            String directory,
            int pageOrder,
            int dirOrder,
            String browser) {
        
        return new WebPage(name, shortcuts, urlAddress, placement, 
                directory, pageOrder, dirOrder, browser);
    }

    public String getName() {
        return this.name;
    }

    public String getShortcuts() {
        return this.shortcuts;
    }

    public String getUrlAddress() {
        return this.urlAddress;
    }

    public WebPagePlacement getPlacement() {
        return this.placement;
    }

    public String getDirectory() {
        return this.directory;
    }
    
    public int getPageOrderInDirectory() {
        return this.pageInDirectoryOrder;
    }
    
    public int getDirectoryOrder() {
        return this.directoryOrder;
    }

    public String getBrowser() {
        return this.browser;
    }
    
    public boolean useDefaultBrowser() {
        return "default".equals(this.browser);
    }
    
    @Override
    public int compareTo(WebPage another) {
        if (this.placement.equals(BOOKMARKS) && another.placement.equals(WEBPANEL)) {
            return -1;
        } else if (this.placement.equals(WEBPANEL) && another.placement.equals(BOOKMARKS)) {
            return 1;
        } else {
            if (this.directoryOrder < another.directoryOrder) {
                return -1;
            } else if (this.directoryOrder > another.directoryOrder) {
                return 1;
            } else {
                if (this.pageInDirectoryOrder < another.pageInDirectoryOrder) {
                    return -1;
                }  else if (this.pageInDirectoryOrder > another.pageInDirectoryOrder) {
                    return 1;
                } else {
                    return 0;
                }
            }
        }          
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 59 * hash + Objects.hashCode(this.name);
        hash = 59 * hash + Objects.hashCode(this.shortcuts);
        hash = 59 * hash + Objects.hashCode(this.urlAddress);
        hash = 59 * hash + Objects.hashCode(this.placement);
        hash = 59 * hash + Objects.hashCode(this.directory);
        hash = 59 * hash + this.pageInDirectoryOrder;
        hash = 59 * hash + this.directoryOrder;
        hash = 59 * hash + Objects.hashCode(this.browser);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final WebPage other = (WebPage) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.shortcuts, other.shortcuts)) {
            return false;
        }
        if (!Objects.equals(this.urlAddress, other.urlAddress)) {
            return false;
        }
        if (this.placement != other.placement) {
            return false;
        }
        if (!Objects.equals(this.directory, other.directory)) {
            return false;
        }
        if (this.pageInDirectoryOrder != other.pageInDirectoryOrder) {
            return false;
        }
        if (this.directoryOrder != other.directoryOrder) {
            return false;
        }
        if (!Objects.equals(this.browser, other.browser)) {
            return false;
        }
        return true;
    }
}
