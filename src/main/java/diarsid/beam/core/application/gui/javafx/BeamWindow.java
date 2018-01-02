package diarsid.beam.core.application.gui.javafx;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import static javafx.geometry.Pos.CENTER_LEFT;
import static javafx.geometry.Pos.TOP_CENTER;

/**
 *
 * @author Diarsid
 */ 
public abstract class BeamWindow implements Comparable<BeamWindow> {    
    
    private final WindowManager windowManager;
    private final GuiJavaFXResources resources;  
    private final Runnable runnableLaunch;
    
    private Label barHeader;
    private boolean ready;
    private Stage stage;
    
    BeamWindow(GuiJavaFXResources resources, WindowManager windowManager) {
        this.resources = resources;
        this.windowManager = windowManager;
        
        this.ready = false;
        
        this.runnableLaunch = () -> {
            if ( ! this.ready ) {
                this.init();
            }
            this.refresh();
            this.show();
        };
    }   
    
    public final void launch() {
        Platform.runLater(this.runnableLaunch);
    }
    
    protected final Stage parentStage() {
        return this.stage;
    }
    
    protected final void setBarTitle(String barTitle) {
        this.barHeader.setText("Beam > " + barTitle);
    }
    
    protected abstract Pane createBeamWindowContentPane();
    
    protected abstract void refreshBeamWindowState();
    
    private void init() {
        this.createStage();
        this.createWindowScene();
        this.ready = true;
    }   
    
    private void refresh() {
        this.refreshBeamWindowState();
    }
    
    private void show() {
        WindowPosition position = windowManager.getNewWindowPosition();
        if ( (position.getX() != 0) && (Double.isNaN(this.stage.getX())) ) {
            this.stage.setX(position.getX());
            this.stage.setY(position.getY());
        } 
        this.stage.sizeToScene();
        this.stage.show();        
        this.windowManager.reportLastWindowPosition(stage.getX(), stage.getY());
    }
    
    protected abstract void onBeamWindowStageCreated();
    
    private void createStage() {
        this.stage = new Stage();
        this.stage.initStyle(StageStyle.TRANSPARENT);
        this.stage.setAlwaysOnTop(true);
        this.stage.setMinWidth(300);
        this.stage.setMinHeight(200);
        this.stage.setResizable(false);
        
        this.stage.setOnCloseRequest((windowEvent) -> {
            this.close();
        });
        this.onBeamWindowStageCreated();
    }    
    
    protected abstract void onBeamWindowClosed();
        
    protected final void close() {
        this.stage.hide();
        this.windowManager.windowClosed(this);
        this.onBeamWindowClosed();
    } 
    
    private Pane createBar() {
        Label barHeader = new Label("Beam > ");
        barHeader.getStyleClass().add("message-bar-header");        
        
        HBox barBox = new HBox(5);
        barBox.getStyleClass().add("message-bar");
        barBox.setAlignment(CENTER_LEFT);
        barBox.getChildren().addAll(barHeader);
        barBox.setPadding(new Insets(0, 3, 3, 0));
        
        this.barHeader = barHeader;
        return barBox;
    }
    
    private Pane createMainArea() {
        VBox mainAreaVBox = new VBox();
        mainAreaVBox.getStyleClass().add("message-main-area");
        mainAreaVBox.getChildren().add(this.createBeamWindowContentPane());
        return mainAreaVBox;
    }
    
    private void createWindowScene() {      
        VBox messageInnerBox = new VBox();
        messageInnerBox.setAlignment(TOP_CENTER);        
        messageInnerBox.getChildren().addAll(
                this.createBar(), 
                this.createMainArea());
        messageInnerBox.getStyleClass().add("message-inner-box");
        
        VBox messageOuterBox = new VBox();
        messageOuterBox.getStyleClass().add("message-outer-box");        
        messageOuterBox.setAlignment(TOP_CENTER);        
        messageOuterBox.setEffect(this.resources.opacityBlackShadow());
        
        messageOuterBox.getChildren().addAll(messageInnerBox);
        
        Scene scene = new Scene(messageOuterBox);
        scene.setFill(Color.TRANSPARENT);
        scene.getStylesheets().add(this.resources.cssFilePath());
        
        this.stage.setScene(scene);
        this.stage.initOwner(this.windowManager.hiddenRoot().hiddenStageForManageableWindows());
    }
    
    @Override
    public int compareTo(BeamWindow other) {
        double thisSum = this.stage.getX() + this.stage.getY();
        double otherSum = other.stage.getX() + other.stage.getY();
        if ( thisSum < otherSum ) {
            return -1;
        } else if ( thisSum > otherSum ) {
            return 1;
        } else {
            return 0;
        }
    }
}
