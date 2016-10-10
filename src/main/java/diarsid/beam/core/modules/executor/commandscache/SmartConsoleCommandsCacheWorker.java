/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.executor.commandscache;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import diarsid.beam.core.modules.IoInnerModule;
import diarsid.beam.core.modules.data.DaoExecutorConsoleCommands;

import static diarsid.beam.core.util.Logs.debug;

/**
 *
 * @author Diarsid
 */
class SmartConsoleCommandsCacheWorker implements SmartConsoleCommandsCache {
    
    private final IoInnerModule ioEngine;
    private final DaoExecutorConsoleCommands dao;
    private final CommandsAnalizer commandsAnalizer;
        
    SmartConsoleCommandsCacheWorker(
            IoInnerModule ioEngine,
            CommandsAnalizer analizer,
            DaoExecutorConsoleCommands consoleCommandsDao) {  
        this.ioEngine = ioEngine;
        this.dao = consoleCommandsDao;
        this.commandsAnalizer = analizer;        
    }    
    
    @Override
    public void addCommand(List<String> commandParams) {
        this.dao.saveNewConsoleCommand(String.join(" ", commandParams));
    }
    
    @Override
    public void addCommand(String command) {
        this.dao.saveNewConsoleCommand(command);
    }
    
    @Override
    public List<String> getConsoleCommandsOfPattern(String pattern) {
        return new ArrayList<>(this.dao.getRawCommandsForPattern(pattern));
    }
    
    @Override
    public boolean deleteCached(String command) {
        List<String> candidates = new ArrayList<>(
                this.dao.getRawCommandsForPattern(command));
        if ( candidates.isEmpty() ) {
            this.ioEngine.reportMessage(
                    "...command '" + command + "' not found in console cache.");
            return false;
        } else if ( candidates.size() == 1 ) {
            if ( this.dao.remove(command) ) {
                this.ioEngine.reportMessage(
                        "...command '" + candidates.get(0) + "' removed from console cache.");
                return true;
            } else {
                this.ioEngine.reportMessage("...fails to delete.");
                return false;
            }
        } else {
            int choice = this.ioEngine.resolveVariants(
                    "...remove from console cache:", candidates);
            if ( choice > 0 ) {
                if ( this.dao.remove(candidates.get(choice - 1)) ) {
                    this.ioEngine.reportMessage("...removed.");
                    return true;
                } else {
                    this.ioEngine.reportMessage("...fails to delete.");
                    return false;
                }
            } else {
                return false;
            }
        }
    }
    
    @Override
    public String getCommandByPattern(String pattern) {
        if ( pattern.length() < 2 ) {
            // There is no reason to search commands that matches only
            // one letter
            return "";
        } else {
            debug("[COMMANDS CACHE] search for pattern: " + pattern);
            Map<String, String> commandsRawCache = this.dao.getImprovedCommandsForPattern(pattern);
            if ( commandsRawCache.isEmpty() ) {
                return "";
            }
            debug("[COMMANDS CACHE] raw commands: " + commandsRawCache);
            return this.commandsAnalizer
                    .analizeAndFindAppropriateCommand(pattern, commandsRawCache);
        }        
    }
}
