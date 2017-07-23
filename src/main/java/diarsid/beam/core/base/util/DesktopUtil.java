/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.util;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import diarsid.beam.core.base.exceptions.WorkflowBrokenException;

import static java.awt.Desktop.getDesktop;
import static java.awt.Desktop.isDesktopSupported;

/**
 *
 * @author Diarsid
 */
public class DesktopUtil {
    
    private static final Desktop DESKTOP;
    
    static {
        if ( isDesktopSupported() ) {
            DESKTOP = getDesktop();
        } else {
            throw new WorkflowBrokenException("java.awt.Desktop is not supported.");
        }
    }
    
    private DesktopUtil() {}
    
    public static void openWithDesktop(File file) throws IOException {
        DESKTOP.open(file);
    }
    
    public static void browseWithDesktop(String url) throws URISyntaxException, IOException {
        DESKTOP.browse(new URI(url));
    }
    
}
