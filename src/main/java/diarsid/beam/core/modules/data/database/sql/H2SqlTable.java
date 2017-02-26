/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.data.database.sql;

import java.util.Objects;


public class H2SqlTable implements SqlTable {
    
    private final String name;
    private final String sql;
    private final int columnsQty;
    
    public H2SqlTable(String name, String sql, int columnsQty) {
        this.name = name;
        this.sql = sql;
        this.columnsQty = columnsQty;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getSqlCreationScript() {
        return this.sql;
    }

    @Override
    public int getColumnsQty() {
        return this.columnsQty;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 53 * hash + Objects.hashCode(this.name);
        hash = 53 * hash + Objects.hashCode(this.sql);
        hash = 53 * hash + this.columnsQty;
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
        final H2SqlTable other = ( H2SqlTable ) obj;
        if ( this.columnsQty != other.columnsQty ) {
            return false;
        }
        if ( !Objects.equals(this.name, other.name) ) {
            return false;
        }
        if ( !Objects.equals(this.sql, other.sql) ) {
            return false;
        }
        return true;
    }
}
