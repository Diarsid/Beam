/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.responsivedata;

import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.data.DataExtractionException;
import diarsid.beam.core.modules.data.Dao;

/**
 *
 * @author Diarsid
 */
public abstract class BeamCommonResponsiveDao <DAO extends Dao> {
    
    private final InnerIoEngine ioEngine;
    private final DAO dao;
    
    public BeamCommonResponsiveDao(DAO dao, InnerIoEngine ioEngine) {
        this.ioEngine = ioEngine;
        this.dao = dao;
    }
    
    protected DAO dao() {
        return this.dao;
    }
    
    protected InnerIoEngine ioEngine() {
        return this.ioEngine;
    }
    
    protected final void responseOn(Initiator initiator, DataExtractionException e) {        
        if ( e.isLogical() ) {
            this.ioEngine.report(initiator, e.getMessage());
        } else {
            this.ioEngine.report(initiator, e.getMessage());
        }
    }
}
