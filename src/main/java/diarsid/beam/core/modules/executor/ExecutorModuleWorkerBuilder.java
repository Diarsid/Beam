/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.executor;

import diarsid.beam.core.base.os.treewalking.advanced.Walker;
import diarsid.beam.core.modules.BeamEnvironmentModule;
import diarsid.beam.core.modules.DomainKeeperModule;
import diarsid.beam.core.modules.ExecutorModule;
import diarsid.beam.core.modules.IoModule;
import diarsid.beam.core.modules.PluginsLoaderModule;
import diarsid.beam.core.modules.ResponsiveDataModule;

import com.drs.gem.injector.module.GemModuleBuilder;

import static diarsid.beam.core.base.os.treewalking.advanced.Walker.newWalker;
import static diarsid.beam.core.base.os.treewalking.base.FolderTypeDetector.getFolderTypeDetector;
import static diarsid.beam.core.base.os.treewalking.listing.FileLister.getLister;
import static diarsid.beam.core.base.os.treewalking.search.FileSearcher.searcherWithDepthsOf;
import static diarsid.support.objects.Pools.pools;

/**
 *
 * @author Diarsid
 */
class ExecutorModuleWorkerBuilder implements GemModuleBuilder<ExecutorModule> {
    
    private final BeamEnvironmentModule beamEnvironmentModule;
    private final IoModule ioModule;
    private final ResponsiveDataModule dataModule;
    private final DomainKeeperModule domainKeeperModule;
    private final PluginsLoaderModule pluginsLoaderModule;

    public ExecutorModuleWorkerBuilder(
            BeamEnvironmentModule beamEnvironmentModule,
            IoModule ioModule, 
            ResponsiveDataModule dataModule,
            DomainKeeperModule domainKeeperModule, 
            PluginsLoaderModule pluginsLoaderModule) {
        this.beamEnvironmentModule = beamEnvironmentModule;
        this.ioModule = ioModule;
        this.dataModule = dataModule;
        this.domainKeeperModule = domainKeeperModule;
        this.pluginsLoaderModule = pluginsLoaderModule;
    }

    @Override
    public ExecutorModule buildModule() {            
        Walker walker = newWalker(
                this.ioModule.getInnerIoEngine(), 
                this.dataModule.patternChoices(), 
                getFolderTypeDetector(), 
                this.beamEnvironmentModule.analyze(), 
                this.beamEnvironmentModule.similarity(),
                pools());
        return new ExecutorModuleWorker(
                this.ioModule.getInnerIoEngine(), 
                this.domainKeeperModule, 
                this.pluginsLoaderModule.plugins(),
                searcherWithDepthsOf(5, this.beamEnvironmentModule.similarity()), 
                walker,
                getLister());
    }
}
