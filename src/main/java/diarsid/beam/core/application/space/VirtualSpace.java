/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.application.space;

import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

/**
 *
 * @author Diarsid
 */
public class VirtualSpace {
    
    private final OSIdentifier osId;
    private final Set<VirtualRoot> roots;
    
    VirtualSpace(OSIdentifier osId, Set<VirtualRoot> roots) {
        this.osId = osId;
        this.roots = roots;
    }
    
    public Optional<VirtualRoot> getVirtualRootOf(Path path) {
        return this.roots
                .stream()
                .filter(vroot -> vroot.path().equals(path.getRoot()))
                .findFirst();
    } 
    
    public VirtualSpace add(VirtualRoot virtualRoot) {
        this.roots.add(virtualRoot);
        return this;
    }
}
