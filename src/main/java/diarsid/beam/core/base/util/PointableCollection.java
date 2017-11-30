/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.util;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Diarsid
 */
public class PointableCollection <T> {
    
    private final int capacity;
    private final T defaultIfEmpty;
    private final List<T> list;
    private int pointer;
    
    public PointableCollection(int capacity, T defaultIfEmpty) {
        this.capacity = capacity;
        this.defaultIfEmpty = defaultIfEmpty;
        this.list = new ArrayList<>();
        this.pointer = -1;
    }
    
    public boolean contains(T t) {
        return this.list.contains(t);
    }
    
    public int size() {
        return this.list.size();
    }
    
    public T toLastAndGet() {
        synchronized ( this.list ) {
            if ( this.list.isEmpty() ) {
                return this.defaultIfEmpty;
            } else {
                if ( this.pointer < 0 ) {
                    this.pointer = 0;
                    return this.list.get(this.pointer);
                } else {
                    this.pointer++;
                    if ( this.pointer >= this.list.size() ) {
                        this.pointer = this.list.size() - 1;
                    }
                    return this.list.get(this.pointer);
                } 
            }
        }
    }
    
    public T toFirstAndGet() {
        synchronized ( this.list ) {
            if ( this.list.isEmpty() ) {
                return this.defaultIfEmpty;
            } else {  
                if ( this.pointer < 0 ) {
                    this.pointer = 0;
                    return this.list.get(this.pointer);
                } else {
                    this.pointer--;
                    if ( this.pointer < 0 ) {
                        this.pointer = 0;
                    }
                    return this.list.get(this.pointer);
                } 
            }
        }
    }
    
    public void add(T t) {
        synchronized ( this.list ) {
            if ( this.list.contains(t) ) {
                this.list.remove(t);
            } else {
                if ( this.list.size() == this.capacity ) {
                    this.list.remove(this.list.size() - 1);
                }                
            }
            this.list.add(0, t);
            this.pointer = -1;            
        }
    }
}
