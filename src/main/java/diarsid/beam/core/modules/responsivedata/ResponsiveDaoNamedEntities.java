/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.responsivedata;


import java.util.List;
import java.util.Optional;

import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.data.DataExtractionException;
import diarsid.beam.core.domain.entities.NamedEntity;
import diarsid.beam.core.modules.data.DaoNamedEntities;

import static java.util.Collections.emptyList;

/**
 *
 * @author Diarsid
 */
public class ResponsiveDaoNamedEntities extends BeamCommonResponsiveDao<DaoNamedEntities> {

    ResponsiveDaoNamedEntities(DaoNamedEntities dao, InnerIoEngine ioEngine) {
        super(dao, ioEngine);
    }
    
    public Optional<NamedEntity> getByExactName(Initiator initiator, String exactName) {
        try {
            return super.dao().getByExactName(exactName);
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return Optional.empty();
        }
    }
    
    public List<NamedEntity> getEntitiesByNamePattern(Initiator initiator, String namePattern) {
        try {
            return super.dao().getEntitiesByNamePattern(namePattern);
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return emptyList();
        }
    }
    
    public List<NamedEntity> getAll(Initiator initiator) {
        try {
            return super.dao().getAll();
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return emptyList();
        }
    }
}
