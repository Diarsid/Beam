/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.domain.commands.executor;

import diarsid.beam.core.domain.commands.Argument;
import diarsid.beam.core.domain.commands.ExecutorCommand;
import diarsid.beam.core.domain.commands.OperationType;

import static diarsid.beam.core.domain.commands.OperationType.CALL_BATCH;


public class CallBatchCommand implements ExecutorCommand {
    
    private final Argument batchArgument;
    
    public CallBatchCommand(String batchName) {
        this.batchArgument = new Argument(batchName);
    }
    
    public CallBatchCommand(String batchName, String extendedBatchName) {
        this.batchArgument = new Argument(batchName, extendedBatchName);
    }

    @Override
    public OperationType getOperation() {
        return CALL_BATCH;
    }

    @Override
    public String stringifyOriginal() {
        return this.batchArgument.getOriginal();
    }

    @Override
    public String stringifyExtended() {
        return this.batchArgument.getExtended();
    }
}
