/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.web.core.jsonconversion;

/**
 *
 * @author Diarsid
 */
public class JsonUtil {
    
    private JsonUtil() {
    }
    
    public static String jsonError(String text) {
        return mapToJson(
                "type", "error", 
                "text", text);
    }
    
    static String mapToJson(
            String key0, String value0) {
        return new StringBuilder()
                .append("{")
                .append("\"").append(key0).append("\"")
                .append(":")
                .append("\"").append(value0).append("\"")
                .append("}")
                .toString();
    }
    
    static String mapToJson(
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
    
    static String mapToJson(
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
    
    static String mapToJson(
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
}
