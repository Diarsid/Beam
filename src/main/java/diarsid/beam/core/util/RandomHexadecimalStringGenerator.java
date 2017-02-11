/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.util;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 *
 * @author Diarsid
 */

public class RandomHexadecimalStringGenerator {
    
    RandomHexadecimalStringGenerator() {
    }
    
    public String randomString(int length) {
        String randomString = this.newRandomHexadecimalString();
        if ( randomString.length() > length ) {
            return randomString.substring(0, length);
        } else {
            return this.cyclicGenerationFor(length);
        }
    }
    
    private String newRandomHexadecimalString() {
        return UUID.randomUUID().toString().replace("-", "");
    }
    
    private String cyclicGenerationFor(int length) {
        StringBuilder generated = new StringBuilder();
        while ( generated.length() < length ) {
            generated.append(this.newRandomHexadecimalString());
        }
        if ( generated.length() > length ) {
            return generated.toString().substring(0, length);
        } else {
            return generated.toString();
        }
    }
    
    public String randomString(int minLength, int maxLength) {
        int randomLength = -1;
        while ( randomLength < minLength ) {            
            randomLength = ThreadLocalRandom.current().nextInt(maxLength + 1);
        }
        return this.randomString(randomLength);
    }
}
