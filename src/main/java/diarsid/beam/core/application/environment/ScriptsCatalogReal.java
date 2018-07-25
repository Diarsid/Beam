/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.application.environment;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import diarsid.beam.core.Beam;
import diarsid.beam.core.application.starter.Starter;
import diarsid.beam.core.application.systemconsole.SystemConsole;
import diarsid.beam.core.base.exceptions.WorkflowBrokenException;

import static java.nio.file.Files.isRegularFile;
import static java.util.stream.Collectors.toList;

import static diarsid.beam.core.application.starter.FlagLaunchable.START_ALL;
import static diarsid.beam.core.base.util.Logs.logError;
import static diarsid.beam.core.base.util.PathUtils.asName;

/**
 *
 * @author Diarsid
 */
class ScriptsCatalogReal implements ScriptsCatalog {
    
    private final CurrentWorkingDirectory currentWorkingDirectory;
    private final LibrariesCatalog librariesCatalog;
    private final Configuration config;
    private final ScriptSyntax scriptSyntax;
    
    ScriptsCatalogReal(
            CurrentWorkingDirectory currentWorkingDirectory,
            LibrariesCatalog librariesCatalog,
            Configuration config,
            ScriptSyntax scriptSyntax) {
        this.currentWorkingDirectory = currentWorkingDirectory;
        this.librariesCatalog = librariesCatalog;
        this.config = config;
        this.scriptSyntax = scriptSyntax;
    }
    
    ScriptsCatalog refreshScripts() {
        
        this.newScript("beam.core")
                .invokeClass(Beam.class)
                .usingJavaw()
                .withClasspath(this.librariesCatalog.libraries())
                .withJvmOptions(this.config.asList("core.jvm.option"))
                .complete()
                .save();

        this.newScript("beam.sysconsole")
                .invokeClass(SystemConsole.class)
                .withClasspath(this.librariesCatalog.librariesWithAny("log", "slf"))
                .withJvmOptions(this.config.asList("sysconsole.jvm.option"))
                .complete()
                .save();

        this.newScript("beam")
                .invokeClass(Starter.class)
                .usingJavaw()
                .withClasspath(this.librariesCatalog.libraries())
                .withJvmOptions(this.config.asList("starter.jvm.option"))
                .withArguments(START_ALL.text())
                .complete()
                .save();            
        
        return this;
    }
    
    @Override
    public ScriptBuilder newScript(String name) {
        return new ScriptBuilder(
                name, 
                this.currentWorkingDirectory.currentClassClasspath(), 
                this.currentWorkingDirectory.path(), 
                this.scriptSyntax);
    }
    
    @Override
    public List<Script> scripts() {
        try {
            return Files.list(this.currentWorkingDirectory.path())
                    .filter(path -> isRegularFile(path))
                    .filter(path -> this.ifFileHasScriptExtension(path))
                    .map(scriptPath -> toScript(scriptPath)) 
                    .collect(toList());
        } catch (IOException ex) {
            logError(this.getClass(), ex);
            throw new WorkflowBrokenException("unable to obtain scripts.");
        }
    }
    
    private Script toScript(Path path) {
        return new ScriptReal(asName(path), this.currentWorkingDirectory.path());
    }
    
    @Override
    public Optional<Script> findScriptByName(String name) {
        String nameWithExtension = this.scriptSyntax.addExtensionTo(name);
        try {
            return Files.find(this.currentWorkingDirectory.path(), 
                    1, 
                    (path, attributes) -> {
                        return 
                                attributes.isRegularFile() && 
                                this.ifFileHasScriptExtension(path) &&
                                asName(path).equals(nameWithExtension);
                    })
                    .findFirst()
                    .map(path -> this.toScript(path));
        } catch (IOException ex) {
            logError(this.getClass(), ex);
            throw new WorkflowBrokenException("unable to obtain scripts.");
        }
    }

    private boolean ifFileHasScriptExtension(Path path) {
        return this.scriptSyntax.examineExtension(asName(path));
    }

    @Override
    public boolean notContains(Script script) {
        try {
            return Files.list(this.currentWorkingDirectory.path())
                    .filter(path -> isRegularFile(path))
                    .filter(path -> this.ifFileHasScriptExtension(path))
                    .filter(path -> script.name().equals(asName(path)))
                    .findFirst()
                    .isPresent();
        } catch (IOException ex) {
            logError(this.getClass(), ex);
            throw new WorkflowBrokenException("unable to list scripts.");
        }
    }

    @Override
    public Path path() {
        return this.currentWorkingDirectory.path();
    }
    
    @Override
    public String name() {
        return "Scripts catalog";
    }
}
