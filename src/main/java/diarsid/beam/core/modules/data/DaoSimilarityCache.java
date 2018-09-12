/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.data;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import diarsid.beam.core.base.analyze.similarity.SimilarityData;

/**
 *
 * @author Diarsid
 */
public interface DaoSimilarityCache {
    
    List<SimilarityData> loadAll(int algorithmVersion);
    
    Map<Long, Boolean> reassessAllHashesOlderThan(
            int algorithmVersion, BiFunction<String, String, Boolean> similarityAssessmentFunction);
    
    Map<Long, Boolean> loadAllHashesWith(int algorithmVersion);
    
    void persistAll(List<SimilarityData> data, int algorithmVersion);
    
}
