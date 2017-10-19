/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.executor;

import diarsid.beam.core.modules.DomainKeeperModule;
import diarsid.beam.core.modules.ExecutorModule;
import diarsid.beam.core.modules.IoModule;
import diarsid.beam.core.modules.PluginsLoaderModule;

import com.drs.gem.injector.module.GemModuleBuilder;

import static diarsid.beam.core.base.os.treewalking.listing.FileLister.getLister;
import static diarsid.beam.core.base.os.treewalking.search.FileSearcher.searcherWithDepthsOf;

/**
 *
 * @author Diarsid
 */
class ExecutorModuleWorkerBuilder implements GemModuleBuilder<ExecutorModule> {
    
    private final IoModule ioModule;
    private final DomainKeeperModule domainKeeperModule;
    private final PluginsLoaderModule pluginsLoaderModule;

    public ExecutorModuleWorkerBuilder(
            IoModule ioModule, 
            DomainKeeperModule domainKeeperModule, 
            PluginsLoaderModule pluginsLoaderModule) {
        this.ioModule = ioModule;
        this.domainKeeperModule = domainKeeperModule;
        this.pluginsLoaderModule = pluginsLoaderModule;
    }

    @Override
    public ExecutorModule buildModule() {
        return new ExecutorModuleWorker(
                this.ioModule.getInnerIoEngine(), 
                this.domainKeeperModule, 
                this.pluginsLoaderModule.plugins(),
                searcherWithDepthsOf(5), 
                getLister());
    }
}
