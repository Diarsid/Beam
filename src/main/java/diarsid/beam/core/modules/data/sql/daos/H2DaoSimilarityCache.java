/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.data.sql.daos;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import diarsid.beam.core.base.analyze.similarity.SimilarityData;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.data.DataBase;
import diarsid.beam.core.modules.data.BeamCommonDao;
import diarsid.beam.core.modules.data.DaoSimilarityCache;
import diarsid.jdbc.transactions.JdbcTransaction;
import diarsid.jdbc.transactions.core.Params;
import diarsid.jdbc.transactions.exceptions.TransactionHandledException;
import diarsid.jdbc.transactions.exceptions.TransactionHandledSQLException;

import static java.time.LocalDateTime.now;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import static diarsid.beam.core.Beam.systemInitiator;
import static diarsid.beam.core.base.util.Logging.logFor;
import static diarsid.jdbc.transactions.core.Params.params;


class H2DaoSimilarityCache  
        extends BeamCommonDao 
        implements DaoSimilarityCache {
    
    H2DaoSimilarityCache(DataBase dataBase, InnerIoEngine ioEngine) {
        super(dataBase, ioEngine);
    }

    @Override
    public List<SimilarityData> loadAll(int algorithmVersion) {
        try (JdbcTransaction transact = super.openTransaction()) {
            
            return transact
                    .doQueryAndStreamVarargParams(
                            (row) -> {
                                return new SimilarityData(
                                        row.get("target", String.class), 
                                        row.get("pattern", String.class), 
                                        row.get("isSimilar", Boolean.class));
                            }, 
                            "SELECT target, pattern, isSimilar " +
                            "FROM similarity_cache " +
                            "WHERE algorithm_version IS ? ", 
                            algorithmVersion)
                    .collect(toList());
            
        } catch(TransactionHandledSQLException|TransactionHandledException e) {
            logFor(this).error(e.getMessage(), e);
            super.ioEngine().report(systemInitiator(), "cannot load similarity data.");
            return emptyList();
        }
    }

    @Override
    public Map<Long, Boolean> loadAllHashesWith(int algorithmVersion) {
        try (JdbcTransaction transact = super.openTransaction()) {
            
            Map<Long, Boolean> cache = new HashMap<>();
            
            transact
                    .doQueryVarargParams(
                            (row) -> {
                                cache.put(
                                        row.get("pair_hash", Long.class), 
                                        row.get("isSimilar", Boolean.class));
                            }, 
                            "SELECT pair_hash, isSimilar " +
                            "FROM similarity_cache " +
                            "WHERE algorithm_version IS ? ", 
                            algorithmVersion);
            
            return cache;
            
        } catch(TransactionHandledSQLException|TransactionHandledException e) {
            logFor(this).error(e.getMessage(), e);
            super.ioEngine().report(systemInitiator(), "cannot load similarity data.");
            return emptyMap();
        }
    }

    @Override
    public void persistAll(List<SimilarityData> similarityData, int algorithmVersion) {
        try (JdbcTransaction transact = super.openTransaction()) {
            
            transact
                    .doBatchUpdate(
                            "INSERT INTO similarity_cache (" +
                            "   uuid, " +
                            "   algorithm_version, " +
                            "   target, " +
                            "   pattern, " +
                            "   pair_hash, " +
                            "   isSimilar, " +
                            "   created ) " +
                            "VALUES ( ?, ?, ?, ?, ?, ?, ? ) ", 
                            toParams(algorithmVersion, similarityData));
            
        } catch(TransactionHandledSQLException|TransactionHandledException e) {
            logFor(this).error(e.getMessage(), e);
            super.ioEngine().report(systemInitiator(), "cannot save similarity data.");
        }
    }
    
    private static Set<Params> toParams(int algorithmVersion, List<SimilarityData> similarityData) {
        LocalDateTime now = now();
        return similarityData
                    .stream()
                    .map(data -> params(
                            randomUUID(),
                            algorithmVersion, 
                            data.target(), 
                            data.pattern(), 
                            data.hash(),
                            data.isSimilar(),
                            now))
                    .collect(toSet());
    }
    
}
