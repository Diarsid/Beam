/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.application.gui.jkavafx.screencapturer;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import static diarsid.beam.core.base.util.Logs.debug;

import diarsid.beam.core.base.control.flow.ValueFlow;

import static diarsid.beam.core.base.control.flow.Flows.valueFlowCompletedWith;
import static diarsid.beam.core.base.control.flow.Flows.valueFlowCompletedWith;
import static diarsid.beam.core.base.control.flow.Flows.valueFlowCompletedWith;
import static diarsid.beam.core.base.control.flow.Flows.valueFlowCompletedWith;
import static diarsid.beam.core.base.control.flow.Flows.valueFlowFail;


/**
 *
 * @author Diarsid
 */
public class ScreenCapturer {
    
    static {
        ImageIO.setUseCache(false);        
    }
    
    private final Robot robot;
    private final boolean doResize;

    public ScreenCapturer(Robot robot, boolean doResize) {
        this.robot = robot;
        this.doResize = doResize;
    }
    
    ValueFlow<byte[]> captureRectangle(Rectangle rectangle) {
        BufferedImage image = this.captureToBufferedImage(rectangle);
        
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", baos);
            baos.flush();                 
            byte[] imageBytes = baos.toByteArray();
            debug("[SCREEN CAPTURER] bytes: " + imageBytes.length);
            return valueFlowCompletedWith(imageBytes);
        } catch (IOException e) {
            // TODO MEDIUM
            e.printStackTrace();
            return valueFlowFail("cannot capture image!");
        }
    }
    
    private BufferedImage captureToBufferedImage(Rectangle rectangle) {
        BufferedImage image = this.robot.createScreenCapture(rectangle);
        
        if ( this.doResize && image.getWidth() > 110 ) {
            image = this.resize(image);
        } 
        
        return image;
    }
    
    private BufferedImage resize(BufferedImage image) {        
        BufferedImage resizedImg = new BufferedImage(110, 80, image.getType());
        Graphics2D graphics = resizedImg.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics.drawImage(image, 0, 0, 110, 80, null);
        graphics.dispose();
        return resizedImg;
    }
}
