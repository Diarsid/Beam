/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.control.io.interpreter;

/**
 *
 * @author Diarsid
 */
public enum RecognizerPriority {
    
    LOWEST  (1000),
    LOWER   (2000),
    LOW     (3000),
    MEDIUM  (4000),
    HIGH    (5000), 
    HIGHER  (6000),
    HIGHEST (7000);

    private final int priorityValue;
    
    private RecognizerPriority(int value) {
        this.priorityValue = value;
    }
    
    public int value() {
        return this.priorityValue;
    }
    
    public static int higherThan(RecognizerPriority priority) {
        return priority.priorityValue + 50;
    }
    
    public static int lowerThan(RecognizerPriority priority) {
        return priority.priorityValue - 50;
    }
    
    public static int higherThan(int priority) {
        return priority + 50;
    }
    
    public static int lowerThan(int priority) {
        return priority - 50;
    }
    
    public static int between(int one, int another) {
        return ( ( one + another ) / 2 );
    }
}
