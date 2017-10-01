/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.control.flow;

import static java.util.Arrays.stream;

/**
 *
 * @author Diarsid
 */
public enum FlowResult {
    COMPLETE,
    FAIL,
    STOP;
    
    public boolean is(FlowResult result) {
        return this.equals(result);
    }
    
    public boolean isAny(FlowResult... results) {
        return stream(results)
                .filter(result -> this.equals(result))
                .findFirst()
                .isPresent();
    }
    
    public boolean isNot(FlowResult result) {
        return ! this.equals(result);
    }
}
