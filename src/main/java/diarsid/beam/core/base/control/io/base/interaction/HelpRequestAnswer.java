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
class HelpRequestAnswer implements Answer {
    
    private final int helpIndex;

    HelpRequestAnswer(int helpIndex) {
        this.helpIndex = helpIndex;
    }

    @Override
    public int index() {
        return this.helpIndex;
    }

    @Override
    public boolean is(String text) {
        throw new IllegalStateException("this Answer is help request."); 
    }

    @Override
    public boolean isGiven() {
        return false;
    }

    @Override
    public boolean isNotGiven() {
        return true;
    }

    @Override
    public boolean isRejection() {
        return false;
    }
    
    @Override
    public boolean variantsAreNotSatisfactory() {
        return false;
    }
    
    @Override
    public boolean isHelpRequest() {
        return true;
    }

    @Override
    public String text() {
        throw new IllegalStateException("this Answer is help request."); 
    }    
    
}
