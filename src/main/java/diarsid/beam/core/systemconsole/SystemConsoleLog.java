/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.systemconsole;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static diarsid.beam.core.systemconsole.SystemConsole.getPassport;

/**
 *
 * @author Diarsid
 */
public class SystemConsoleLog {
    
    public static final Logger logger = LoggerFactory.getLogger("consoleDebugger");
    
    public SystemConsoleLog() {
    }
    
    public static void consoleDebug(String message) {
        logger.debug("[" + getPassport().getInitiatorId() + "] " + message);
    }
}
