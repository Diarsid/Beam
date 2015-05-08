package com.drs.beam.tasks;

/**
 * Org by Diarsid
 * Time: 22:04 - 16.01.15
 * IDE: IntelliJ IDEA 12
 */

class VerifyFailureException extends Exception{
    private String message;
    String getVerifyMessage(){
        return message;
    }
    VerifyFailureException(String message){
      this.message = message;
    }

}
