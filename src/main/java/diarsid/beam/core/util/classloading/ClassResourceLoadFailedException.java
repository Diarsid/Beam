/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.util.classloading;

/**
 *
 * @author Diarsid
 */
class ClassResourceLoadFailedException extends Exception {

    /**
     * Creates a new instance of <code>ClassResourceLoadFailedException</code> without detail
     * message.
     */
    ClassResourceLoadFailedException() {
    }

    /**
     * Constructs an instance of <code>ClassResourceLoadFailedException</code> with the specified
     * detail message.
     *
     * @param msg the detail message.
     */
    ClassResourceLoadFailedException(String msg) {
        super(msg);
    }
}
