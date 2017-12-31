package diarsid.beam.core.application.gui.javafx;

import java.awt.AWTException;
import java.awt.Robot;
import java.util.List;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;

import diarsid.beam.core.application.environment.Configuration;
import diarsid.beam.core.application.gui.Gui;
import diarsid.beam.core.application.gui.InteractionGui;
import diarsid.beam.core.application.gui.OutputMessagesGui;
import diarsid.beam.core.application.gui.OutputTasksGui;
import diarsid.beam.core.application.gui.javafx.screencapturer.ScreenCapturerWindow;
import diarsid.beam.core.base.control.flow.ValueFlow;
import diarsid.beam.core.base.control.io.base.interaction.Message;
import diarsid.beam.core.base.exceptions.WorkflowBrokenException;
import diarsid.beam.core.domain.entities.Picture;
import diarsid.beam.core.domain.entities.Task;

import static diarsid.beam.core.application.gui.javafx.screencapturer.ScreenCapturerWindow.buildScreenCapturerWindow;

public class GuiJavaFX 
        implements 
                Gui, 
                OutputMessagesGui, 
                OutputTasksGui, 
                InteractionGui {
    
    // static JavaFX platform initialization.
    // new JFXPanel creation is used for JavaFX platform init.
    static {
        new JFXPanel();
        Platform.setImplicitExit(false);
    }    
    
    private final WindowManager windowManager;  
    private final ScreenCapturerWindow screenCapturerWindow;
    private final GuiJavaFXResources guiResources;
    
    public GuiJavaFX(Configuration configuration) {        
        this.guiResources = new GuiJavaFXResources(configuration);
        WindowPositionManager windowPositionManager = new WindowPositionManager();
        this.windowManager = new WindowManager(windowPositionManager, this.guiResources);        
        
        try {
            Robot robot = new Robot();
            this.screenCapturerWindow = buildScreenCapturerWindow(
                    configuration, robot, this.guiResources);
            Platform.runLater(screenCapturerWindow);
        } catch (AWTException aWTException) {
            throw new WorkflowBrokenException(aWTException);
        }
    }
    
    @Override
    public GuiJavaFXResources resources() {
        return this.guiResources;
    }

    @Override
    public OutputMessagesGui messagesGui() {
        return this;
    }

    @Override
    public OutputTasksGui tasksGui() {
        return this;
    }

    @Override
    public InteractionGui interactionGui() {
        return this;
    }
    
    @Override
    public void show(Message message) { 
        BeamPopupWindow popupWindow = this.windowManager.getBeamPopupWindow();
        popupWindow.accept(message);
        popupWindow.launch();
    }

    @Override
    public void show(Task task) {
        BeamTaskWindow taskWindow = this.windowManager.getBeamTaskWindow();
        taskWindow.acceptTask(task);
        taskWindow.launch();
    }

    @Override
    public void showAllSeparately(List<Task> tasks) {
        tasks.forEach(task -> this.show(task));
    }

    @Override
    public void showAllJointly(String header, List<Task> tasks) {
        BeamTaskListWindow taskListWindow = this.windowManager.getBeamTaskListWindow();
        taskListWindow.accept(header, tasks);
        taskListWindow.launch();
    }
    
    @Override
    public void exitAfterAllWindowsClosed() {
        this.windowManager.setExitAfterAllWindowsClosed();
    }

    @Override
    public ValueFlow<Picture> capturePictureOnScreen(String imageName) {        
        return this.screenCapturerWindow.blockingGetCaptureFor(imageName);
    }
}
