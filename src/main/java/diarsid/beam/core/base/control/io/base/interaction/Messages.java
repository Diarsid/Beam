/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.control.io.base.interaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import diarsid.beam.core.domain.entities.Task;

import static java.util.stream.Collectors.toList;

import static diarsid.beam.core.base.control.io.base.interaction.Message.MessageType.ERROR;
import static diarsid.beam.core.base.control.io.base.interaction.Message.MessageType.INFO;

/**
 *
 * @author Diarsid
 */
public class Messages {
    
    private Messages() {
    }
    
    public static Message error(String line, String... lines) {
        return new TextMessage(ERROR, line, lines);
    }
    
    public static Message text(String line, String... lines) {
        return new TextMessage(INFO, line, lines);
    }
    
    public static Message linesToMessage(List<String> lines) {
        return new TextMessage(INFO, lines);
    }
    
    public static Optional<Message> joinToOptionalMessage(List<Message> messages) {
        if ( messages.isEmpty() ) {
            return Optional.empty();
        } else {
            return Optional.of(new TextMessage(messages
                    .stream()
                    .flatMap(message -> message.toText().stream())
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
        return new TextMessage(INFO, lines);
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
            return Optional.of(new TextMessage(INFO, lines).addHeader(header));
        }
    }
    
    public static Message fromException(Exception e) {
        return new TextMessage(e);
    }
}
