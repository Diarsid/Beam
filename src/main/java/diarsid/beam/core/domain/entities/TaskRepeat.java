/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.domain.entities;

import java.util.List;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

/**
 *
 * @author Diarsid
 */
public enum TaskRepeat {
    
    NO_REPEAT ("no repeat"),
    HOURLY_REPEAT ("hourly repeat"),
    DAILY_REPEAT ("every day of week"),
    MONTHLY_REPEAT ("every month"),
    YEARLY_REPEAT ("every year"),
    
    UNDEFINED_REPEAT ("undefined"); 
    
    private final String displayName;
    
    private TaskRepeat(String name) {
        this.displayName = name;
    }
    
    public boolean isDefined() {
        return ! this.equals(UNDEFINED_REPEAT);
    }
    
    public boolean isUndefined() {
        return this.equals(UNDEFINED_REPEAT);
    }
    
    public boolean isOneOf(TaskRepeat... others) {
        return stream(others)
                .anyMatch(other -> this.equals(other));
    }
    
    public boolean isNot(TaskRepeat other) {
        return ! this.equals(other);
    }
    
    public boolean isNot(TaskRepeat... others) {
        return stream(others)
                .noneMatch(other -> this.equals(other));
    }
    
    public static List<String> repeatNames() {
        return stream(values())
                .filter(repeat -> repeat.isDefined())
                .map(repeat -> repeat.displayName)
                .collect(toList());
    }
    
    public static TaskRepeat repeatByItsName(String displayName) {
        return stream(values())
                .filter(repeat -> displayName.equals(repeat.displayName))
                .findFirst()
                .orElseGet(() -> UNDEFINED_REPEAT);
    }
}
