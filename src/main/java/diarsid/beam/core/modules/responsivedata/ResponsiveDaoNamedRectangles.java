/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.responsivedata;

import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.data.DataExtractionException;
import diarsid.beam.core.modules.data.DaoNamedRectangles;
import diarsid.beam.core.modules.io.gui.geometry.MutableNamedRectangle;

/**
 *
 * @author Diarsid
 */
public class ResponsiveDaoNamedRectangles 
        extends BeamCommonResponsiveDao<DaoNamedRectangles> {

    public ResponsiveDaoNamedRectangles(
            DaoNamedRectangles dao, InnerIoEngine ioEngine) {
        super(dao, ioEngine);
    }    
    
    public boolean fetchDataAndFillIfPresent(
            Initiator initiator, MutableNamedRectangle rectangleToFill) {
        try {
            return super.dao().fetchDataInto(rectangleToFill);
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return false;
        }
    }
    
}
