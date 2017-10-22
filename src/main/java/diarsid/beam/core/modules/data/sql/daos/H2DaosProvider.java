/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.data.sql.daos;

import diarsid.beam.core.modules.ApplicationComponentsHolderModule;
import diarsid.beam.core.modules.IoModule;
import diarsid.beam.core.modules.data.DaoBatches;
import diarsid.beam.core.modules.data.DaoCommands;
import diarsid.beam.core.modules.data.DaoCommandsChoices;
import diarsid.beam.core.modules.data.DaoKeyValueStorage;
import diarsid.beam.core.modules.data.DaoLocationSubPathChoices;
import diarsid.beam.core.modules.data.DaoLocationSubPaths;
import diarsid.beam.core.modules.data.DaoLocations;
import diarsid.beam.core.modules.data.DaoNamedEntities;
import diarsid.beam.core.modules.data.DaoPictures;
import diarsid.beam.core.modules.data.DaoTasks;
import diarsid.beam.core.modules.data.DaoWebDirectories;
import diarsid.beam.core.modules.data.DaoWebPages;
import diarsid.beam.core.modules.data.DaosProvider;
import diarsid.beam.core.base.data.DataBase;

/**
 *
 * @author Diarsid
 */
public class H2DaosProvider implements DaosProvider {
    
    private final IoModule ioModule;
    private final DataBase dataBase;
    private final ApplicationComponentsHolderModule components;
    
    public H2DaosProvider(
            DataBase dataBase, IoModule ioModule, ApplicationComponentsHolderModule components) {
        this.dataBase = dataBase;
        this.ioModule = ioModule;
        this.components = components;
    }

    @Override
    public DaoLocations createDaoLocations() {
        return new H2DaoLocations(this.dataBase, this.ioModule.getInnerIoEngine());
    }

    @Override
    public DaoBatches createDaoBatches() {
        return new H2DaoBatches(this.dataBase, this.ioModule.getInnerIoEngine());
    }

    @Override
    public DaoNamedEntities createDaoNamedEntities() {
        return new H2DaoNamedEntities(
                this.dataBase, 
                this.ioModule.getInnerIoEngine(), 
                this.components.programsCatalog());
    }

    @Override
    public DaoCommands createDaoCommands() {
        return new H2DaoCommands(this.dataBase, this.ioModule.getInnerIoEngine());
    }
    
    @Override
    public DaoCommandsChoices createDaoCommandsChoices() {
        return new H2DaoCommandsChoices(this.dataBase, this.ioModule.getInnerIoEngine());
    }

    @Override
    public DaoKeyValueStorage createDaoKeyValueStorage() {
        return new H2DaoKeyValueStorage(this.dataBase, this.ioModule.getInnerIoEngine());
    }
    
    @Override
    public DaoTasks createDaoTasks() {
        return new H2DaoTasks(this.dataBase, this.ioModule.getInnerIoEngine());
    }

    @Override
    public DaoWebDirectories createDaoWebDirectories() {
        return new H2DaoWebDirectories(this.dataBase, this.ioModule.getInnerIoEngine());
    }

    @Override
    public DaoWebPages createDaoWebPages() {
        return new H2DaoWebPages(this.dataBase, this.ioModule.getInnerIoEngine());
    }
    
    @Override
    public DaoPictures createDaoImages() {
        return new H2DaoPictures(this.dataBase, this.ioModule.getInnerIoEngine());
    }
    
    @Override
    public DaoLocationSubPaths createDaoLocationSubPaths() {
        return new H2DaoLocationSubPaths(this.dataBase, this.ioModule.getInnerIoEngine());
    }

    @Override
    public DaoLocationSubPathChoices createDaoLocationSubPathChoices() {
        return new H2DaoLocationSubPathChoices(this.dataBase, this.ioModule.getInnerIoEngine());
    }    
}
