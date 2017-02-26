/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.application.systemconsole;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 *
 * @author Diarsid
 */
public class SystemIO {
    
    private SystemIO() {
    }
    
    public static BufferedWriter provideWriter() {
        BufferedWriter bufferedWriter = null;
        try {
            bufferedWriter = new BufferedWriter(System.console().writer());
        } catch (NullPointerException e) {
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(System.out));
        }
        return bufferedWriter;
    }

    public static BufferedReader provideReader() {
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(System.console().reader());
        } catch (NullPointerException e) {
            bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        }
        return bufferedReader;
    }
}
