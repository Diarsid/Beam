/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.drs.beam.core.modules;

import com.drs.beam.util.config.ConfigParam;
import com.drs.gem.injector.module.GemModule;

/**
 *
 * @author Diarsid
 */
public interface ConfigModule extends GemModule {
    
    void parseStartArgumentsIntoConfiguration(String[] args);
    
    String getParameter(ConfigParam param);
}
