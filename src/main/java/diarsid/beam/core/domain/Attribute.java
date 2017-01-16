/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.domain;

import diarsid.beam.core.util.Pair;

/**
 *
 * @author Diarsid
 */
public class Attribute extends Pair<String, String> {
    
    public Attribute(String key, String value) {
        super(key, value);
    }
}
