/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam.tasks;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Objects;
import java.util.StringJoiner;

public class Task implements Comparable<Task>, Serializable{
    // Fields =============================================================================
    public static final String OUTPUT_TIME_PATTERN = "HH:mm (dd-MM-uuuu)";
    public static final String DB_TIME_PATTERN = "uuuu-MM-dd HH:mm";
    public static final String CALENDAR_EVENT = "event";
    public static final String USUAL_TASK = "task";
    
    private static int idGen = 0;
    
    private final int id;
    private final LocalDateTime time;
    private final String[] content;
    private final String type;
    
    // Constructors =======================================================================
    private Task(String type, LocalDateTime time, String... content) {
        this.type = type;
        this.id = getNextID();
        this.time = time;
        this.content = content;
    }
    
    private Task(String type, int id, LocalDateTime time, String content) {
        this.type = type;
        this.id = id;
        this.time = time;
        this.content = content.split("~}");
    }

    // Methods ============================================================================

    public static Task newTask(String type, LocalDateTime time, String... content){
        return new Task(type, time, content);
    }
    public static Task restoreTask(String type, int id, LocalDateTime time, String content){
        return new Task(type, id, time, content);
    }   
    private static int getNextID(){
        idGen++;
        return idGen;
    }
    static void setInitId(int lastUsedId){
        idGen = lastUsedId;
    }
    
    // Getters ----------------------------------------------------------------------------
    public int getId(){
        return this.id;
    }
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
        StringJoiner sj = new StringJoiner("~}");
        for (String s : this.content){
            sj.add(s);
        }
        return sj.toString();
    }
    public String[] getContent(){
        return this.content;
    }
    public String getType(){
        return this.type;
    }    
    
    /*
    * Implementation of abstract method compareTo() in Comparable interface
    */
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
        hash = 17 * hash + this.id;
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
        if (this.id != other.id) {
            return false;
        }
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