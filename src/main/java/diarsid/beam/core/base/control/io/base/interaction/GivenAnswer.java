/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.control.io.base.interaction;

/**
 *
 * @author Diarsid
 */
class GivenAnswer implements Answer {
    
    static final GivenAnswer EMPTY_CHOICE;
    
    static {
        EMPTY_CHOICE = new GivenAnswer(null, -1);
    }
    
    private final String text;
    private final int index;
        
    GivenAnswer(String text, int index) {
        this.text = text;
        this.index = index;
    }
    
    @Override
    public boolean isGiven() {
        return true;
    }
    
    @Override
    public boolean isNotGiven() {
        return false;
    }
    
    @Override
    public String text() {
        return this.text;
    }
    
    @Override
    public boolean is(String text) {
        return this.text.equals(text);
    }
    
    @Override
    public int index() {
        return this.index;
    }

    @Override
    public boolean variantsAreNotSatisfactory() {
        return false;
    }

    @Override
    public boolean isRejection() {
        return false;
    }

    @Override
    public boolean isHelpRequest() {
        return false;
    }
}
