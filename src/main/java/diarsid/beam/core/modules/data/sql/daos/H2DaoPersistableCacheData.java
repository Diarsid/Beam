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

import diarsid.beam.core.base.analyze.cache.PersistableCacheData;
import diarsid.beam.core.base.data.DataBase;
import diarsid.beam.core.base.data.DataExtractionException;
import diarsid.beam.core.modules.data.DaoPersistableCacheData;
import diarsid.jdbc.transactions.JdbcTransaction;
import diarsid.jdbc.transactions.Row;
import diarsid.jdbc.transactions.RowConversion;
import diarsid.jdbc.transactions.RowOperation;
import diarsid.jdbc.transactions.core.Params;
import diarsid.jdbc.transactions.exceptions.TransactionHandledException;
import diarsid.jdbc.transactions.exceptions.TransactionHandledSQLException;

import static java.lang.String.format;
import static java.time.LocalDateTime.now;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import static diarsid.beam.core.base.util.CollectionsUtils.nonEmpty;
import static diarsid.jdbc.transactions.core.Params.params;
import static diarsid.support.log.Logging.logFor;


class H2DaoPersistableCacheData <T>  
        extends BeamCommonDao 
        implements DaoPersistableCacheData<T> {
    
    private static final String CACHE_TABLE;
    private static final String CACHE_COLUMN;
    private static final String SQL_TEMPLATE_SELECT_ALL_CACHED;
    private static final String SQL_TEMPLATE_SELECT_HASHES_WITH_ALGORITHM;
    private static final String SQL_TEMPLATE_SELECT_UUIDS_WHERE_HASHES_IN;
    private static final String SQL_TEMPLATE_DELETE_WHERE_UUID_IS;
    private static final String SQL_TEMPLATE_INSERT_INTO_TABLE;
    private static final String SQL_TEMPLATE_SELECT_WITH_ALGORITHM_OLDER_THAN;
    private static final String SQL_TEMPLATE_SELECT_WHERE_HASH_IS;
    private static final String SQL_TEMPLATE_DELETE_WHERE_HASH_AND_UUID_ARE;
    private static final String SQL_UPDATE_CACHED_ALGORITHM_TIME_WHERE_UUID_IS;
    
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
        
        SQL_TEMPLATE_INSERT_INTO_TABLE = 
                "INSERT INTO " + CACHE_TABLE + " (" +
                "   uuid, " +
                "   algorithm_version, " +
                "   target, " +
                "   pattern, " +
                "   pair_hash, " +
                "   " + CACHE_COLUMN + ", " +
                "   created ) " +
                "VALUES ( ?, ?, ?, ?, ?, ?, ? ) ";
        
        SQL_TEMPLATE_SELECT_WITH_ALGORITHM_OLDER_THAN = 
                "SELECT uuid, target, pattern, pair_hash " +
                "FROM " + CACHE_TABLE + " " +
                "WHERE algorithm_version < ? ";
        
        SQL_TEMPLATE_SELECT_WHERE_HASH_IS = 
                "SELECT * " +
                "FROM " + CACHE_TABLE + " " +
                "WHERE pair_hash = ? ";
        
        SQL_TEMPLATE_DELETE_WHERE_HASH_AND_UUID_ARE = 
                "DELETE FROM " + CACHE_TABLE + " " +
                "WHERE pair_hash = ? AND uuid != ? ";
        
        SQL_UPDATE_CACHED_ALGORITHM_TIME_WHERE_UUID_IS = 
                "UPDATE " + CACHE_TABLE + " " +
                "SET " +
                "   " + CACHE_COLUMN + " = ?, " +
                "   algorithm_version = ?, " +
                "   created = ? " + 
                "WHERE uuid = ? ";
        
    }
    
    private static class Cached {
        
        private UUID uuid;
        private String target;
        private String pattern;
        private Long pairHash;
        private boolean filled;
        
        void fillFrom(Row row) throws TransactionHandledSQLException {
            try {
                this.target = row.get("target", String.class);
                this.pattern = row.get("pattern", String.class);
                this.pairHash = row.get("pair_hash", Long.class);
                this.uuid = row.get("uuid", UUID.class);
                this.filled = true;
            } catch (Exception e) {
                this.clear();
                throw e;
            }            
        }

        UUID uuid() {
            return this.uuid;
        }

        String target() {
            return this.target;
        }

        String pattern() {
            return this.pattern;
        }

        Long pairHash() {
            return this.pairHash;
        }
        
        boolean isFilled() {
            return this.filled;
        }
        
        boolean isEmpty() {
            return ! this.filled;
        }
        
        void clear() {
            this.target = null;
            this.pattern = null;
            this.pairHash = null;
            this.uuid = null;
        }
        
    }
    
    private final Object similarityCacheLock;
    private final Class<T> cachedType;
    private final String cacheName;
    private final RowConversion<PersistableCacheData<T>> rowToDataConversion;
    private final RowConversion<UUID> rowToUuidConversion;
    private final String cacheDataTableName;
    private final String cacheDataColumnName;   
    private final String sqlSelectAllFromCacheTable;
    private final String sqlSelectHashesWithAlgorithm;
    private final String sqlSelectUuidsWhereHashesIn;
    private final String sqlDeleteWhereUuidIs;
    private final String sqlInsertInto;
    private final String sqlSelectAllWithOlderAlgorithm;
    private final String sqlSelectWhereHashIs;
    private final String sqlDeleteWhereHashAndUuidAre;
    private final String sqlUpdateCachedAlgorithmTimeWhereUuidIs;
    
    H2DaoPersistableCacheData(
            DataBase dataBase,
            String cacheDataTableName,
            String cacheDataColumnName,
            Class<T> cachedType,
            String cacheName,
            RowConversion<PersistableCacheData<T>> rowToDataConversion) {
        super(dataBase);
        this.similarityCacheLock = new Object();
        this.cachedType = cachedType;
        this.cacheName = cacheName;
        
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
        
        this.sqlInsertInto = 
                SQL_TEMPLATE_INSERT_INTO_TABLE
                        .replace(CACHE_TABLE, this.cacheDataTableName)
                        .replace(CACHE_COLUMN, this.cacheDataColumnName);
        
        this.sqlSelectAllWithOlderAlgorithm = 
                SQL_TEMPLATE_SELECT_WITH_ALGORITHM_OLDER_THAN
                        .replace(CACHE_TABLE, this.cacheDataTableName);
        
        this.sqlSelectWhereHashIs = 
                SQL_TEMPLATE_SELECT_WHERE_HASH_IS
                        .replace(CACHE_TABLE, this.cacheDataTableName);
        
        this.sqlDeleteWhereHashAndUuidAre = 
                SQL_TEMPLATE_DELETE_WHERE_HASH_AND_UUID_ARE
                        .replace(CACHE_TABLE, this.cacheDataTableName);
        
        this.sqlUpdateCachedAlgorithmTimeWhereUuidIs = 
                SQL_UPDATE_CACHED_ALGORITHM_TIME_WHERE_UUID_IS
                        .replace(CACHE_TABLE, this.cacheDataTableName)
                        .replace(CACHE_COLUMN, this.cacheDataColumnName);                
                
    }

    @Override
    public List<PersistableCacheData<T>> loadAll(int algorithmVersion) 
            throws DataExtractionException {
        try (JdbcTransaction transact = super.openTransaction()) {
            
            return transact
                    .doQueryAndStreamVarargParams(
                            this.rowToDataConversion, 
                            this.sqlSelectAllFromCacheTable, 
                            algorithmVersion)
                    .collect(toList());
            
        } catch(TransactionHandledSQLException | TransactionHandledException e) {
            throw super.logAndWrap(e);
        }
    }

    @Override
    public Map<Long, T> loadAllHashesWith(int algorithmVersion) throws DataExtractionException {
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
            throw super.logAndWrap(e);
        }
    }

    @Override
    public void persistAll(
            List<PersistableCacheData<T>> persistableCacheData, int algorithmVersion) 
            throws DataExtractionException {
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
                
                LocalDateTime now = now();
                
                Set<Params> dataAsParams = persistableCacheData
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

                transact.doBatchUpdate(this.sqlInsertInto, dataAsParams);

            } catch(TransactionHandledSQLException|TransactionHandledException e) {
                throw super.logAndWrap(e);
            }
        }    
    }

    @Override
    public Map<Long, T> reassessAllHashesOlderThan(
            int algorithmVersion, BiFunction<String, String, T> analyzeFunction) 
            throws DataExtractionException {
        
        Map<Long, T> reassessedCachHashes = new HashMap<>();
        
        Cached cached = new Cached();
        
        RowOperation fillCachedFromRow = (row) -> {
            cached.fillFrom(row);
        };
        
        T newCachedValue;
        boolean sameDataExist;
        
        logFor(this).info(format("try to reassess old cached data in %s", this.cacheName));
        
        synchronized ( this.similarityCacheLock ) {
            boolean dataForProcessExist = true;
            while ( dataForProcessExist ) {
                try (JdbcTransaction transact = super.openTransaction()) {

                    cached.clear();

                    transact
                            .doQueryAndProcessFirstRowVarargParams(
                                    fillCachedFromRow, 
                                    this.sqlSelectAllWithOlderAlgorithm,
                                    algorithmVersion);

                    if ( cached.isEmpty() ) {
                        dataForProcessExist = false;
                        continue;
                    }

                    newCachedValue = analyzeFunction.apply(cached.target(), cached.pattern());

                    sameDataExist = transact
                            .doesQueryHaveResultsVarargParams(
                                    this.sqlSelectWhereHashIs, 
                                    cached.pairHash());

                    if ( sameDataExist ) {                    
                        transact
                                .doUpdateVarargParams(
                                        this.sqlDeleteWhereHashAndUuidAre, 
                                        cached.pairHash(), cached.uuid());
                    } else {
                        reassessedCachHashes.put(cached.pairHash(), newCachedValue);
                    }

                    LocalDateTime now = now();
                    
                    transact
                            .doUpdateVarargParams(
                                    this.sqlUpdateCachedAlgorithmTimeWhereUuidIs, 
                                    newCachedValue, algorithmVersion, now, cached.uuid());

                } catch(TransactionHandledSQLException|TransactionHandledException e) {
                    throw super.logAndWrap(e);
                }
            }
        }
        
        return reassessedCachHashes;
    }
    
}
