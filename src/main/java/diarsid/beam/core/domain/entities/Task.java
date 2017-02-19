/*
 * project: Beam
 * author: Diarsid
 */
package diarsid.beam.core.domain.entities;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import diarsid.beam.core.base.control.io.base.interaction.ConvertableToVariant;
import diarsid.beam.core.base.control.io.base.interaction.TaskMessage;
import diarsid.beam.core.base.control.io.base.interaction.Variant;

import static java.time.LocalDateTime.now;

import static diarsid.beam.core.base.util.CollectionsUtils.hasMany;
import static diarsid.beam.core.domain.entities.Tasks.NEW_TASK_ID;
import static diarsid.beam.core.domain.entities.Tasks.TIME_PRINT_FORMAT;
import static diarsid.beam.core.domain.entities.Tasks.stringifyTaskText;

/*
 * Class describes different tasks user able to set.
 * Task object represent some task in future which should be performed in future.  
 * It can be simple text message like reminder to do something. Or it can be a  
 * scheduled command which should be excecuted by program executor part when specified 
 * time comes.
 * 
 * All tasks stores in database whith status TRUE until they time will come. After task 
 * is performed it remains in database but has now status FALSE. Tasks of all statuses  
 * can be removed by they status TRUE or FALSE.
 * 
 */

public abstract class Task    
        implements 
                ConvertableToVariant, 
                Comparable<Task>, 
                Serializable {
    
    private final int id;       
    private boolean activeStatus;
    private LocalDateTime time;
    private final TaskRepeat type;
    private final List<String> text;

    Task(int id, LocalDateTime time, boolean activeStatus, TaskRepeat type, List<String> text) {
        this.id = id;
        this.time = time;
        this.activeStatus = activeStatus;
        this.type = type;
        this.text = text;
    }
    
    @Override
    public Variant toVariant(int variantIndex) {
        String displayText = this.stringifyTime() + " " + this.text.get(0);
        if ( hasMany(this.text) ) {
            displayText = displayText + "...";
        }         
        return new Variant(String.valueOf(this.id), displayText, variantIndex);
    }    
    
    public abstract void switchTime();
    
    public final boolean isNew() {
        return this.id == NEW_TASK_ID;
    }
    
    public final boolean isFuture() {
        return this.time.isAfter(now());
    }
    
    public final boolean isSaved() {
        return this.id != NEW_TASK_ID;
    }
    
    public final TaskMessage toTimeMessage() {
        return new TaskMessage(this.stringifyTime(), this.text);
    }
    
    public final String stringifyText() {
        return stringifyTaskText(this.text);
    }
            
    public final String stringifyTime() {
        return this.time.format(TIME_PRINT_FORMAT);
    }

    public final int getId() {
        return this.id;
    }

    public final LocalDateTime getTime() {
        return this.time;
    }
    
    public List<String> getText() {
        return this.text;
    }

    public final boolean getStatus() {
        return this.activeStatus;
    }
    
    protected final void setStatus(boolean status) {
        this.activeStatus = status;
    }
    
    protected final void setTime(LocalDateTime newTime) {
        this.time = newTime;
    }

    public final TaskRepeat type() {
        return this.type;
    }    
    
    @Override
    public final int compareTo(Task task) {
        if (this.time.isBefore(task.time)) {
            return -1;
        } else if (this.time.isAfter(task.time)) {
            return 1;
        } else {
            return 0;  
        }
    }

    @Override
    public final int hashCode() {
        if ( this.isNew() ) {
            int hash = 3;
            hash = 43 * hash + Objects.hashCode(this.time);
            hash = 43 * hash + Objects.hashCode(this.type);
            hash = 43 * hash + Objects.hashCode(this.text);
            return hash;
        } else {
            int hash = 3;
            hash = 43 * hash + this.id;
            return hash;
        }
    }

    @Override
    public final boolean equals(Object obj) {
        if ( this == obj ) {
            return true;
        }
        if ( obj == null ) {
            return false;
        }
        if ( getClass() != obj.getClass() ) {
            return false;
        }
        final Task other = ( Task ) obj;
        
        if ( this.isNew() ) {
            if ( !Objects.equals(this.time, other.time) ) {
                return false;
            }
            if ( this.type != other.type ) {
                return false;
            }
            if ( !Objects.equals(this.text, other.text) ) {
                return false;
            }
            return true;
        } else {
            return this.id == other.id;
        }
    }
    
    
}