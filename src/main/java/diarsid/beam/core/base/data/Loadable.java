/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.data;

import diarsid.beam.core.base.control.flow.ValueFlow;

/**
 *
 * @author Diarsid
 */
public interface Loadable <T> {
    
    ValueFlow<T> load();
    
}
