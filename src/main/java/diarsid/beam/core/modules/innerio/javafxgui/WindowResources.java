/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.innerio.javafxgui;

import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;

/**
 *
 * @author Diarsid
 */
public interface WindowResources {

    DropShadow getButtonShadow();
    
    String getPathToCssFile();

    Image getErrorIconImage();

    Image getErrorImage();

    Image getMessageIconImage();

    Image getMessageImage();    

    Image getTaskIconImage();

    Image getTaskImage(); 
    
    void addTaskWindowToReusable(ReusableTaskWindow window);
}
