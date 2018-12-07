/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.responsivedata;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import diarsid.beam.core.base.analyze.cache.PersistableCacheData;
import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.data.DataExtractionException;
import diarsid.beam.core.modules.data.DaoPersistableCacheData;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;

/**
 *
 * @author Diarsid
 */
public class ResponsiveDaoPersistableCacheData<T> 
        extends BeamCommonResponsiveDao<DaoPersistableCacheData<T>> {

    ResponsiveDaoPersistableCacheData(DaoPersistableCacheData<T> dao, InnerIoEngine ioEngine) {
        super(dao, ioEngine);
    }
    
    public List<PersistableCacheData<T>> loadAll(
            Initiator initiator, int algorithmVersion) {
        try {
            return super.dao().loadAll(algorithmVersion);
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return emptyList();
        }
    }
    
    public Map<Long, T> reassessAllHashesOlderThan(
            Initiator initiator, 
            int algorithmVersion, 
            BiFunction<String, String, T> similarityAssessmentFunction) {
        try {
            return super.dao().reassessAllHashesOlderThan(algorithmVersion, similarityAssessmentFunction);
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return emptyMap();
        }
    }
    
    public Map<Long, T> loadAllHashesWith(
            Initiator initiator, int algorithmVersion) {
        try {
            return super.dao().loadAllHashesWith(algorithmVersion);
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return emptyMap();
        }
    }
    
    public void persistAll(
            Initiator initiator, List<PersistableCacheData<T>> data, int algorithmVersion) {
        try {
            super.dao().persistAll(data, algorithmVersion);
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);            
        }
    }
    
}
