/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.control.io.base.interaction;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import diarsid.beam.core.domain.entities.Batch;
import diarsid.beam.core.domain.entities.Location;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

import static diarsid.beam.core.base.control.io.base.interaction.Message.MessageType.INFO;

/**
 *
 * @author Diarsid
 */
public class DomainToMessageConversion {
    
    private DomainToMessageConversion() {
    }
    
    public static Message toMessage(List<Location> locations) {
        return new TextMessage(INFO, locations
                .stream()
                .map(location -> location.toString())
                .collect(toList())
        );
    }
    
    public static Message toMessage(Location location) {
        return new TextMessage(INFO, location.toString());
    }
    
    public static Message toMessage(Batch batch) {
        AtomicInteger counter = new AtomicInteger(1);
        List<String> batchText = batch.stringifyCommands()
                .stream()
                .map(command -> format(" %d) %s", counter.getAndIncrement(), command))
                .collect(toList());
        batchText.add(batch.getName());
        return new TextMessage(INFO, batchText);
    }
}
