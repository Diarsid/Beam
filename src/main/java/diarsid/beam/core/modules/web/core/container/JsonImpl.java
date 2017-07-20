/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.web.core.container;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import diarsid.beam.core.base.control.io.base.interaction.Json;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 *
 * @author Diarsid
 */
class JsonImpl implements Json {
    
    private final JSONObject json;

    public JsonImpl(JSONObject json) {
        this.json = json;
    }
    
    private Object nonNullPropertyOf(String name) {
        return requireNonNull(this.json.get(name), format("Json property %s is not found!", name));
    }

    @Override
    public String stringOf(String name) {
        return (String) this.nonNullPropertyOf(name);
    }

    @Override
    public int intOf(String name) {
        Object obj = this.nonNullPropertyOf(name);
        if ( obj instanceof Integer ) {
            return (int) obj;
        } else if ( obj instanceof String ) {
            return Integer.valueOf((String) obj);
        } else {
            throw new JsonConversionException(
                    format("Cannot convert %s: %s to int.", name, obj));
        }
    }

    @Override
    public boolean booleanOf(String name) {
        Object obj = this.nonNullPropertyOf(name);
        if ( obj instanceof Boolean ) {
            return (boolean) obj;
        } else if ( obj instanceof String ) {
            return Boolean.valueOf((String) obj);
        } else {
            throw new JsonConversionException(
                    format("Cannot convert %s: %s to boolean.", name, obj));
        }
    }

    @Override
    public Json jsonOf(String name) {
        return new JsonImpl((JSONObject) this.nonNullPropertyOf(name));
    }

    @Override
    public List<Json> jsonListOf(String name) {
        List<Json> jsons = new ArrayList<>();
        for (Object object : (JSONArray) this.nonNullPropertyOf(name)) {
            jsons.add(new JsonImpl((JSONObject) object));
        }        
        return jsons;
    }
    
}
