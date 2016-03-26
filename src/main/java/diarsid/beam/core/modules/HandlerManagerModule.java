/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules;

import diarsid.beam.core.modules.handlers.LocationsHandler;
import diarsid.beam.core.modules.handlers.WebPagesHandler;

import com.drs.gem.injector.module.GemModule;

/**
 *
 * @author Diarsid
 */
public interface HandlerManagerModule extends GemModule {
    
    public LocationsHandler getLocationsHandler();
    
    public WebPagesHandler getWebPagesHandler();
}
