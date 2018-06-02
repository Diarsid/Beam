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
public abstract class PooledReusable {
    
    protected PooledReusable() {
        // empty constructor for creating new objects in pool
    }
    
    protected static <T extends PooledReusable> void createPoolFor(
            Class<T> type, Supplier<T> tSupplier) {
        Pool.createPool(type, tSupplier);
    }
    
    protected abstract void clearForReuse();
    
    final Class getPooleableClass() {
        return this.getClass();
    }
}
