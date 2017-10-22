/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.data.sql.database;

import diarsid.beam.core.base.data.SqlObjectType;
import diarsid.beam.core.base.data.SqlConstraint;

import java.util.Objects;

import static diarsid.beam.core.base.data.SqlObjectType.CONSTRAINT;

/**
 *
 * @author Diarsid
 */
class H2SqlConstraint implements SqlConstraint {
    
    private final String name;
    private final String sql;

    H2SqlConstraint(String name, String sql) {
        this.name = name;
        this.sql = sql;
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public String creationScript() {
        return this.sql;
    }
    
    @Override
    public SqlObjectType type() {
        return CONSTRAINT;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 79 * hash + Objects.hashCode(this.name);
        hash = 79 * hash + Objects.hashCode(this.sql);
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
        final H2SqlConstraint other = ( H2SqlConstraint ) obj;
        if ( !Objects.equals(this.name, other.name) ) {
            return false;
        }
        if ( !Objects.equals(this.sql, other.sql) ) {
            return false;
        }
        return true;
    }    
    
}
