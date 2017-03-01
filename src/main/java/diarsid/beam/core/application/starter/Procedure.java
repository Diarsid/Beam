/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.application.starter;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static java.lang.String.format;
import static java.util.Arrays.stream;

import static diarsid.beam.core.application.starter.Flags.flagOf;
import static diarsid.beam.core.base.util.CollectionsUtils.nonEmpty;
import static diarsid.beam.core.base.util.Logs.log;

/**
 *
 * @author Diarsid
 */
public class Procedure {
    
    private Optional<FlagLaunchable> launchable;
    private boolean procedureIsSet;
    private final Set<FlagConfigurable> configurables;
    
    public Procedure() {
        this.procedureIsSet = false;
        this.launchable = Optional.empty();
        this.configurables = new HashSet<>();
    }
    
    static Procedure defineProcedure(String[] flags) {
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
                FlagLaunchable startableFlag = (FlagLaunchable) newFlag;
                if ( this.launchable.isPresent() ) {
                    if ( this.launchable.get().hasLowerPriorityThan(startableFlag) ) {
                        this.warnThatPreviousFlagIsSuppressed(startableFlag);
                        this.launchable = Optional.of(startableFlag);
                    } else {
                        this.warnThatNewFlagIsSuppressed(startableFlag);
                    }
                } else {
                    this.launchable = Optional.of(startableFlag);
                }    
                break;
            }
            case CONFIGURABLE : {
                this.configurables.add((FlagConfigurable) newFlag);
                break;
            }
            default : {}
        }
    }

    private void warnThatPreviousFlagIsSuppressed(FlagLaunchable startableFlag) {
        log(Procedure.class, format(" %s is suppressed by %s",
                this.launchable.get().text(),
                startableFlag.text()));
    }

    private void warnThatNewFlagIsSuppressed(FlagLaunchable startableFlag) {
        log(Procedure.class, format(" %s is suppressed by %s",
                startableFlag.text(),
                this.launchable.get().text()));
    }
    
    void flagsAccepted() {
        this.procedureIsSet = true;
    }
    
    boolean hasLaunchable() {
        return this.launchable.isPresent();
    }
    
    boolean hasAnyConfigurables() {
        return nonEmpty(this.configurables);
    }
    
    FlagLaunchable getLaunchable() {
        return this.launchable.get();
    }

    public Set<FlagConfigurable> getConfigurables() {
        return this.configurables;
    }
}
