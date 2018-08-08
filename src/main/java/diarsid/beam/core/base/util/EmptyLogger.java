/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.util;

import org.slf4j.Logger;
import org.slf4j.Marker;

/**
 *
 * @author Diarsid
 */
class EmptyLogger implements Logger {
    
    EmptyLogger() {}

    private boolean disabled() {
        return false;
    }
    
    @Override
    public String getName() {
        return "empty logger";
    }

    @Override
    public boolean isTraceEnabled() {
        return this.disabled();
    }

    @Override
    public void trace(String msg) {
        
    }

    @Override
    public void trace(String format, Object arg) {
        
    }

    @Override
    public void trace(String format, Object arg1, Object arg2) {
        
    }

    @Override
    public void trace(String format, Object... arguments) {
        
    }

    @Override
    public void trace(String msg, Throwable t) {
        
    }

    @Override
    public boolean isTraceEnabled(Marker marker) {
        return this.disabled();
    }

    @Override
    public void trace(Marker marker, String msg) {
        
    }

    @Override
    public void trace(Marker marker, String format, Object arg) {
        
    }

    @Override
    public void trace(Marker marker, String format, Object arg1, Object arg2) {
        
    }

    @Override
    public void trace(Marker marker, String format, Object... argArray) {
        
    }

    @Override
    public void trace(Marker marker, String msg, Throwable t) {
        
    }

    @Override
    public boolean isDebugEnabled() {
        return this.disabled();
    }

    @Override
    public void debug(String msg) {
        
    }

    @Override
    public void debug(String format, Object arg) {
        
    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {
        
    }

    @Override
    public void debug(String format, Object... arguments) {
        
    }

    @Override
    public void debug(String msg, Throwable t) {
        
    }

    @Override
    public boolean isDebugEnabled(Marker marker) {
        return this.disabled();
    }

    @Override
    public void debug(Marker marker, String msg) {
        
    }

    @Override
    public void debug(Marker marker, String format, Object arg) {
        
    }

    @Override
    public void debug(Marker marker, String format, Object arg1, Object arg2) {
        
    }

    @Override
    public void debug(Marker marker, String format, Object... arguments) {
        
    }

    @Override
    public void debug(Marker marker, String msg, Throwable t) {
        
    }

    @Override
    public boolean isInfoEnabled() {
        return this.disabled();
    }

    @Override
    public void info(String msg) {
        
    }

    @Override
    public void info(String format, Object arg) {
        
    }

    @Override
    public void info(String format, Object arg1, Object arg2) {
        
    }

    @Override
    public void info(String format, Object... arguments) {
        
    }

    @Override
    public void info(String msg, Throwable t) {
        
    }

    @Override
    public boolean isInfoEnabled(Marker marker) {
        return this.disabled();
    }

    @Override
    public void info(Marker marker, String msg) {
        
    }

    @Override
    public void info(Marker marker, String format, Object arg) {
        
    }

    @Override
    public void info(Marker marker, String format, Object arg1, Object arg2) {
        
    }

    @Override
    public void info(Marker marker, String format, Object... arguments) {
        
    }

    @Override
    public void info(Marker marker, String msg, Throwable t) {
        
    }

    @Override
    public boolean isWarnEnabled() {
        return this.disabled();
    }

    @Override
    public void warn(String msg) {
        
    }

    @Override
    public void warn(String format, Object arg) {
        
    }

    @Override
    public void warn(String format, Object... arguments) {
        
    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {
        
    }

    @Override
    public void warn(String msg, Throwable t) {
        
    }

    @Override
    public boolean isWarnEnabled(Marker marker) {
        return this.disabled();
    }

    @Override
    public void warn(Marker marker, String msg) {
        
    }

    @Override
    public void warn(Marker marker, String format, Object arg) {
        
    }

    @Override
    public void warn(Marker marker, String format, Object arg1, Object arg2) {
        
    }

    @Override
    public void warn(Marker marker, String format, Object... arguments) {
        
    }

    @Override
    public void warn(Marker marker, String msg, Throwable t) {
        
    }

    @Override
    public boolean isErrorEnabled() {
        return this.disabled();
    }

    @Override
    public void error(String msg) {
        
    }

    @Override
    public void error(String format, Object arg) {
        
    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
        
    }

    @Override
    public void error(String format, Object... arguments) {
        
    }

    @Override
    public void error(String msg, Throwable t) {
        
    }

    @Override
    public boolean isErrorEnabled(Marker marker) {
        return this.disabled();
    }

    @Override
    public void error(Marker marker, String msg) {
        
    }

    @Override
    public void error(Marker marker, String format, Object arg) {
        
    }

    @Override
    public void error(Marker marker, String format, Object arg1, Object arg2) {
        
    }

    @Override
    public void error(Marker marker, String format, Object... arguments) {
        
    }

    @Override
    public void error(Marker marker, String msg, Throwable t) {
        
    }
    
}
