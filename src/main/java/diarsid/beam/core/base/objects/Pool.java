/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.objects;

import java.lang.reflect.Constructor;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.function.Supplier;

import diarsid.beam.core.base.exceptions.WorkflowBrokenException;

import static java.lang.String.format;
import static java.util.Arrays.stream;

import static diarsid.beam.core.base.util.Logs.log;

/**
 *
 * @author Diarsid
 */
public class Pool<T extends PooledReusable> {
    
    private static final Map<Class, Pool> POOLS_BY_POOLED_CLASS;
    
    static {
        POOLS_BY_POOLED_CLASS = new HashMap<>();
    }
    
    private final Queue<T> queue;
    private final Supplier<T> tNewObjectSupplier;
    
    private Pool(Supplier<T> newTSupplier) {
        this.queue = new ArrayDeque<>();
        this.tNewObjectSupplier = newTSupplier;
    }
    
    static <T extends PooledReusable> void createPool(Class<T> type, Supplier<T> tSupplier) {
        synchronized ( POOLS_BY_POOLED_CLASS ) {
            Pool<T> existedPool = POOLS_BY_POOLED_CLASS.get(type);
            if ( existedPool == null ) {
                Pool<T> newTPool = new Pool<>(tSupplier);
                POOLS_BY_POOLED_CLASS.put(type, newTPool);
                log(Pool.class, format("Pool for %s created.", type.getCanonicalName()));
            }       
        } 
    }
    
    public static <T extends PooledReusable> Optional<Pool<T>> poolOf(Class<T> type) {
        return Optional.ofNullable(POOLS_BY_POOLED_CLASS.get(type));
    }
    
    public static <T extends PooledReusable> T takeFromPool(Class<T> type) {
        Pool<T> pool = POOLS_BY_POOLED_CLASS.get(type);
        T pooled;
        if ( pool == null ) {
            pooled = initializePoolAndGetInstanceOf(type);
        } else {
            pooled = pool.give();
        }        
        return pooled;
    }
    
    private static <T extends PooledReusable> T initializePoolAndGetInstanceOf(Class<T> type) {
        Constructor noArgsConstructor = stream(type.getDeclaredConstructors())
                .filter(constructor -> constructor.getParameterCount() == 0)
                .peek(constructor -> constructor.setAccessible(true))
                .findFirst()
                .orElseThrow(() -> {
                    return new WorkflowBrokenException("Cannot found pooled no-args constructor!");
                });
        
        try {
            return (T) noArgsConstructor.newInstance();
        } catch (Exception e) {
            throw new WorkflowBrokenException(
                    "Cannot initialize instance and Pool for class " + type.getCanonicalName());
        }
    }
    
    public static <T extends PooledReusable> void giveBackToPool(T pooleable) {
        if ( pooleable == null ) {
            return;
        }
        
        Pool<T> cache = POOLS_BY_POOLED_CLASS.get(pooleable.getPooleableClass());
        cache.takeBack(pooleable);
    }
    
    public static <T extends PooledReusable> void giveBackAllToPool(List<T> pooleable) {
        if ( pooleable == null || pooleable.isEmpty() ) {
            return;
        }
        
        Pool<T> pool = POOLS_BY_POOLED_CLASS.get(pooleable.get(0).getPooleableClass());
        pool.takeBackAll(pooleable);
    }
    
    public T give() {
        synchronized ( this.queue ) {
            if ( this.queue.peek() == null ) {
                T newT = this.tNewObjectSupplier.get();
                return newT;
            } else {
                return this.queue.poll();
            }
        }
    }
    
    public void takeBack(T t) {
        synchronized ( this.queue ) {
            t.clearForReuse();
            this.queue.offer(t);
        }
    }
    
    public void takeBackAll(Collection<T> ts) {
        synchronized ( this.queue ) {
            for (T t : ts) {
                t.clearForReuse();
                this.queue.offer(t);
            }            
        }        
        ts.clear();
    }
}
