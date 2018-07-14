/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.util;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Objects.isNull;

/**
 *
 * @author Diarsid
 */
public class Logging {
    
    private static final Map<Class, Logger> LOGGERS = new HashMap<>();
    
    private Logging() {}
    
    public static final Logger logFor(Object object) {
        Logger logger = LOGGERS.get(object.getClass());
        if ( isNull(logger) ) {
            logger = LoggerFactory.getLogger(object.getClass());
            LOGGERS.put(object.getClass(), logger);
        }
        return logger;
    }
    
}
