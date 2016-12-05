/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.executor.processors.workers;

import java.util.ArrayList;
import java.util.List;

import diarsid.beam.core.domain.entities.Location;
import diarsid.beam.core.modules.IoInnerModule;
import diarsid.beam.core.modules.data.HandlerLocations;
import diarsid.beam.core.modules.executor.OS;
import diarsid.beam.core.modules.executor.PathAnalizer;
import diarsid.beam.core.modules.executor.context.ExecutorContext;
import diarsid.beam.core.modules.executor.processors.ProcessorLocations;

import static java.lang.String.join;
import static java.util.Objects.nonNull;

import static diarsid.beam.core.util.Logs.debug;

/**
 *
 * @author Diarsid
 */
class ProcessorLocationsWorker implements ProcessorLocations {
    
    private final IoInnerModule ioEngine;
    private final OS system;
    private final HandlerLocations locationsHandler;
    private final ExecutorContext context;
    private final PathAnalizer pathAnalizer;

    ProcessorLocationsWorker(
            IoInnerModule io, 
            OS sys, 
            HandlerLocations locs, 
            ExecutorContext context,
            PathAnalizer pathAnalizer) {        
        this.ioEngine = io;
        this.system = sys;
        this.locationsHandler = locs;
        this.context = context;
        this.pathAnalizer = pathAnalizer;
    }
    
    
    @Override
    public void open(List<String> commandParams) {
        commandParams = this.pathAnalizer.normalizeArguments(commandParams);
        this.context.adjustCurrentlyExecutedCommand(join(" ", commandParams));
        try {
            if ( commandParams.contains("in") ) {
                // command pattern: open [file|folder] in [location]
                this.openFileInLocation(
                        commandParams.get(1), 
                        commandParams.get(3));
            } else {
                // command pattern: open [location]
                this.openLocation(commandParams.get(1));
            }
        } catch (IndexOutOfBoundsException indexException) {
            this.ioEngine.reportError("Unrecognizable command.");
        }
    }
    
    @Override
    public List<String> listLocationContent(String locationName) {
        Location location = this.getLocation(locationName, false);
        if ( nonNull(location) ) {
            List<String> locationContent = this.system.listContentIn(location, 5);
            if ( nonNull(locationContent) ) {
                locationContent.add(0, location.getName());                
                return locationContent;
            } else {                
                return new ArrayList<>();
            }
        } else {
            return new ArrayList<>();
        }
    }
    
    @Override
    public List<String> listLocationAndSubPathContent(
            String locationName, String subPath) {
        Location location = this.getLocation(locationName, false);
        List<String> locationContent;
        if ( nonNull(location) ) {
            locationContent = this.system.listContentIn(location, subPath, 5);
            if ( nonNull(locationContent) ) {
                if ( ! locationContent.isEmpty()) {
                    locationContent.add(0, location.getName().concat("/").concat(subPath)); 
                }               
            } else {                
                locationContent = new ArrayList<>();
            }
        } else {
            locationContent = new ArrayList<>();
        }
        return locationContent;
    }
       
    private void openLocation(String locationName) {
        Location location = this.getLocation(locationName);
        if ( nonNull(location) ) {
            this.context.adjustCurrentlyExecutedCommand("open", location.getName());
            this.system.openLocation(location);
        } else {
            this.context.discardCurrentlyExecutedCommandByInvalidLocation(locationName);
        }
    }
    
    private void openFileInLocation(String targetName, String locationName) {        
        Location location = this.getLocation(locationName);
        if ( nonNull(location) ) {
            this.context.adjustCurrentlyExecutedCommand("open", targetName, "in", location.getName());
            this.system.openFileInLocation(targetName, location);
        } else {
            this.context.discardCurrentlyExecutedCommandByInvalidLocation(locationName);
        }             
    }
    
    private Location getLocation(String locationName) {
        return this.getLocation(locationName, true);
    }
    
    private Location getLocation(
            String locationName, boolean useIntelligentContext) {
        List<Location> foundLocations = this.locationsHandler.getLocations(locationName);      
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
            varNumber = this.context.resolve(
                    "Desired location?",
                    requiredLocationName,
                    locationNames);
        } else {
            varNumber = this.ioEngine.resolveVariants(
                    "Desired location?", 
                    locationNames);
        }        
        
        if (varNumber < 0) {
            return null;
        } else {
            debug("[LOCATIONS PROCESSOR] resolved: " + requiredLocationName + " -> " + 
                    foundLocations.get(varNumber-1).getName());
            return foundLocations.get(varNumber-1);
        }
    }
}
