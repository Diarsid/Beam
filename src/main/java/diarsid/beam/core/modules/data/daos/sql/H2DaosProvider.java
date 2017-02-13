/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.data.daos.sql;

import diarsid.beam.core.modules.IoModule;
import diarsid.beam.core.modules.data.DaoBatches;
import diarsid.beam.core.modules.data.DaoCommands;
import diarsid.beam.core.modules.data.DaoKeyValueStorage;
import diarsid.beam.core.modules.data.DaoLocations;
import diarsid.beam.core.modules.data.DaoNamedEntities;
import diarsid.beam.core.modules.data.DaoTasks;
import diarsid.beam.core.modules.data.DaosProvider;
import diarsid.beam.core.modules.data.DataBase;

/**
 *
 * @author Diarsid
 */
public class H2DaosProvider implements DaosProvider {
    
    private final IoModule ioModule;
    private final DataBase dataBase;
    
    public H2DaosProvider(DataBase dataBase, IoModule ioModule) {
        this.dataBase = dataBase;
        this.ioModule = ioModule;
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
        return new H2DaoNamedEntities(this.dataBase, this.ioModule.getInnerIoEngine());
    }

    @Override
    public DaoCommands createDaoCommands() {
        return new H2DaoCommands(this.dataBase, this.ioModule.getInnerIoEngine());
    }

    @Override
    public DaoKeyValueStorage createDaoKeyValueStorage() {
        return new H2DaoKeyValueStorage(this.dataBase, this.ioModule.getInnerIoEngine());
    }
    
    @Override
    public DaoTasks createDaoTasks() {
        return new H2DaoTasks(this.dataBase, this.ioModule.getInnerIoEngine());
    }
}
