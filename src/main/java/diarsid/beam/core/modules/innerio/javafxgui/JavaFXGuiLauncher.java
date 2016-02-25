/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.innerio.javafxgui;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;

import diarsid.beam.core.modules.innerio.Gui;

/**
 *
 * @author Diarsid
 */
public class JavaFXGuiLauncher {
    
    // static JavaFX platform initialization.
    // new JFXPanel creation is used for JavaFX platform init.
    static {
        new JFXPanel();
        Platform.setImplicitExit(false);
    }
    
    public JavaFXGuiLauncher() {
    }
    
    public Gui buildGui(String imagesLocation) {
        GuiJavaFX gui = new GuiJavaFX(imagesLocation);
        new Thread(new Runnable() {
            @Override
            public void run() {                
                gui.go();
            }
        });
        return gui;
    }
}
