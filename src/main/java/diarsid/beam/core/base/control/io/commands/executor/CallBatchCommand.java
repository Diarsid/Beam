/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.control.io.commands.executor;

import diarsid.beam.core.base.control.io.commands.CommandType;
import diarsid.beam.core.base.control.io.commands.ExtendableCommonCommand;

import static diarsid.beam.core.base.control.io.commands.CommandType.CALL_BATCH;


public class CallBatchCommand extends ExtendableCommonCommand {
        
    public CallBatchCommand(String batchName) {
        super(batchName);
    }
    
    public CallBatchCommand(String batchName, String extendedBatchName) {
        super(batchName, extendedBatchName);
    }

    @Override
    public CommandType type() {
        return CALL_BATCH;
    }
}
