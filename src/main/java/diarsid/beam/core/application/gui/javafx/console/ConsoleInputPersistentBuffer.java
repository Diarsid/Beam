/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.application.gui.javafx.console;

import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.util.PointableCollection;
import diarsid.beam.core.modules.data.DaoKeyValueStorage;

import static java.util.concurrent.TimeUnit.MINUTES;

import static diarsid.beam.core.Beam.beamRuntime;
import static diarsid.beam.core.Beam.systemInitiator;
import static diarsid.beam.core.base.util.ConcurrencyUtil.asyncDo;
import static diarsid.beam.core.base.util.ConcurrencyUtil.asyncDoPeriodically;
import static diarsid.beam.core.base.util.PointableCollection.parseListFromStringifiedPointableCollection;

/**
 *
 * @author Diarsid
 */
class ConsoleInputPersistentBuffer {
    
    private final static String PERSIST_BUFFER = "persist_buffer";
    
    private final Initiator initiator;
    private final String key;
    private final DaoKeyValueStorage daoKeyValueStorage;
    private final PointableCollection<String> buffer;
    private final Object stateChangeLock;
    private int inputCounter;

    public ConsoleInputPersistentBuffer(int capacity, DaoKeyValueStorage daoKeyValueStorage) {
        this.initiator = systemInitiator();
        this.key = "console_input_buffer_state";
        this.daoKeyValueStorage = daoKeyValueStorage;
        this.buffer = new PointableCollection<>(capacity, ""); 
        this.stateChangeLock = new Object();
        this.inputCounter = 0;
        this.fillBufferWithPersistedData();
        asyncDoPeriodically(PERSIST_BUFFER, () -> this.persist(), 5, MINUTES);
        beamRuntime().doBeforeExit(() -> this.persist());
    }
    
    private void fillBufferWithPersistedData() {
        this.daoKeyValueStorage.get(this.key).ifPresent(value -> {
            this.buffer.addAll(parseListFromStringifiedPointableCollection(value));
        });
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
                this.daoKeyValueStorage.save(this.key, this.buffer.stringify());
                this.inputCounter = 0;
            }            
        }        
    }
    
    void close() {
        this.persist();
    }
}
