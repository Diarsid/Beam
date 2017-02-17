/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.domain.inputparsing.locations;

/**
 *
 * @author Diarsid
 */
public class LocationNameAndPath {
    
    private final String name;
    private final String path;
    
    public LocationNameAndPath(String name, String path) {
        this.name = name;
        this.path = path;
    }

    public String getName() {
        return this.name;
    }

    public String getPath() {
        return this.path;
    }
}
