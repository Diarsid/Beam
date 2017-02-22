/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.domain.entities;

import static diarsid.beam.core.domain.entities.WebPlace.BOOKMARKS;
import static diarsid.beam.core.domain.entities.WebPlace.WEBPANEL;

/**
 *
 * @author Diarsid
 */
public interface Orderable extends Comparable<Orderable> {
    
    WebPlace place();
    
    int order();
    
    void setOrder(int newOrder);   
    
    @Override
    default int compareTo(Orderable another) {
        if ( this.place().equals(BOOKMARKS) && another.place().equals(WEBPANEL) ) {
            return -1;
        } else if ( this.place().equals(WEBPANEL) && another.place().equals(BOOKMARKS) ) {
            return 1;
        } else {
            if ( this.order() < another.order() ) {
                return -1;
            } else if ( this.order() > another.order() ) {
                return 1;
            } else {
                return 0;
            }
        }
    }
}
