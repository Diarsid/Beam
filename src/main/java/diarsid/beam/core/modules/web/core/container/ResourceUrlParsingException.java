/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.web.core.container;

import java.io.IOException;

/**
 *
 * @author Diarsid
 */
public class ResourceUrlParsingException extends IOException {

    /**
     * Constructs an instance of <code>ResourceUrlParsingException</code> with the specified detail
     * message.
     *
     * @param msg the detail message.
     */
    public ResourceUrlParsingException(String msg) {
        super(msg);
    }
}
