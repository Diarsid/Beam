/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.data.database.sql;

import diarsid.beam.core.modules.data.DataBaseInitializer;

import java.util.List;

import diarsid.beam.core.modules.data.DataBase;
import diarsid.beam.core.modules.data.DataBaseModel;
import diarsid.beam.core.modules.data.DataBaseVerifier;

import static java.util.stream.Collectors.toList;

import static diarsid.beam.core.util.StringUtils.nonEmpty;


public class H2DataBaseVerifier implements DataBaseVerifier {
    
    private final SqlDataBaseInitializer initializer;
    
    public H2DataBaseVerifier(DataBaseInitializer initializer) {
        this.initializer = (SqlDataBaseInitializer) initializer;
    }

    @Override
    public List<String> verify(DataBase dataBase, DataBaseModel dataBaseModel) {
        H2DataBaseModel h2SqlModel = (H2DataBaseModel) dataBaseModel;
        return h2SqlModel
                .getTables()
                .stream()
                .map(table -> this.initializer.initializeTableIfNecessaryAndProvideReport(table))
                .filter(report -> nonEmpty(report))
                .collect(toList());
    }
}
