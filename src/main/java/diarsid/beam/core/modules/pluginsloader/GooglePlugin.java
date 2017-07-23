/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.pluginsloader;

import java.io.IOException;
import java.net.URISyntaxException;

import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.control.io.commands.executor.PluginTaskCommand;
import diarsid.beam.core.base.control.plugins.Plugin;

import static diarsid.beam.core.base.util.DesktopUtil.browseWithDesktop;
import static diarsid.beam.core.base.util.Logs.logError;
import static diarsid.beam.core.base.util.StringUtils.normalizeSpaces;

/**
 *
 * @author Diarsid
 */
public class GooglePlugin extends Plugin {
    
    public GooglePlugin(InnerIoEngine ioEngine) {
        super(ioEngine);
    }
    
    @Override
    public boolean isPluginCommandFirstArg(String arg) {
        return arg.startsWith("G/") && arg.length() > 2;
    }

    @Override
    public String name() {
        return "GooglePlugin";
    }

    @Override
    public void process(Initiator initiator, PluginTaskCommand command) {
        String line = normalizeSpaces(command.argument().replace("G/", ""));
        try {
            browseWithDesktop(this.convertToGoolgeQuery(line));
            this.ioEngine().report(initiator, "...googling");
        } catch (URISyntaxException|IOException ex) {
            logError(this.getClass(), ex.getMessage());
            this.ioEngine().report(initiator, ex.getMessage());
        } 
    }
    
    private String convertToGoolgeQuery(String line) {
        return "https://www.google.com/#q=" + line.replace(" ", "+");
    }
}
