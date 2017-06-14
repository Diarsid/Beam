/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.control.io.console;

/**
 *
 * @author Diarsid
 */
public class ConsoleControllerBuilder {
    
    private ConsoleControllerBuilder() {}
    
    public static ConsoleController build(ConsolePlatform consolePlatform) {
        ConsoleEngine consoleEngine = new ConsoleEngine(consolePlatform);
        ConsoleController consoleController = new ConsoleController(consoleEngine);
        return consoleController;
    }
}
