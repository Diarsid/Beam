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
public class Cache<T extends CachedReusable> {
    
    private static final Map<Class, Cache> CACHES_BY_CACHED_CLASS;
    
    static {
        CACHES_BY_CACHED_CLASS = new HashMap<>();
    }
    
    private final Queue<T> queue;
    private final Supplier<T> tNewObjectSupplier;
    
    public Cache(Supplier<T> newTSupplier) {
        this.queue = new ArrayDeque<>();
        this.tNewObjectSupplier = newTSupplier;
    }
    
    static <T extends CachedReusable> void createCache(Class<T> type, Supplier<T> tSupplier) {
        Cache<T> existedCache = CACHES_BY_CACHED_CLASS.get(type);
        if ( existedCache == null ) {
            Cache<T> newTCache = new Cache<>(tSupplier);
            CACHES_BY_CACHED_CLASS.put(type, newTCache);
            log(Cache.class, format("Cache for %s created.", type.getCanonicalName()));
        }        
    }
    
    public static <T extends CachedReusable> T takeFromCache(Class<T> type) {
        Cache<T> cache = CACHES_BY_CACHED_CLASS.get(type);
        T cached;
        if ( cache == null ) {
            cached = initializeCacheAndGetInstanceOf(type);
        } else {
            cached = cache.give();
        }        
        return cached;
    }
    
    private static <T extends CachedReusable> T initializeCacheAndGetInstanceOf(Class<T> type) {
        Constructor noArgsConstructor = stream(type.getDeclaredConstructors())
                .filter(constructor -> constructor.getParameterCount() == 0)
                .peek(constructor -> constructor.setAccessible(true))
                .findFirst()
                .orElseThrow(() -> {
                    return new WorkflowBrokenException("Cannot found cached no-args constructor!");
                });
        
        try {
            return (T) noArgsConstructor.newInstance();
        } catch (Exception e) {
            throw new WorkflowBrokenException(
                    "Cannot initialize instance and Cache for class " + type.getCanonicalName());
        }
    }
    
    public static <T extends CachedReusable> void giveBackToCache(T cacheable) {
        if ( cacheable == null ) {
            return;
        }
        
        Cache<T> cache = CACHES_BY_CACHED_CLASS.get(cacheable.getCacheableClass());
        cache.takeBack(cacheable);
    }
    
    public static <T extends CachedReusable> void giveBackAllToCache(List<T> cacheable) {
        if ( cacheable == null || cacheable.isEmpty() ) {
            return;
        }
        
        Cache<T> cache = CACHES_BY_CACHED_CLASS.get(cacheable.get(0).getCacheableClass());
        cache.takeBackAll(cacheable);
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
