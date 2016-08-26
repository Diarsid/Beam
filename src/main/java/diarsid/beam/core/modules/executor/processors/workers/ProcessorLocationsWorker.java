/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.executor.processors.workers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import diarsid.beam.core.entities.local.Location;
import diarsid.beam.core.modules.IoInnerModule;
import diarsid.beam.core.modules.data.HandlerLocations;
import diarsid.beam.core.modules.executor.IntelligentExecutorCommandContext;
import diarsid.beam.core.modules.executor.OS;
import diarsid.beam.core.modules.executor.processors.ProcessorLocations;
import diarsid.beam.core.modules.executor.workflow.OperationResult;

import static java.lang.String.join;
import static java.util.stream.Collectors.toList;

import static diarsid.beam.core.modules.executor.os.search.FileSearchUtils.containsFileSeparator;
import static diarsid.beam.core.modules.executor.os.search.FileSearchUtils.indexOfFirstFileSeparator;
import static diarsid.beam.core.modules.executor.workflow.OperationResultImpl.failByInvalidArgument;
import static diarsid.beam.core.modules.executor.workflow.OperationResultImpl.failByInvalidLogic;

/**
 *
 * @author Diarsid
 */
class ProcessorLocationsWorker implements ProcessorLocations {
    
    private final IoInnerModule ioEngine;
    private final OS system;
    private final HandlerLocations locationsHandler;
    private final IntelligentExecutorCommandContext intellContext;

    ProcessorLocationsWorker(
            IoInnerModule io, 
            OS sys, 
            HandlerLocations locs, 
            IntelligentExecutorCommandContext intell) {
        
        this.ioEngine = io;
        this.system = sys;
        this.locationsHandler = locs;
        this.intellContext = intell;
    }
    
    
    private List<String> normalizeArguments(List<String> commandParams) {
        return commandParams.stream()
                .map((param) -> param.trim().replaceAll("[/\\\\]+", "/").replaceAll("[-]+", "-"))
                .collect(toList());
    }
    
    @Override
    public OperationResult open(List<String> commandParams) {
        commandParams = this.normalizeArguments(commandParams);
        this.intellContext.adjustCurrentlyExecutedCommand(join(" ", commandParams));
        OperationResult result;
        try {
            if ( commandParams.contains("in") ) {
                if ( commandParams.contains("with") ) {
                    // command pattern: open [file] in [location] with [program]
                    result = this.openFileInLocationWithProgram(
                            commandParams.get(1), 
                            commandParams.get(3),
                            commandParams.get(5));
                } else {
                    // command pattern: open [file|folder] in [location]
                    result = this.openFileInLocation(
                            commandParams.get(1), 
                            commandParams.get(3));
                }
            } else {
                // command pattern: open [location]
                result = this.openLocation(commandParams.get(1));
            }
        } catch (IndexOutOfBoundsException indexException) {
            this.ioEngine.reportError("Unrecognizable command.");
            result = failByInvalidLogic();
        }
        return result;
    }
    
    @Override
    public List<String> listLocationContent(String locationName) {
        Location location = this.getLocation(locationName, false);
        if ( location != null ) {
            List<String> locationContent = this.system.getLocationContent(location);
            if ( locationContent != null ) {
                locationContent.add(0, location.getName());
                return locationContent;
            } else {                
                return new ArrayList<>();
            }
        } else {
            return Collections.emptyList();
        }
    }
       
    private OperationResult openLocation(String locationName) {
        if ( containsFileSeparator(locationName) ) {
            String targetName = this.extractSubPathFromLocation(locationName);
            locationName = this.removeSubPathFromLocation(locationName);
            this.intellContext.adjustCurrentlyExecutedCommand(
                    "open " + targetName + " in " + locationName);
            return this.openFileInLocation(targetName, locationName);
        } else {
            Location location = this.getLocation(locationName);
            if ( location != null ) {
                return this.system.openLocation(location);
            } else {
                return failByInvalidArgument(locationName);
            }
        }
    }
    
    private OperationResult openFileInLocation(
            String targetName, String locationName) {
        if ( containsFileSeparator(locationName) ) {
            targetName = targetName + "/" + this.extractSubPathFromLocation(locationName);
            locationName = this.removeSubPathFromLocation(locationName);  
            this.intellContext.adjustCurrentlyExecutedCommand(
                    "open " + targetName + " in " + locationName);
        }
        Location location = this.getLocation(locationName);
        if ( location != null ) {
            return this.system.openFileInLocation(targetName, location);
        } else {
            return failByInvalidArgument(locationName);
        }             
    }

    private String extractSubPathFromLocation(String locationName) {
        return locationName.substring(
                indexOfFirstFileSeparator(locationName) + 1, locationName.length());
    }
    
    private String removeSubPathFromLocation(String locationName) {
        locationName = locationName.substring(0, indexOfFirstFileSeparator(locationName));
        return locationName;
    }
    
    private OperationResult openFileInLocationWithProgram(
            String target, String locationName, String program) {
        target = target.trim();
        program = program.trim();
        Location location = this.getLocation(locationName);
        if ( containsFileSeparator(locationName) ) {
            target = target + "/" + this.extractSubPathFromLocation(locationName);
            locationName = removeSubPathFromLocation(locationName);            
        }
        if ( location != null ) {
            return this.system.openFileInLocationWithProgram(
                    target, location, program);
        } else {
            return failByInvalidArgument(locationName);
        }   
    }
    
    private Location getLocation(String locationName) {
        return this.getLocation(locationName, true);
    }
    
    private Location getLocation(
            String locationName, boolean useIntelligentContext) {  
        
        List<Location> foundLocations = this.locationsHandler.getLocations(
                locationName);        
        
        if ( foundLocations.size() < 1 ) {
            this.ioEngine.reportMessage("Couldn`t find such location.");
            return null;
        } else if ( foundLocations.size() == 1 ) {
            return foundLocations.get(0);
        } else { 
            return this.resolveMultipleLocations(
                    locationName, foundLocations, useIntelligentContext);
        }
    }
    
    private Location resolveMultipleLocations(
            String requiredLocationName,
            List<Location> foundLocations,
            boolean useIntelligentContext) {
        
        List<String> locationNames = new ArrayList();
        for (Location loc : foundLocations) {
            locationNames.add(loc.getName());
        }
        int varNumber = -1;
        if ( useIntelligentContext ) {
            varNumber = this.intellContext.resolve(
                    "Desired location?",
                    requiredLocationName,
                    locationNames);
        } else {
            varNumber = this.ioEngine.resolveVariantsWithExternalIO(
                    "Desired location?", 
                    locationNames);
        }        
        
        if (varNumber < 0) {
            return null;
        } else {
            return foundLocations.get(varNumber-1);
        }
    }
}
