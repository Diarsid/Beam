/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.responsivedata;


import java.util.List;

import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.data.DataExtractionException;
import diarsid.beam.core.domain.entities.LocationSubPath;
import diarsid.beam.core.modules.data.DaoLocationSubPaths;

import static java.util.Collections.emptyList;

/**
 *
 * @author Diarsid
 */
public class ResponsiveDaoLocationSubPaths extends BeamCommonResponsiveDao<DaoLocationSubPaths> {

    ResponsiveDaoLocationSubPaths(DaoLocationSubPaths dao, InnerIoEngine ioEngine) {
        super(dao, ioEngine);
    }
    
    public List<LocationSubPath> getSubPathesByPattern(Initiator initiator, String pattern) {
        try {
            return super.dao().getSubPathesByPattern(pattern);
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return emptyList();
        }
    }
}
