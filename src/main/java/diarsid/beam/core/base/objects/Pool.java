/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.objects;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Queue;
import java.util.function.Supplier;


/**
 *
 * @author Diarsid
 */
public final class Pool<T extends PooledReusable> {
    
    private final Queue<T> queue;
    private final Supplier<T> tNewObjectSupplier;
    
    Pool(Supplier<T> newTSupplier) {
        this.queue = new ArrayDeque<>();
        this.tNewObjectSupplier = newTSupplier;
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
        t.clearForReuse();
        synchronized ( this.queue ) {
            this.queue.offer(t);
        }
    }
    
    public void takeBackAll(Collection<T> ts) {
        for (T t : ts) {
            t.clearForReuse();
        }
        synchronized ( this.queue ) {
            for (T t : ts) {
                this.queue.offer(t);
            }            
        }        
        ts.clear();
    }
}
