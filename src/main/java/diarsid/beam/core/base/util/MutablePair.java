/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.util;

import java.util.Objects;

/**
 *
 * @author Diarsid
 */
public class MutablePair <L, R> {
    
    private L left;
    private R right;
    
    public MutablePair(L left, R right) {
        this.left = left;
        this.right = right;
    }
    
    public boolean hasLeft() {
        return this.left != null;
    }
    
    public boolean hasRight() {
        return this.right != null;
    }

    public L getLeft() {
        return this.left;
    }

    public void setLeft(L left) {
        this.left = left;
    }

    public R getRight() {
        return this.right;
    }

    public void setRight(R right) {
        this.right = right;
    }  
    
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 37 * hash + Objects.hashCode(this.left);
        hash = 37 * hash + Objects.hashCode(this.right);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj ) {
            return true;
        }
        if ( obj == null ) {
            return false;
        }
        if ( getClass() != obj.getClass() ) {
            return false;
        }
        final MutablePair<?, ?> other = ( MutablePair<?, ?> ) obj;
        if ( !Objects.equals(this.left, other.left) ) {
            return false;
        }
        if ( !Objects.equals(this.right, other.right) ) {
            return false;
        }
        return true;
    }
    
    
}
