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
import diarsid.beam.core.domain.entities.WebPage;
import diarsid.beam.core.modules.data.DaoWebPages;
import diarsid.beam.core.modules.data.DataBase;
import diarsid.beam.core.modules.data.daos.BeamCommonDao;


class H2DaoWebPages 
        extends BeamCommonDao 
        implements DaoWebPages {

    H2DaoWebPages(DataBase dataBase, InnerIoEngine ioEngine) {
        super(dataBase, ioEngine);
    }

    @Override
    public Optional<Integer> freeNameNextIndex(Initiator initiator, String name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Optional<WebPage> getByName(Initiator initiator, String name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<WebPage> findByPattern(Initiator initiator, String pattern) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<WebPage> allFromDirectory(Initiator initiator, int directoryId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean save(Initiator initiator, WebPage page) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean remove(Initiator initiator, String name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean editName(Initiator initiator, String oldName, String newName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean editShortcuts(Initiator initiator, String name, String newShortcuts) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean editUrl(Initiator initiator, String name, String newUrl) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean movePageFromDirToDir(Initiator initiator, String pageName, int oldDirId, int newDirId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean updatePageOrdersInDir(Initiator initiator, List<WebPage> pages) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
