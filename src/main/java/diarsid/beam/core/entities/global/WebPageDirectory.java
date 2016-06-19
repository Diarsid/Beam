/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.entities.global;

import java.util.Objects;

import static diarsid.beam.core.entities.global.WebPagePlacement.BOOKMARKS;
import static diarsid.beam.core.entities.global.WebPagePlacement.WEBPANEL;

/**
 *
 * @author Diarsid
 */
public class WebPageDirectory implements Comparable<WebPageDirectory> {
    
    private final String name;
    private final WebPagePlacement placement;
    private int order = -1; 
    
    public WebPageDirectory(String name, WebPagePlacement place, int order) {
        this.name = name;
        this.placement = place;
        this.order = order;
    }
    
    public WebPageDirectory(String name, WebPagePlacement place) {
        this.name = name;
        this.placement = place;
    }

    public String getName() {
        return this.name;
    }

    public WebPagePlacement getPlacement() {
        return this.placement;
    }

    public int getOrder() {
        return this.order;
    }
    
    public void setOrder(int newOrder) {
        this.order = newOrder;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + Objects.hashCode(this.name);
        hash = 29 * hash + Objects.hashCode(this.placement);
        hash = 29 * hash + this.order;
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
        final WebPageDirectory other = (WebPageDirectory) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (this.placement != other.placement) {
            return false;
        }
        if (this.order != other.order) {
            return false;
        }
        return true;
    }
    
    @Override
    public int compareTo(WebPageDirectory dir) {
        if (this.placement.equals(BOOKMARKS) && dir.placement.equals(WEBPANEL)) {
            return -1;
        } else if (this.placement.equals(WEBPANEL) && dir.placement.equals(BOOKMARKS)) {
            return 1;
        } else {
            if (this.order < dir.order) {
                return -1;
            } else if (this.order > dir.order) {
                return 1;
            } else {
                return 0;  
            }
        }    
    }
}
