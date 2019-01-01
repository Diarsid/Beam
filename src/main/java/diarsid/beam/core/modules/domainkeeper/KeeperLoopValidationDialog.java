/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.domainkeeper;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import diarsid.beam.core.domain.entities.validation.ValidationRule;
import diarsid.beam.core.domain.entities.validation.Validity;
import diarsid.support.objects.PooledReusable;
import diarsid.support.objects.Possible;

import static diarsid.beam.core.base.util.StringUtils.nonEmpty;
import static diarsid.beam.core.domain.entities.validation.Validation.validateUsingRules;
import static diarsid.support.objects.Possibles.possibleButEmpty;

/**
 *
 * @author Diarsid
 */
class KeeperLoopValidationDialog extends PooledReusable {
    
    static {
        createPoolFor(KeeperLoopValidationDialog.class, () -> new KeeperLoopValidationDialog());
    }
    
    private final Possible<String> initialArgument;
    private final Possible<Consumer<String>> outputDestination;
    private final Possible<Supplier<String>> inputSource;
    private final List<ValidationRule> validationRules;
    private final Possible<String> inputReplacement;

    KeeperLoopValidationDialog() {
        this.initialArgument = possibleButEmpty();
        this.outputDestination = possibleButEmpty();
        this.inputSource = possibleButEmpty();
        this.validationRules = new ArrayList<>();
        this.inputReplacement = possibleButEmpty();
    }
    
    KeeperLoopValidationDialog withInitialArgument(String argument) {
        this.initialArgument.resetTo(argument);
        return this;
    }
    
    KeeperLoopValidationDialog withOutputDestination(Consumer<String> outputHelpConsumer) {
        this.outputDestination.resetTo(outputHelpConsumer);
        return this;
    }
    
    KeeperLoopValidationDialog withInputSource(Supplier<String> inputSupplier) {
        this.inputSource.resetTo(inputSupplier);
        return this;
    }
    
    KeeperLoopValidationDialog withRule(ValidationRule rule) {
        this.validationRules.add(rule);
        return this;
    }

    @Override
    protected void clearForReuse() {
        this.initialArgument.nullify();
        this.outputDestination.nullify();
        this.inputSource.nullify();
        this.validationRules.clear();
        this.inputReplacement.nullify();
    }
    
    public KeeperLoopValidationDialog replaceInput(String newValue) {
        this.inputReplacement.resetTo(newValue);
        return this;
    }
    
    private String replacedValueOr(String inputValue) {
        return this.inputReplacement.or(inputValue);
    }

    String validateAndGet() {
        if ( this.validationRules.isEmpty() ) {
            return this.initialArgument.or("");
        }
        
        if ( this.initialArgument.match(nonEmpty()) ) {
            String argument = this.initialArgument.orThrow();
            
            Validity result = validateUsingRules(argument, this.validationRules);
            
            if ( result.isOk() ) {
                return this.replacedValueOr(argument);                
            } else {
                this.outputDestination.orThrow().accept(result.getFailureMessage());
                return this.replacedValueOr(this.loopValidation());
            }
        } else {
            return this.replacedValueOr(this.loopValidation());
        }
    }
    
    private String loopValidation() {
        String value;
        Validity validity;
        valueDefining: while ( true ) {
            value = this.inputSource.orThrow().get();
            if ( value.isEmpty() ) {
                return "";
            } else {
                validity = validateUsingRules(value, this.validationRules);
                if ( validity.isOk() ) {
                    return value;
                } else {
                    this.outputDestination.orThrow().accept(validity.getFailureMessage());
                }
            }
        }        
    }
    
}
