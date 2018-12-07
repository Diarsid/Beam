package diarsid.beam.core.modules.io.gui.javafx;

import java.awt.AWTException;
import java.awt.Robot;
import java.util.List;

import javafx.application.Platform;

import diarsid.beam.core.application.starter.Launcher;
import diarsid.beam.core.base.control.flow.ValueFlow;
import diarsid.beam.core.base.control.io.base.console.ConsoleBlockingExecutor;
import diarsid.beam.core.base.control.io.base.console.ConsolePlatform;
import diarsid.beam.core.base.control.io.base.interaction.Message;
import diarsid.beam.core.base.data.DataExtractionException;
import diarsid.beam.core.base.exceptions.WorkflowBrokenException;
import diarsid.beam.core.domain.entities.Picture;
import diarsid.beam.core.domain.entities.Task;
import diarsid.beam.core.modules.DataModule;
import diarsid.beam.core.modules.data.DaoNamedRectangles;
import diarsid.beam.core.modules.io.gui.Gui;
import diarsid.beam.core.modules.io.gui.MessagesGui;
import diarsid.beam.core.modules.io.gui.TasksGui;
import diarsid.beam.core.modules.io.gui.geometry.Screen;
import diarsid.beam.core.modules.io.gui.javafx.console.JavaFXConsolePlatformWindow;
import diarsid.beam.core.modules.io.gui.javafx.screencapturer.ScreenCapturerWindow;
import diarsid.support.configuration.Configuration;

import static java.util.Objects.nonNull;

import static diarsid.beam.core.modules.io.gui.geometry.Screen.screenUsingJavaFXScreenSize;
import static diarsid.beam.core.modules.io.gui.javafx.console.JavaFXConsolePlatformWindow.createAndLaunchJavaFXConsolePlatform;
import static diarsid.beam.core.modules.io.gui.javafx.screencapturer.ScreenCapturerWindow.buildScreenCapturerWindow;

public class GuiJavaFX 
        implements 
                Gui, 
                MessagesGui, 
                TasksGui {
    
    private final DataModule dataModule;
    private final BeamHiddenRoot beamHiddenRoot;
    private final BeamControlWindow beamControlWindow;
    private final WindowManager windowManager;  
    private final PersistableFrameManager persistableFrameManager;
    private final ScreenCapturerWindow screenCapturerWindow;
    private final GuiJavaFXResources guiResources;
    
    private JavaFXConsolePlatformWindow consoleWindow;
    
    public GuiJavaFX(Configuration configuration, Launcher launcher, DataModule dataModule) 
            throws DataExtractionException {
        this.dataModule = dataModule;
        this.beamHiddenRoot = new BeamHiddenRoot();
        this.guiResources = new GuiJavaFXResources(configuration);
        DaoNamedRectangles daoNamed2DPoints = this.dataModule.namedRectangles();
        Screen screen = screenUsingJavaFXScreenSize(configuration);
        this.persistableFrameManager = new PersistableFrameManager(screen, daoNamed2DPoints);
        WindowPersistableFrame controlFrame = 
                this.persistableFrameManager.get("JavaFX Beam Control Window");
        this.beamControlWindow = new BeamControlWindow(
                controlFrame, this.beamHiddenRoot, this.guiResources, launcher);
        
        ControledWindowPositionManager windowPositionManager = new ControledWindowPositionManager();
        this.windowManager = new WindowManager(
                this.beamHiddenRoot, 
                windowPositionManager, 
                this.guiResources);
                
        try {
            Robot robot = new Robot();
            this.screenCapturerWindow = buildScreenCapturerWindow(
                    configuration, robot, this.guiResources, this.beamHiddenRoot);
            Platform.runLater(screenCapturerWindow);
        } catch (AWTException awtException) {
            throw new WorkflowBrokenException(awtException);
        }
    }

    @Override
    public MessagesGui messagesGui() {
        return this;
    }

    @Override
    public TasksGui tasksGui() {
        return this;
    }
    
    @Override
    public void show(Message message) { 
        this.windowManager
                .getBeamPopupWindow()
                .accept(message)
                .launch();
    }

    @Override
    public void show(Task task) {
        this.windowManager
                .getBeamTaskWindow()
                .acceptTask(task)
                .launch();
    }

    @Override
    public void showAllSeparately(List<Task> tasks) {
        tasks.forEach(task -> this.show(task));
    }

    @Override
    public void showAllJointly(String header, List<Task> tasks) {
        this.windowManager
                .getBeamTaskListWindow()
                .accept(header, tasks)
                .launch();
    }
    
    @Override
    public void exitAfterAllWindowsClosed() {
        this.windowManager.setExitAfterAllWindowsClosed();
    }

    @Override
    public ValueFlow<Picture> capturePictureOnScreen(String imageName) {        
        return this.screenCapturerWindow.blockingGetCaptureFor(imageName);
    }

    @Override
    public ConsolePlatform guiConsolePlatformFor(ConsoleBlockingExecutor blockingExecutor) 
            throws DataExtractionException {
        synchronized ( this ) {
            if ( nonNull(this.consoleWindow) ) {
                return this.consoleWindow;
            } else {
                this.consoleWindow = createAndLaunchJavaFXConsolePlatform(
                        this.dataModule, this, this.guiResources, blockingExecutor);
                this.beamControlWindow.setConsoleWindow(this.consoleWindow);
                return this.consoleWindow;
            }
        }
    }
    
    public PersistableFrameManager persistableFrameManager() {
        return this.persistableFrameManager;
    }
}
