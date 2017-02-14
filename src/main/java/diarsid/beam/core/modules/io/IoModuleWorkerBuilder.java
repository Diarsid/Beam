/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.io;


import diarsid.beam.core.modules.IoModule;
import diarsid.beam.core.modules.io.javafxgui.GuiJavaFX;

import com.drs.gem.injector.module.GemModuleBuilder;

/**
 *
 * @author Diarsid
 */
class IoModuleWorkerBuilder implements GemModuleBuilder<IoModule> {
    
    IoModuleWorkerBuilder() {
    }
    
    @Override
    public IoModule buildModule() {
        Gui gui = new GuiJavaFX("./../res/images/");
        OuterIoEnginesHolder ioEnginesHolder = new OuterIoEnginesHolder();
        MainInnerIoEngine mainIo = new MainInnerIoEngine(ioEnginesHolder, gui);
        return new IoModuleWorker(ioEnginesHolder, mainIo);
    }
}
