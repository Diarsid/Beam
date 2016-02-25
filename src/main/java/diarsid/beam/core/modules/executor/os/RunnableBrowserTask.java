/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.executor.os;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import diarsid.beam.core.modules.IoInnerModule;

/**
 *
 * @author Diarsid
 */
public class RunnableBrowserTask implements Runnable {
    
    private final String url;
    private final IoInnerModule ioEngine;

    RunnableBrowserTask(IoInnerModule ioEngine, String url) {
        this.url = url;
        this.ioEngine = ioEngine;
    }
    
    @Override
    public void run() {
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (IOException e) {
            ioEngine.reportException(e, "browse URL with Desktop -> IOException: given url may be invalid.");
        } catch (IllegalArgumentException argumentException) {
            ioEngine.reportError("Unknown target");
        } catch (URISyntaxException urie){
            ioEngine.reportException(urie, "URL syntax error in address: " + url);
        }
    }
}
