/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules;

import diarsid.beam.core.StoppableBeamModule;
import diarsid.beam.core.modules.domain.keepers.LocationsKeeper;

/**
 *
 * @author Diarsid
 */
public interface DomainKeeperModule extends StoppableBeamModule  {
    
    LocationsKeeper getLocationsKeeper();
}
