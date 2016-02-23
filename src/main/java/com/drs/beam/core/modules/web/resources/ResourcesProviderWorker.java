/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.drs.beam.core.modules.web.resources;

import com.drs.beam.core.modules.DataModule;
import com.drs.beam.core.modules.data.DaoWebPages;
import com.drs.beam.core.modules.web.ResourcesProvider;

/**
 *
 * @author Diarsid
 */
public class ResourcesProviderWorker implements ResourcesProvider {
    
    private final DataModule data;
    
    ResourcesProviderWorker(DataModule data) {
        this.data = data;
    }
}
