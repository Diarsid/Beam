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

import diarsid.beam.core.base.analyze.PersistableCacheData;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.data.DataBase;
import diarsid.beam.core.modules.data.BeamCommonDao;
import diarsid.beam.core.modules.data.DaoPersistableCacheData;
import diarsid.jdbc.transactions.JdbcTransaction;
import diarsid.jdbc.transactions.RowConversion;
import diarsid.jdbc.transactions.RowOperation;
import diarsid.jdbc.transactions.core.Params;
import diarsid.jdbc.transactions.exceptions.TransactionHandledException;
import diarsid.jdbc.transactions.exceptions.TransactionHandledSQLException;
import diarsid.support.objects.Possible;

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
import static diarsid.jdbc.transactions.core.Params.params;
import static diarsid.support.log.Logging.logFor;
import static diarsid.support.objects.Possibles.possibleButEmpty;


class H2DaoPersistableCacheData <T>  
        extends BeamCommonDao 
        implements DaoPersistableCacheData<T> {
    
    private static final String CACHE_TABLE;
    private static final String CACHE_COLUMN;
    private static final String SQL_TEMPLATE_SELECT_ALL_CACHED;
    private static final String SQL_TEMPLATE_SELECT_HASHES_WITH_ALGORITHM;
    private static final String SQL_TEMPLATE_SELECT_UUIDS_WHERE_HASHES_IN;
    private static final String SQL_TEMPLATE_DELETE_WHERE_UUID_IS;
    
    static {
        CACHE_TABLE = ":table";
        CACHE_COLUMN = ":column";
        
        SQL_TEMPLATE_SELECT_ALL_CACHED = 
                "SELECT target, pattern, " + CACHE_COLUMN + " " +
                "FROM " + CACHE_TABLE + " " +
                "WHERE algorithm_version IS ? ";
        
        SQL_TEMPLATE_SELECT_HASHES_WITH_ALGORITHM = 
                "SELECT pair_hash, " + CACHE_COLUMN + " " +
                "FROM " + CACHE_TABLE + " " +
                "WHERE algorithm_version IS ? ";
        
        SQL_TEMPLATE_SELECT_UUIDS_WHERE_HASHES_IN = 
                "SELECT uuid " +
                "FROM " + CACHE_TABLE + " " +
                "WHERE pair_hash IN ( %s )";
        
        SQL_TEMPLATE_DELETE_WHERE_UUID_IS = 
                "DELETE FROM " + CACHE_TABLE + " " +
                "WHERE uuid = ? ";
        
    }
    
    private final Object similarityCacheLock;
    private final Class<T> cachedType;
    private final RowConversion<PersistableCacheData<T>> rowToDataConversion;
    private final RowConversion<UUID> rowToUuidConversion;
    private final String cacheDataTableName;
    private final String cacheDataColumnName;   
    private final String sqlSelectAllFromCacheTable;
    private final String sqlSelectHashesWithAlgorithm;
    private final String sqlSelectUuidsWhereHashesIn;
    private final String sqlDeleteWhereUuidIs;
    
    H2DaoPersistableCacheData(
            DataBase dataBase,
            InnerIoEngine ioEngine,
            String cacheDataTableName,
            String cacheDataColumnName,
            Class<T> cachedType,
            RowConversion<PersistableCacheData<T>> rowToDataConversion) {
        super(dataBase, ioEngine);
        this.similarityCacheLock = new Object();
        this.cachedType = cachedType;
        this.rowToDataConversion = rowToDataConversion;
        this.rowToUuidConversion = (row) -> {
            return row.get("uuid", UUID.class);
        };
        
        this.cacheDataTableName = cacheDataTableName;
        this.cacheDataColumnName = cacheDataColumnName;
        
        this.sqlSelectAllFromCacheTable = 
                SQL_TEMPLATE_SELECT_ALL_CACHED
                        .replace(CACHE_TABLE, this.cacheDataTableName)
                        .replace(CACHE_COLUMN, this.cacheDataColumnName);
        
        this.sqlSelectHashesWithAlgorithm = 
                SQL_TEMPLATE_SELECT_HASHES_WITH_ALGORITHM
                        .replace(CACHE_TABLE, this.cacheDataTableName)
                        .replace(CACHE_COLUMN, this.cacheDataColumnName);
        
        this.sqlSelectUuidsWhereHashesIn = 
                SQL_TEMPLATE_SELECT_UUIDS_WHERE_HASHES_IN
                        .replace(CACHE_TABLE, this.cacheDataTableName);
        
        this.sqlDeleteWhereUuidIs = 
                SQL_TEMPLATE_DELETE_WHERE_UUID_IS
                        .replace(CACHE_TABLE, this.cacheDataTableName);
    }

    @Override
    public List<PersistableCacheData<T>> loadAll(int algorithmVersion) {
        try (JdbcTransaction transact = super.openTransaction()) {
            
            return transact
                    .doQueryAndStreamVarargParams(
                            this.rowToDataConversion, 
                            this.sqlSelectAllFromCacheTable, 
                            algorithmVersion)
                    .collect(toList());
            
        } catch(TransactionHandledSQLException | TransactionHandledException e) {
            logFor(this).error(e.getMessage(), e);
            super.ioEngine().report(systemInitiator(), "cannot load cache data.");
            return emptyList();
        }
    }

    @Override
    public Map<Long, T> loadAllHashesWith(int algorithmVersion) {
        try (JdbcTransaction transact = super.openTransaction()) {
            
            Map<Long, T> cache = new HashMap<>();
            
            String columnName = this.cacheDataColumnName;
            Class<T> type = this.cachedType;
            transact
                    .doQueryVarargParams(
                            (row) -> {
                                cache.put(
                                        row.get("pair_hash", Long.class), 
                                        row.get(columnName, type));
                            }, 
                            this.sqlSelectHashesWithAlgorithm, 
                            algorithmVersion);
            
            return cache;
            
        } catch(TransactionHandledSQLException|TransactionHandledException e) {
            logFor(this).error(e.getMessage(), e);
            super.ioEngine().report(systemInitiator(), "cannot load similarity data.");
            return emptyMap();
        }
    }

    @Override
    public void persistAll(List<PersistableCacheData<T>> persistableCacheData, int algorithmVersion) {
        synchronized ( this.similarityCacheLock ) {
            try (JdbcTransaction transact = super.openTransaction()) {
                
                String in = persistableCacheData
                        .stream()
                        .map(data -> "?")
                        .collect(joining(", "));
                
                List<Long> hashes = persistableCacheData
                        .stream()
                        .map(data -> data.hash())
                        .collect(toList());
                
                List<UUID> uuids = transact
                        .doQueryAndStreamVarargParams(
                                this.rowToUuidConversion, 
                                format(this.sqlSelectUuidsWhereHashesIn, in),                                
                                hashes)
                        .collect(toList());
                
                if ( nonEmpty(uuids) ) {
                    
                    Set<Params> params = uuids
                            .stream()
                            .map(uuid -> params(uuid))
                            .collect(toSet());
                    
                    transact
                            .doBatchUpdate(
                                    this.sqlDeleteWhereUuidIs, 
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
                                toParams(algorithmVersion, persistableCacheData));

            } catch(TransactionHandledSQLException|TransactionHandledException e) {
                logFor(this).error(e.getMessage(), e);
                super.ioEngine().report(systemInitiator(), "cannot save similarity data.");
            }
        }    
    }
    
    private Set<Params> toParams(
            int algorithmVersion, List<PersistableCacheData<T>> cacheData) {
        LocalDateTime now = now();
        return cacheData
                    .stream()
                    .map(data -> params(
                            randomUUID(),
                            algorithmVersion, 
                            data.target(), 
                            data.pattern(), 
                            data.hash(),
                            data.cacheable(),
                            now))
                    .collect(toSet());
    }

    @Override
    public Map<Long, T> reassessAllHashesOlderThan(
            int algorithmVersion, 
            BiFunction<String, String, T> similarityFunction) {
        
        Map<Long, T> reassessedCachHashes = new HashMap<>();
        
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
        
        T newSimilarity;
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
