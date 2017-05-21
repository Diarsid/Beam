/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.control.io.commands.executor;

import java.util.Objects;

import diarsid.beam.core.base.control.io.commands.CommandType;
import diarsid.beam.core.domain.entities.NamedEntityType;

import static diarsid.beam.core.base.control.io.commands.CommandType.CALL_BATCH;
import static diarsid.beam.core.domain.entities.NamedEntityType.BATCH;


public class CallBatchCommand extends InvocationCommand {
        
    public CallBatchCommand(String batchName) {
        super(batchName);
    }
    
    public CallBatchCommand(
            String batchName, 
            String extendedBatchName, 
            InvocationCommandLifePhase lifePhase, 
            InvocationCommandTargetState targetState) {
        super(batchName, extendedBatchName, lifePhase, targetState);
    }

    @Override
    public CommandType type() {
        return CALL_BATCH;
    }

    @Override
    public String stringify() {
        return "call " + super.bestArgument();
    }

    @Override
    public String stringifyOriginal() {
        return "call " + super.originalArgument();
    }

    @Override
    public NamedEntityType subjectedEntityType() {
        return BATCH;
    } 

    @Override
    public boolean equals(Object obj) {
        if ( this == obj ) {
            return true;
        }
        if ( obj == null ) {
            return false;
        }
        if ( this.getClass() != obj.getClass() ) {
            return false;
        }
        return super.equals(obj); 
    }

    @Override
    public int hashCode() {
        int hash = 13;
        hash = 71 * hash + Objects.hashCode(this.subjectedEntityType());
        return super.hashCode() * hash; 
    }
}
