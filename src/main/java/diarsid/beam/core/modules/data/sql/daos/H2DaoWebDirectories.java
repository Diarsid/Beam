/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.data.sql.daos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import diarsid.beam.core.base.data.DataBase;
import diarsid.beam.core.base.data.DataExtractionException;
import diarsid.beam.core.domain.entities.WebDirectory;
import diarsid.beam.core.domain.entities.WebDirectoryPages;
import diarsid.beam.core.domain.entities.WebPage;
import diarsid.beam.core.domain.entities.WebPlace;
import diarsid.beam.core.modules.data.DaoWebDirectories;
import diarsid.jdbc.transactions.JdbcTransaction;
import diarsid.jdbc.transactions.RowConversion;
import diarsid.jdbc.transactions.exceptions.TransactionHandledException;
import diarsid.jdbc.transactions.exceptions.TransactionHandledSQLException;
import diarsid.jdbc.transactions.exceptions.TransactionTerminationException;

import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import static diarsid.beam.core.base.util.CollectionsUtils.nonEmpty;
import static diarsid.beam.core.base.util.SqlUtil.lowerWildcard;
import static diarsid.beam.core.base.util.SqlUtil.multipleLowerGroupedLikesAndOr;
import static diarsid.beam.core.base.util.SqlUtil.multipleLowerLikeAnd;
import static diarsid.beam.core.base.util.SqlUtil.patternToCharCriterias;
import static diarsid.beam.core.base.util.SqlUtil.shift;
import static diarsid.beam.core.base.util.StringUtils.lower;
import static diarsid.beam.core.domain.entities.WebDirectories.restoreDirectory;
import static diarsid.beam.core.domain.entities.WebPages.restorePage;
import static diarsid.beam.core.domain.entities.WebPlace.parsePlace;
import static diarsid.beam.core.modules.data.sql.daos.RowToEntityConversions.ROW_TO_WEBDIRECTORY;
import static diarsid.beam.core.modules.data.sql.daos.RowToEntityConversions.ROW_TO_WEBPAGE;
import static diarsid.jdbc.transactions.core.Params.params;


class H2DaoWebDirectories 
        extends BeamCommonDao 
        implements DaoWebDirectories {
    
    private final RowConversion<Integer> rowToDirectoryIdConversion;

    H2DaoWebDirectories(DataBase dataBase) {
        super(dataBase);        
        this.rowToDirectoryIdConversion = (row) -> {
            return (int) row.get("id");
        };
    }   

    @Override
    public Optional<Integer> findFreeNameNextIndex(String name, WebPlace place) 
            throws DataExtractionException {
        try (JdbcTransaction transact = super.openTransaction()) {
            
            // test if original name is free
            boolean exists = transact
                    .doesQueryHaveResultsVarargParams(
                            "SELECT name " +
                            "FROM web_directories " +
                            "WHERE ( LOWER(name) IS ? ) AND ( place IS ? ) ", 
                            lower(name), place);
            
            if ( ! exists ) {
                // if free, return 0 as no such name exists 
                // and there is no need for name counter.
                return Optional.of(0);
            }
            
            // else count existing names until free counter will be found.
            // possible names can be:
            // - myName         <- first saved name
            // - myName (2) 
            // - myName (3)
            // ... in this case 4 must be returned as there are no entry 'myName (4)'.
            int nameCounter = 1;
            do {
                nameCounter++;
                name = format("%s (%d)", name, nameCounter);
                exists = transact
                        .doesQueryHaveResultsVarargParams(
                                "SELECT name " +
                                "FROM web_directories " +
                                "WHERE ( LOWER(name) IS ? ) AND ( place IS ? ) ", 
                                lower(name), place);
            } while ( exists ) ;
            return Optional.of(nameCounter);
        } catch (TransactionHandledSQLException|TransactionHandledException e) {
            throw super.logAndWrap(e);
        }
    }
    
    @Override
    public List<WebDirectoryPages> getAllDirectoriesPages() throws DataExtractionException {
        try {

            Map<WebDirectory, List<WebPage>> data = new HashMap<>();
            super.openDisposableTransaction()
                    .doQuery(
                            (row) -> {
                                WebDirectory dir = restoreDirectory(
                                        (int) row.get("d_id"),
                                        (String) row.get("d_name"), 
                                        parsePlace((String) row.get("d_place")), 
                                        (int) row.get("d_order"));                                
                                if ( nonNull(row.get("p_name")) ) {
                                    WebPage page = restorePage(
                                            (String) row.get("p_name"), 
                                            (String) row.get("p_short"), 
                                            (String) row.get("p_url"), 
                                            (int) row.get("p_order"), 
                                            (int) row.get("d_id")
                                    );
                                    List<WebPage> newPageList = new ArrayList<>();
                                    newPageList.add(page);
                                    data.merge(
                                            dir, 
                                            newPageList, 
                                            (oldPages, newPages) -> {
                                                oldPages.addAll(newPages);
                                                return oldPages;
                                            });
                                } else {
                                    data.merge(
                                            dir, 
                                            new ArrayList<>(), 
                                            (oldPages, newPages) -> { 
                                                return newPages;
                                            }
                                    );
                                }
                            },
                            "SELECT " +
                            "   page.name       AS p_name, " +
                            "   page.url        AS p_url, " +
                            "   page.shortcuts  AS p_short, " +
                            "   page.ordering   AS p_order, " +
                            "   dir.id          AS d_id, " +
                            "   dir.name        AS d_name, " +
                            "   dir.place       AS d_place, " +
                            "   dir.ordering    AS d_order " +
                            "FROM " +
                            "   web_directories AS dir " +
                            "       LEFT JOIN " +
                            "   web_pages AS page " +
                            "       ON page.dir_id = dir.id ");
            
            return data
                    .entrySet()
                    .stream()
                    .map(entry -> entry.getKey().withPages(entry.getValue()))
                    .sorted()
                    .collect(toList());
        } catch (TransactionHandledSQLException|TransactionHandledException e) {
            throw super.logAndWrap(e);
        }
    }

    @Override
    public List<WebDirectoryPages> getAllDirectoriesPagesInPlace(WebPlace place) 
            throws DataExtractionException {
        try {
            Map<WebDirectory, List<WebPage>> data = new HashMap<>();
            super.openDisposableTransaction()
                    .doQueryVarargParams(
                            (row) -> {
                                WebDirectory dir = restoreDirectory(
                                        (int) row.get("d_id"),
                                        (String) row.get("d_name"), 
                                        parsePlace((String) row.get("d_place")), 
                                        (int) row.get("d_order"));                                
                                if ( nonNull(row.get("p_name")) ) {
                                    WebPage page = restorePage(
                                            (String) row.get("p_name"), 
                                            (String) row.get("p_short"), 
                                            (String) row.get("p_url"), 
                                            (int) row.get("p_order"), 
                                            (int) row.get("d_id")
                                    );
                                    List<WebPage> newPageList = new ArrayList<>();
                                    newPageList.add(page);
                                    data.merge(
                                            dir, 
                                            newPageList, 
                                            (oldPages, newPages) -> {
                                                oldPages.addAll(newPages);
                                                return oldPages;
                                            });
                                } else {
                                    data.merge(
                                            dir, 
                                            new ArrayList<>(), 
                                            (oldPages, newPages) -> { 
                                                return newPages;
                                            }
                                    );
                                }
                            }, 
                            "SELECT " +
                            "   page.name       AS p_name, " +
                            "   page.url        AS p_url, " +
                            "   page.shortcuts  AS p_short, " +
                            "   page.ordering   AS p_order, " +
                            "   dir.id          AS d_id, " +
                            "   dir.name        AS d_name, " +
                            "   dir.place       AS d_place, " +
                            "   dir.ordering    AS d_order " +
                            "FROM " +
                            "   web_directories AS dir " +
                            "       LEFT JOIN " +
                            "   web_pages AS page " +
                            "       ON page.dir_id = dir.id " +
                            "WHERE dir.place IS ? ",
                            place);
            
            return data
                    .entrySet()
                    .stream()
                    .map(entry -> entry.getKey().withPages(entry.getValue()))
                    .sorted()
                    .collect(toList());
        } catch (TransactionHandledSQLException|TransactionHandledException e) {
            throw super.logAndWrap(e);
        }
    }

    @Override
    public Optional<WebDirectoryPages> getDirectoryPagesById(int id) 
            throws DataExtractionException {        
        try (JdbcTransaction transact = super.openTransaction()) {
            
            Optional<WebDirectory> directory = transact
                    .doQueryAndConvertFirstRowVarargParams(
                            ROW_TO_WEBDIRECTORY, 
                            "SELECT id, name, place, ordering " +
                            "FROM web_directories " +
                            "WHERE id IS ? ", 
                            id);
            
            if ( ! directory.isPresent() ) {
                return Optional.empty();
            }
            
            List<WebPage> directoryPages = transact
                    .doQueryAndStreamVarargParams(
                            ROW_TO_WEBPAGE, 
                            "SELECT name, url, shortcuts, ordering, dir_id " +
                            "FROM web_pages " +
                            "WHERE dir_id IS ? ", 
                            directory.get().id())
                    .collect(toList());
            
            return Optional.of(directory.get().withPages(directoryPages));
        } catch (TransactionHandledSQLException|TransactionHandledException e) {
            throw super.logAndWrap(e);
        }
    }

    @Override
    public Optional<WebDirectoryPages> getDirectoryPagesByNameAndPlace(String name, WebPlace place) 
            throws DataExtractionException {
        try (JdbcTransaction transact = super.openTransaction()) {
            
            Optional<WebDirectory> directory = transact
                    .doQueryAndConvertFirstRowVarargParams(
                            ROW_TO_WEBDIRECTORY, 
                            "SELECT id, name, place, ordering " +
                            "FROM web_directories " +
                            "WHERE ( LOWER(name) IS ? ) AND ( place IS ? ) ", 
                            lower(name), place);
            
            if ( ! directory.isPresent() ) {
                return Optional.empty();
            }
            
            List<WebPage> directoryPages = transact
                    .doQueryAndStreamVarargParams(
                            ROW_TO_WEBPAGE, 
                            "SELECT name, url, shortcuts, ordering, dir_id " +
                            "FROM web_pages " +
                            "WHERE dir_id IS ? ", 
                            directory.get().id())
                    .collect(toList());
            
            return Optional.of(directory.get().withPages(directoryPages));
        } catch (TransactionHandledSQLException|TransactionHandledException e) {
            throw super.logAndWrap(e);
        }
    }

    @Override
    public Optional<WebDirectory> getDirectoryByNameAndPlace(String name, WebPlace place) 
            throws DataExtractionException {
        try {
            return super.openDisposableTransaction()
                    .doQueryAndConvertFirstRowVarargParams(
                            ROW_TO_WEBDIRECTORY, 
                            "SELECT id, name, place, ordering " +
                            "FROM web_directories " +
                            "WHERE ( LOWER(name) IS ? ) AND ( place IS ? )", 
                            lower(name), place);
        } catch (TransactionHandledSQLException|TransactionHandledException e) {
            
            return Optional.empty();
        }
    }

    @Override
    public List<WebDirectory> findDirectoriesByPatternInPlace(String pattern, WebPlace place) 
            throws DataExtractionException {
        try (JdbcTransaction transact = super.openTransaction()) {
            
            List<WebDirectory> dirs;
            
            dirs = transact
                    .doQueryAndStreamVarargParams(
                            ROW_TO_WEBDIRECTORY, 
                            "SELECT id, name, place, ordering " +
                            "FROM web_directories " + 
                            "WHERE ( place IS ? ) AND ( LOWER(name) LIKE ? ) ", 
                            place, lowerWildcard(pattern))
                    .sorted()
                    .collect(toList());
            
            if ( nonEmpty(dirs) ) {
                return dirs;
            }
            
            List<String> criterias = patternToCharCriterias(pattern);
            dirs = transact
                    .doQueryAndStreamVarargParams(
                            ROW_TO_WEBDIRECTORY, 
                            "SELECT id, name, place, ordering " +
                            "FROM web_directories " + 
                            "WHERE ( place IS ? ) AND " + 
                                    multipleLowerLikeAnd("name", criterias.size()), 
                            place, criterias)
                    .sorted()
                    .collect(toList());
            
            if ( nonEmpty(dirs) ) {
                return dirs;
            }
            
            String multipleGroupedLikeOrNameCondition = 
                    multipleLowerGroupedLikesAndOr("name", criterias.size());
            dirs = transact
                    .doQueryAndStreamVarargParams(
                            ROW_TO_WEBDIRECTORY, 
                            "SELECT id, name, place, ordering " +
                            "FROM web_directories " + 
                            "WHERE ( place IS ? ) AND " + multipleGroupedLikeOrNameCondition, 
                            place, criterias)
                    .sorted()
                    .collect(toList());
            
            shift(criterias);
                        
            List<WebDirectory> shiftedDirs = transact
                    .doQueryAndStreamVarargParams(
                            ROW_TO_WEBDIRECTORY, 
                            "SELECT id, name, place, ordering " +
                            "FROM web_directories " + 
                            "WHERE ( place IS ? ) AND " + multipleGroupedLikeOrNameCondition, 
                            place, criterias)
                    .sorted()
                    .collect(toList());
            
            shiftedDirs.retainAll(dirs);
            dirs.retainAll(shiftedDirs);
            
            return dirs;            
            
        } catch (TransactionHandledSQLException|TransactionHandledException e) {
            throw super.logAndWrap(e);
        }
    }

    @Override
    public List<WebDirectory> findDirectoriesByPatternInAnyPlace(String pattern) 
            throws DataExtractionException {
        try (JdbcTransaction transact = super.openTransaction()) {
            
            List<WebDirectory> dirs;
            
            dirs = transact
                    .doQueryAndStreamVarargParams(
                            ROW_TO_WEBDIRECTORY, 
                            "SELECT id, name, place, ordering " +
                            "FROM web_directories " + 
                            "WHERE ( LOWER(name) LIKE ? ) ", 
                            lowerWildcard(pattern))
                    .sorted()
                    .collect(toList());
            
            if ( nonEmpty(dirs) ) {
                return dirs;
            }
            
            List<String> criterias = patternToCharCriterias(pattern);
            dirs = transact
                    .doQueryAndStreamVarargParams(
                            ROW_TO_WEBDIRECTORY, 
                            "SELECT id, name, place, ordering " +
                            "FROM web_directories " + 
                            "WHERE " + multipleLowerLikeAnd("name", criterias.size()), 
                            criterias)
                    .sorted()
                    .collect(toList());
            
            if ( nonEmpty(dirs) ) {
                return dirs;
            }
            
            String multipleGroupedLikeOrNameCondition = 
                    multipleLowerGroupedLikesAndOr("name", criterias.size());
            dirs = transact
                    .doQueryAndStreamVarargParams(
                            ROW_TO_WEBDIRECTORY, 
                            "SELECT id, name, place, ordering " +
                            "FROM web_directories " + 
                            "WHERE " + multipleGroupedLikeOrNameCondition, 
                            criterias)
                    .sorted()
                    .collect(toList());
            
            shift(criterias);
                        
            List<WebDirectory> shiftedDirs = transact
                    .doQueryAndStreamVarargParams(
                            ROW_TO_WEBDIRECTORY, 
                            "SELECT id, name, place, ordering " +
                            "FROM web_directories " + 
                            "WHERE " + multipleGroupedLikeOrNameCondition, 
                            criterias)
                    .sorted()
                    .collect(toList());
            
            shiftedDirs.retainAll(dirs);
            dirs.retainAll(shiftedDirs);
            
            return dirs;            
            
        } catch (TransactionHandledSQLException|TransactionHandledException e) {
            throw super.logAndWrap(e);
        }
    }

    @Override
    public List<WebDirectory> getAllDirectories() throws DataExtractionException {
        try {
                        
            return super.openDisposableTransaction()
                    .doQueryAndStream(
                            ROW_TO_WEBDIRECTORY,
                            "SELECT id, name, place, ordering " +
                            "FROM web_directories ")
                    .sorted()
                    .collect(toList());
            
        } catch (TransactionHandledSQLException|TransactionHandledException e) {
            throw super.logAndWrap(e);
        }
    }

    @Override
    public List<WebDirectory> getAllDirectoriesInPlace(WebPlace place) 
            throws DataExtractionException {
        try {
                        
            return super.openDisposableTransaction()
                    .doQueryAndStreamVarargParams(
                            ROW_TO_WEBDIRECTORY, 
                            "SELECT id, name, place, ordering " +
                            "FROM web_directories " +
                            "WHERE place IS ? ", 
                            place)
                    .sorted()
                    .collect(toList());
            
        } catch (TransactionHandledSQLException|TransactionHandledException e) {
            throw super.logAndWrap(e);
        }
    }

    @Override
    public boolean exists(String directoryName, WebPlace place) throws DataExtractionException {
        try {
            return super.openDisposableTransaction()
                    .doesQueryHaveResultsVarargParams(
                            "SELECT * " +
                            "FROM web_directories " +
                            "WHERE ( LOWER(name) IS ? ) AND ( place IS ? ) ", 
                            lower(directoryName), place);
        } catch (TransactionHandledSQLException|TransactionHandledException e) {
            throw super.logAndWrap(e);
        }
    }

    @Override
    public boolean updateWebDirectoryOrders(List<WebDirectory> directories) 
            throws DataExtractionException {
        try (JdbcTransaction transact = super.openTransaction()) {
            int[] udpated = transact
                    .doBatchUpdate(
                            "UPDATE web_directories " +
                            "SET ordering = ? " +
                            "WHERE id IS ? ",
                            directories
                                    .stream()
                                    .map(directory -> params(directory.order(), directory.id()))
                                    .collect(toSet()));
            
            int updatedRows = stream(udpated).sum();
            if ( updatedRows > 0 && updatedRows <= directories.size() ) {
                return true;
            } else {
                transact.rollbackAndProceed();
                return false;
            }
        } catch (TransactionHandledSQLException|TransactionHandledException e) {
            throw super.logAndWrap(e);
        }
    }

    @Override
    public boolean save(WebDirectory directory) throws DataExtractionException {
        try (JdbcTransaction transact = super.openTransaction()) {
            
            boolean alreadyExists = transact
                    .doesQueryHaveResultsVarargParams(
                            "SELECT * " +
                            "FROM web_directories " +
                            "WHERE ( LOWER(name) IS ? ) AND ( place IS ? ) ", 
                            lower(directory.name()), directory.place());
            
            if ( alreadyExists ) {
                throw transact.rollbackAndTermination("this directory already exists.");
            } 
            
            int order = transact
                    .countQueryResultsVarargParams(
                            "SELECT * " +
                            "FROM web_directories " +
                            "WHERE place IS ? ", 
                            directory.place());            
            
            if ( order < 0 ) {
                throw transact.rollbackAndTermination("new order is less than 0, cannot proceed.");
            }
            
            directory.setOrder(order);
            
            int saved = transact
                    .doUpdateVarargParams(
                            "INSERT INTO web_directories ( name, ordering, place ) " +
                            "VALUES ( ?, ?, ? ) ", 
                            directory.name(), directory.order(), directory.place());

            if ( saved != 1 ) {
                throw transact.rollbackAndTermination(
                        format("directory %s has not been saved", directory.name()));
            } else {
                return true;
            }
            
        } catch (TransactionHandledSQLException|TransactionHandledException e) {
            throw super.logAndWrap(e);
        } catch (TransactionTerminationException e) {
            throw super.wrap(e.getMessage());
        }
    }
    
    @Override
    public boolean save(String name, WebPlace place) throws DataExtractionException {
        try (JdbcTransaction transact = super.openTransaction()) {
            
            boolean alreadyExists = transact
                    .doesQueryHaveResultsVarargParams(
                            "SELECT * " +
                            "FROM web_directories " +
                            "WHERE ( LOWER(name) IS ? ) AND ( place IS ? ) ", 
                            lower(name), place);
            
            if ( alreadyExists ) {
                throw transact.rollbackAndTermination("this directory already exists.");
            } 
            
            int order = transact
                    .countQueryResultsVarargParams(
                            "SELECT * " +
                            "FROM web_directories " +
                            "WHERE place IS ? ", 
                            place);
            
            if ( order < 0 ) {
                throw transact.rollbackAndTermination("new order is less than 0, cannot proceed.");
            }
            
            int saved = transact
                    .doUpdateVarargParams(
                            "INSERT INTO web_directories ( name, ordering, place ) " +
                            "VALUES ( ?, ?, ? ) ", 
                            name, order, place);

            if ( saved != 1 ) {
                throw transact.rollbackAndTermination(
                        format("directory %s has not been saved", name));
            } else {
                return true;
            }
            
        } catch (TransactionHandledSQLException|TransactionHandledException e) {
            throw super.logAndWrap(e);
        } catch (TransactionTerminationException e) {
            throw super.wrap(e.getMessage());
        }
    }

    @Override
    public boolean remove(String name, WebPlace place) throws DataExtractionException {
        try (JdbcTransaction transact = super.openTransaction()) {
            
            Optional<WebDirectory> optDirectory = transact
                    .doQueryAndConvertFirstRowVarargParams(
                            ROW_TO_WEBDIRECTORY, 
                            "SELECT id, name, ordering, place " +
                            "FROM web_directories " +
                            "WHERE ( LOWER(name) IS ? ) AND ( place IS ? ) ", 
                            lower(name), place);
            
            if ( ! optDirectory.isPresent() ) {
                throw transact.rollbackAndTermination("cannot find such directory.");
            }
            
            transact
                    .doUpdateVarargParams(
                            "DELETE FROM web_pages " +
                            "WHERE dir_id IS ? ", 
                            optDirectory.get().id());
            
            boolean deleted = transact
                    .doUpdateVarargParams(
                            "DELETE FROM web_directories " +
                            "WHERE ( LOWER(name) IS ? ) AND ( place IS ? ) ", 
                            lower(name), place)
                    == 1;
            
            if ( !deleted ) {
                transact.rollbackAndProceed();
                return false;
            }
            
            transact
                    .doUpdateVarargParams(
                            "UPDATE web_directories " +
                            "SET ordering = (ordering - 1) " +
                            "WHERE ( ordering > ? ) AND ( place IS ? )", 
                            optDirectory.get().order(), place);
            
            return true;
            
        } catch (TransactionHandledSQLException|TransactionHandledException e) {
            throw super.logAndWrap(e);
        } catch (TransactionTerminationException e) {
            throw super.wrap(e.getMessage());
        }
    }
    
    @Override
    public boolean moveDirectoryToPlace(String name, WebPlace oldPlace, WebPlace newPlace) 
            throws DataExtractionException {
        try (JdbcTransaction transact = super.openTransaction()) {
            
            Optional<WebDirectory> movedDir = transact
                    .doQueryAndConvertFirstRowVarargParams(
                            ROW_TO_WEBDIRECTORY,
                            "SELECT id, name, place, ordering " +
                            "FROM web_directories " +
                            "WHERE ( LOWER(name) IS ? ) AND ( place IS ? ) ", 
                            lower(name), oldPlace);
            
            if ( ! movedDir.isPresent() ) {
                throw transact.rollbackAndTermination("cannot find such directory.");
            }
            
            // test if such directory name already exists in destination place
            boolean exists = transact
                    .doesQueryHaveResultsVarargParams(
                            "SELECT name " +
                            "FROM web_directories " +
                            "WHERE ( LOWER(name) IS ? ) AND ( place IS ? ) ", 
                            lower(name), newPlace);
            
            if ( exists ) {
                // if exists, must find and assign new index to its name.
                int nameCounter = 1;
                do {
                    nameCounter++;
                    name = format("%s (%d)", name, nameCounter);
                    exists = transact
                            .doesQueryHaveResultsVarargParams(
                                    "SELECT name " +
                                    "FROM web_directories " +
                                    "WHERE ( LOWER(name) IS ? ) AND ( place IS ? ) ", 
                                    lower(name), newPlace);
                } while ( exists ) ;
                // name has been already reassigned as following.
//                super.ioEngine().report(initiator, "directory will be moved with name " + name);
            }
            
            int newOrder = transact
                    .countQueryResultsVarargParams(
                            "SELECT * " +
                            "FROM web_directories " +
                            "WHERE place IS ? ", 
                            newPlace);            
                        
            int moved = transact
                    .doUpdateVarargParams(
                            "UPDATE web_directories " +
                            "SET " +
                            "   name = ?, " +
                            "   ordering = ?, " +
                            "   place = ? " +
                            "WHERE LOWER(name) IS ? ", 
                            name, newOrder, newPlace, lower(movedDir.get().name()));
            
            if ( moved != 1 ) {
                throw transact.rollbackAndTermination(format("directory %s move failed", name));
            }
            
            int oldPlaceReordered = transact
                    .doUpdateVarargParams(
                            "UPDATE web_directories " +
                            "SET ordering = (ordering - 1) " +
                            "WHERE ( ordering > ? ) AND ( place IS ? )", 
                            movedDir.get().order(), oldPlace);
            
            return true;
            
        } catch (TransactionHandledSQLException|TransactionHandledException e) {
            throw super.logAndWrap(e);
        } catch (TransactionTerminationException e) {
            throw super.wrap(e.getMessage());
        }
    }

    @Override
    public boolean editDirectoryName(String name, WebPlace place, String newName) 
            throws DataExtractionException {
        try (JdbcTransaction transact = super.openTransaction()) {
            
            boolean alreadyExists = transact
                    .doesQueryHaveResultsVarargParams(
                            "SELECT name " +
                            "FROM web_directories " +
                            "WHERE ( LOWER(name) IS ? ) AND ( place IS ? ) ", 
                            lower(newName), place);
            
            if ( alreadyExists ) {
                throw transact.rollbackAndTermination("directory with this name already exists.");
            }
            
            int renamed = transact
                    .doUpdateVarargParams(
                            "UPDATE web_directories " +
                            "SET name = ? " +
                            "WHERE ( LOWER(name) IS ? ) AND ( place IS ? ) ", 
                            newName, lower(name), place);
            
            transact
                    .ifTrue( renamed != 1 )
                    .rollbackAndProceed();
            
            return ( renamed == 1 ); 
            
        } catch (TransactionHandledSQLException|TransactionHandledException e) {
            throw super.logAndWrap(e);
        } catch (TransactionTerminationException e) {
            throw super.wrap(e.getMessage());
        }
    }

    @Override
    public Optional<WebDirectory> getDirectoryById(int id) throws DataExtractionException {
        try {
            return super.openDisposableTransaction()
                    .doQueryAndConvertFirstRow(
                            ROW_TO_WEBDIRECTORY,
                            "SELECT id, name, place, ordering " +
                            "FROM web_directories " +
                            "WHERE id IS ? ");
        } catch (TransactionHandledSQLException|TransactionHandledException e) {
            throw super.logAndWrap(e);
        }
    }

    @Override
    public Optional<Integer> getDirectoryIdByNameAndPlace(String name, WebPlace place) 
            throws DataExtractionException {
        try {
            return super.openDisposableTransaction()
                    .doQueryAndConvertFirstRowVarargParams(
                            this.rowToDirectoryIdConversion,
                            "SELECT id " +
                            "FROM web_directories " +
                            "WHERE ( LOWER(name) IS ? ) AND ( place IS ? )",
                            lower(name), place);
        } catch (TransactionHandledSQLException|TransactionHandledException e) {
            throw super.logAndWrap(e);
        }        
    }
    
}
