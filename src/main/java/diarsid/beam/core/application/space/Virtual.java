/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.application.space;

import java.util.Set;

/**
 *
 * @author Diarsid
 */
public class Virtual {
    
    
    
    private Virtual() {
    }
    
    public static OSIdentifier defineOS() {
        return new OSIdentifier(
                System.getProperty("os.name") + " " + System.getProperty("os.version"));
    }
    
    public static VirtualSpace getVirtualSpace(OSIdentifier osId, Set<VirtualRoot> roots) {
        return new VirtualSpace(osId, roots);
    }
}
