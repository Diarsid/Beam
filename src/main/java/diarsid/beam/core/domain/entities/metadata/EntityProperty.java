/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.domain.entities.metadata;

import java.io.Serializable;
import java.util.List;

import diarsid.beam.core.base.util.ParseableEnum;

import static java.lang.String.join;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;

import static diarsid.beam.core.base.util.EnumUtils.argMatchesEnum;

/**
 *
 * @author Diarsid
 */
public enum EntityProperty implements Serializable, ParseableEnum {
    
    COMMANDS (
            "commands", 
            asList("commands", "comm")),
    NAME (
            "name", 
            asList("name")),
    TEXT (
            "text", 
            asList("text", "txt")),
    ORDER (
            "order", 
            asList("order")),
    WEB_PLACE (
            "WebPlace", 
            asList("webplace", "wplace", "place")),
    WEB_DIRECTORY (
            "WebDirectory", 
            asList("webdirectory", "wdirectory", "webdir", "wdir", "directory")),
    WEB_URL (
            "url", 
            asList("url", "weburl", "wurl", "link")),
    SHORTCUTS (
            "shortcuts", 
            asList("shortcuts", "shorts", "alias")),
    FILE_URL (
            "path", 
            asList("path", "file")),
    
    UNDEFINED_PROPERTY ("undefined", emptyList());
    
    private final String displayName;
    private final List<String> keyWords;

    private EntityProperty(String name, List<String> keyWords) {
        this.displayName = name;
        this.keyWords = keyWords;
    }
    
    @Override
    public List<String> keyWords() {
        return this.keyWords;
    }
    
    public String displayName() {
        return this.displayName;
    }
    
    public String joinKeywords() {
        return join(", ", this.keyWords);
    }

    public boolean isDefined() {
        return this != UNDEFINED_PROPERTY;
    }
    
    public boolean isUndefined() {
        return this == UNDEFINED_PROPERTY;
    }
    
    public static EntityProperty propertyOf(String property) {
        return stream(values())
                .filter(value -> value.name().equalsIgnoreCase(property))
                .findFirst()
                .orElse(UNDEFINED_PROPERTY);
    }
    
    public boolean isOneOf(EntityProperty... possibleProperties) {
        return stream(possibleProperties)
                .anyMatch(possibleProperty -> this.equals(possibleProperty));
    }
    
    public boolean isNotOneOf(EntityProperty... possibleProperties) {
        return stream(possibleProperties)
                .noneMatch(possibleProperty -> this.equals(possibleProperty));
    }
    
    // TODO MEDIUM add tests
    public static EntityProperty argToProperty(String arg) {
        return stream(values())
                .filter(property -> argMatchesEnum(arg, property))
                .findFirst()
                .orElse(UNDEFINED_PROPERTY);
    }
}
