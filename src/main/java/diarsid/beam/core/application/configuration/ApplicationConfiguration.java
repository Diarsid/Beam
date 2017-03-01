/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.application.configuration;

import java.nio.file.Path;
import java.nio.file.Paths;

import static diarsid.beam.core.application.configuration.ApplicationConfigurationReading.parse;
import static diarsid.beam.core.application.configuration.ApplicationConfigurationReading.readConfigEntriesAsLinesFrom;

/**
 *
 * @author Diarsid
 */
public class ApplicationConfiguration {
    
    private static final Configuration CONFIGURATION;
    
    static {
        Path configFile = Paths.get("../config/beam.config");
        Configuration defaultConfig = parse(
                "data.store = ../res/data",
                "data.user = root",
                "data.pass = root",
                "catalogs.programs = ../env/programs",
                "catalogs.notes = ../env/notes",
                "web.local.host = 127.0.0.1",
                "web.local.port = 32001",
                "web.local.path = /beam/core",
                "rmi.core.port = 43006",
                "rmi.core.host = 127.0.0.1",
                "rmi.sysconsole.port = 43005",
                "rmi.sysconsole.host = 127.0.0.1",
                "core.jvm.option = -Djava.rmi.server.hostname=127.0.0.1",
                "core.jvm.option = -Dfile.encoding=UTF-8",
                "core.jvm.option = -Dlog4j.configuration=file:../config/log4j.properties",
                "core.jvm.option = -Xms32m",
                "core.jvm.option = -Xmx32m",
                "sysconsole.jvm.option = -Djava.rmi.server.hostname=127.0.0.1",
                "sysconsole.jvm.option = -Dfile.encoding=UTF-8",
                "sysconsole.jvm.option = -Dlog4j.configuration=file:../config/log4j.properties",
                "sysconsole.jvm.option = -Xms4m",
                "sysconsole.jvm.option = -Xmx4m",
                "starter.jvm.option = -Xmx32m",
                "starter.jvm.option = -Xms32m",
                "starter.jvm.option = -Dfile.encoding=UTF-8",
                "starter.jvm.option = -Dlog4j.configuration=file:../config/log4j.properties");
        Configuration actualConfig = parse(readConfigEntriesAsLinesFrom(configFile));
        CONFIGURATION = actualConfig.merge(defaultConfig);
    }
    
    private ApplicationConfiguration() {
    }
    
    public static Configuration getConfiguration() {
        return CONFIGURATION;
    }
}
