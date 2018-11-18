/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.data;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import diarsid.beam.core.base.analyze.cache.PersistableCacheData;

/**
 *
 * @author Diarsid
 */
public interface DaoPersistableCacheData<T> {
    
    List<PersistableCacheData<T>> loadAll(int algorithmVersion);
    
    Map<Long, T> reassessAllHashesOlderThan(
            int algorithmVersion, BiFunction<String, String, T> similarityAssessmentFunction);
    
    Map<Long, T> loadAllHashesWith(int algorithmVersion);
    
    void persistAll(List<PersistableCacheData<T>> data, int algorithmVersion);
    
}
