/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.tasks;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Objects;

import static diarsid.beam.core.modules.tasks.Task.OUTPUT_TIME_PATTERN;

/**
 *
 * @author Diarsid
 */
public class TaskMessage implements Serializable, Comparable<TaskMessage> {
    
    private final LocalDateTime time;
    private final String[] content;
    
    public TaskMessage(LocalDateTime time, String[] content) {
        this.time = time;
        this.content = content;
    }

    public String getTime() {
        return this.time.format(DateTimeFormatter.ofPattern(OUTPUT_TIME_PATTERN));
    }

    public String[] getContent() {
        return this.content;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + Objects.hashCode(this.time);
        hash = 47 * hash + Arrays.deepHashCode(this.content);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TaskMessage other = (TaskMessage) obj;
        if (!Objects.equals(this.time, other.time)) {
            return false;
        }
        if (!Arrays.deepEquals(this.content, other.content)) {
            return false;
        }
        return true;
    }
    
    @Override
    public int compareTo(TaskMessage task) {
        if (this.time.isBefore(task.time)) {
            return -1;
        } else if (this.time.isAfter(task.time)) {
            return 1;
        } else {
            return 0;  
        }
    }
}
