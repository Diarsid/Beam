/*
 * project: Beam
 * author: Diarsid
 */

package diarsid.beam.core.modules.innerio.javafxgui;

import java.util.List;
import java.util.PriorityQueue;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import diarsid.beam.core.modules.innerio.Gui;
import diarsid.beam.core.modules.innerio.javafxgui.window.WindowsBuilderWorker;
import diarsid.beam.core.modules.tasks.TaskMessage;

/*
 * Main class for JavaFX based gui.
 * 
 * Serve as connection point with Java FX Application Thread
 * trough encapsulated Platform.runLater() calls. Therefore all
 * window classes used by this program, must implement Runnable. 
 */
public class GuiJavaFX extends Application implements Gui, WindowResources {
    
    // static JavaFX platform initialization.
    // new JFXPanel creation is used for JavaFX platform init.
    static {
        new JFXPanel();
        Platform.setImplicitExit(false);
    }
    
    private final String pathToCssFile = "file:./../config/BeamWindow.css";
    private final WindowController windowsController;  
    private final WindowsBuilder windowsBuilder;
    private final PriorityQueue<ReusableTaskWindow> taskWindows;
    
    private Image taskImage;
    private Image taskIcon;
    private Image errorImage;
    private Image errorIcon;
    private Image messageImage;
    private Image messageIcon;
    
    private DropShadow buttonShadow;
        
    public GuiJavaFX(String imagesLocation) {
        this.windowsController = new WindowController();
        this.windowsBuilder = new WindowsBuilderWorker();
        this.taskWindows = new PriorityQueue<>();
        // any work with Java FX objects is possible
        // only within Java FX Application Thread, so
        // it is necessary to init them in run() {...}
        // that will be executed inside Java FX platform.
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                buttonShadow = new DropShadow();
                buttonShadow.setColor(Color.YELLOW);

                taskIcon = new Image("file:"+ imagesLocation + "task_ico.png");
                taskImage = new Image("file:"+ imagesLocation + "task.png");                
                messageIcon = new Image("file:"+ imagesLocation + "message_ico.png");                
                messageImage = new Image("file:"+ imagesLocation + "message.png");
                errorIcon = new Image("file:"+ imagesLocation + "exception_ico.png");
                errorImage = new Image("file:"+ imagesLocation + "exception.png");
            }
        });
    }
    
    @Override
    public void stopJavaFXPlatform() {
        Platform.exit();
    }
    
    @Override
    public void start(Stage stage) {
        // JavaFX Platform is used for background 
        // notifications only when some important event
        // occurs. Thus, only Platform.runLater() is used.
        //
        // Actually, this method will not be invoked at all 
        // because initialization of Java FX Platform has been
        // perfomed during this class loading and before object
        // of this class can be created.
        // See static init block at the beginning of this class.
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
    public void showMessage(String[] message) {
        Runnable window = this.windowsBuilder.newMessageWindow(
                message, (WindowResources) this, this.windowsController);
        Platform.runLater(window);
    } 
    
    @Override
    public void showError(String[] error) {
        Runnable window = this.windowsBuilder.newErrorWindow(
                error, (WindowResources) this, this.windowsController);
        Platform.runLater(window);
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
}
