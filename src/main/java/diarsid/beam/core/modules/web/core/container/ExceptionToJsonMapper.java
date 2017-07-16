/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.web.core.container;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import diarsid.beam.core.domain.entities.exceptions.DomainConsistencyException;
import diarsid.beam.core.domain.entities.exceptions.DomainOperationException;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

import static diarsid.beam.core.base.util.JsonUtil.errorJson;

/**
 *
 * @author Diarsid
 */
public class ExceptionToJsonMapper {
    
    private final Map<Class<? extends Throwable>, Function<Throwable, JsonError>> mappers;
    private final Function<Throwable, JsonError> defaultMapper;
    
    public ExceptionToJsonMapper() {
        this.mappers = new HashMap<>();
        this.mappers.put(DomainConsistencyException.class, (throwable) -> {
            return new JsonError(SC_BAD_REQUEST, errorJson(throwable.getMessage()));
        });
        this.mappers.put(DomainOperationException.class, (throwable) -> {
            return new JsonError(SC_INTERNAL_SERVER_ERROR, errorJson(throwable.getMessage()));
        });
        this.defaultMapper = (throwable) -> {
            return new JsonError(SC_INTERNAL_SERVER_ERROR, errorJson(throwable.getMessage()));
        };
    }
    
    JsonError map(Throwable throwable) {
        if ( this.hasMapperFor(throwable) ) {
            return this.applyMapperTo(throwable);
        } else {
            return this.applyDefaultTo(throwable);
        }
    }

    private JsonError applyDefaultTo(Throwable throwable) {
        return this.defaultMapper.apply(throwable);
    }

    private JsonError applyMapperTo(Throwable throwable) {
        return this.mappers.get(throwable.getClass()).apply(throwable);
    }

    private boolean hasMapperFor(Throwable throwable) {
        return this.mappers.containsKey(throwable.getClass());
    }    
}
