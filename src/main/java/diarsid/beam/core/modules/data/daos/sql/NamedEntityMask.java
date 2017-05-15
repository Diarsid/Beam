/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.data.daos.sql;

import java.util.Objects;

import diarsid.beam.core.base.control.io.base.interaction.Variant;
import diarsid.beam.core.domain.entities.NamedEntity;
import diarsid.beam.core.domain.entities.NamedEntityType;
import diarsid.jdbc.transactions.Row;
import diarsid.jdbc.transactions.exceptions.TransactionHandledSQLException;

import static java.lang.String.format;

import static diarsid.beam.core.domain.entities.NamedEntityType.fromString;

/**
 *
 * @author Diarsid
 */
class NamedEntityMask implements NamedEntity {
    
    private final String name;
    private final NamedEntityType type;

    NamedEntityMask(Row row) throws TransactionHandledSQLException {
        this.name = (String) row.get("entity_name");
        this.type = fromString((String) row.get("entity_type"));
    }
    
    NamedEntityMask(String name, NamedEntityType type) {
        this.name = name;
        this.type = type;
    }
    
    @Override
    public String name() {
        return this.name;
    }

    @Override
    public NamedEntityType type() {
        return this.type;
    }

    @Override
    public Variant toVariant(int variantIndex) {
        return new Variant(
                this.name,
                format("%s (%s)", this.name, this.type.displayName()), 
                variantIndex);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 61 * hash + Objects.hashCode(this.name);
        hash = 61 * hash + Objects.hashCode(this.type);
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
        final NamedEntityMask other = ( NamedEntityMask ) obj;
        if ( !Objects.equals(this.name, other.name) ) {
            return false;
        }
        if ( this.type != other.type ) {
            return false;
        }
        return true;
    }
    
    @Override
    public String toString() {
        return format("Mask(%s : $s)", this.type, this.name);
    }
}
