/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.util;

import java.util.Collection;

import diarsid.beam.core.base.control.io.base.interaction.ConvertableToJson;

import static java.lang.String.join;
import static java.util.stream.Collectors.joining;

import static diarsid.beam.core.base.util.StringNumberUtils.isNumeric;

/**
 *
 * @author Diarsid
 */
public class JsonUtil {
    
    private JsonUtil() {
    }
    
    public static String errorJson(String text) {
        return JsonUtil.asJson(
                "type", "error", 
                "text", text);
    }
    
    public static String stringsAsJsonArray(Collection<String> jsons) {
        return new StringBuilder()
                .append("[")
                .append(join(",", jsons))
                .append("]")
                .toString();
    }
    
    public static String convertablesAsJsonArray(
            Collection<? extends ConvertableToJson> convertables) {
        return convertables
                .stream()
                .map(convertable -> convertable.toJson())
                .collect(joining(",", "[", "]"));
    }
    
    private static String jsonStringOf(String value) {
        if ( isArray(value) || isNumeric(value) || isObject(value) ) {
            return value;
        } else {
            return "\"" + value + "\"";
        }
    }

    private static boolean isArray(String value) {
        return value.startsWith("[") && value.endsWith("]");
    }
    
    private static boolean isObject(String value) {
        return value.startsWith("{") && value.endsWith("}");
    }
    
    public static String asJson(
            String key0, String value0) {
        return new StringBuilder()
                .append("{")
                .append("\"").append(key0).append("\"")
                .append(":")
                .append(jsonStringOf(value0))
                .append("}")
                .toString();
    }
    
    public static String asJson(
            String key0, String value0, 
            String key1, String value1) {
        return new StringBuilder()
                .append("{")
                .append("\"").append(key0).append("\"")
                .append(":")
                .append(jsonStringOf(value0))
                .append(",")
                .append("\"").append(key1).append("\"")
                .append(":")
                .append(jsonStringOf(value1))
                .append("}")
                .toString();
    }
    
    public static String asJson(
            String key0, String value0, 
            String key1, String value1, 
            String key2, String value2) {
        return new StringBuilder()
                .append("{")
                .append("\"").append(key0).append("\"")
                .append(":")
                .append(jsonStringOf(value0))
                .append(",")
                .append("\"").append(key1).append("\"")
                .append(":")
                .append(jsonStringOf(value1))
                .append(",")
                .append("\"").append(key2).append("\"")
                .append(":")
                .append(jsonStringOf(value2))
                .append("}")
                .toString();
    }
    
    public static String asJson(
            String key0, String value0, 
            String key1, String value1, 
            String key2, String value2, 
            String key3, String value3) {
        return new StringBuilder()
                .append("{")
                .append("\"").append(key0).append("\"")
                .append(":")
                .append(jsonStringOf(value0))
                .append(",")
                .append("\"").append(key1).append("\"")
                .append(":")
                .append(jsonStringOf(value1))
                .append(",")
                .append("\"").append(key2).append("\"")
                .append(":")
                .append(jsonStringOf(value2))
                .append(",")
                .append("\"").append(key3).append("\"")
                .append(":")
                .append(jsonStringOf(value3))
                .append("}")
                .toString();
    }
    
    public static String asJson(
            String key0, String value0, 
            String key1, String value1, 
            String key2, String value2, 
            String key3, String value3,
            String key4, String value4) {
        return new StringBuilder()
                .append("{")
                .append("\"").append(key0).append("\"")
                .append(":")
                .append(jsonStringOf(value0))
                .append(",")
                .append("\"").append(key1).append("\"")
                .append(":")
                .append(jsonStringOf(value1))
                .append(",")
                .append("\"").append(key2).append("\"")
                .append(":")
                .append(jsonStringOf(value2))
                .append(",")
                .append("\"").append(key3).append("\"")
                .append(":")
                .append(jsonStringOf(value3))
                .append(",")
                .append("\"").append(key4).append("\"")
                .append(":")
                .append(jsonStringOf(value4))
                .append("}")
                .toString();
    }
    
    public static String asJson(
            String key0, String value0, 
            String key1, String value1, 
            String key2, String value2, 
            String key3, String value3,
            String key4, String value4,
            String key5, String value5) {
        return new StringBuilder()
                .append("{")
                .append("\"").append(key0).append("\"")
                .append(":")
                .append(jsonStringOf(value0))
                .append(",")
                .append("\"").append(key1).append("\"")
                .append(":")
                .append(jsonStringOf(value1))
                .append(",")
                .append("\"").append(key2).append("\"")
                .append(":")
                .append(jsonStringOf(value2))
                .append(",")
                .append("\"").append(key3).append("\"")
                .append(":")
                .append(jsonStringOf(value3))
                .append(",")
                .append("\"").append(key4).append("\"")
                .append(":")
                .append(jsonStringOf(value4))
                .append(",")
                .append("\"").append(key5).append("\"")
                .append(":")
                .append(jsonStringOf(value5))
                .append("}")
                .toString();
    }
}
