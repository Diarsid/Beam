/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.application.gui.jkavafx.screencapturer;

import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;

import javax.imageio.ImageIO;

import static diarsid.beam.core.base.util.Logs.debug;


/**
 *
 * @author Diarsid
 */
public class ScreenCapturer {
    
    private final Robot robot;

    public ScreenCapturer(Robot robot) {
        this.robot = robot;
        ImageIO.setUseCache(false);
    }
    
    Optional<byte[]> captureRectangle(Rectangle rectangle) {
        BufferedImage image = this.robot.createScreenCapture(rectangle);
        
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", baos);
            baos.flush();                 
            byte[] imageBytes = baos.toByteArray();
            debug("[SCREEN CAPTURER] bytes: " + imageBytes.length);
            return Optional.of(imageBytes);
        } catch (IOException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }
}
