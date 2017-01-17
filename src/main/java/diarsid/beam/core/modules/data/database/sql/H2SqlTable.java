/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.data.database.sql;


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
}
