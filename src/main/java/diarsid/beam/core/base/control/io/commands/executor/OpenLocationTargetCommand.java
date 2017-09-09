/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.control.io.commands.executor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import diarsid.beam.core.base.control.io.commands.CommandType;
import diarsid.beam.core.domain.entities.NamedEntityType;

import static diarsid.beam.core.base.control.io.commands.CommandType.OPEN_LOCATION;
import static diarsid.beam.core.base.control.io.commands.CommandType.OPEN_LOCATION_TARGET;
import static diarsid.beam.core.base.control.io.commands.Commands.createInvocationCommandFrom;
import static diarsid.beam.core.base.util.PathUtils.decomposePath;
import static diarsid.beam.core.base.util.PathUtils.extractLocationFromPath;
import static diarsid.beam.core.base.util.PathUtils.extractTargetFromPath;
import static diarsid.beam.core.domain.entities.NamedEntityType.LOCATION;


public class OpenLocationTargetCommand extends InvocationCommand {
    
    public OpenLocationTargetCommand(String originalTarget) {
        super(originalTarget);
    }
    
    public OpenLocationTargetCommand(
            String originalTarget, 
            String extendedTarget, 
            InvocationCommandLifePhase lifePhase, 
            InvocationCommandTargetState targetState) {
        super(originalTarget, extendedTarget, lifePhase, targetState);
    }

    @Override
    public CommandType type() {
        return OPEN_LOCATION_TARGET;
    }

    @Override
    public String stringify() {
        return "open " + super.bestArgument();
    }

    @Override
    public String stringifyOriginal() {
        return "open " + super.originalArgument();
    }
    
    public String originalLocation() {
        return extractLocationFromPath(super.originalArgument());
    }
    
    public String extendedLocation() {
        return extractLocationFromPath(super.extendedArgument());
    }
    
    public String originalTarget() {
        return extractTargetFromPath(super.originalArgument());
    }
    
    public String extendedTarget() {
        return extractTargetFromPath(super.extendedArgument());
    }
    
    public List<InvocationCommand> decompose() {
        List<InvocationCommand> commands = new ArrayList<>();
        commands.add(this);
        
        if ( super.argument().isNotExtended() ) {
            return commands;
        }
        
        String locationName = this.extendedLocation();
        commands.add(createInvocationCommandFrom(OPEN_LOCATION, locationName, locationName));
        
        String path = super.extendedArgument();
        List<String> decomposedPaths = decomposePath(path);
        decomposedPaths.remove(locationName);
        
        decomposedPaths.forEach(decomosedPath -> {
            commands.add(createInvocationCommandFrom(
                    OPEN_LOCATION_TARGET, decomosedPath, decomosedPath));
        });
        
        return commands;
    }

    @Override
    public NamedEntityType subjectedEntityType() {
        return LOCATION;
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
