/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.objects;

import java.util.UUID;
import java.util.function.Supplier;

import static java.util.UUID.randomUUID;

import static diarsid.beam.core.base.objects.Pool.giveBackToPool;

/**
 *
 * @author Diarsid
 */
public abstract class PooledReusable implements AutoCloseable {
    
    private final UUID uuid;
    
    protected PooledReusable() {
        // empty constructor for creating new objects in pool
        this.uuid = randomUUID();
    }
    
    protected static <T extends PooledReusable> void createPoolFor(
            Class<T> type, Supplier<T> tSupplier) {
        Pool.createPool(type, tSupplier);
    }
    
    protected abstract void clearForReuse();
    
    final Class getPooleableClass() {
        return this.getClass();
    }

    @Override
    public void close() {
        giveBackToPool(this);
    }
    
    public final UUID identityUuid() {
        return this.uuid;
    }
    
    public final boolean isSame(PooledReusable other) {
        return this.uuid.equals(other.uuid);
    }
}
