/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.util;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import diarsid.beam.core.base.control.flow.ValueFlow;

import static java.lang.Runtime.getRuntime;
import static java.util.Optional.empty;

import static diarsid.beam.core.base.control.flow.Flows.valueFlowFail;
import static diarsid.beam.core.base.util.Logs.logError;

/**
 *
 * @author Diarsid
 */
public class ConcurrencyUtil {
    
    private static final ScheduledThreadPoolExecutor EXECUTOR;
    static {
        EXECUTOR = new ScheduledThreadPoolExecutor(10);
        EXECUTOR.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
        EXECUTOR.setRemoveOnCancelPolicy(false);
        getRuntime().addShutdownHook(new Thread(() -> EXECUTOR.shutdown()));
    }
    
    private ConcurrencyUtil() {
    }
    
    public static <T> ValueFlow<T> awaitGetValue(Callable<ValueFlow<T>> callable) {
        try {
            return EXECUTOR.submit(callable).get();
        } catch (InterruptedException | ExecutionException e) {
            logError(ConcurrencyUtil.class, e);
            return valueFlowFail("waiting for value has been interrupted!");
        } 
    }
    
    public static <T> Optional<T> awaitGet(Callable<T> callable) {
        try {
            return Optional.ofNullable(EXECUTOR.submit(callable).get());
        } catch (InterruptedException | ExecutionException e) {
            logError(ConcurrencyUtil.class, e);
            return empty();
        } 
    }
    
    public static void awaitDo(Runnable runnable) {
        try {
            EXECUTOR.submit(runnable).get();
        } catch (InterruptedException | ExecutionException e) {
             logError(ConcurrencyUtil.class, e);
        } 
    }
    
    public static void asyncDo(Runnable runnable) {
        EXECUTOR.submit(runnable);
    }
    
    public static void asyncDoIndependently(String threadName, Runnable runnable) {
        new Thread(runnable, threadName).start();
    }
}
