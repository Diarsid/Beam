/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.domainkeeper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.control.io.base.interaction.Help;
import diarsid.beam.core.domain.entities.metadata.EntityProperty;
import diarsid.beam.core.domain.entities.validation.ValidationResult;
import diarsid.beam.core.domain.entities.validation.ValidationRule;

import static java.lang.Integer.parseInt;
import static java.lang.String.format;

import static diarsid.beam.core.base.control.io.base.interaction.Help.asHelp;
import static diarsid.beam.core.base.util.StringNumberUtils.notNumeric;
import static diarsid.beam.core.base.util.StringUtils.lower;
import static diarsid.beam.core.base.util.StringUtils.nonEmpty;
import static diarsid.beam.core.domain.entities.metadata.EntityProperty.UNDEFINED_PROPERTY;
import static diarsid.beam.core.domain.entities.metadata.EntityProperty.argToProperty;
import static diarsid.beam.core.domain.entities.validation.ValidationRule.ENTITY_NAME_RULE;

/**
 *
 * @author Diarsid
 */
public class KeeperDialogHelper {
    
    private final InnerIoEngine ioEngine;
    private final Map<ValidationRule, Help> validationRulesHelp;
    
    public KeeperDialogHelper(InnerIoEngine ioEngine) {
        this.ioEngine = ioEngine;
        this.validationRulesHelp = new HashMap<>();
        Help ruleHelp;
        for (ValidationRule rule : ValidationRule.values()) {
            ruleHelp = this.ioEngine.addToHelpContext(rule.helpInfo());
            this.validationRulesHelp.put(rule, ruleHelp);
        }
    }
    
    public String validateInteractively(
            Initiator initiator, String argument, String ioRequest, ValidationRule rule) {
        return this.validate(initiator, ioRequest, rule, argument);
    } 

    private String validate( 
            Initiator initiator, 
            String ioRequest, 
            ValidationRule rule, 
            String argument) {
        if ( nonEmpty(argument) ) {
            ValidationResult result = rule.applyTo(argument);
            if ( result.isOk() ) {
                return argument;
            } else {
                this.ioEngine.report(initiator, result.getFailureMessage());
                return this.loopValidation(initiator, ioRequest, rule);
            }
        } else {
            return this.loopValidation(initiator, ioRequest, rule);
        }
    }
    
    private String loopValidation(
            Initiator initiator, String ioRequest, ValidationRule rule) {
        String value;
        ValidationResult validity;
        valueDefining: while ( true ) {
            value = this.ioEngine.askInput(
                    initiator, ioRequest, this.validationRulesHelp.get(rule));
            if ( value.isEmpty() ) {
                return "";
            } else {
                validity = rule.applyTo(value);
                if ( validity.isOk() ) {
                    return value;
                } else {
                    this.ioEngine.report(initiator, validity.getFailureMessage());
                }
            }
        }        
    }

    public String validateEntityNameInteractively(
            Initiator initiator, String argument) {
        return this.validate(initiator, "name", ENTITY_NAME_RULE, argument);
    }
    
    public EntityProperty validatePropertyInteractively(
            Initiator initiator, EntityProperty property, EntityProperty... possibleProperties) {
        String propertyArg;
        while ( property.isUndefined() || property.isNotOneOf(possibleProperties) ) { 
            
            propertyArg = this.ioEngine.askInput(
                    initiator, "property to edit", this.propertiesToHelp(possibleProperties));
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
    
    private Help propertiesToHelp(EntityProperty[] properties) {
        List<String> helpLines = new ArrayList<>();
        helpLines.add("Choose property to edit:");
        for (EntityProperty property : properties) {
            helpLines.add(format(
                    "  - %s (use %s)", property.displayName(), property.joinKeywords()));
        }
        return asHelp(helpLines);
    }
    
    public int discussIntInRange(
            Initiator initiator, int fromInclusive, int toInclusive, String request) {
        int i = -1;
        boolean intNotDefined = true;
        String intInput;
        Help help = asHelp(
                format("Choose number from %s to %s inclusive.", fromInclusive, toInclusive));
        intDefining: while ( intNotDefined ) {            
            intInput = this.ioEngine.askInput(initiator, request, help);
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
