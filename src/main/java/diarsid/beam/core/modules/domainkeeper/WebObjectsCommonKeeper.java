/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.domainkeeper;

import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.domain.entities.WebPlace;

import static diarsid.beam.core.domain.entities.WebPlace.UNDEFINED_PLACE;
import static diarsid.beam.core.domain.entities.WebPlace.parsePlace;

/**
 *
 * @author Diarsid
 */
abstract class WebObjectsCommonKeeper {
    
    private final InnerIoEngine ioEngine;

    public WebObjectsCommonKeeper(InnerIoEngine ioEngine) {
        this.ioEngine = ioEngine;
    }    
    
    WebPlace discussWebPlace(Initiator initiator) {
        String placeInput;
        WebPlace place = UNDEFINED_PLACE;
        placeDefining: while ( place.isUndefined() ) {     
            placeInput = this.ioEngine.askInput(initiator, "place");
            if ( placeInput.isEmpty() ) {
                break placeDefining;
            }
            place = parsePlace(placeInput);
        }
        return place;
    }
}
