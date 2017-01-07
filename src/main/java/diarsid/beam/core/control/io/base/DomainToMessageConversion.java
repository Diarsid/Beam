/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.control.io.base;

import java.util.List;

import diarsid.beam.core.domain.entities.Location;

import static java.util.stream.Collectors.toList;

import static diarsid.beam.core.control.io.base.Message.MessageType.INFO;

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
}
