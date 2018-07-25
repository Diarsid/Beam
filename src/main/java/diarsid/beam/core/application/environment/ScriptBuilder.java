/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.application.environment;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import diarsid.beam.core.base.exceptions.RequirementException;

import static java.lang.String.join;
import static java.util.Arrays.asList;

import static diarsid.beam.core.application.environment.ScriptSyntax.WIN;
import static diarsid.beam.core.base.util.CollectionsUtils.nonEmpty;
import static diarsid.beam.core.base.util.Requirements.requireNonEmpty;

/**
 *
 * @author Diarsid
 */
public class ScriptBuilder {
    
    private final Path scripts;
    private final ScriptSyntax syntax;
    private final String currentClassClasspath;
    private final String scriptName;
    private final List<String> comments;
    private final List<String> classpath;
    private final List<String> jvmOptions;
    private final List<String> arguments;
    private String javaExecutable;
    private String executableClass;
    
    ScriptBuilder(
            String name, 
            String currentClassClasspath, 
            Path scriptsCatalog, 
            ScriptSyntax syntax) {
        this.scripts = scriptsCatalog;
        this.syntax = syntax;
        this.scriptName = name;
        this.currentClassClasspath = currentClassClasspath;
        this.comments = new ArrayList<>();
        this.arguments = new ArrayList<>();
        this.jvmOptions = new ArrayList<>();
        this.classpath = new ArrayList<>();
        this.classpath.add(currentClassClasspath);
        this.executableClass = "";
        this.javaExecutable = "java";
    }
    
    public ScriptBuilder invokeClass(Class executableClass) {
        this.executableClass = executableClass.getCanonicalName();
        return this;
    }
    
    public ScriptBuilder usingJavaw() {
        if ( this.syntax.equals(WIN) ) {
            this.javaExecutable = "javaw";
        }        
        return this;
    }
    
    public ScriptBuilder withComments(List<String> comments) {
        this.comments.addAll(comments);
        return this;
    }
    
    public ScriptBuilder withClasspath(List<String> classpath) {
        this.classpath.addAll(classpath);
        return this;
    }
    
    public ScriptBuilder withJvmOptions(List<String> jvmOptions) {
        this.jvmOptions.addAll(jvmOptions);
        return this;
    }
    
    public ScriptBuilder withArguments(List<String> arguments) {
        this.arguments.addAll(arguments);
        return this;
    }
    
    public ScriptBuilder withArguments(String... arguments) {
        this.arguments.addAll(asList(arguments));
        return this;
    }
    
    public Script complete() throws RequirementException {
        requireNonEmpty(this.executableClass, "class not specified.");
        StringBuilder invocation = new StringBuilder();
        invocation
                .append(this.syntax.invocationPrefix())
                .append(javaExecutable);
        if ( nonEmpty(this.classpath) ) {
            invocation
                    .append(" -cp ")
                    .append(this.syntax.joinClasspath(this.classpath));
        }
        if ( nonEmpty(this.jvmOptions) ) {
            invocation
                    .append(" ")
                    .append(join(" ", this.jvmOptions));
        }
        invocation
                .append(" ")
                .append(this.executableClass);
        if ( nonEmpty(this.arguments) ) {
            invocation
                    .append(" ")
                    .append(join(" ", this.arguments));
        }
        List<String> lines = new ArrayList<>();
        if ( nonEmpty(this.comments) ) {
            lines.addAll(this.syntax.wrapComments(this.comments));
        }
        lines.add("");
        lines.addAll(this.syntax.preliminaryCommands());
        lines.add("");
        lines.add(invocation.toString());
        return new ScriptReal(
                this.syntax.addExtensionTo(this.scriptName), lines, this.scripts);
    }
    
    
}
