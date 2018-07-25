/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.util;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 *
 * @author Diarsid
 */
public class Possible<T> {
    
    private T t;
    
    private Possible(T t) {
        this.t = t;
    }
    
    public static <T> Possible<T> possibleButEmpty() {
        return new Possible<>(null);
    }
    
    public static <T> Possible<T> possibleWith(T t) {
        return new Possible<>(t);
    }
    
    public static <T> Possible<T> possibleOf(Optional<T> optionalT) {
        return new Possible<>(optionalT.orElse(null));
    }
    
    public Possible<T> orDefault(T defaultT) {
        if ( isNull(this.t) ) {
            this.t = defaultT;
        }
        return this;
    }
    
    private void checkValueNotNull() {
        if ( isNull(this.t) ) {
            throw new NullPointerException();
        }
    }
    
    public boolean match(Predicate<T> predicate) {
        return this.isPresent() && predicate.test(this.t);
    }
    
    public boolean notMatch(Predicate<T> predicate) {
        return this.isNotPresent() || ! predicate.test(this.t);
    }
    
    public boolean isPresent() {
        return nonNull(this.t);
    }
    
    public boolean isNotPresent() {
        return isNull(this.t);
    }
    
    public void ifPresent(Consumer<T> consumer) {
        if ( this.isPresent() ) {
            consumer.accept(this.t);
        }
    }
    
    public void ifNotPresent(Runnable runnable) {
        if ( this.isNotPresent() ) {
            runnable.run();
        }
    }
    
    public T orThrow() {
        this.checkValueNotNull();
        return this.t;
    }
    
    public T orThrow(Supplier<RuntimeException> exceptionCreator) {
        if ( isNull(this.t) ) {
            throw exceptionCreator.get();
        } else {
            return this.t;
        }
    }
    
    public T or(T otherT) {
        return isNull(this.t) ? otherT : t;
    }
    
    public Optional<T> optional() {
        return Optional.ofNullable(this.t);
    }
    
    public T resetTo(T newT) {
        T oldT = this.t;
        this.t = newT;
        return oldT;
    }
    
    public T resetTo(Optional<T> optionalT) {
        return this.resetTo(optionalT.orElse(null));
    }
    
    public T resetTo(Possible<T> possibleT) {
        return this.resetTo(possibleT.t);
    }
    
    public T ifPresentResetTo(T newT) {
        if ( this.isPresent() ) {
            return this.resetTo(newT);
        } else {
            return this.t;
        }
    }
    
    public T ifNotPresentResetTo(T newT) {
        if ( this.isNotPresent() ) {
            return this.resetTo(newT);
        } else {
            return this.t;
        }
    }
    
    public T nullify() {
        T oldT = this.t;
        this.t = null;
        return oldT;
    }    
    
    @Override
    public boolean equals(Object o) {
        return 
                nonNull(this.t) && 
                nonNull(o) && 
                o instanceof Possible && 
                this.t.equals(((Possible) o).t);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 37 * hash + Objects.hashCode(this.t);
        return hash;
    }
    
    public boolean notEquals(Possible<T> possibleT) {
        return ! this.equals(possibleT);
    }
    
    public boolean equalTo(T otherT) {
        return nonNull(this.t) && this.t.equals(otherT);
    }
    
    public boolean notEqualTo(T otherT) {
        return nonNull(this.t) && ! this.t.equals(otherT);
    }
}
