/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.web.core.jsonconversion;

import java.util.List;

import static java.lang.String.join;

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
    
    static String asJsonArray(List<String> jsons) {
        return new StringBuilder()
                .append("[")
                .append(join(",", jsons))
                .append("]")
                .toString();
    }
    
    static String asJson(
            String key0, String value0) {
        return new StringBuilder()
                .append("{")
                .append("\"").append(key0).append("\"")
                .append(":")
                .append("\"").append(value0).append("\"")
                .append("}")
                .toString();
    }
    
    static String asJson(
            String key0, String value0, 
            String key1, String value1) {
        return new StringBuilder()
                .append("{")
                .append("\"").append(key0).append("\"")
                .append(":")
                .append("\"").append(value0).append("\"")
                .append(",")
                .append("\"").append(key1).append("\"")
                .append(":")
                .append("\"").append(value1).append("\"")
                .append("}")
                .toString();
    }
    
    static String asJson(
            String key0, String value0, 
            String key1, String value1, 
            String key2, String value2) {
        return new StringBuilder()
                .append("{")
                .append("\"").append(key0).append("\"")
                .append(":")
                .append("\"").append(value0).append("\"")
                .append(",")
                .append("\"").append(key1).append("\"")
                .append(":")
                .append("\"").append(value1).append("\"")
                .append(",")
                .append("\"").append(key2).append("\"")
                .append(":")
                .append("\"").append(value2).append("\"")
                .append("}")
                .toString();
    }
    
    static String asJson(
            String key0, String value0, 
            String key1, String value1, 
            String key2, String value2, 
            String key3, String value3) {
        return new StringBuilder()
                .append("{")
                .append("\"").append(key0).append("\"")
                .append(":")
                .append("\"").append(value0).append("\"")
                .append(",")
                .append("\"").append(key1).append("\"")
                .append(":")
                .append("\"").append(value1).append("\"")
                .append(",")
                .append("\"").append(key2).append("\"")
                .append(":")
                .append("\"").append(value2).append("\"")
                .append(",")
                .append("\"").append(key3).append("\"")
                .append(":")
                .append("\"").append(value3).append("\"")
                .append("}")
                .toString();
    }
    
    static String asJson(
            String key0, String value0, 
            String key1, String value1, 
            String key2, String value2, 
            String key3, String value3,
            String key4, String value4) {
        return new StringBuilder()
                .append("{")
                .append("\"").append(key0).append("\"")
                .append(":")
                .append("\"").append(value0).append("\"")
                .append(",")
                .append("\"").append(key1).append("\"")
                .append(":")
                .append("\"").append(value1).append("\"")
                .append(",")
                .append("\"").append(key2).append("\"")
                .append(":")
                .append("\"").append(value2).append("\"")
                .append(",")
                .append("\"").append(key3).append("\"")
                .append(":")
                .append("\"").append(value3).append("\"")
                .append(",")
                .append("\"").append(key4).append("\"")
                .append(":")
                .append("\"").append(value4).append("\"")
                .append("}")
                .toString();
    }
    
    static String asJson(
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
                .append("\"").append(value0).append("\"")
                .append(",")
                .append("\"").append(key1).append("\"")
                .append(":")
                .append("\"").append(value1).append("\"")
                .append(",")
                .append("\"").append(key2).append("\"")
                .append(":")
                .append("\"").append(value2).append("\"")
                .append(",")
                .append("\"").append(key3).append("\"")
                .append(":")
                .append("\"").append(value3).append("\"")
                .append(",")
                .append("\"").append(key4).append("\"")
                .append(":")
                .append("\"").append(value4).append("\"")
                .append(",")
                .append("\"").append(key5).append("\"")
                .append(":")
                .append("\"").append(value5).append("\"")
                .append("}")
                .toString();
    }
}
