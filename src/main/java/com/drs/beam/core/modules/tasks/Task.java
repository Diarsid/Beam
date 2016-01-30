/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam.core.modules.tasks;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Objects;
import java.util.StringJoiner;

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
 * Task`s objects can have one of types:
 * USUAL_TASK - means this is disposable task, which will be executed just once an
 * than it will change his status to FALSE.
 * CALENDAR_EVENT - means this task is not disposable and it will be performed once 
 * per a year in it`s time during current year. Once task is performed his status 
 * remains TRUE but it`s next execution time increases by 1 year.
 */
public class Task implements Comparable<Task>, Serializable{
// ________________________________________________________________________________________
//                                       Fields                                            
// ¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯
    
    public static final String OUTPUT_TIME_PATTERN = "HH:mm (dd-MM-uuuu)";
    public static final String DB_TIME_PATTERN = "uuuu-MM-dd HH:mm";
    public static final String CALENDAR_EVENT = "event";
    public static final String USUAL_TASK = "task";
    public static final String DB_TASK_DELIMITER = "~}";
    
    private final LocalDateTime time;
    private final String[] content;
    private final String type;
    
// ________________________________________________________________________________________
//                                      Constructor                                        
// ¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯
    
    public Task(String type, LocalDateTime time, String[] content) {
        this.type = type;
        this.time = time;
        this.content = content;
    }
    
    public Task(String type, LocalDateTime time, String contentString) {
        this.type = type;
        this.time = time;
        this.content = contentString.split(Task.DB_TASK_DELIMITER);
    }

// ________________________________________________________________________________________
//                                       Methods                                           
// ¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯
    
    public LocalDateTime getTime() {
        return this.time;
    }
    
    public String getTimeOutputString(){
        return this.time.format(DateTimeFormatter.ofPattern(OUTPUT_TIME_PATTERN));
    }   
    
    public String getTimeDBString(){
        return this.time.format(DateTimeFormatter.ofPattern(DB_TIME_PATTERN));
    }
    
    /*
     * Converts content as String[] into one String in order to write it into one TEXT 
     * database field. Uses StringJoiner object with char sequence '~}' as a delimiter 
     * between single strings.
     */
    public String getContentForStoring(){
        return String.join(DB_TASK_DELIMITER, this.content);
    }
    
    public String[] getContent(){
        return this.content;
    }
    
    public String getType(){
        return this.type;
    }    
    
    @Override
    public int compareTo(Task task){
      if (this.time.isBefore(task.time)) 
          return -1;
      else if (this.time.isAfter(task.time))
          return 1;
      else
          return 0;      
    }

    @Override
    public String toString(){
        StringJoiner sj = new StringJoiner(" ");
        for (String s : this.content){
            sj.add(s);
        }
        return sj.toString();
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 17 * hash + Objects.hashCode(this.time);
        hash = 17 * hash + Arrays.deepHashCode(this.content);
        hash = 17 * hash + Objects.hashCode(this.type);
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
        final Task other = (Task) obj;
        if (!Objects.equals(this.time, other.time)) {
            return false;
        }
        if (!Arrays.deepEquals(this.content, other.content)) {
            return false;
        }
        if (!Objects.equals(this.type, other.type)) {
            return false;
        }
        return true;
    }    
}