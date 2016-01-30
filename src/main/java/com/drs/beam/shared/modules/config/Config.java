/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam.shared.modules.config;

/**
 *
 * @author Diarsid
 */
public enum Config {
    
    CORE_PORT ("/configuration/rmi-info/beam-core", "port"),
    CORE_HOST ("/configuration/rmi-info/beam-core", "host"),
    SYS_CONSOLE_PORT("/configuration/rmi-info/beam-system-console", "port"),
    SYS_CONSOLE_HOST ("/configuration/rmi-info/beam-system-console", "host"),
    SYS_CONSOLE_NAME ("/configuration/rmi-info/beam-system-console/console-rmi-name"),
    TASK_MANAGER_NAME  ("/configuration/rmi-info/beam-core/task-manager-rmi-name"),
    EXECUTOR_NAME ("/configuration/rmi-info/beam-core/executor-rmi-name"),
    BEAM_ACCESS_NAME ("/configuration/rmi-info/beam-core/access-rmi-name"),
    LOCATIONS_HANDLER_NAME ("/configuration/rmi-info/beam-core/locations-rmi-name"),
    WEB_PAGES_HANDLER_NAME ("/configuration/rmi-info/beam-core/web-pages-rmi-name"),    
    CORE_JDBC_DRIVER ("/configuration/databases/core/jdbc-driver"),
    CORE_JDBC_URL ("/configuration/databases/core/jdbc-url"),
    CORE_DB_LOCATION("/configuration/databases/core/db-location"),
    CORE_DB_NAME ("/configuration/databases/core/db-name"),
    PROGRAMS_LOCATION ("/configuration/resources/programs"),
    IMAGES_LOCATION ("/configuration/resources/images"),
    LIBRARIES_LOCATION ("/configuration/resources/libraries"),
    CLASSPATH_CORE ("/configuration/classpath/beam-core/element"),
    CLASSPATH_SYS_CONSOLE ("/configuration/classpath/beam-system-console/element"),
    JVM_CORE_OPTIONS ("/configuration/jvm-options/beam-core/option"),
    JVM_SYS_CONSOLE_OPTIONS ("/configuration/jvm-options/beam-system-console/option");
    
    private final String xmlElementName;
    private final String xmlElemAttr;
    
    Config(String elemName) {
        this.xmlElementName = elemName;
        this.xmlElemAttr = "";
    }

    Config(String elemName, String attrName) {
        this.xmlElementName = elemName;
        this.xmlElemAttr = attrName;
    }
    
    public String xmlPath() {
        return this.xmlElementName;
    }
    
    public String xmlAttribute() {
        return this.xmlElemAttr;
    }
}
