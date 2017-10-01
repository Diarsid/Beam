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
        EMPTY_CHOICE = new GivenAnswer(null);
    }
    
    private final Variant chosen;
        
    GivenAnswer(Variant chosen) {
        this.chosen = chosen;
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
        return this.chosen.text();
    }
    
    @Override
    public boolean is(String text) {
        return this.chosen.text().equals(text);
    }
    
    @Override
    public int index() {
        return this.chosen.index();
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
