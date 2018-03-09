/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.objects;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.function.Supplier;

/**
 *
 * @author Diarsid
 */
public class SyncCache<T extends Clearable> {
    
    private final Queue<T> queue;
    private final Supplier<T> newTSupplier;
    
    public SyncCache(Supplier<T> newTSupplier) {
        this.queue = new ArrayDeque<>();
        this.newTSupplier = newTSupplier;
    }
    
    public T get() {
        synchronized ( this.queue ) {
            if ( this.queue.peek() == null ) {
                T newT = this.newTSupplier.get();
                return newT;
            } else {
                return this.queue.poll();
            }
        }
    }
    
    public void back(T t) {
        synchronized ( this.queue ) {
            t.clear();
            this.queue.offer(t);
        }
    }
}
