/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.executor.os.actions;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import diarsid.beam.core.modules.IoInnerModule;

/**
 *
 * @author Diarsid
 */
public class RunnableDesktopBrowserAction extends AbstractRunnableAction {
    
    public RunnableDesktopBrowserAction(IoInnerModule ioEngine, String url) {
        super(ioEngine, url);
    }
    
    @Override
    public void run() {
        try {
            Desktop.getDesktop().browse(new URI(super.getArgument()));
        } catch (IOException e) {
            super.getIo().reportException(e, "browse URL with Desktop -> IOException: given url may be invalid.");
        } catch (IllegalArgumentException argumentException) {
            super.getIo().reportError("Unknown target");
        } catch (URISyntaxException urie){
            super.getIo().reportException(urie, "URL syntax error in address: " + super.getArgument());
        }
    }
}
