/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.drs.beam.external.sysconsole.modules.workers;

import com.drs.beam.external.sysconsole.modules.ConsoleDispatcherModule;
import com.drs.beam.external.sysconsole.modules.ConsoleListenerModule;
import com.drs.gem.injector.module.GemModuleBuilder;

/**
 *
 * @author Diarsid
 */
class ConsoleListenerBuilder implements GemModuleBuilder<ConsoleListenerModule>{
    
    private final ConsoleDispatcherModule dispatcher;
    
    ConsoleListenerBuilder(ConsoleDispatcherModule dispatcher) {
        this.dispatcher = dispatcher;
    }
    
    @Override
    public ConsoleListenerModule buildModule() {
        ConsoleListenerModule listener = new ConsoleListener(dispatcher);
        new Thread(listener).start();
        return listener;
    }
}