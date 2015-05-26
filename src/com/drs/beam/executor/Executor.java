/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam.executor;

import com.drs.beam.executor.dao.ExecutorDao;
import com.drs.beam.io.BeamIO;
import com.drs.beam.io.InnerIOIF;
import com.drs.beam.io.jfxgui.Gui;
import com.drs.beam.io.jfxgui.GuiEngine;
import com.drs.beam.remote.codebase.ExecutorIF;
import com.drs.beam.tasks.Task;
import java.rmi.RemoteException;


public class Executor implements ExecutorIF {
    // Fields ---------------------------------------------------------------------------------
    private final InnerIOIF ioEngine;
    private final ExecutorDao dao;
    
    // Syntactical parts of commands.
    private final String CALL = "call";
    private final String RUN = "run";
    private final String OPEN = "open";
    private final String IN = "in";
    private final String WITH = "with";

    // Constructors ---------------------------------------------------------------------------
    public Executor() {
        this.ioEngine = BeamIO.getInnerIO();
        this.dao = ExecutorDao.getDao();
    }

    // Methods --------------------------------------------------------------------------------
    @Override
    public void execute(String command) throws RemoteException{
        command = command.trim();
        String[] parts = command.split(" ");
        choosing: switch (parts[0]){
            case(OPEN) : {
                open(parts);
                break choosing;
            }
            case(RUN) : {
                run(parts);
                break choosing;
            }
            case(CALL) : {
                call(parts);
                break choosing;
            }
            default : {
                ioEngine.inform("'" + parts[0] + "' is unknown command.");
            }
        }
    }
    
    @Override
    public void newCommand(String[] command, String commandName) throws RemoteException{
        
    }
    
    @Override
    public void newLocation(String location) throws RemoteException{
        
    }
    
    @Override
    public void deleteCommand(String commandName) throws RemoteException{
        
    }
    
    @Override
    public void deleteLocation(String locationName) throws RemoteException{
        
    }
    
    private void open(String[] parts){
        
    }
    
    private void openInLocation(String[] parts){
        
    }
    
    private void openFileInLocation(String[] parts){
        
    }
    
    private void openFileInLocationWithProgram(String[] parts){
        
    }
    
    private void run(String[] parts){
        
    }
    
    private void call(String[] parts){
        
    }
}