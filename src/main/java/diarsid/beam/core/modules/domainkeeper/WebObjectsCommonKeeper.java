/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.domainkeeper;

import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.control.io.base.interaction.Help;
import diarsid.beam.core.domain.entities.WebPlace;

import static java.lang.String.format;
import static java.lang.String.join;

import static diarsid.beam.core.domain.entities.WebPlace.BOOKMARKS;
import static diarsid.beam.core.domain.entities.WebPlace.UNDEFINED_PLACE;
import static diarsid.beam.core.domain.entities.WebPlace.WEBPANEL;
import static diarsid.beam.core.domain.entities.WebPlace.parsePlace;

/**
 *
 * @author Diarsid
 */
abstract class WebObjectsCommonKeeper {
    
    private final InnerIoEngine ioEngine;
    private final Help chooseWebPlaceHelp;

    public WebObjectsCommonKeeper(InnerIoEngine ioEngine) {
        this.ioEngine = ioEngine;
        this.chooseWebPlaceHelp = this.ioEngine.addToHelpContext(
                "Choose web place.",
                "Use:",
                format("   - %s to choose WebPanel", join(", ", WEBPANEL.keyWords())),
                format("   - %s to choose Bookmarks", join(", ", BOOKMARKS.keyWords())),
                "   - dot to break");
    }    
    
    WebPlace discussWebPlace(Initiator initiator) {
        String placeInput;
        WebPlace place = UNDEFINED_PLACE;
        placeDefining: while ( place.isUndefined() ) {     
            placeInput = this.ioEngine.askInput(initiator, "place", this.chooseWebPlaceHelp);
            if ( placeInput.isEmpty() ) {
                break placeDefining;
            }
            place = parsePlace(placeInput);
        }
        return place;
    }
}
