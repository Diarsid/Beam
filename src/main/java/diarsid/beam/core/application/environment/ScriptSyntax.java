/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.application.environment;

import java.util.List;

import diarsid.beam.core.base.exceptions.WorkflowBrokenException;

import static java.lang.String.join;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

import static diarsid.beam.core.base.util.StringIgnoreCaseUtil.containsIgnoreCase;

/**
 *
 * @author Diarsid
 */
public enum ScriptSyntax {
    
    WIN {
        @Override
        String joinClasspath(List<String> classpath) {
            return join(";", classpath);
        }

        @Override
        List<String> wrapComments(List<String> comments) {
            return comments.stream().map(comment -> ":: " + comment).collect(toList());
        }

        @Override
        String invocationPrefix() {
            return "cmd /c start ";
        }

        @Override
        List<String> preliminaryCommands() {
            return asList("@echo off");
        }        

        @Override
        String addExtensionTo(String fileName) {
            return fileName.concat(".bat");
        }

        @Override
        boolean examineExtension(String name) {
            return name.endsWith(".bat");
        }
    },
    NIX {
        @Override
        String joinClasspath(List<String> classpath) {
            return join(":", classpath);
        }

        @Override
        List<String> wrapComments(List<String> comments) {
            return comments.stream().map(comment -> "# " + comment).collect(toList());
        }

        @Override
        String invocationPrefix() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        List<String> preliminaryCommands() {
            throw new UnsupportedOperationException("Not supported yet.");
        }        

        @Override
        String addExtensionTo(String fileName) {
            return fileName.concat(".sh");
        }

        @Override
        boolean examineExtension(String name) {
            return name.endsWith(".sh");
        }
    };
    
    abstract String invocationPrefix();
    
    abstract List<String> preliminaryCommands();
    
    abstract String joinClasspath(List<String> classpath);
    
    abstract List<String> wrapComments(List<String> comments);
    
    abstract String addExtensionTo(String fileName);
    
    abstract boolean examineExtension(String name);
    
    static ScriptSyntax getScriptSyntax() {
        String systemName = System.getProperty("os.name");
        if ( containsIgnoreCase(systemName, "win") ) {
            return WIN;
        } else if ( containsIgnoreCase(systemName, "nux") ) {
            return NIX;
        } else {
            throw new WorkflowBrokenException("Unsupported OS.");
        }
    }
}
