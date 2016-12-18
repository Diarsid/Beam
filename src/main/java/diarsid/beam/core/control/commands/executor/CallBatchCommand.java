/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.control.commands.executor;

import diarsid.beam.core.control.commands.CommandType;
import diarsid.beam.core.control.commands.SingleArgumentCommand;

import static diarsid.beam.core.control.commands.CommandType.CALL_BATCH;


public class CallBatchCommand extends SingleArgumentCommand {
        
    public CallBatchCommand(String batchName) {
        super(batchName);
    }
    
    public CallBatchCommand(String batchName, String extendedBatchName) {
        super(batchName, extendedBatchName);
    }

    @Override
    public CommandType getType() {
        return CALL_BATCH;
    }
}
