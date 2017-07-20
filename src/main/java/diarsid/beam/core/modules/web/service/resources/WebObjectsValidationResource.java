/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.web.service.resources;

import java.io.IOException;

import diarsid.beam.core.base.control.io.base.interaction.WebRequest;
import diarsid.beam.core.base.control.io.base.interaction.WebResponse;
import diarsid.beam.core.domain.entities.validation.ValidationResult;
import diarsid.beam.core.domain.entities.validation.ValidationRule;
import diarsid.beam.core.modules.web.core.container.Resource;

import static java.util.Objects.nonNull;

import static diarsid.beam.core.base.control.io.base.interaction.WebResponse.badRequestWithJson;
import static diarsid.beam.core.base.control.io.base.interaction.WebResponse.ok;
import static diarsid.beam.core.domain.entities.validation.ValidationRule.ENTITY_NAME_RULE;
import static diarsid.beam.core.domain.entities.validation.ValidationRule.WEB_URL_RULE;

/**
 *
 * @author Diarsid
 */
public class WebObjectsValidationResource extends Resource {
    
    public WebObjectsValidationResource() {
        super("resources/validation/webobjects/{property}");
    }
    
    @Override
    protected void POST(WebRequest webRequest) throws IOException {
        String property = webRequest.pathParam("property");
        String propertyValue = webRequest.json().stringOf("payload");        
        
        ValidationRule validationRule = this.defineValidationRuleOf(property);
        
        WebResponse webResponse = this.applyRuleToValue(validationRule, propertyValue);
        
        webRequest.send(webResponse);
    }

    private ValidationRule defineValidationRuleOf(String property) {
        switch ( property ) {
            case "names" : {
                return ENTITY_NAME_RULE;
            }
            case "urls" : {
                return WEB_URL_RULE;
            }
            default : {
                return null;
            }
        }
    }

    private WebResponse applyRuleToValue(ValidationRule validationRule, String value) {
        if ( nonNull(validationRule) ) {
            ValidationResult validity = validationRule.applyTo(value);
            if ( validity.isFail() ) {
                return badRequestWithJson(validity.getFailureMessage());
            } else {
                return ok();
            }
        } else {
            return badRequestWithJson("property is not defined!");
        }
    }
    
}
