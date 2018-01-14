/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.application.gui;

import diarsid.beam.core.base.control.flow.ValueFlow;
import diarsid.beam.core.base.control.io.base.console.ConsoleBlockingExecutor;
import diarsid.beam.core.base.control.io.base.console.ConsolePlatform;
import diarsid.beam.core.domain.entities.Picture;
import diarsid.beam.core.modules.DataModule;

/**
 *
 * @author Diarsid
 */
public interface InteractionGui {
    
    ValueFlow<Picture> capturePictureOnScreen(String imageName);
    
    ConsolePlatform guiConsolePlatformFor(
            DataModule dataModule, ConsoleBlockingExecutor blockingExecutor);
    
}
