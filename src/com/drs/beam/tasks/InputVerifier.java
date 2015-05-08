package com.drs.beam.tasks;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Org by Diarsid
 * Time: 17:30 - 07.11.14
 * IDE: IntelliJ IDEA 12
 */

    /* class to manage
    */

class InputVerifier {
    // field declarations ==============================================================================================
    
    // Empty constructor
    InputVerifier() {
    }

    // Methods =========================================================================================================

    //
    LocalDateTime verifyTimeFormat(String timeString, boolean mustBeFuture) throws VerifyFailureException{
        LocalDateTime time = null;
        try{
            switch (timeString.length()){   // obtain length of incoming string to define it's format
                case (16) : {
                    // time format: dd-MM-uuuu HH:mm
                    // full format
                    time = LocalDateTime.parse(
                            timeString,
                            DateTimeFormatter.ofPattern(Task.DB_TIME_PATTERN));
                    break;
                }
                case (5) : {
                    // time format: HH:MM
                    // specifies today's hours and minutes
                    time = LocalDateTime.now().withSecond(00).withNano(000)
                            .withHour(Integer.parseInt(timeString.substring(0,2)))
                            .withMinute(Integer.parseInt(timeString.substring(3,5)));
                    break;
                }
                case (6) : {
                    // time format: +HH:MM
                    // specifies time in hours and minutes, which is added to current time-date like timer
                    time = LocalDateTime.now().withSecond(00).withNano(000)
                            .plusHours(Integer.parseInt(timeString.substring(1,3)))
                            .plusMinutes(Integer.parseInt(timeString.substring(4,6)));
                    break;
                }
                case (8) : {
                    // time format: dd HH:MM
                    // specifies hours, minutes and day of current month
                    time = LocalDateTime.now().withSecond(00).withNano(000)
                            .withDayOfMonth(Integer.parseInt(timeString.substring(0,2)))
                            .withHour(Integer.parseInt(timeString.substring(3,5)))
                            .withMinute(Integer.parseInt(timeString.substring(6,8)));
                    break;
                }
                case (11) : {
                    // time format: dd-mm HH:MM
                    // specifies hours, minutes, day and month of current year
                    time = LocalDateTime.now().withSecond(00).withNano(000)
                            .withDayOfMonth(Integer.parseInt(timeString.substring(0,2)))
                            .withMonth(Integer.parseInt(timeString.substring(3,5)))
                            .withHour(Integer.parseInt(timeString.substring(6,8)))
                            .withMinute(Integer.parseInt(timeString.substring(9,11)));
                    break;
                }
                default: {
                    throw new VerifyFailureException("Time verifying: Unrecognizable time format.");
                }
            }
        } catch (DateTimeParseException e){
            throw new VerifyFailureException("Time verifying: Wrong time format.");
        } catch (NumberFormatException e){
            throw new VerifyFailureException("Time verifying: Wrong characters have been inputted!");
        }
        
        if (time == null){
            throw new VerifyFailureException("Time veryfying: InputVerifier.verifyTimeFormat() returned NULL");
        } else if (mustBeFuture && time.isBefore(LocalDateTime.now())){
            throw new VerifyFailureException("Time verifying: Given time is past. It must be future!");
        } else {
            return time;
        }       
    }

    //
    void verifyTask(String[] text) throws VerifyFailureException{
        if (text.length==0)
            throw new VerifyFailureException("Text verifying: Task info length = 0.");
        for (String s : text){
            if (s.contains("~}"))
                throw new VerifyFailureException("Text verifying: Forbidden character sequence '~}' was inputted!");
        }
    }

    //
    void verifyText(String text) throws VerifyFailureException{
        if (text.length()==0)
            throw new VerifyFailureException("Text verifying: input is empty!");
        if (text.contains("~}"))
            throw new VerifyFailureException("Text verifying: Forbidden character sequence '~}' was inputted!");
    }
}
