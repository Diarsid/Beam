/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.web.service.json.entities;

/**
 *
 * @author Diarsid
 */
public class JsonPayload {
    
    private final String payload;
    
    public JsonPayload(String payload) {
        this.payload = payload;
    }
    
    public String get() {
        return this.payload;
    }
}

