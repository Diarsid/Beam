/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.control.io.commands.executor;

import java.util.Objects;

import diarsid.beam.core.base.control.io.commands.CommandType;
import diarsid.beam.core.domain.entities.NamedEntityType;

import static diarsid.beam.core.base.control.io.commands.CommandType.RUN_PROGRAM;
import static diarsid.beam.core.domain.entities.NamedEntityType.PROGRAM;


public class RunProgramCommand extends InvocationCommand {
        
    public RunProgramCommand(String program) {
        super(program);
    }
    
    public RunProgramCommand(
            String program, 
            String extendedProgram, 
            InvocationCommandLifePhase lifePhase, 
            InvocationCommandTargetState targetState) {
        super(program, extendedProgram, lifePhase, targetState);
    }

    @Override
    public CommandType type() {
        return RUN_PROGRAM;
    }

    @Override
    public String stringify() {
        return "run " + super.bestArgument();
    }

    @Override
    public String stringifyOriginal() {
        return "run " + super.originalArgument();
    }

    @Override
    public NamedEntityType subjectedEntityType() {
        return PROGRAM;
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
