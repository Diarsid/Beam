/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.responsivedata;

import java.util.Optional;

import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.data.DataExtractionException;
import diarsid.beam.core.domain.entities.Picture;
import diarsid.beam.core.modules.data.DaoPictures;

/**
 *
 * @author Diarsid
 */
public class ResponsiveDaoPictures extends BeamCommonResponsiveDao<DaoPictures> {

    ResponsiveDaoPictures(DaoPictures dao, InnerIoEngine ioEngine) {
        super(dao, ioEngine);
    }
    
    public Optional<Picture> getByName(Initiator initiator, String name) {
        try {
            return super.dao().getByName(name);
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return Optional.empty();
        }
    }
    
    public boolean save(Initiator initiator, Picture image) {
        try {
            return super.dao().save(image);
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return false;
        }
    }
    
    public boolean removeByName(Initiator initiator, String name) {
        try {
            return super.dao().removeByName(name);
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return false;
        }
    }
    
    public boolean remove(Initiator initiator, Picture image) {
        try {
            return super.dao().remove(image);
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return false;
        }
    }
    
}
