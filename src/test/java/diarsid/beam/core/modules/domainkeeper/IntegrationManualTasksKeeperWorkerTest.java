/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.domainkeeper;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import mocks.InnerIoEngineForManualTests;

import diarsid.beam.core.base.control.flow.VoidFlow;
import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.control.io.commands.ArgumentsCommand;
import diarsid.beam.core.domain.inputparsing.time.AllowedTimePeriodsParser;
import diarsid.beam.core.domain.inputparsing.time.TimeParser;
import diarsid.beam.core.modules.data.DaoTasks;

import static java.util.Arrays.asList;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static diarsid.beam.core.base.control.flow.FlowResult.FAIL;
import static diarsid.beam.core.base.control.io.base.actors.OuterIoEngineType.IN_MACHINE;
import static diarsid.beam.core.base.control.io.commands.CommandType.CREATE_TASK;
import static diarsid.beam.core.domain.inputparsing.time.TimeParsing.allowedTimePeriodsParser;
import static diarsid.beam.core.domain.inputparsing.time.TimeParsing.timePatternParsersHolder;

/**
 *
 * @author Diarsid
 */
public class IntegrationManualTasksKeeperWorkerTest {
    
    static BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    
    public IntegrationManualTasksKeeperWorkerTest() {
    }
    
    public static void main(String[] args) throws Exception {
        InnerIoEngine ioEngine = new InnerIoEngineForManualTests(reader);
        KeeperDialogHelper helper = new KeeperDialogHelper(ioEngine);
        Initiator initiator = new Initiator(56, IN_MACHINE);
        DaoTasks dao = mock(DaoTasks.class);
        when(dao.saveTask(any(Initiator.class), anyObject())).thenReturn(true);
        
        TimeParser timeParser = timePatternParsersHolder();
        AllowedTimePeriodsParser timePeriodsParser = allowedTimePeriodsParser();
        TasksKeeper tasksKeeper = new TasksKeeperWorker(
                ioEngine, dao, helper, timeParser, timePeriodsParser);
        
        String input = "";
        ArgumentsCommand command;
        VoidFlow flow;
        while ( true ) {
            System.out.print("command : ");            
            input = reader.readLine();
            if ( input.equals("exit") ) {
                break;
            }
            command = new ArgumentsCommand(CREATE_TASK, asList(input.split(" ")));
            flow = tasksKeeper.createTask(initiator, command);
            if ( flow.result().equals(FAIL) ) {
                System.out.println(flow.message());
            } else {
                System.out.println(flow.result().name());
            }            
        }
        System.out.println("end");
        System.exit(0);
    }
}
