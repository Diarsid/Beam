/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.domain.entities;

/**
 *
 * @author Diarsid
 */
public interface Orderable {    
    
    int order();
    
    void setOrder(int newOrder);
}
