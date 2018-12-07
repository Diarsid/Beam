/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.io.gui.javafx.console;

import diarsid.beam.core.modules.io.gui.javafx.GuiJavaFX;
import diarsid.beam.core.base.data.DataExtractionException;
import diarsid.beam.core.base.util.PointableCollection;
import diarsid.beam.core.modules.data.DaoKeyValueStorage;

import static java.util.concurrent.TimeUnit.MINUTES;

import static diarsid.beam.core.Beam.beamRuntime;
import static diarsid.beam.core.base.control.io.base.interaction.Messages.info;
import static diarsid.beam.core.base.util.ConcurrencyUtil.asyncDo;
import static diarsid.beam.core.base.util.ConcurrencyUtil.asyncDoPeriodically;
import static diarsid.beam.core.base.util.PointableCollection.parseListFromStringifiedPointableCollection;

/**
 *
 * @author Diarsid
 */
class ConsoleInputPersistentBuffer {
    
    private final static String PERSIST_BUFFER = "persist_buffer";
    
    private final String key;
    private final GuiJavaFX gui;
    private final DaoKeyValueStorage daoKeyValueStorage;
    private final PointableCollection<String> buffer;
    private final Object stateChangeLock;
    private final ConsoleInputBufferFilter filter;
    private int inputCounter;

    public ConsoleInputPersistentBuffer(
            int capacity, 
            DaoKeyValueStorage daoKeyValueStorage, 
            ConsoleInputBufferFilter filter,
            GuiJavaFX gui) {
        this.key = "console_input_buffer_state";
        this.gui = gui;
        this.daoKeyValueStorage = daoKeyValueStorage;
        this.buffer = new PointableCollection<>(capacity, ""); 
        this.stateChangeLock = new Object();
        this.filter = filter;
        this.inputCounter = 0;
        
        this.fillBufferWithPersistedData();
        asyncDoPeriodically(PERSIST_BUFFER, () -> this.persist(), 5, MINUTES);
        beamRuntime().doBeforeExit(() -> this.persist());
    }
    
    private void fillBufferWithPersistedData() {
        try {
            this.daoKeyValueStorage.get(this.key).ifPresent(value -> {
                this.buffer.addAll(parseListFromStringifiedPointableCollection(value));
            });
        } catch (DataExtractionException e) {
            this.gui.show(info(e.getMessage()));
        }    
    }
    
    void add(String input) {
        synchronized ( this.stateChangeLock ) {
            this.buffer.add(input);
            this.inputCounter++;
            if ( this.inputCounter >= this.buffer.capacity() ) {
                asyncDo(() -> this.persist());
            }
        }        
    }
    
    String toLastAndGet() {
        return this.buffer.toLastAndGet();
    }
    
    String toFirstAndGet() {
        return this.buffer.toFirstAndGet();
    }
    
    private void persist() {
        synchronized ( this.stateChangeLock ) {
            if ( this.inputCounter > 0 ) {
                try {
                    this.daoKeyValueStorage.save(this.key, this.buffer.stringify());
                    this.inputCounter = 0;
                } catch (DataExtractionException e) {
                    this.gui.show(info(e.getMessage()));
                }    
            }            
        }        
    }
    
    void close() {
        this.persist();
    }
}
