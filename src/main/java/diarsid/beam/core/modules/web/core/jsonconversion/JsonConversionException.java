/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.web.core.jsonconversion;

/**
 *
 * @author Diarsid
 */
public class JsonConversionException extends RuntimeException {

    /**
     * Constructs an instance of <code>JsonConvertionException</code> with the specified detail
     * message.
     *
     * @param msg the detail message.
     */
    public JsonConversionException(String msg) {
        super(msg);
    }
}
