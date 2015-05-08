package com.drs.beam.util;

/**
 * Org by Diarsid
 * Time: 15:38 - 17.01.15
 * IDE: IntelliJ IDEA 12
 */

class DBManagerException extends Exception {
    private String message;
    String getVerifyMessage(){
        return message;
    }
    DBManagerException(String message){
        this.message = message;
    }
}
