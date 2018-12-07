/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.io.gui.javafx.console;

import javafx.util.StringConverter;

/**
 *
 * @author Diarsid
 */
public class DefaultStringConverter extends StringConverter<String> {

    @Override
    public String toString(String string) {
        return string;
    }

    @Override
    public String fromString(String string) {
        return string;
    }
    
}