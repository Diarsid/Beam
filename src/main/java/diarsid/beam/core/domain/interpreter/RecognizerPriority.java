/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.domain.interpreter;

/**
 *
 * @author Diarsid
 */
public enum RecognizerPriority {
    
    LOWEST  (1000),
    LOW     (2000),
    MEDIUM  (3000),
    HIGH    (4000), 
    HIGHEST (5000);

    private final int priorityValue;
    
    private RecognizerPriority(int value) {
        this.priorityValue = value;
    }
    
    public int value() {
        return this.priorityValue;
    }
    
    public static int slightlyHigherThan(RecognizerPriority priority) {
        return priority.priorityValue + 50;
    }
    
    public static int slightlyLowerThan(RecognizerPriority priority) {
        return priority.priorityValue - 50;
    }
    
    public static int slightlyHigherThan(int priority) {
        return priority + 50;
    }
    
    public static int slightlyLowerThan(int priority) {
        return priority - 50;
    }
    
    public static int between(int one, int another) {
        return ( ( one + another ) / 2 );
    }
}
