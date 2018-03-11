/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.objects;

import java.util.function.Supplier;

/**
 *
 * @author Diarsid
 */
public abstract class CachedReusable {
    
    protected CachedReusable() {
        // empty constructor for creating new objects in cache
    }
    
    protected static <T extends CachedReusable> void createCacheFor(
            Class<T> type, Supplier<T> tSupplier) {
        Cache.createCache(type, tSupplier);
    }
    
    protected abstract void clearForReuse();
    
    final Class getCacheableClass() {
        return this.getClass();
    }
}
