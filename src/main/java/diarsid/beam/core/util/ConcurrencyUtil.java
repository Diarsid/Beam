/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.util;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import static java.lang.Runtime.getRuntime;
import static java.util.Optional.empty;

import static diarsid.beam.core.util.Logs.log;
import static diarsid.beam.core.util.Logs.logError;

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
        getRuntime().addShutdownHook(new Thread(() -> shutdownConcurrentRuntime()));
    }
    
    private ConcurrencyUtil() {
    }
    
    public static <T> Optional<T>  awaitGet(Callable<T> callable) {
        try {
            return Optional.of(EXECUTOR.submit(callable).get());
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
    
    private static void shutdownConcurrentRuntime() {
        EXECUTOR.shutdown();
        log(ConcurrencyUtil.class, "shutdown.");
    }
}
