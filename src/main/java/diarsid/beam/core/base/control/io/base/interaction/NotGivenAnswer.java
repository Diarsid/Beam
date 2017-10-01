/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.control.io.base.interaction;


abstract class NotGivenAnswer implements Answer {
    
    static final NotGivenAnswer UNSATISFIED_ANSWER;
    static final NotGivenAnswer REJECTED_ANSWER;
    static final NotGivenAnswer HELP_REQUEST_ANSWER;
    
    static {
        UNSATISFIED_ANSWER = new NotGivenAnswer() {
            @Override
            public boolean variantsAreNotSatisfactory() {
                return true;
            }
        };
        
        REJECTED_ANSWER = new NotGivenAnswer() {
            @Override
            public boolean isRejection() {
                return true;
            }
        };
        
        HELP_REQUEST_ANSWER = new NotGivenAnswer() {
            @Override
            public boolean isHelpRequest() {
                return true;
            }
        };
    }

    private NotGivenAnswer() {
    }

    @Override
    public int index() {
        throw new IllegalStateException("this Answer is not given."); 
    }

    @Override
    public boolean is(String text) {
        throw new IllegalStateException("this Answer is not given."); 
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
        return false;
    }

    @Override
    public String text() {
        throw new IllegalStateException("this Answer is not given."); 
    }    
}
