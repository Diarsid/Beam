/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.starter;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static java.lang.String.format;

import static diarsid.beam.core.base.util.CollectionsUtils.nonEmpty;
import static diarsid.beam.core.base.util.Logs.log;

/**
 *
 * @author Diarsid
 */
public class Procedure {
    
    private Optional<FlagStartable> startable;
    private boolean procedureIsSet;
    private final Set<FlagConfigurable> configurables;
    private final Set<FlagExecutable> executables;
    
    public Procedure() {
        this.procedureIsSet = false;
        this.startable = Optional.empty();
        this.configurables = new HashSet<>();
        this.executables = new HashSet<>();
    }
    
    void acceptFlag(Flag newFlag) {
        if ( this.procedureIsSet ) {
            return;
        }
        switch ( newFlag.type() ) {
            case STARTABLE : {
                FlagStartable startableFlag = (FlagStartable) newFlag;
                if ( this.startable.isPresent() ) {
                    if ( this.startable.get().hasLowerPriorityThan(startableFlag) ) {
                        this.warnThatPreviousFlagIsSuppressed(startableFlag);
                        this.startable = Optional.of(startableFlag);
                    } else {
                        this.warnThatNewFlagIsSuppressed(startableFlag);
                    }
                } else {
                    this.startable = Optional.of(startableFlag);
                }    
                break;
            }
            case EXECUTABLE : {
                this.executables.add((FlagExecutable) newFlag);
                break;
            }
            case CONFIGURABLE : {
                this.configurables.add((FlagConfigurable) newFlag);
                break;
            }
            default : {}
        }
    }

    private void warnThatPreviousFlagIsSuppressed(FlagStartable startableFlag) {
        log(Procedure.class, format(" %s is suppressed by %s",
                this.startable.get().text(),
                startableFlag.text()));
    }

    private void warnThatNewFlagIsSuppressed(FlagStartable startableFlag) {
        log(Procedure.class, format(" %s is suppressed by %s",
                startableFlag.text(),
                this.startable.get().text()));
    }
    
    void flagsAccepted() {
        this.procedureIsSet = true;
    }
    
    boolean hasStartable() {
        return this.startable.isPresent();
    }
    
    boolean hasAnyExecutables() {
        return nonEmpty(this.executables);
    }
    
    boolean hasAnyConfigurables() {
        return nonEmpty(this.configurables);
    }
    
    FlagStartable getStartable() {
        return this.startable.get();
    }

    public Set<FlagExecutable> getExecutables() {
        return this.executables;
    }

    public Set<FlagConfigurable> getConfigurables() {
        return this.configurables;
    }
}
