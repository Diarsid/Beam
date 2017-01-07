/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.domain.keepers;

import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import diarsid.beam.core.control.io.base.Initiator;
import diarsid.beam.core.control.io.base.InnerIoEngine;
import diarsid.beam.core.control.io.base.VariantAnswer;
import diarsid.beam.core.control.io.base.VariantsQuestion;
import diarsid.beam.core.control.io.commands.EditEntityCommand;
import diarsid.beam.core.control.io.commands.FindEntityCommand;
import diarsid.beam.core.control.io.commands.RemoveEntityCommand;
import diarsid.beam.core.control.io.commands.creation.CreateLocationCommand;
import diarsid.beam.core.domain.entities.Location;
import diarsid.beam.core.modules.data.DaoLocations;
import diarsid.beam.core.modules.domain.LocationsKeeper;

import static diarsid.beam.core.control.io.commands.CommandType.CREATE_LOCATION;
import static diarsid.beam.core.control.io.commands.CommandType.FIND_LOCATION;
import static diarsid.beam.core.control.io.interpreter.ControlKeys.hasWildcard;
import static diarsid.beam.core.util.CollectionsUtils.containsOne;
import static diarsid.beam.core.util.CollectionsUtils.getOne;
import static diarsid.beam.core.util.PathUtils.pathIsDirectory;
import static diarsid.beam.core.util.StringUtils.splitByWildcard;


public class LocationsKeeperWorker implements LocationsKeeper {
    
    private final DaoLocations dao;
    private final InnerIoEngine ioEngine;
    
    public LocationsKeeperWorker(DaoLocations dao, InnerIoEngine ioEngine) {
        this.dao = dao;
        this.ioEngine = ioEngine;
    }

    @Override
    public Optional<Location> getLocation(Initiator initiator, FindEntityCommand command) {
        if ( this.isConsistent(command, initiator) ) {
            List<Location> locations = this.getLocationsBy(command, initiator);
            if ( locations.size() > 1 ) {
                VariantAnswer answer = this.ioEngine.resolveVariants(
                        initiator, new VariantsQuestion("choose location", locations));
                if ( answer.isPresent() ) {
                    return locations
                            .stream()
                            .filter(location -> location.getName().equals(answer.get().getText()))
                            .findFirst();
                } else {
                    return Optional.empty();
                }
            } else if ( containsOne(locations) ) {
                return Optional.of(getOne(locations));
            } else {
                return Optional.empty();
            }
        } else {
            return Optional.empty();
        }
    }

    private List<Location> getLocationsBy(FindEntityCommand command, Initiator initiator) {
        if ( hasWildcard(command.getArg()) ) {
            return this.dao.getLocationsByNameParts(
                    initiator, splitByWildcard(command.getArg()));
        } else {
            return this.dao.getLocationsByName(
                    initiator, command.getArg());
        }
    }

    private boolean isConsistent(FindEntityCommand command, Initiator initiator) {
        if ( command.type().isNot(FIND_LOCATION) ) {
            return false;
        }
        if ( command.hasNoArg() ) {
            String input = this.ioEngine.askForInput(initiator, "enter name");
            if ( input.isEmpty() ) {
                return false;
            } else {
                command.resetArg(input);                
            }
        } else {
            return false;
        }   
        return true;
    }

    @Override
    public boolean createLocation(Initiator initiator, CreateLocationCommand command) {
        if ( this.isConsistent(command, initiator) ) {
            return this.dao.saveNewLocation(
                    initiator, 
                    new Location(
                            command.getName(), 
                            command.getPath()));
        } else {
            return false;
        }
    }

    private boolean isConsistent(CreateLocationCommand command, Initiator initiator) {
        if ( command.type().isNot(CREATE_LOCATION) ) {
            return false;
        }
        
        if ( command.hasName() ) {
            
        } else {
            String input = this.ioEngine.askForInput(initiator, "name");
            if ( input.isEmpty() ) {
                return false;
            } else {
                command.resetName(input);
            }
        }
        
        if ( command.hasPath() ) {
            
        } else {
            String input = this.ioEngine.askForInput(initiator, "path");
            if ( pathIsDirectory(Paths.get(input)) ) {
                command.resetPath(input);
            } else {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean removeLocation(Initiator initiator, RemoveEntityCommand command) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean editLocation(Initiator initiator, EditEntityCommand command) {
        if ( this.isConsistent(initiator, command) ) {
            switch ( command.getTarget() ) {
                case TARGET_NAME : {
                    String newName = this.ioEngine.askForInput(initiator, "enter new name");
                    return this.dao.editLocationName(initiator, command.getName(), newName);
                }
                case TARGET_PATH : {
                    
                }
                default : {
                    return false;
                }
            }
        } else {
            return false;
        }
    }
    
    private boolean isConsistent(Initiator initiator, EditEntityCommand command) {
        
    }

    @Override
    public boolean replaceInPaths(Initiator initiator, String replaceable, String replacement) {
        
        return this.dao.replaceInPaths(initiator, replaceable, replacement);
    }

    @Override
    public List<Location> getAllLocations(Initiator initiator) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
