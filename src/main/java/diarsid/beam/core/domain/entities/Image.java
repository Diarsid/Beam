/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.domain.entities;

import static java.util.Objects.nonNull;

/**
 *
 * @author Diarsid
 */
public class Image implements Binary {
    
    private final String name;
    private byte[] bytes;
    
    public Image(String name) {
        this.name = name;
    }
    
    public Image(String name, byte[] bytes) {
        this.name = name;
        this.bytes = bytes;
    }
    
    public String name() {
        return this.name;
    }
    
    @Override
    public byte[] bytes() {
        return this.bytes;
    }
    
    @Override
    public boolean hasData() {
        return nonNull(this.bytes) && this.bytes.length > 0;
    }
}
