/*
 * project: Beam
 * author: Diarsid
 */

package diarsid.beam.core.application.gui.javafx;

import java.awt.AWTException;
import java.awt.Robot;
import java.util.List;
import java.util.PriorityQueue;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import diarsid.beam.core.application.environment.Configuration;
import diarsid.beam.core.application.gui.Gui;
import diarsid.beam.core.application.gui.javafx.window.WindowsBuilderWorker;
import diarsid.beam.core.application.gui.javafx.screencapturer.ScreenCapturerWindow;
import diarsid.beam.core.base.control.io.base.interaction.Message;
import diarsid.beam.core.base.control.io.base.interaction.TaskMessage;
import diarsid.beam.core.base.exceptions.WorkflowBrokenException;
import diarsid.beam.core.domain.entities.Picture;

import static diarsid.beam.core.application.gui.javafx.screencapturer.ScreenCapturerWindow.buildScreenCapturerWindow;
import static diarsid.beam.core.base.control.io.base.interaction.Message.MessageType.ERROR;
import static diarsid.beam.core.base.control.io.base.interaction.Message.MessageType.INFO;

import diarsid.beam.core.base.control.flow.ValueFlow;

/*
 * Main class for JavaFX based gui.
 * 
 * Serve as connection point with Java FX Application Thread
 * trough encapsulated Platform.runLater() calls. Therefore all
 * window classes used by this program, must implement Runnable. 
 */
public class GuiJavaFX 
        implements 
                Gui, 
                WindowResources {
    
    // static JavaFX platform initialization.
    // new JFXPanel creation is used for JavaFX platform init.
    static {
        new JFXPanel();
        Platform.setImplicitExit(false);
    }
    
    private final String pathToCssFile = "file:./../config/beam.css";
    private final WindowController windowsController;  
    private final WindowsBuilder windowsBuilder;
    private final PriorityQueue<ReusableTaskWindow> taskWindows;
    private final ScreenCapturerWindow screenCapturerWindow;
    
    private Image taskImage;
    private Image taskIcon;
    private Image errorImage;
    private Image errorIcon;
    private Image messageImage;
    private Image messageIcon;
    
    private DropShadow buttonShadow;
    
    public GuiJavaFX(Configuration configuration) {
        this.windowsController = new WindowController();
        this.windowsBuilder = new WindowsBuilderWorker();
        this.taskWindows = new PriorityQueue<>();
        // any work with Java FX objects is possible
        // only within Java FX Application Thread, so
        // it is necessary to init them inside of Runnable.run() {...}
        // that will be executed inside the Java FX platform own thread.
        String imagesLocation = configuration.asString("ui.images.resources");
        Platform.runLater(() -> {
            buttonShadow = new DropShadow();
            buttonShadow.setColor(Color.YELLOW);

            taskIcon = new Image("file:"+ imagesLocation + "task_ico.png");
            taskImage = new Image("file:"+ imagesLocation + "task.png");                
            messageIcon = new Image("file:"+ imagesLocation + "message_ico.png");                
            messageImage = new Image("file:"+ imagesLocation + "message.png");
            errorIcon = new Image("file:"+ imagesLocation + "exception_ico.png");
            errorImage = new Image("file:"+ imagesLocation + "exception.png");           
        });
        try {
            Robot robot = new Robot();
            this.screenCapturerWindow = buildScreenCapturerWindow(configuration, robot, this);
            Platform.runLater(screenCapturerWindow);
        } catch (AWTException aWTException) {
            throw new WorkflowBrokenException(aWTException);
        }
    }
    
    @Override
    public void showTask(TaskMessage task) { 
        ReusableTaskWindow window;
        synchronized ( this.taskWindows ) {            
            if ( this.taskWindows.isEmpty() ) {
                window = this.windowsBuilder.newTaskWindow(
                        task, (WindowResources) this, this.windowsController);
            } else {
                window = this.taskWindows.poll();
                window.reuseWithNewTask(task);
            }
        }
        Platform.runLater(window);
    }
    
    @Override
    public void showTasks(String period, List<TaskMessage> tasks) {         
        Runnable window = this.windowsBuilder.newNotificationWindow(
                period, tasks, (WindowResources) this, this.windowsController);
        Platform.runLater(window);        
    }
    
    @Override
    public void showMessage(Message message) {
        switch ( message.getType() ) {
            case INFO : {
                Platform.runLater(this.windowsBuilder.newMessageWindow(
                        message.toText(), 
                        (WindowResources) this, 
                        this.windowsController));
                break;
            }
            case ERROR : {
                Platform.runLater(this.windowsBuilder.newErrorWindow(
                        message.toText(), 
                        (WindowResources) this, 
                        this.windowsController));
                break;
            }
            default : {
                // show as usual message
                Platform.runLater(this.windowsBuilder.newMessageWindow(
                        message.toText(), 
                        (WindowResources) this, 
                        this.windowsController));
            }
        }        
    } 
    
    @Override
    public void exitAfterAllWindowsClosed() {
        this.windowsController.setExitAfterAllWindowsClosed();
    }
    
    @Override
    public void addTaskWindowToReusable(ReusableTaskWindow window) {
        synchronized ( this.taskWindows ) {
            this.taskWindows.offer(window);
        }        
    }
    
    @Override
    public DropShadow getButtonShadow() {
        return this.buttonShadow;
    }
    
    @Override
    public Image getTaskImage() {
        return this.taskImage;
    }
    
    @Override
    public Image getErrorImage() {
        return this.errorImage;
    }
    
    @Override
    public Image getMessageImage() {
        return this.messageImage;
    }
    
    @Override
    public Image getTaskIconImage() {
        return this.taskIcon;
    }
    
    @Override
    public Image getErrorIconImage() {
        return this.errorIcon;
    }
    
    @Override
    public Image getMessageIconImage() {
        return this.messageIcon;
    }
    
    @Override
    public String getPathToCssFile() {
        return this.pathToCssFile;
    }

    @Override
    public ValueFlow<Picture> capturePictureOnScreen(String imageName) {        
        return this.screenCapturerWindow.blockingGetCaptureFor(imageName);
    }
}