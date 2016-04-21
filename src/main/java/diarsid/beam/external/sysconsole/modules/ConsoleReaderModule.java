/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.external.sysconsole.modules;

import java.io.IOException;

import com.drs.gem.injector.module.GemModule;

/**
 *
 * @author Diarsid
 */
public interface ConsoleReaderModule extends GemModule {
    
    String read() throws IOException;
    
    String readWithoutStopChecking() throws IOException;
    
    String readRawLine() throws IOException;
}
