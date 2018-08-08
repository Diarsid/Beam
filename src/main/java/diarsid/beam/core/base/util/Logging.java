/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.util;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Objects.isNull;

import static diarsid.beam.core.application.environment.BeamEnvironment.configuration;

/**
 *
 * @author Diarsid
 */
public class Logging {
    
    private static final Function<Object, Logger> LOGGER_ACTION;
    
    static {
        if ( configuration().asBoolean("log") ) {
            Map<Class, Logger> loggers = new HashMap<>();
            LOGGER_ACTION = (object) -> {
                Class clss;
                if ( object instanceof Class ) {
                    clss = (Class) object;
                } else {
                    clss = object.getClass();
                }

                Logger logger = loggers.get(clss);
                if ( isNull(logger) ) {
                    logger = LoggerFactory.getLogger(clss);
                    loggers.put(clss, logger);
                }
                return logger;
            };
        } else {
            Logger emptyLogger = new EmptyLogger();
            LOGGER_ACTION = (object) -> {
                return emptyLogger;
            };
        }
    }
    
    private Logging() {}
    
    public static final Logger logFor(Object object) {
        return LOGGER_ACTION.apply(object);
    }
    
}
