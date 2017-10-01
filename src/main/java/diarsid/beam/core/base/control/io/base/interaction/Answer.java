/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.control.io.base.interaction;

import java.io.Serializable;

/**
 *
 * @author Diarsid
 */
public interface Answer extends Serializable {

    int index();

    boolean is(String text);

    boolean isGiven();

    boolean isNotGiven();

    String text();
    
    boolean variantsAreNotSatisfactory();
    
    boolean isRejection();
    
    boolean isHelpRequest();
    
}
