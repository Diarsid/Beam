/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.control.io.base.interaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import diarsid.beam.core.domain.entities.NamedEntity;
import diarsid.beam.core.domain.entities.Task;

import static java.util.stream.Collectors.toList;

import static diarsid.beam.core.base.control.io.base.interaction.Message.MessageType.ERROR;
import static diarsid.beam.core.base.control.io.base.interaction.Message.MessageType.INFO;
import static diarsid.beam.core.base.control.io.base.interaction.Message.MessageType.TASK;

/**
 *
 * @author Diarsid
 */
public class Messages {
    
    private Messages() {
    }
    
    static String inline(String line) {
        return "   " + line;
    }
    
    public static Message task(String header, List<String> lines) {
        return new SingleMessage(TASK, header, lines);
    }
    
    public static Message error(String... lines) {
        return new SingleMessage(ERROR, "", lines);
    }
    
    public static Message error(Exception e) {
        return new SingleMessage(ERROR, "", e.getMessage());
    }
    
    public static Message info(String... lines) {
        return new SingleMessage(INFO, "", lines);
    }
    
    public static Message info(List<String> lines) {
        return new SingleMessage(INFO, "", lines);
    }
    
    public static Message infoWithHeader(String header, String... lines) {
        return new SingleMessage(INFO, header, lines);
    }
    
    public static Message infoWithHeader(String header, List<String> lines) {
        return new SingleMessage(INFO, header, lines);
    }
    
    public static Optional<Message> joinToOptionalMessage(List<Message> messages) {
        if ( messages.isEmpty() ) {
            return Optional.empty();
        } else {
            return Optional.of(new SingleMessage(INFO, "", messages
                    .stream()
                    .flatMap(message -> message.allLines().stream())
                    .collect(toList())));
        }
    }
    
    public static Message tasksToMessage(List<Task> tasks) {
        List<String> lines = new ArrayList<>();
        tasks.stream().forEach(task -> {
            lines.add(task.stringifyTime());
            lines.addAll(task.text());
            lines.add(" ");
        });
        return new SingleMessage(INFO, "", lines);
    }
    
    public static Optional<Message> tasksToOptionalMessageWithHeader(
            String header, List<Task> tasks) {
        if ( tasks.isEmpty() ) {
            return Optional.empty();
        } else {
            return Optional.of(tasksToMessage(tasks).addHeader(header));
        }
    }
    
    public static Optional<Message> linesToOptionalMessageWithHeader(
            String header, List<String> lines) {
        if ( lines.isEmpty() ) {
            return Optional.empty();
        } else {
            return Optional.of(new SingleMessage(INFO, "", lines).addHeader(header));
        }
    }
    
    public static Optional<Message> entitiesToOptionalMessageWithHeader(
            String header, List<? extends NamedEntity> entities) {
        return linesToOptionalMessageWithHeader(
                    header, 
                    entities
                            .stream()
                            .map(entity -> entity.name())
                            .collect(toList()));    
    }
}
