/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.domain.keepers;

import java.util.List;
import java.util.Optional;

import diarsid.beam.core.control.io.base.Initiator;
import diarsid.beam.core.control.io.base.InnerIoEngine;
import diarsid.beam.core.control.io.commands.CreateEntityCommand;
import diarsid.beam.core.control.io.commands.EditEntityCommand;
import diarsid.beam.core.control.io.commands.FindEntityCommand;
import diarsid.beam.core.control.io.commands.RemoveEntityCommand;
import diarsid.beam.core.domain.entities.Batch;
import diarsid.beam.core.modules.data.DaoBatches;
import diarsid.beam.core.modules.domain.BatchesKeeper;
import diarsid.beam.core.modules.domain.CommandConsistencyChecker;


public class BatchesKeeperWorker implements BatchesKeeper {
    
    private final DaoBatches dao;
    private final InnerIoEngine ioEngine;
    private final CommandConsistencyChecker consistencyChecker;
    
    public BatchesKeeperWorker(
            DaoBatches daoBatches, 
            InnerIoEngine ioEngine,
            CommandConsistencyChecker consistencyChecker) {
        this.dao = daoBatches;
        this.ioEngine = ioEngine;
        this.consistencyChecker = consistencyChecker;
    }

    @Override
    public Optional<Batch> getBatch(Initiator initiator, String batchNamePattern) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Optional<Batch> findBatch(Initiator initiator, FindEntityCommand command) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<Batch> getAllBatches(Initiator initiator) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean createBatch(Initiator initiator, CreateEntityCommand command) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean editBatch(Initiator initiator, EditEntityCommand command) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean removeBatch(Initiator initiator, RemoveEntityCommand command) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
