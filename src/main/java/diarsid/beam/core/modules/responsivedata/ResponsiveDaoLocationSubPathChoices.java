/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.responsivedata;

import java.util.Optional;

import diarsid.beam.core.base.analyze.variantsweight.Variants;
import diarsid.beam.core.base.control.flow.VoidFlow;
import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.data.DataExtractionException;
import diarsid.beam.core.domain.entities.LocationSubPath;
import diarsid.beam.core.modules.data.DaoLocationSubPathChoices;

import static diarsid.beam.core.base.control.flow.Flows.voidFlowDone;

/**
 *
 * @author Diarsid
 */
public class ResponsiveDaoLocationSubPathChoices 
        extends BeamCommonResponsiveDao<DaoLocationSubPathChoices> {

    ResponsiveDaoLocationSubPathChoices(
            DaoLocationSubPathChoices dao, InnerIoEngine ioEngine) {
        super(dao, ioEngine);
    }    
    
    public boolean saveSingle(
            Initiator initiator, 
            LocationSubPath subPath, 
            String pattern) {
        try {
            return super.dao().saveSingle(subPath, pattern);
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return false;
        }
    }
    
    public boolean saveWithVariants(
            Initiator initiator, 
            LocationSubPath subPath, 
            String pattern, 
            Variants variants) {
        try {
            return super.dao().saveWithVariants(subPath, pattern, variants);
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return false;
        }
    }
    
    public boolean isChoiceExistsForSingle(
            Initiator initiator, 
            LocationSubPath subPath, 
            String pattern) {
        try {
            return super.dao().isChoiceExistsForSingle(subPath, pattern);
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return false;
        }
    }
    
    public Optional<LocationSubPath> getChoiceFor(
            Initiator initiator, 
            String pattern, 
            Variants variants) {
        try {
            return super.dao().getChoiceFor(pattern, variants);
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return Optional.empty();
        }
    }    
   
    public VoidFlow remove(
            Initiator initiator, 
            LocationSubPath subPath) {
        try {
            return super.dao().remove(subPath);
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return voidFlowDone();
        }
    }
    
}
