/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.domainkeeper;

import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.domain.entities.metadata.EntityProperty;
import diarsid.beam.core.domain.entities.validation.ValidationResult;
import diarsid.beam.core.domain.entities.validation.ValidationRule;

import static java.lang.Integer.parseInt;
import static java.lang.String.format;

import static diarsid.beam.core.base.util.StringNumberUtils.notNumeric;
import static diarsid.beam.core.base.util.StringUtils.lower;
import static diarsid.beam.core.domain.entities.metadata.EntityProperty.UNDEFINED_PROPERTY;
import static diarsid.beam.core.domain.entities.metadata.EntityProperty.argToProperty;
import static diarsid.beam.core.domain.entities.validation.ValidationRule.ENTITY_NAME_RULE;

/**
 *
 * @author Diarsid
 */
public class KeeperDialogHelper {
    
    private final InnerIoEngine ioEngine;
    
    public KeeperDialogHelper(InnerIoEngine ioEngine) {
        this.ioEngine = ioEngine;
    }
    
    public String validateInteractively(
            Initiator initiator, String argument, String ioRequest, ValidationRule rule) {
        ValidationResult result = rule.apply(argument);
        if ( result.isOk() ) {
            return argument;
        } else {
            return this.inputAndValidateInLoop(result, initiator, ioRequest, rule, argument);
        }
    } 

    private String inputAndValidateInLoop(
            ValidationResult result, 
            Initiator initiator, 
            String ioRequest, 
            ValidationRule rule, 
            String argument) {
        String anotherValue = "";
        while ( result.isFail() ) {
            this.ioEngine.report(initiator, result.getFailureMessage());
            anotherValue = this.ioEngine.askInput(initiator, ioRequest);
            if ( anotherValue.isEmpty() ) {
                return "";
            }
            result = rule.apply(argument);
        }
        return anotherValue;
    }

    public String validateEntityNameInteractively(
            Initiator initiator, String argument) {
        ValidationResult result = ENTITY_NAME_RULE.apply(argument);
        if ( result.isOk() ) {
            return argument;
        } else {
            return this.inputAndValidateInLoop(result, initiator, "name", ENTITY_NAME_RULE, argument);
        }
    }
    
    public EntityProperty validatePropertyInteractively(
            Initiator initiator, EntityProperty property, EntityProperty... possibleProperties) {
        String propertyArg;
        while ( property.isUndefined() || property.isNotOneOf(possibleProperties) ) { 
            propertyArg = this.ioEngine.askInput(initiator, "property to edit");
            if ( propertyArg.isEmpty() ) {
                return UNDEFINED_PROPERTY;
            }
            property = argToProperty(propertyArg);
            if ( property.isDefined() && property.isNotOneOf(possibleProperties) ) {
                this.ioEngine.report(
                        initiator, 
                        format("%s is not editable in this context.", lower(property.name())));
            }
        }
        return property;
    }
    
    public int discussIntInRange(
            Initiator initiator, int fromInclusive, int toInclusive, String request) {
        int i = -1;
        boolean intNotDefined = true;
        String intInput;
        intDefining: while ( intNotDefined ) {            
            intInput = this.ioEngine.askInput(initiator, request);
            if ( intInput.isEmpty() ) {
                i = -1;
                intNotDefined = false;
            }
            if ( notNumeric(intInput) ) {
                this.ioEngine.report(initiator, "not a number.");
                continue intDefining;
            }
            i = parseInt(intInput);
            if ( i < fromInclusive || i > toInclusive ) {
                this.ioEngine.report(initiator, format("out of range %d-%d", fromInclusive, toInclusive));
                continue intDefining;
            }
            intNotDefined = false;
        }
        return i;
    }
}
