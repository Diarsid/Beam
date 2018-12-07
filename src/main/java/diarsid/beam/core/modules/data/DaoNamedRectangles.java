/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.data;

import diarsid.beam.core.base.data.DataExtractionException;
import diarsid.beam.core.modules.io.gui.geometry.MutableNamedRectangle;
import diarsid.beam.core.modules.io.gui.geometry.NamedRectangle;

/**
 *
 * @author Diarsid
 */
public interface DaoNamedRectangles extends Dao {
    
    boolean fetchDataInto(MutableNamedRectangle rectangleToFill) 
            throws DataExtractionException;
    
    boolean save(NamedRectangle rectangle)
            throws DataExtractionException;
    
//    MutableNamed2DPoint saveNew(String name, double x, double y)
//            throws DataExtractionException;
    
}
