/*
 * project: Beam
 * author: Diarsid
 */
package diarsid.beam.core.modules.tasks;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import diarsid.beam.core.exceptions.TemporaryCodeException;

import static diarsid.beam.core.modules.tasks.TaskType.DAILY;
import static diarsid.beam.core.modules.tasks.TaskType.HOURLY;
import static diarsid.beam.core.modules.tasks.TaskType.MONTHLY;
import static diarsid.beam.core.modules.tasks.TaskType.USUAL;
import static diarsid.beam.core.modules.tasks.TaskType.YEARLY;

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

public class Task implements Comparable<Task>, Serializable {
    
    public static final String OUTPUT_TIME_PATTERN = "HH:mm (dd-MM-uuuu)";
    
    private final int id;
    private LocalDateTime time;
    private final String[] content;
    private boolean activeStatus;
    private final TaskType type;
    private final Set<Integer> activeHours;
    private final Set<Integer> activeDays;
    
    public static Task restoreTask(
            int id, 
            TaskType type, 
            LocalDateTime time, 
            String[] content, 
            boolean status, 
            Integer[] days, 
            Integer[] hours) {
        
        return new Task(id, type, time, content, status, 
                new HashSet<>(Arrays.asList(days)),
                new HashSet<>(Arrays.asList(hours)));
    }
    
    public static Task newTask(TaskType type, LocalDateTime time, String[] content, 
            Set<Integer> days, Set<Integer> hours) {
        
        return new Task(type, time, content, days, hours);
    }
        
    private Task(TaskType type, LocalDateTime time, String[] content, 
            Set<Integer> days, Set<Integer> hours) {
                
        this.id = -1;
        this.type = type;
        this.time = time;
        this.content = content;        
        this.activeStatus = true;
        this.activeHours = hours;
        this.activeDays = days;
    }
    
    private Task(int id, TaskType type, LocalDateTime time, String[] content, 
            boolean status, Set<Integer> days, Set<Integer> hours) {
        
        this.id = id;
        this.type = type;
        this.time = time;
        this.content = content;
        this.activeStatus = status;
        this.activeHours = hours;
        this.activeDays = days;
    }
    
    TaskMessage generateMessage() {
        return new TaskMessage(
                this.time,
                this.content);
    }

    public static String getOUTPUT_TIME_PATTERN() {
        return OUTPUT_TIME_PATTERN;
    }

    public int getId() {
        return id;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public String[] getContent() {
        return content;
    }

    public boolean getStatus() {
        return activeStatus;
    }

    public TaskType getType() {
        return type;
    }

    public Integer[] getActiveHours() {
        return activeHours.toArray(new Integer[activeHours.size()]);
    }

    public Integer[] getActiveDays() {
        return activeDays.toArray(new Integer[activeDays.size()]);
    }
    
    void modifyAccordingToType() {
        typeSwitch: switch (this.type) {
            case USUAL : {
                // if task is usual one-off task, it will be only 
                // disabled and stored in DB as non-active past task.
                this.activeStatus = false;
                break typeSwitch;
            }                
            case MONTHLY : {
                // if task is monthly, its time should be reseted to
                // + 1 month from its current time and stored in DB.
                this.time = this.time.plusMonths(1);
                break typeSwitch;
            }
            case YEARLY : {
                // if task is yearly, its time should be reseted to
                // + 1 year from its current time and stored in DB.
                this.time = this.time.plusYears(1);
                break typeSwitch;
            }
            case HOURLY : {
                // if task is hourly, it can be performed only at 
                // hours and days that have been permitted for this
                // particular task during its creation.
                //
                // If new execution time of this task does not 
                // contained in list of permitted days it will be
                // postponed to the next day with 0 hour and verified again.
                //
                // If new execution time of this task does not 
                // contained in list of permitted hours it will be 
                // postponed to the next hour and verified again.
                this.time = this.time.plusHours(1);
                if (this.time.isBefore(LocalDateTime.now())) {
                    this.time = LocalDateTime.now().withMinute(this.time.getMinute());
                    if (this.time.isBefore(LocalDateTime.now())) {
                        this.time = this.time.plusHours(1);
                    }
                }
                boolean dayIsLegal = this.activeDays.contains(
                        this.time.getDayOfWeek().getValue());
                boolean hourIsLegal = this.activeHours.contains(
                        this.time.getHour());
                
                while ( ( ! dayIsLegal) || (! hourIsLegal) ) {
                    while ( ! dayIsLegal ) {
                        this.time = this.time.plusDays(1).withHour(0);
                        dayIsLegal = this.activeDays.contains(
                                this.time.getDayOfWeek().getValue());
                    }
                    while ( ! hourIsLegal ) {
                        this.time = time.plusHours(1);
                        hourIsLegal = this.activeHours.contains(
                                this.time.getHour());
                    }
                }                                
                break typeSwitch;
            } 
            case DAILY : {
                // if task is daily, it can be performed only at 
                // days that have been permitted for this
                // particular task during its creation.
                //
                // If new execution time of this task does not 
                // contained in list of permitted days it will be
                // postponed to the next day with and verified again.
                this.time = this.time.plusDays(1);
                if (this.time.isBefore(LocalDateTime.now())) {
                    this.time = LocalDateTime.now()
                            .withHour(this.time.getHour())
                            .withMinute(this.time.getMinute());
                    if (this.time.isBefore(LocalDateTime.now())) {
                        this.time = this.time.plusDays(1);
                    }
                }
                boolean dayIsLegal = this.activeDays.contains(
                        this.time.getDayOfWeek().getValue());
                
                while ( ! dayIsLegal ) {
                    this.time = this.time.plusDays(1);
                    dayIsLegal = this.activeDays.contains(
                            this.time.getDayOfWeek().getValue());
                }
                break typeSwitch;
            }
            default: {
                // It seems that this block is actualy unreachable now.
                // So I can even devide by zero!
                int zero = 42 - 42;
                int numberOfGod = 42 / zero;
                // ... and throw some pretty exceptions!
                throw new TemporaryCodeException();
            }
        }
    }
    
    @Override
    public int compareTo(Task task) {
        if (this.time.isBefore(task.time)) {
            return -1;
        } else if (this.time.isAfter(task.time)) {
            return 1;
        } else {
            return 0;  
        }
    }
}