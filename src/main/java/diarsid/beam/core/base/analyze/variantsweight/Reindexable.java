/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.analyze.variantsweight;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author Diarsid
 */
public interface Reindexable extends Indexable {
    
    void setIndex(int index);
    
    static void reindex(List<? extends Reindexable> reindexables) {
        AtomicInteger index = new AtomicInteger(0);
        reindexables
                .stream()
                .sorted()
                .forEach(reindexable -> reindexable.setIndex(index.getAndIncrement()));
    }
}
