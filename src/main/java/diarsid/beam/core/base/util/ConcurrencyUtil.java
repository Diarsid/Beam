/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.util;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import diarsid.beam.core.base.control.flow.ValueFlow;

import static java.util.Optional.empty;
import static java.util.concurrent.CompletableFuture.supplyAsync;

import static diarsid.beam.core.base.control.flow.Flows.valueFlowFail;
import static diarsid.support.log.Logging.logFor;

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
        EXECUTOR.setMaximumPoolSize(EXECUTOR.getCorePoolSize());
    }
    
    private ConcurrencyUtil() {
    }
    
    public static <T> ValueFlow<T> awaitGetFlow(Callable<ValueFlow<T>> callable) {
        try {
            return EXECUTOR.submit(callable).get();
        } catch (InterruptedException | ExecutionException e) {
            logFor(ConcurrencyUtil.class).error(e.getMessage(), e);
            return valueFlowFail("waiting for value has been interrupted!");
        } 
    }
    
    public static <T> Optional<T> awaitGet(Callable<T> callable) {
        try {
            return Optional.ofNullable(EXECUTOR.submit(callable).get());
        } catch (InterruptedException | ExecutionException e) {
            logFor(ConcurrencyUtil.class).error(e.getMessage(), e);
            return empty();
        } 
    }
    
    public static <T> CompletableFuture<T> asyncGet(Supplier<T> tSupplier) {
        return supplyAsync(
                () -> tSupplier.get(), 
                EXECUTOR);
    }
    
    public static void awaitDo(Runnable runnable) {
        try {
            EXECUTOR.submit(runnable).get();
        } catch (InterruptedException | ExecutionException e) {
            logFor(ConcurrencyUtil.class).error(e.getMessage(), e);
        } 
    }
    
    public static void asyncDo(Runnable runnable) {
        EXECUTOR.submit(runnable);
    }
    
    public static void asyncDoIndependently(String threadName, Runnable runnable) {
        new Thread(runnable, threadName).start();
    }
    
    public static ScheduledFuture asyncDoPeriodically(
            String name, Runnable runnable, int period, TimeUnit timeUnit) {
        EXECUTOR.setMaximumPoolSize(EXECUTOR.getMaximumPoolSize() + 1);
        EXECUTOR.setCorePoolSize(EXECUTOR.getCorePoolSize() + 1);
        return EXECUTOR.scheduleAtFixedRate(runnable, period, period, timeUnit);
    }
}
