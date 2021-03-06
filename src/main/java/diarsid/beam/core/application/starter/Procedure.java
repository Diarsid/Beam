/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.application.starter;

import diarsid.support.configuration.Configuration;
import diarsid.support.objects.Possible;

import static java.lang.String.format;
import static java.util.Arrays.stream;

import static diarsid.beam.core.application.environment.BeamEnvironment.configuration;
import static diarsid.beam.core.application.starter.Flags.flagOf;
import static diarsid.support.log.Logging.logFor;
import static diarsid.support.objects.Possibles.possibleButEmpty;

/**
 *
 * @author Diarsid
 */
class Procedure {
    
    private final Possible<FlagLaunchable> launchable;
    private boolean procedureIsSet;
    
    Procedure() {
        this.procedureIsSet = false;
        this.launchable = possibleButEmpty();
    }
    
    static Procedure defineProcedure(String[] flags) {
        Configuration configuration = configuration();
        Procedure procedure = new Procedure();
        stream(flags)
                .map(flagString -> flagOf(flagString))
                .filter(optionalFlag -> optionalFlag.isPresent())      
                .map(optionalFlag -> optionalFlag.get())
                .forEach(flag -> procedure.acceptFlag(flag));
        procedure.flagsAccepted();
        return procedure;
    }
    
    void acceptFlag(Flag newFlag) {
        if ( this.procedureIsSet ) {
            return;
        }
        switch ( newFlag.type() ) {
            case STARTABLE : {
                FlagLaunchable launchableFlag = (FlagLaunchable) newFlag;
                if ( this.launchable.isPresent() ) {
                    if ( this.launchable.orThrow().hasLowerPriorityThan(launchableFlag) ) {
                        this.warnThatPreviousFlagIsSuppressed(launchableFlag);
                        this.launchable.resetTo(launchableFlag);
                    } else {
                        this.warnThatNewFlagIsSuppressed(launchableFlag);
                    }
                } else {
                    this.launchable.resetTo(launchableFlag);
                }    
                break;
            }
            default : {
            }
        }
    }

    private void warnThatPreviousFlagIsSuppressed(FlagLaunchable startableFlag) {
        logFor(this).info(format(" %s is suppressed by %s",
                this.launchable.orThrow().text(),
                startableFlag.text()));
    }

    private void warnThatNewFlagIsSuppressed(FlagLaunchable startableFlag) {
        logFor(this).info(format(" %s is suppressed by %s",
                startableFlag.text(),
                this.launchable.orThrow().text()));
    }
    
    void flagsAccepted() {
        this.procedureIsSet = true;
    }
    
    boolean hasLaunchable() {
        return this.launchable.isPresent();
    }
    
    FlagLaunchable getLaunchable() {
        return this.launchable.orThrow();
    }
}
