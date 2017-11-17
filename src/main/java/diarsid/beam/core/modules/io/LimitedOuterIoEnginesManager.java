/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.io;

import java.io.IOException;
import java.util.SortedSet;
import java.util.TreeSet;

import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.OuterIoEngine;
import diarsid.beam.core.base.exceptions.RequirementException;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Objects.nonNull;

import static diarsid.beam.core.base.util.IntUtil.adjustBetween;
import static diarsid.beam.core.base.util.Logs.log;
import static diarsid.beam.core.base.util.Logs.logError;
import static diarsid.beam.core.base.util.Requirements.requireNotNull;
import static diarsid.beam.core.base.util.Requirements.requireNull;

/**
 *
 * @author Diarsid
 */
class LimitedOuterIoEnginesManager implements OuterIoEnginesManager {
    
    private static final int ENGINES_SLOTS_QTY;    
    private static final SortedSet<Integer> FREE_ENGINES_SLOTS;  
    private static final Object ENGINES_LOCK;
    static {
        FREE_ENGINES_SLOTS = new TreeSet<>(asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
        ENGINES_SLOTS_QTY = FREE_ENGINES_SLOTS.size();
        ENGINES_LOCK = new Object();
    }
        
    private static OuterIoEngine engine1;
    private static OuterIoEngine engine2;
    private static OuterIoEngine engine3;
    private static OuterIoEngine engine4;
    private static OuterIoEngine engine5;
    private static OuterIoEngine engine6;
    private static OuterIoEngine engine7;
    private static OuterIoEngine engine8;
    private static OuterIoEngine engine9;
    private static OuterIoEngine engine10;
    
    LimitedOuterIoEnginesManager() {
    }
    
    private static OuterIoEngine getEngineByNumber(int engineNumber) {
        engineNumber = adjustBetween(engineNumber, 1, ENGINES_SLOTS_QTY);
        switch ( engineNumber ) {
            case 1 : {
                return engine1;
            }
            case 2 : {
                return engine2;
            }
            case 3 : {
                return engine3;
            }
            case 4 : {
                return engine4;
            }
            case 5 : {
                return engine5;
            }
            case 6 : {
                return engine6;
            }
            case 7 : {
                return engine7;
            }
            case 8 : {
                return engine8;
            }
            case 9 : {
                return engine9;
            }
            case 10 : {
                return engine10;
            }
            default : {
                return null;
            }
        }
    }
    
    private static void setEngineByNumber(int engineNumber, OuterIoEngine engine) {
        engineNumber = adjustBetween(engineNumber, 1, ENGINES_SLOTS_QTY);
        switch ( engineNumber ) {
            case 1 : {
                requireNull(engine1, format("slot %d is not free!", engineNumber));
                engine1 = engine;
                break;
            }
            case 2 : {
                requireNull(engine2, format("slot %d is not free!", engineNumber));
                engine2 = engine;
                break;
            }
            case 3 : {
                requireNull(engine3, format("slot %d is not free!", engineNumber));
                engine3 = engine;
                break;
            }
            case 4 : {
                requireNull(engine4, format("slot %d is not free!", engineNumber));
                engine4 = engine;
                break;
            }
            case 5 : {
                requireNull(engine5, format("slot %d is not free!", engineNumber));
                engine5 = engine;
                break;
            }
            case 6 : {
                requireNull(engine6, format("slot %d is not free!", engineNumber));
                engine6 = engine;
                break;
            }
            case 7 : {
                requireNull(engine7, format("slot %d is not free!", engineNumber));
                engine7 = engine;
                break;
            }
            case 8 : {
                requireNull(engine8, format("slot %d is not free!", engineNumber));
                engine8 = engine;
                break;
            }
            case 9 : {
                requireNull(engine9, format("slot %d is not free!", engineNumber));
                engine9 = engine;
                break;
            }
            case 10 : {
                requireNull(engine10, format("slot %d is not free!", engineNumber));
                engine10 = engine;
                break;
            }
            default : {
                // 
            }
        }
    }
    
//    private static void closeAndNullifyAllEngines() {
//        if ( nonNull(engine1) ) {
//            engine1.close();
//        }
//        engine1 = null;
//        if ( nonNull(engine2) ) {
//            engine1.close();
//        }
//        engine2 = null;
//        if ( nonNull(engine3) ) {
//            engine1.close();
//        }
//        engine3 = null;
//        if ( nonNull(engine4) ) {
//            engine1.close();
//        }
//        engine4 = null;
//        if ( nonNull(engine5) ) {
//            engine1.close();
//        }
//        engine5 = null;
//        if ( nonNull(engine6) ) {
//            engine1.close();
//        }
//        engine6 = null;
//        if ( nonNull(engine7) ) {
//            engine1.close();
//        }
//        engine7 = null;
//        if ( nonNull(engine8) ) {
//            engine1.close();
//        }
//        engine8 = null;
//        if ( nonNull(engine9) ) {
//            engine1.close();
//        }
//        engine9 = null;
//        if ( nonNull(engine10) ) {
//            engine1.close();
//        }
//        engine10 = null;
//    }
    
    private static void nullifyEngineByNumber(int engineNumber) {
        engineNumber = adjustBetween(engineNumber, 1, ENGINES_SLOTS_QTY);
        switch ( engineNumber ) {
            case 1 : {
                requireNotNull(engine1, format("slot %d was free!", engineNumber));
                engine1 = null;
                break;
            }
            case 2 : {
                requireNotNull(engine2, format("slot %d was free!", engineNumber));
                engine2 = null;
                break;
            }
            case 3 : {
                requireNotNull(engine3, format("slot %d was free!", engineNumber));
                engine3 = null;
                break;
            }
            case 4 : {
                requireNotNull(engine4, format("slot %d was free!", engineNumber));
                engine4 = null;
                break;
            }
            case 5 : {
                requireNotNull(engine5, format("slot %d was free!", engineNumber));
                engine5 = null;
                break;
            }
            case 6 : {
                requireNotNull(engine6, format("slot %d was free!", engineNumber));
                engine6 = null;
                break;
            }
            case 7 : {
                requireNotNull(engine7, format("slot %d was free!", engineNumber));
                engine7 = null;
                break;
            }
            case 8 : {
                requireNotNull(engine8, format("slot %d was free!", engineNumber));
                engine8 = null;
                break;
            }
            case 9 : {
                requireNotNull(engine9, format("slot %d was free!", engineNumber));
                engine9 = null;
                break;
            }
            case 10 : {
                requireNotNull(engine10, format("slot %d was free!", engineNumber));
                engine10 = null;
                break;
            }
            default : {
                //
            }
        }
    }
    
    private static boolean hasFreeSlots() {
        return FREE_ENGINES_SLOTS.size() > 0;
    } 
    
    boolean hasSlots() {
        synchronized ( ENGINES_LOCK ) {
            return hasFreeSlots();
        }
    }
    
    int addEngine(OuterIoEngine engine) {
        synchronized ( ENGINES_LOCK ) {
            if ( hasFreeSlots() ) {
                int slotNumber = FREE_ENGINES_SLOTS.first();
                FREE_ENGINES_SLOTS.remove(slotNumber);
                try {
                    setEngineByNumber(slotNumber, engine);
                    return slotNumber;
                } catch (RequirementException e) {
                    logError(this.getClass(), e);
                    return -1;
                }                
            } else {
                log(this.getClass(), "there are no free slots.");
                return -1;
            }
        }        
    }
    
    @Override
    public boolean closeAndRemoveEngineBy(Initiator initiator) throws IOException {
        int engineNumber = adjustBetween(initiator.engineNumber(), 1, ENGINES_SLOTS_QTY);
        getEngineByNumber(engineNumber).close();
        synchronized ( ENGINES_LOCK ) {
            try {
                nullifyEngineByNumber(engineNumber);
                FREE_ENGINES_SLOTS.add(engineNumber);
                return true;
            } catch (RequirementException e) {
                logError(this.getClass(), e);
                return false;
            }
        }
    }
    
    @Override
    public OuterIoEngine getEngineBy(Initiator initiator) {
        synchronized ( ENGINES_LOCK ) {
            return getEngineByNumber(initiator.engineNumber());
        }        
    }
    
    @Override
    public boolean hasEngineBy(Initiator initiator) {
        synchronized ( ENGINES_LOCK ) {
            return nonNull(getEngineByNumber(initiator.engineNumber()));
        }
    }
}
