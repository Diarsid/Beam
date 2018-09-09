/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.data.sql.daos;

import diarsid.beam.core.application.environment.ProgramsCatalog;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.data.DataBase;
import diarsid.beam.core.base.exceptions.WorkflowBrokenException;
import diarsid.beam.core.modules.ApplicationComponentsHolderModule;
import diarsid.beam.core.modules.IoModule;
import diarsid.beam.core.modules.data.DaoBatches;
import diarsid.beam.core.modules.data.DaoCommands;
import diarsid.beam.core.modules.data.DaoKeyValueStorage;
import diarsid.beam.core.modules.data.DaoLocationSubPathChoices;
import diarsid.beam.core.modules.data.DaoLocationSubPaths;
import diarsid.beam.core.modules.data.DaoLocations;
import diarsid.beam.core.modules.data.DaoNamedEntities;
import diarsid.beam.core.modules.data.DaoPatternChoices;
import diarsid.beam.core.modules.data.DaoPictures;
import diarsid.beam.core.modules.data.DaoSimilarityCache;
import diarsid.beam.core.modules.data.DaoTasks;
import diarsid.beam.core.modules.data.DaoWebDirectories;
import diarsid.beam.core.modules.data.DaoWebPages;
import diarsid.beam.core.modules.data.DaosProvider;

import static java.lang.String.format;

import static diarsid.beam.core.modules.data.sql.daos.DataAccessVersion.V1;
import static diarsid.beam.core.modules.data.sql.daos.DataAccessVersion.getDataAccessVersion;

/**
 *
 * @author Diarsid
 */
public class H2DaosProvider implements DaosProvider {
    
    private final IoModule ioModule;
    private final DataBase dataBase;
    private final ApplicationComponentsHolderModule components;
    private final DataAccessVersion dataAccessVersion;
    
    public H2DaosProvider(
            DataBase dataBase, IoModule ioModule, ApplicationComponentsHolderModule components) {
        this.dataBase = dataBase;
        this.ioModule = ioModule;
        this.components = components;
        this.dataAccessVersion = getDataAccessVersion(components.configuration());
    }
    
    private WorkflowBrokenException currentDataAccessVersionHasNoSupportFor(Class daoClass) {
        return new WorkflowBrokenException(
                format("%s does not have %s support", daoClass.getName(), this.dataAccessVersion));
    }

    @Override
    public DaoLocations createDaoLocations() {
        switch ( this.dataAccessVersion ) {
            case V1 : return new H2DaoLocationsV1(this.dataBase, this.ioModule.getInnerIoEngine());
            case V2 : return new H2DaoLocationsV2(this.dataBase, this.ioModule.getInnerIoEngine());
            default : {
                throw this.currentDataAccessVersionHasNoSupportFor(DaoLocations.class);
            }
        }
    }

    @Override
    public DaoBatches createDaoBatches() {
        switch ( this.dataAccessVersion ) {
            case V1 : return new H2DaoBatchesV1(this.dataBase, this.ioModule.getInnerIoEngine());
            case V2 : return new H2DaoBatchesV2(this.dataBase, this.ioModule.getInnerIoEngine());
            default : {
                throw this.currentDataAccessVersionHasNoSupportFor(DaoBatches.class);
            }
        }
    }

    @Override
    public DaoNamedEntities createDaoNamedEntities() {
        InnerIoEngine ioEngine = this.ioModule.getInnerIoEngine();
        ProgramsCatalog programsCatalog = this.components.programsCatalog();
        switch ( this.dataAccessVersion ) {
            case V1 : return new H2DaoNamedEntitiesV1(this.dataBase, ioEngine, programsCatalog);
            case V2 : return new H2DaoNamedEntitiesV2(this.dataBase, ioEngine, programsCatalog);
            default : {
                throw this.currentDataAccessVersionHasNoSupportFor(DaoNamedEntities.class);
            }
        }
    }

    @Override
    public DaoCommands createDaoCommands() {
        switch ( this.dataAccessVersion ) {
            case V1 : return new H2DaoCommandsV1(this.dataBase, this.ioModule.getInnerIoEngine());
            case V2 : return new H2DaoCommandsV2(this.dataBase, this.ioModule.getInnerIoEngine());
            default : {
                throw this.currentDataAccessVersionHasNoSupportFor(DaoCommands.class);
            }
        }        
    }
    
    @Override
    public DaoPatternChoices createDaoPatternChoices() {
        return new H2DaoPatternChoices(this.dataBase, this.ioModule.getInnerIoEngine());
    }

    @Override
    public DaoKeyValueStorage createDaoKeyValueStorage() {
        return new H2DaoKeyValueStorage(this.dataBase, this.ioModule.getInnerIoEngine());
    }
    
    @Override
    public DaoTasks createDaoTasks() {
        switch ( this.dataAccessVersion ) {
            case V1 : return new H2DaoTasksV1(this.dataBase, this.ioModule.getInnerIoEngine());
            case V2 : return new H2DaoTasksV2(this.dataBase, this.ioModule.getInnerIoEngine());
            default : {
                throw this.currentDataAccessVersionHasNoSupportFor(DaoTasks.class);
            }
        }
        
    }

    @Override
    public DaoWebDirectories createDaoWebDirectories() {
        return new H2DaoWebDirectories(this.dataBase, this.ioModule.getInnerIoEngine());
    }

    @Override
    public DaoWebPages createDaoWebPages() {
        switch ( this.dataAccessVersion ) {
            case V1 : return new H2DaoWebPagesV1(this.dataBase, this.ioModule.getInnerIoEngine());
            case V2 : return new H2DaoWebPagesV2(this.dataBase, this.ioModule.getInnerIoEngine());
            default : {
                throw this.currentDataAccessVersionHasNoSupportFor(DaoWebPages.class);
            }
        }
    }
    
    @Override
    public DaoPictures createDaoImages() {
        return new H2DaoPictures(this.dataBase, this.ioModule.getInnerIoEngine());
    }
    
    @Override
    public DaoLocationSubPaths createDaoLocationSubPaths() {
        switch ( this.dataAccessVersion ) {
            case V1 : return new H2DaoLocationSubPathsV1(
                    this.dataBase, this.ioModule.getInnerIoEngine());
            case V2 : return new H2DaoLocationSubPathsV2(
                    this.dataBase, this.ioModule.getInnerIoEngine());
            default : {
                throw this.currentDataAccessVersionHasNoSupportFor(DaoLocationSubPaths.class);
            }
        }
    }

    @Override
    public DaoLocationSubPathChoices createDaoLocationSubPathChoices() {
        return new H2DaoLocationSubPathChoices(this.dataBase, this.ioModule.getInnerIoEngine());
    }    
    
    @Override
    public DaoSimilarityCache createDaoSimilarityCache() {
        return new H2DaoSimilarityCache(this.dataBase, this.ioModule.getInnerIoEngine());
    }
}
