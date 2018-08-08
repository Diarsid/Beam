/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.application.environment;

import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import diarsid.beam.core.base.exceptions.WorkflowBrokenException;

import static java.nio.charset.Charset.forName;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.SYNC;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;

import static diarsid.beam.core.base.util.Logging.logFor;

/**
 *
 * @author Diarsid
 */
class ScriptReal implements Script {
    
    private final Path scripts;
    private final List<String> lines;
    private final String name;
    
    ScriptReal(String name, Path scriptsCatalog) {
        this.name = name;
        this.scripts = scriptsCatalog;
        this.lines = new ArrayList<>();
    }

    ScriptReal(String name, List<String> lines, Path scriptsCatalog) {
        this.name = name;
        this.scripts = scriptsCatalog;
        this.lines = lines;
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public Script save() {
        if ( ! this.lines.isEmpty() ) {
            try {
                Path saved = Files.write(this.scriptPath(),
                        this.lines,
                        forName("UTF-8"),
                        WRITE, CREATE, TRUNCATE_EXISTING, SYNC);
                saved.toFile().setExecutable(true);
            } catch (IOException ex) {
                logFor(this).error("failed to save script " + this.name, ex);
                throw new WorkflowBrokenException("failed to save script " + this.name);
            }
        } 
        return this;
    }

    @Override
    public Script execute() {
        try {
            Desktop.getDesktop().open(this.scriptPath().toFile());
        } catch (IOException ex) {
            logFor(this).error("failed to execute script " + this.name, ex);
            throw new WorkflowBrokenException("failed to execute script " + this.name);
        }
        return this;
    }

    private Path scriptPath() {
        return this.scripts.resolve(this.name);
    }    
}
