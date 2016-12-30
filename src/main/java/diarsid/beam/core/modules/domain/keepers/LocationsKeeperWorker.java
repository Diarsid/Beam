/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.domain.keepers;

import java.nio.file.Paths;
import java.util.List;

import diarsid.beam.core.control.io.base.Initiator;
import diarsid.beam.core.control.io.base.InnerIoEngine;
import diarsid.beam.core.control.io.commands.EditEntityCommand;
import diarsid.beam.core.control.io.commands.FindEntityCommand;
import diarsid.beam.core.control.io.commands.RemoveEntityCommand;
import diarsid.beam.core.control.io.commands.creation.CreateLocationCommand;
import diarsid.beam.core.domain.entities.Location;
import diarsid.beam.core.modules.data.DaoLocations;

import static java.util.Collections.emptyList;

import static diarsid.beam.core.control.io.commands.CommandType.CREATE_LOCATION;
import static diarsid.beam.core.control.io.commands.CommandType.FIND_LOCATION;
import static diarsid.beam.core.control.io.interpreter.ControlKeys.hasWildcard;
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
    public List<Location> getLocations(Initiator initiator, FindEntityCommand command) {
        if ( this.isConsistent(command, initiator) ) {
            if ( hasWildcard(command.getArg()) ) {
                return this.dao.getLocationsByNameParts(splitByWildcard(command.getArg()));
            } else {
                return this.dao.getLocationsByName(command.getArg());
            }
        } else {
            return emptyList();
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
        } else if (  ) {
            return false;
        }   
        return true;
    }

    @Override
    public boolean createLocation(Initiator initiator, CreateLocationCommand command) {
        if ( this.isConsistent(command, initiator) ) {
            return this.dao.saveNewLocation(
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
        if ( command.hasNoName() ) {
            String input = this.ioEngine.askForInput(initiator, "name");
            if ( input.isEmpty() ) {
                return false;
            } else {
                command.resetName(input);
            }
        }
        if ( command.hasNoPath() ) {
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
        
    }

    @Override
    public boolean editLocation(Initiator initiator, EditEntityCommand command) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean replaceInPaths(Initiator initiator, String replaceable, String replacement) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<Location> getAllLocations(Initiator initiator) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
