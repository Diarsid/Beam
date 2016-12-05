/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.external.sysconsole.modules.workers;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import diarsid.beam.core.domain.entities.WebPage;
import diarsid.beam.core.domain.entities.Location;
import diarsid.beam.core.domain.entities.StoredCommandsBatch;
import diarsid.beam.core.modules.tasks.TaskMessage;
import diarsid.beam.external.sysconsole.modules.ConsolePrinterModule;

/**
 *
 * @author Diarsid
 */
class ConsolePrinter implements ConsolePrinterModule {
    
    private final String BEAM =     "Beam > ";    
    private final String UNDER =    "     > ";
    private final String SPACE =    "       ";
    private final String LEVEL_INDENT = "  ";
    private final String ERROR =    "Beam error > ";
    
    private final String HELP_START =   "   +---- HELP ------------------------------------------";
    private final String HELP =         "   |  ";
    private final String HELP_END =     "   +----------------------------------------------------";
    
    private final BufferedWriter writer;
    
    ConsolePrinter() {
        BufferedWriter bufferedWriter = null;
        try {
            bufferedWriter = new BufferedWriter(System.console().writer());
        } catch (NullPointerException e) {
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(System.out));
        }
        this.writer = bufferedWriter;
    }
    
    private String format(String info, int formatLength){        
        while(formatLength <= info.length()){
            formatLength += 10;
        }
        char[] result = Arrays.copyOf(info.toCharArray(), formatLength);
        Arrays.fill(result, info.length(), formatLength, ' ');
        return new String(result);
    }

    @Override
    public void printBeam() throws IOException{
            this.writer.write(BEAM);
            this.writer.flush();
    }
    
    @Override
    public void printBeamWithMessage(String s) throws IOException{
            this.writer.write(BEAM);
            this.writer.write(s);
            this.writer.flush();
    }
    
    @Override
    public void printBeamWithUnderLn(String... message) throws IOException{
        for (String s : message){
            this.writer.write(UNDER);
            this.writer.write(s);
            this.writer.newLine();
        }
        this.writer.flush();
    }
    
    @Override
    public void printBeamWithMessageLn(String... message) throws IOException{
        for (String s : message){
            this.writer.write(BEAM);
            this.writer.write(s);
            this.writer.newLine();
        }
        this.writer.flush();
    }
    
    @Override
    public void printUnder(String s) throws IOException{
        this.writer.write(UNDER);
        this.writer.write(s);
        this.writer.flush();
    }
    
    @Override
    public void printUnderLn(String s) throws IOException{
        this.writer.write(UNDER);
        this.writer.write(s);
        this.writer.newLine();
        this.writer.flush();
    }
    
    @Override
    public void printSpace(String s) throws IOException{
        this.writer.write(SPACE);
        this.writer.write(s);
        this.writer.flush();
    }
    
    @Override
    public void printSpaceLn(String s) throws IOException {
        int screenLength = 68;
        if ( s.length() < screenLength ) {
            this.writer.write(SPACE);        
            this.writer.write(s);
            this.writer.newLine();
            this.writer.flush();
        } else {
            this.writer.write(SPACE);
            this.writer.write(s.substring(0, screenLength));
            this.writer.newLine();
            this.writer.flush();
            int qty = s.length() / screenLength;
            for (int i = 2; i <= qty; i ++) {
                this.writer.write(SPACE);
                this.writer.write("    ");
                this.writer.write(s.substring(((i-1) * screenLength), i * screenLength));
                this.writer.newLine();
                this.writer.flush();
            }
            this.writer.write(SPACE);   
            this.writer.write("    ");
            this.writer.write(s.substring((qty * screenLength), s.length()));
            this.writer.newLine();
            this.writer.flush();
        } 
    }
    
    @Override
    public void printBeamErrorWithMessageLn(String[] message) throws IOException{
        for (String s : message){
            this.writer.write(ERROR);
            this.writer.write(s);
            this.writer.newLine();
        }                
        this.writer.flush();
    }
    
    @Override
    public void printTasks(String label, List<TaskMessage> tasks) throws IOException{
        if (tasks.isEmpty()) {
            printUnderLn(label+" there aren`t any tasks.");
            return;
        }
        printUnderLn(label);
        this.writer.write(SPACE);
        this.writer.write("=========================");
        this.writer.newLine();
        for(TaskMessage task : tasks){
            this.writer.newLine();
            this.writer.write(SPACE);
            this.writer.write("| " + task.getTime());
            this.writer.newLine();
            for (String s : task.getContent()){
                this.writer.write(SPACE);
                this.writer.write("| " + s);
                this.writer.newLine();
            }
            this.writer.flush();    
        }
        this.writer.newLine();
        this.writer.write(SPACE);
        this.writer.write("=========================");
        this.writer.newLine();
        this.writer.flush();
    }
    
    @Override
    public void printHelp(){
        
    }
    
    @Override
    public void printBatches(List<StoredCommandsBatch> commands) throws IOException {
        if (commands.isEmpty()) {
            printUnderLn("There aren`t any commands.");
            return;
        }
        printUnderLn("Batches:");
        this.writer.write(SPACE);
        this.writer.write("==================================================");
        this.writer.newLine();
        for(StoredCommandsBatch command : commands){
            this.writer.write(SPACE);
            this.writer.write(command.getName() + ":");
            this.writer.newLine();
            this.writer.flush();
            for (String commandLine : command.getCommands()){
                this.writer.write(SPACE);
                this.writer.write("   > ");
                this.writer.write(commandLine);
                this.writer.newLine();
                this.writer.flush();
            }
        }
        this.writer.write(SPACE);
        this.writer.write("==================================================");
        this.writer.newLine();
        this.writer.flush();
    }
    
    @Override
    public void printLocations(List<Location> locations) throws IOException{        
        if (locations.isEmpty()) {
            printUnderLn("There aren`t any locations.");
            return;
        }
        StringBuilder sb = new StringBuilder();
        printUnderLn("Locations:");
        this.writer.write(SPACE);
        this.writer.write("==================================================");
        this.writer.newLine();
        for(Location location : locations){
            sb.append(SPACE)
                    .append(format(location.getName(), 15))
                    .append(location.getPath());
            this.writer.write(sb.toString());
            sb = sb.delete(0, sb.length());
            this.writer.newLine();
            this.writer.flush();
        }
        this.writer.write(SPACE);
        this.writer.write("==================================================");
        this.writer.newLine();
        this.writer.flush();
    }
    
    @Override
    public void printLocationContent(List<String> locationContent) throws IOException {
        if (locationContent.size() > 1){
            this.printUnderLn(locationContent.get(0) + ":");
            for (int i = 1; i < locationContent.size(); i++){
                this.writer.write(SPACE+locationContent.get(i));
                this.writer.newLine();
            }
            this.writer.flush();
        } else if (locationContent.size() == 1){
            this.printUnderLn("Location " + locationContent.get(0) + " is empty.");
        }
    }
    
    @Override
    public void printWebPages(List<WebPage> pages, boolean compressOutput) throws IOException {
        if (pages.isEmpty()) {
            printUnderLn("There aren`t any pages.");
            return;
        }
        StringBuilder sb = new StringBuilder();
        printUnderLn("Web Pages:");
        this.writer.write(SPACE);
        this.writer.write("==================================================");
        this.writer.newLine();
        for(WebPage page : pages){
            sb.append(SPACE)
                    .append(format(page.getName(), 20))
                    .append(page.getDirectory())
                    .append("::")
                    .append(page.getPlacement().name().toLowerCase());
            if (compressOutput && sb.length() > 79) {
                sb.delete(76, sb.length());
                sb.append("...");
            }
            this.writer.write(sb.toString());
            sb = sb.delete(0, sb.length());
            this.writer.newLine();
            this.writer.flush();
        }
        this.writer.write(SPACE);
        this.writer.write("==================================================");
        this.writer.newLine();
        this.writer.flush();
    }
    
    @Override
    public void printDirs(List<String> dirs) throws IOException {
        if (dirs.isEmpty()) {
            printUnderLn("There aren't any directories.");
            return;
        }
        this.writer.write(SPACE);
        this.writer.write("==================================================");
        this.writer.newLine();
        this.writer.flush();
        for (String dir : dirs) {
            this.writer.write(SPACE);
            this.writer.write(dir);
            this.writer.newLine();
            this.writer.flush();
        }
        this.writer.write(SPACE);
        this.writer.write("==================================================");
        this.writer.newLine();
        this.writer.flush();
    }
    
    @Override
    public void printMemories(Map<String, List<String>> memories) throws IOException {
        if (memories.isEmpty()) {
            printUnderLn("There aren't any command choices.");
            return;
        } 
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, List<String>> entry : memories.entrySet()) {
            sb.append(SPACE).append(entry.getKey());
            this.writer.write(sb.toString());
            sb = sb.delete(0, sb.length());
            this.writer.newLine();
            this.writer.flush();
            for (String item : entry.getValue()) {
                sb.append(SPACE).append(LEVEL_INDENT).append(item);
                this.writer.write(sb.toString());
                sb = sb.delete(0, sb.length());
                this.writer.newLine();
                this.writer.flush();
            }            
        }
    }
    
    @Override
    public void printTimeFormats() throws IOException {
        String[] timeFormats = {
            "Possible time formats:",
            "+MM              - timer, minutes",
            "+HH:MM           - timer, houres and minutes",
            "HH:MM            - today task",
            "DD HH:MM         - this month task",
            "DD-MM HH:MM      - this year task",
            "YYYY-MM-DD HH:MM - full date format"
        };
        printHelpText(timeFormats);
    }
    
    private void printHelpText(String[] text) throws IOException{
        writer.newLine();
        writer.write(HELP_START);
        writer.newLine();
        writer.flush();
        for (String line : text) {
            writer.write(HELP + line);
            writer.newLine();
            writer.flush();
        }
        writer.write(HELP_END);
        writer.newLine();
        writer.newLine();
        writer.flush();
    }
    
    @Override
    public void exitMessage() throws IOException {
        this.writer.write(" :(");
        this.writer.newLine();
        this.writer.write("will meet you another time...");
        this.writer.flush();
    }
    
    @Override
    public void showTask(TaskMessage task) throws IOException {
        this.writer.newLine();
        this.writer.write(SPACE);
        this.writer.write("| " + task.getTime());
        this.writer.newLine();
        for (String s : task.getContent()){
            this.writer.write(SPACE);
            this.writer.write("| " + s);
            this.writer.newLine();
        }
        this.writer.flush();
    }
}
