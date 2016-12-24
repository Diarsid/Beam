/*
 * project: Beam
 * author: Diarsid
 */
package old.diarsid.beam.external.sysconsole.modules.workers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import diarsid.beam.core.modules.tasks.ExecutionTime;

import old.diarsid.beam.external.sysconsole.modules.ConsolePrinterModule;
import old.diarsid.beam.external.sysconsole.modules.ConsoleReaderModule;

/**
 *
 * @author Diarsid
 */
public class InputHandler {
    
    private final String[] yesPatterns = {"y", "+", "yes", "ye", "true", "enable"};
    private final String[] stopPatterns = {".", "", "s", " ", "-", "false", "disable"};
    private final String[] helpPatterns = {"h", "help", "hlp", "hp"};
    
    private final ConsolePrinterModule printer;
    private final ConsoleReaderModule reader;
    
    InputHandler(ConsolePrinterModule pr, ConsoleReaderModule re) {
        this.reader = re;
        this.printer = pr;
    }
    
    boolean checkOnYes(String input) {
        return check(input, this.yesPatterns);
    }
    
    boolean checkOnHelp(String input) {
        return check(input, this.helpPatterns);
    }
    
    private boolean check(String s, String[] patterns) {
        for (String pattern : patterns) {
            if (pattern.equals(s)) {
                return true;
            }
        }
        return false;
    }
    
    String inputTime() throws IOException{
        String input = "";
        while (true) {
            this.printer.printUnder("time: ");
            input = this.reader.read();
            if (this.checkOnHelp(input)){
                this.printer.printTimeFormats();
                continue;
            } else {
                break;
            }
        }
        return input;
    }

    String[] inputTask() throws IOException{
        String taskLine;
        ArrayList<String> taskContent = new ArrayList<>();
        // endless input loop until '.' will be entered which means end of task
        taskInput: while (true){
            this.printer.printUnder("task line: ");
            taskLine = this.reader.readWithoutStopChecking();
            // symbol '.' finishes input and breaks input loop
            if ((".").equals(taskLine)){
                // if user doesn't specify any text
                if (taskContent.size()==0){
                    // it will be filled only with "empty" value and break input loop
                    taskContent.add("empty");
                    break taskInput;
                } else {
                    break taskInput;
                }    
            } else {
                // in other cases entered string is added to the list
                taskContent.add(taskLine);
            }    
        }
        return taskContent.toArray(new String[taskContent.size()]);
    }

    String inputTextToDelete() throws IOException{
        String text = "";
        // begin of input loop
        inputText: while (true){
            this.printer.printUnder("Input text to delete: ");
            text = this.reader.readWithoutStopChecking();
            // symbol '.' finishes input and breaks input loop, method returns null
            if (text.equals(".")) {
                text = "";
                break inputText;
            }    
            if (text.length()==0) {
                continue inputText;
            }    
            // if given string satisfied all requirements input loop ends, method returns it's value
            break inputText;
        }
        return text;
    }
    
    List<String> inputCommands() throws IOException{
        List<String> commands = new ArrayList<>();
        String command;
        input: while(true){
            this.printer.printUnder("command: ");
            command = this.reader.read();
            if (command.isEmpty()){
                break input;
            }
            commands.add(command);
        }
        return commands;
    }
    
    int chooseVariants(String message, List<String> variants) {
        int choosed = -1;
        StringBuilder variantsPrinter = new StringBuilder();
        try {
            this.printer.printUnderLn(message);
            for (int i = 0; i < variants.size(); i++) {
                variantsPrinter
                        .append(String.valueOf(i+1))
                        .append(" : ")
                        .append(variants.get(i));
                this.printer.printSpaceLn(variantsPrinter.toString());
                variantsPrinter.delete(0, variantsPrinter.length());
            } 
            String input;
            while (true) {
                this.printer.printUnder("choose: ");
                input = this.reader.read();
                if (input.isEmpty()) {
                    return -1;
                }
                try {
                    choosed = Integer.parseInt(input);
                    if (0 < choosed && choosed <= variants.size()) {
                        break;
                    } else {
                        this.printer.printUnderLn("Out of variants range.");
                        continue;
                    }
                } catch (NumberFormatException nfe) {
                    this.printer.printUnderLn("Not a number.");
                    continue;
                }
            }
        } catch (IOException ioe) {}        
        return choosed;
    }
    
    boolean confirmActionWithoutBeamSign(String question) throws IOException {
        this.printer.printUnderLn(question);
        this.printer.printUnder("yes / no : ");
        String response = this.reader.read();
        if (response.isEmpty()) {
            return false;
        } else {
            return checkOnYes(response);
        }
    }
    
    boolean confirmAction(String question) throws IOException {
        this.printer.printBeamWithMessageLn(question);
        this.printer.printUnder("yes / no : ");
        String response = this.reader.read();
        if (response.isEmpty()) {
            return false;
        } else {
            return checkOnYes(response);
        }
    }
    
    Set<Integer> inputAllowedDays() throws IOException, NumberFormatException {
        this.printer.printUnder("days of week (1-7, inclusive): ");
        String days = this.reader.read();
        days = days.replace(",", "");
        ExecutionTime schedule = new ExecutionTime();
        for (String day : days.split("\\s+")) {
            if ( day.contains("-")) {
                schedule.includeDaysOfWeekBetween(
                        Character.getNumericValue(day.charAt(0)), 
                        Character.getNumericValue(day.charAt(2)));
            } else {
                schedule.includeDayOfWeek(Integer.parseInt(day));
            }
        }
        return schedule.aggregateDays();
    }
    
    Set<Integer> inputAllowedHours() throws IOException, NumberFormatException {
        this.printer.printUnder("hours of day (0-24, exclusive): ");
        String hours = this.reader.read();
        hours = hours.replace(",", "");
        ExecutionTime schedule = new ExecutionTime();
        for (String hour : hours.split("\\s+")) {
            if ( hour.contains("-")) {
                int fromHourInclusive = Integer.parseInt(
                        hour.substring(0, hour.indexOf("-")));
                int toHourExclusive = Integer.parseInt(
                        hour.substring(hour.indexOf("-")+1));
                schedule.includeHoursOfDayBetween(
                        fromHourInclusive, toHourExclusive);
            } else {
                schedule.includeHourOfDay(Integer.parseInt(hour));
            }
        }
        return schedule.aggregateHours();
    }
}
