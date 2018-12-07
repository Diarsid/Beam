/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.io.gui.javafx;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import diarsid.beam.core.modules.io.gui.geometry.MutableNamedRectangle;
import diarsid.beam.core.modules.io.gui.geometry.RectangleAnchor;
import diarsid.beam.core.modules.io.gui.geometry.RectangleSize;

import static java.util.UUID.randomUUID;

import static diarsid.beam.core.base.util.ConcurrencyUtil.asyncDo;

/**
 *
 * @author Diarsid
 */
public class WindowPersistableFrame {
    
    private final UUID uuid;
    private final MutableNamedRectangle rectangle;
    private final AtomicBoolean isPersistent;
    private final Function<MutableNamedRectangle, Boolean> persistRectangleCall;

    public WindowPersistableFrame(
            MutableNamedRectangle rectangle, 
            boolean persistent, 
            Function<MutableNamedRectangle, Boolean> persistRectangleCall) {
        this.uuid = randomUUID();
        this.rectangle = rectangle;
        boolean anchorPresent = this.rectangle.anchor().isNotEmpty();
        boolean sizePresent = this.rectangle.size().isNotEmpty();
        this.isPersistent = new AtomicBoolean(persistent && anchorPresent && sizePresent); 
        this.persistRectangleCall = persistRectangleCall;
    }

    private void persist() {
        boolean persisted = this.persistRectangleCall.apply(this.rectangle);
        this.isPersistent.set(persisted);
    }
        
    public void setAnchor(double x, double y) {        
        asyncDo(() -> {
            synchronized ( this.uuid ) {       
                this.rectangle.anchor().set(x, y);
                if ( this.rectangle.isNotEmpty() ) {
                    this.persist();
                }                
            }
        });
    }
    
    public void setSize(double width, double height) {        
        asyncDo(() -> {
            synchronized ( this.uuid ) { 
                this.rectangle.size().set(width, height);
                if ( this.rectangle.isNotEmpty() ) {
                    this.persist();
                }
            }
        });
    }
    
    public void setMinSize(double width, double height) {
        
    }
    
    public boolean isPersistent() {
        return this.isPersistent.get();
    }
    
    public boolean isTransient() {
        return ! this.isPersistent.get();
    }
    
    public RectangleAnchor anchor() {
        return this.rectangle.anchor();
    }
    
    public RectangleSize size() {
        return this.rectangle.size();
    }
    
//    public NamedRectangle rectangle() {
//        return this.rectangle.asImmutable();
//    }
//    
//    public MutableNamedRectangle mutableRectangle() {
//        return this.rectangle;
//    }
    
}
