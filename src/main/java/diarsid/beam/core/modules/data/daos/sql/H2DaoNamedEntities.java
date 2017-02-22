/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.data.daos.sql;

import java.util.List;

import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.domain.entities.NamedEntity;
import diarsid.beam.core.domain.entities.NamedEntityType;
import diarsid.beam.core.modules.data.DaoNamedEntities;
import diarsid.beam.core.modules.data.DataBase;
import diarsid.beam.core.modules.data.daos.BeamCommonDao;
import diarsid.jdbc.transactions.PerRowConversion;
import diarsid.jdbc.transactions.exceptions.TransactionHandledSQLException;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import static diarsid.beam.core.domain.entities.NamedEntityType.fromString;
import static diarsid.beam.core.base.util.Logs.logError;
import static diarsid.beam.core.base.util.SqlUtil.SqlOperator.AND;
import static diarsid.beam.core.base.util.SqlUtil.lowerWildcard;
import static diarsid.beam.core.base.util.SqlUtil.lowerWildcardLists;
import static diarsid.beam.core.base.util.SqlUtil.multipleLowerLIKE;


class H2DaoNamedEntities 
        extends BeamCommonDao
        implements DaoNamedEntities {
    
    private final PerRowConversion<NamedEntity> rowToNamedEntityConversion;
    
    H2DaoNamedEntities(DataBase dataBase, InnerIoEngine ioEngine) {
        super(dataBase, ioEngine);
        this.rowToNamedEntityConversion = (row) -> {
            return new NamedEntity() {
                
                private final String name = (String) row.get("entity_name");
                private final NamedEntityType type = fromString((String) row.get("entity_type"));
                
                @Override
                public String getName() {
                    return this.name;
                }

                @Override
                public NamedEntityType getEntityType() {
                    return this.type;
                }
            };
        };
    }

    @Override
    public List<NamedEntity> getEntitiesByNamePattern(
            Initiator initiator, String namePattern) {
        try {
            return super.getDisposableTransaction()
                    .doQueryAndStreamVarargParams(
                            NamedEntity.class,
                            "SELECT loc_name AS entity_name, 'location' AS entity_type " +
                            "FROM locations " +
                            "WHERE LOWER(loc_name) LIKE ? " +
                            "       UNION ALL " +
                            "SELECT bat_name, 'batch' " +
                            "FROM batches " +
                            "WHERE LOWER(bat_name) LIKE ? " +
                            "       UNION ALL " +
                            "SELECT page_name, 'webpage' " +
                            "FROM webpages " +
                            "WHERE LOWER(page_name) LIKE ? ",
                            this.rowToNamedEntityConversion,
                            lowerWildcard(namePattern), 
                            lowerWildcard(namePattern), 
                            lowerWildcard(namePattern))
                    .collect(toList());
        } catch (TransactionHandledSQLException ex) {
            logError(H2DaoNamedEntities.class, ex);
            super.ioEngine().report(
                    initiator, "named entities obtaining by name pattern failed.");
            return emptyList();
        }
    }

    @Override
    public List<NamedEntity> getEntitiesByNamePatternParts(
            Initiator initiator, List<String> namePatternParts) {
        try {
            return super.getDisposableTransaction()
                    .doQueryAndStream(NamedEntity.class,
                            "SELECT loc_name AS entity_name, 'location' AS entity_type " +
                            "FROM locations " +
                            "WHERE " + multipleLowerLIKE("loc_name", namePatternParts.size(), AND) + 
                            "       UNION ALL " +
                            "SELECT bat_name, 'batch' " +
                            "FROM batches " +
                            "WHERE " + multipleLowerLIKE("bat_name", namePatternParts.size(), AND) + 
                            "       UNION ALL " +
                            "SELECT page_name, 'webpage' " +
                            "FROM webpages " +
                            "WHERE " + multipleLowerLIKE("page_name", namePatternParts.size(), AND),
                            this.rowToNamedEntityConversion,
                            lowerWildcardLists(
                                    namePatternParts, 
                                    namePatternParts, 
                                    namePatternParts))
                    .collect(toList());
        } catch (TransactionHandledSQLException ex) {
            logError(H2DaoNamedEntities.class, ex);
            super.ioEngine().report(
                    initiator, "named entities obtaining by name pattern parts failed.");
            return emptyList();
        }
    }
}
