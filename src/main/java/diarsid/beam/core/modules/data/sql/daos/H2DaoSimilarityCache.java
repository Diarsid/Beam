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
import java.util.UUID;
import java.util.function.BiFunction;

import diarsid.beam.core.base.analyze.similarity.SimilarityData;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.data.DataBase;
import diarsid.support.objects.Possible;
import diarsid.beam.core.modules.data.BeamCommonDao;
import diarsid.beam.core.modules.data.DaoSimilarityCache;
import diarsid.jdbc.transactions.JdbcTransaction;
import diarsid.jdbc.transactions.RowOperation;
import diarsid.jdbc.transactions.core.Params;
import diarsid.jdbc.transactions.exceptions.TransactionHandledException;
import diarsid.jdbc.transactions.exceptions.TransactionHandledSQLException;

import static java.lang.String.format;
import static java.time.LocalDateTime.now;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import static diarsid.beam.core.Beam.systemInitiator;
import static diarsid.beam.core.base.util.CollectionsUtils.nonEmpty;
import static diarsid.support.log.Logging.logFor;
import static diarsid.jdbc.transactions.core.Params.params;
import static diarsid.support.objects.Possibles.possibleButEmpty;


class H2DaoSimilarityCache  
        extends BeamCommonDao 
        implements DaoSimilarityCache {
    
    private final Object similarityCacheLock;
    
    H2DaoSimilarityCache(DataBase dataBase, InnerIoEngine ioEngine) {
        super(dataBase, ioEngine);
        this.similarityCacheLock = new Object();
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
                                        row.get("pair_hash", Long.class),
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
        synchronized ( this.similarityCacheLock ) {
            try (JdbcTransaction transact = super.openTransaction()) {
                
                String in = similarityData
                        .stream()
                        .map(data -> "?")
                        .collect(joining(", "));
                
                List<Long> hashes = similarityData
                        .stream()
                        .map(data -> data.hash())
                        .collect(toList());
                
                List<UUID> uuids = transact
                        .doQueryAndStreamVarargParams(
                                (row) -> {
                                    return row.get("uuid", UUID.class);
                                }, 
                                format(
                                        "SELECT uuid " +
                                        "FROM similarity_cache " +
                                        "WHERE pair_hash IN ( %s )",
                                        in),                                
                                hashes)
                        .collect(toList());
                
                if ( nonEmpty(uuids) ) {
                    
                    Set<Params> params = uuids
                            .stream()
                            .map(uuid -> params(uuid))
                            .collect(toSet());
                    
                    transact
                            .doBatchUpdate(
                                    "DELETE FROM similarity_cache " +
                                    "WHERE uuid = ? ", 
                                    params);
                }

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

    @Override
    public Map<Long, Boolean> reassessAllHashesOlderThan(
            int algorithmVersion, 
            BiFunction<String, String, Boolean> similarityFunction) {
        
        Map<Long, Boolean> reassessedCachHashes = new HashMap<>();
        
        Possible<String> target = possibleButEmpty();
        Possible<String> pattern = possibleButEmpty();
        Possible<Long> hash = possibleButEmpty();
        Possible<UUID> uuid = possibleButEmpty();
        
        RowOperation sqlRowOperation = (row) -> {
            target.resetTo(row.get("target", String.class));
            pattern.resetTo(row.get("pattern", String.class));
            hash.resetTo(row.get("pair_hash", Long.class));
            uuid.resetTo(row.get("uuid", UUID.class));
        };
        
        boolean newSimilarity;
        boolean sameDataExist;
        
        List<Possible> fields = asList(target, pattern, hash, uuid);
        
        logFor(this).info("try to reassess old similarity data");
        
        synchronized ( this.similarityCacheLock ) {
            boolean dataForProcessExist = true;
            while ( dataForProcessExist ) {
                try (JdbcTransaction transact = super.openTransaction()) {

                    fields.forEach(field -> field.nullify());

                    transact
                            .doQueryAndProcessFirstRowVarargParams(
                                    sqlRowOperation, 
                                    "SELECT uuid, target, pattern, pair_hash " +
                                    "FROM similarity_cache " +
                                    "WHERE algorithm_version < ? ",
                                    algorithmVersion);

                    if ( uuid.isNotPresent() ) {
                        dataForProcessExist = false;
                        continue;
                    }

                    newSimilarity = similarityFunction.apply(target.orThrow(), pattern.orThrow());

                    sameDataExist = transact
                            .doesQueryHaveResultsVarargParams(
                                    "SELECT * " +
                                    "FROM similarity_cache " +
                                    "WHERE pair_hash = ? ", 
                                    hash.orThrow());

                    if ( sameDataExist ) {                    
                        transact
                                .doUpdateVarargParams(
                                        "DELETE FROM similarity_cache " +
                                        "WHERE pair_hash = ? AND uuid != ? ", 
                                        hash.orThrow(), uuid.orThrow());
                    } else {
                        reassessedCachHashes.put(hash.orThrow(), newSimilarity);
                    }

                    transact
                            .doUpdateVarargParams(
                                    "UPDATE similarity_cache " +
                                    "SET " +
                                    "   isSimilar = ?, " +
                                    "   algorithm_version = ?, " +
                                    "   created = ? " + 
                                    "WHERE uuid = ? ", 
                                    newSimilarity, algorithmVersion, now(), uuid.orThrow());

                } catch(TransactionHandledSQLException|TransactionHandledException e) {
                    logFor(this).error(e.getMessage(), e);
                    super.ioEngine().report(systemInitiator(), "cannot reassess obsolete hashes.");
                }
            }
        }
        
        return reassessedCachHashes;
    }
    
}
