/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.data.daos.sql;

import java.util.List;
import java.util.Optional;

import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.domain.entities.WebDirectory;
import diarsid.beam.core.domain.entities.WebDirectoryPages;
import diarsid.beam.core.domain.entities.WebPlace;
import diarsid.beam.core.modules.data.DaoWebDirectories;
import diarsid.beam.core.modules.data.DataBase;
import diarsid.beam.core.modules.data.daos.BeamCommonDao;


class H2DaoWebDirectories 
        extends BeamCommonDao 
        implements DaoWebDirectories {

    H2DaoWebDirectories(DataBase dataBase, InnerIoEngine ioEngine) {
        super(dataBase, ioEngine);
    }   

    @Override
    public Optional<Integer> freeNameNextIndex(Initiator initiator, String name, WebPlace place) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<WebDirectoryPages> getAllDirectoriesPages(Initiator initiator) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<WebDirectoryPages> getAllDirectoriesPagesInPlace(Initiator initiator, WebPlace place) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Optional<WebDirectoryPages> getDirectoryPagesByNameInPlace(Initiator initiator, String name, WebPlace place) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Optional<WebDirectory> getDirectoryByNameInPlace(Initiator initiator, String name, WebPlace place) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<WebDirectory> findDirectoriesByPatternInPlace(Initiator initiator, String pattern, WebPlace place) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<WebDirectory> findDirectoriesByPatternInAnyPlace(Initiator initiator, String pattern) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<WebDirectory> getAllDirectories(Initiator initiator) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<WebDirectory> getAllDirectoriesInPlace(Initiator initiator, WebPlace place) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean exists(Initiator initiator, String directoryName, WebPlace place) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean updateWebDirectoryOrders(Initiator initiator, List<WebDirectory> directories) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean save(Initiator initiator, WebDirectory directory) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean remove(Initiator initiator, String name, WebPlace place) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean moveDirectoryToPlace(Initiator initiator, String name, WebPlace from, WebPlace to) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean editDirectoryName(Initiator initiator, String name, WebPlace place, String newName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
