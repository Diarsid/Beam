/*
 * project: Beam
 * author: Diarsid
 */

package com.drs.beam.modules.io.gui.swing;

import com.drs.beam.modules.io.gui.Gui;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 *
 * @author Diarsid
 */
public class ExceptionWindowSwing {
    // Fields =============================================================================
    private final JFrame frame = new JFrame();
    
    // Constructors =======================================================================
    public ExceptionWindowSwing() {
    }
    
    // Methods ============================================================================
    
    private void disposeFrame(){
        frame.dispose();
    }
    
    public void invoke(Exception e, boolean isCritical) {
        Font font = new Font("Arial", Font.PLAIN, 12);
        ImageIcon icon = new ImageIcon(Gui.IMAGES_LOCATION+"exception_ico.jpeg");
          
        frame.setMinimumSize(new Dimension(300, 50));
        frame.setResizable(false);
        frame.setTitle("Exception");
        frame.setFont(font);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setIconImage(icon.getImage());
                
        JPanel mainPanel = new JPanel();
        JPanel infoPanel = new JPanel();
        JPanel labelPanel = new JPanel();
        JPanel buttonPanel = new JPanel(); 
                
        mainPanel.setLayout(new BorderLayout(0, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10,20,10,10));
        labelPanel.setBorder(BorderFactory.createEmptyBorder(0,20,0,0));
        
        labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.Y_AXIS));
        labelPanel.setAlignmentY(Component.TOP_ALIGNMENT);
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        
        infoPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        
        Color color = new Color(164, 0, 0);
        JLabel exceptionLabel = new JLabel(e.getMessage());
        exceptionLabel.setFont(font);
        exceptionLabel.setForeground(color);
        labelPanel.add(exceptionLabel);
        JLabel traceLabel = new JLabel("Stack trace :");
        traceLabel.setForeground(color);
        traceLabel.setFont(font);
        labelPanel.add(traceLabel);
        for(StackTraceElement elem : e.getStackTrace()){
            JLabel label = new JLabel(elem.toString());
            label.setFont(font);
            label.setForeground(color);
            labelPanel.add(label);
        }
        
        
        JButton button = new JButton("Ok");
        button.setFont(font);
        button.setHorizontalAlignment(SwingConstants.CENTER);
        button.setVerticalAlignment(SwingConstants.CENTER);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(100, 30));
        buttonPanel.add(button);        
        button.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent event){
                if (isCritical){
                    System.exit(1);
                } else {
                    disposeFrame();
                }                
            }
        });
        
        ImageIcon image = new ImageIcon(Gui.IMAGES_LOCATION+"exception.jpeg");
        JLabel imageLabel = new JLabel(image);
        
        infoPanel.add(imageLabel);
        infoPanel.add(labelPanel);
        
        mainPanel.add(infoPanel, BorderLayout.WEST);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        frame.add(mainPanel);
        frame.pack();
        frame.toFront();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

}
