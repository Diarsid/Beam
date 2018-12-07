/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.responsivedata;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.data.DataExtractionException;
import diarsid.beam.core.domain.entities.Attribute;
import diarsid.beam.core.modules.data.DaoKeyValueStorage;

import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;

/**
 *
 * @author Diarsid
 */
public class ResponsiveDaoKeyValueStorage extends BeamCommonResponsiveDao<DaoKeyValueStorage> {

    ResponsiveDaoKeyValueStorage(DaoKeyValueStorage dao, InnerIoEngine ioEngine) {
        super(dao, ioEngine);
    }
    
    public Optional<String> get(Initiator initiator, String key) {
        try {
            return super.dao().get(key);
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return Optional.empty();
        }
    }
    
    public boolean save(Initiator initiator, String key, String value) {
        try {
            return super.dao().save(key, value);
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return false;
        }
    }
    
    public boolean delete(Initiator initiator, String key) {
        try {
            return super.dao().delete(key);
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return false;
        }
    }
    
    public Map<String, String> getAll(Initiator initiator) {
        try {
            return super.dao().getAll();
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return emptyMap();
        }
    }
    
    public Optional<Attribute> getAttribute(Initiator initiator, String key) {
        try {
            return super.dao().getAttribute(key);
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return Optional.empty();
        }
    }
    
    public boolean saveAttribute(Initiator initiator, Attribute attribute) {
        try {
            return super.dao().saveAttribute(attribute);
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return false;
        }
    }
    
    public boolean deleteAttribute(Initiator initiator, Attribute attribute) {
        try {
            return super.dao().deleteAttribute(attribute);
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return false;
        }
    }
    
    public Set<Attribute> getAllAttributes(Initiator initiator) {
        try {
            return super.dao().getAllAttributes();
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return emptySet();
        }
    }
}
