/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.data.daos.sql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.domain.entities.WebDirectory;
import diarsid.beam.core.domain.entities.WebDirectoryPages;
import diarsid.beam.core.domain.entities.WebPage;
import diarsid.beam.core.domain.entities.WebPlace;
import diarsid.beam.core.modules.data.DaoWebDirectories;
import diarsid.beam.core.modules.data.DataBase;
import diarsid.beam.core.modules.data.daos.BeamCommonDao;
import diarsid.jdbc.transactions.JdbcTransaction;
import diarsid.jdbc.transactions.PerRowConversion;
import diarsid.jdbc.transactions.exceptions.TransactionHandledException;
import diarsid.jdbc.transactions.exceptions.TransactionHandledSQLException;

import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import static diarsid.beam.core.base.control.io.interpreter.ControlKeys.hasWildcard;
import static diarsid.beam.core.base.util.SqlUtil.SqlOperator.AND;
import static diarsid.beam.core.base.util.SqlUtil.lowerWildcard;
import static diarsid.beam.core.base.util.SqlUtil.lowerWildcardList;
import static diarsid.beam.core.base.util.SqlUtil.multipleLowerLIKE;
import static diarsid.beam.core.base.util.StringUtils.lower;
import static diarsid.beam.core.base.util.StringUtils.splitByWildcard;
import static diarsid.beam.core.domain.entities.WebDirectories.restoreDirectory;
import static diarsid.beam.core.domain.entities.WebPages.restorePage;
import static diarsid.beam.core.domain.entities.WebPlace.parsePlace;
import static diarsid.jdbc.transactions.core.Params.params;


class H2DaoWebDirectories 
        extends BeamCommonDao 
        implements DaoWebDirectories {
    
    private final PerRowConversion<WebDirectory> rowToDirectoryConversion;
    private final PerRowConversion<WebPage> rowTorPageConversion;

    H2DaoWebDirectories(DataBase dataBase, InnerIoEngine ioEngine) {
        super(dataBase, ioEngine);
        this.rowToDirectoryConversion = (row) -> {
            return restoreDirectory(
                    (int) row.get("id"),
                    (String) row.get("name"), 
                    parsePlace((String) row.get("place")), 
                    (int) row.get("ordering"));
        };
        this.rowTorPageConversion = (row) -> {
            return restorePage(
                    (String) row.get("name"), 
                    (String) row.get("shortcuts"), 
                    (String) row.get("url"), 
                    (int) row.get("ordering"), 
                    (int) row.get("dir_id"));
        };
    }   

    @Override
    public Optional<Integer> freeNameNextIndex(
            Initiator initiator, String name, WebPlace place) {
        try (JdbcTransaction transact = super.getTransaction()) {
            
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
            
            return Optional.empty();
        }
    }
    
    @Override
    public List<WebDirectoryPages> getAllDirectoriesPages(
            Initiator initiator) {
        try {

            Map<WebDirectory, List<WebPage>> data = new HashMap<>();
            super.getDisposableTransaction()
                    .doQuery(
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
                            "       ON page.dir_id = dir.id ",
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
                            });
            
            return data
                    .entrySet()
                    .stream()
                    .map(entry -> entry.getKey().withPages(entry.getValue()))
                    .sorted()
                    .collect(toList());
        } catch (TransactionHandledSQLException|TransactionHandledException e) {
            
            return emptyList();
        }
    }

    @Override
    public List<WebDirectoryPages> getAllDirectoriesPagesInPlace(
            Initiator initiator, WebPlace place) {
        try {

            Map<WebDirectory, List<WebPage>> data = new HashMap<>();
            super.getDisposableTransaction()
                    .doQueryVarargParams(
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
                            place);
            
            return data
                    .entrySet()
                    .stream()
                    .map(entry -> entry.getKey().withPages(entry.getValue()))
                    .sorted()
                    .collect(toList());
        } catch (TransactionHandledSQLException|TransactionHandledException e) {
            
            return emptyList();
        }
    }

    @Override
    public Optional<WebDirectoryPages> getDirectoryPagesById(
            Initiator initiator, int id) {
        
        try (JdbcTransaction transact = super.getTransaction()) {
            
            Optional<WebDirectory> directory = transact
                    .doQueryAndConvertFirstRowVarargParams(
                            WebDirectory.class, 
                            "SELECT id, name, place, ordering " +
                            "FROM web_directories " +
                            "WHERE id IS ? ", 
                            (row) -> {
                                return Optional.of(this.rowToDirectoryConversion.convert(row));
                            }, 
                            id);
            
            if ( ! directory.isPresent() ) {
                return Optional.empty();
            }
            
            List<WebPage> directoryPages = transact
                    .doQueryAndStreamVarargParams(
                            WebPage.class, 
                            "SELECT name, url, shortcuts, ordering, dir_id " +
                            "FROM web_pages " +
                            "WHERE dir_id IS ? ", 
                            this.rowTorPageConversion, 
                            directory.get().id())
                    .collect(toList());
            
            return Optional.of(directory.get().withPages(directoryPages));
        } catch (TransactionHandledSQLException|TransactionHandledException e) {
            
            return Optional.empty();
        }
    }

    @Override
    public Optional<WebDirectoryPages> getDirectoryPagesByNameAndPlace(
            Initiator initiator, String name, WebPlace place) {
        try (JdbcTransaction transact = super.getTransaction()) {
            
            Optional<WebDirectory> directory = transact
                    .doQueryAndConvertFirstRowVarargParams(
                            WebDirectory.class, 
                            "SELECT id, name, place, ordering " +
                            "FROM web_directories " +
                            "WHERE ( LOWER(name) IS ? ) AND ( place IS ? ) ", 
                            (row) -> {
                                return Optional.of(this.rowToDirectoryConversion.convert(row));
                            }, 
                            lower(name), place);
            
            if ( ! directory.isPresent() ) {
                return Optional.empty();
            }
            
            List<WebPage> directoryPages = transact
                    .doQueryAndStreamVarargParams(
                            WebPage.class, 
                            "SELECT name, url, shortcuts, ordering, dir_id " +
                            "FROM web_pages " +
                            "WHERE dir_id IS ? ", 
                            this.rowTorPageConversion, 
                            directory.get().id())
                    .collect(toList());
            
            return Optional.of(directory.get().withPages(directoryPages));
        } catch (TransactionHandledSQLException|TransactionHandledException e) {
            
            return Optional.empty();
        }
    }

    @Override
    public Optional<WebDirectory> getDirectoryByNameAndPlace(
            Initiator initiator, String name, WebPlace place) {
        try {
            return super.getDisposableTransaction()
                    .doQueryAndConvertFirstRowVarargParams(
                            WebDirectory.class, 
                            "SELECT id, name, place, ordering " +
                            "FROM web_directories " +
                            "WHERE ( LOWER(name) IS ? ) AND ( place IS ? )", 
                            (row) -> {
                                return Optional.of(this.rowToDirectoryConversion.convert(row));
                            }, 
                            lower(name), place);
        } catch (TransactionHandledSQLException|TransactionHandledException e) {
            
            return Optional.empty();
        }
    }

    @Override
    public List<WebDirectory> findDirectoriesByPatternInPlace(
            Initiator initiator, String pattern, WebPlace place) {
        try {
            if ( hasWildcard(pattern) ) {
                return this.getDirectoriesByPatternPartsInPlace(splitByWildcard(pattern), place);
            } else {
                return this.getDirectoriesByPatternInPlace(pattern, place);
            }
        } catch (TransactionHandledSQLException|TransactionHandledException e) {
            
            return emptyList();
        }
    }
    
    private List<WebDirectory> getDirectoriesByPatternPartsInPlace(
            List<String> patterns, WebPlace place) 
            throws TransactionHandledSQLException, TransactionHandledException {
        return super.getDisposableTransaction()
                .doQueryAndStreamVarargParams(
                        WebDirectory.class, 
                        "SELECT id, name, place, ordering " +
                        "FROM web_directories " +
                        "WHERE " + 
                                multipleLowerLIKE("name", patterns.size(), AND) + 
                                " AND ( place IS ? ) ", 
                        this.rowToDirectoryConversion, 
                        lowerWildcardList(patterns), place)
                .sorted()
                .collect(toList());
    }
    
    private List<WebDirectory> getDirectoriesByPatternInPlace(
            String pattern, WebPlace place) 
            throws TransactionHandledSQLException, TransactionHandledException {
        return super.getDisposableTransaction()
                .doQueryAndStreamVarargParams(
                        WebDirectory.class, 
                        "SELECT id, name, place, ordering " +
                        "FROM web_directories " + 
                        "WHERE ( LOWER(name) LIKE ? ) AND ( place IS ? )", 
                        this.rowToDirectoryConversion, 
                        lowerWildcard(pattern), place)
                .sorted()
                .collect(toList());
    }

    @Override
    public List<WebDirectory> findDirectoriesByPatternInAnyPlace(
            Initiator initiator, String pattern) {
        try {
            if ( hasWildcard(pattern) ) {
                return this.getDitrectoriesByPatternPartsInAnyPlace(splitByWildcard(pattern));
            } else {
                return this.getDirectoriesByPatternInAnyPlace(pattern);
            }
        } catch (TransactionHandledSQLException|TransactionHandledException e) {
            
            return emptyList();
        }
    }
    
    private List<WebDirectory> getDirectoriesByPatternInAnyPlace(
            String pattern) 
            throws TransactionHandledSQLException, TransactionHandledException {
        return super.getDisposableTransaction()
                .doQueryAndStreamVarargParams(
                        WebDirectory.class, 
                        "SELECT id, name, place, ordering " +
                        "FROM web_directories " + 
                        "WHERE ( LOWER(name) LIKE ? ) ", 
                        this.rowToDirectoryConversion, 
                        lowerWildcard(pattern))
                .sorted()
                .collect(toList());
    }
    
    private List<WebDirectory> getDitrectoriesByPatternPartsInAnyPlace
        (List<String> patterns) 
            throws TransactionHandledSQLException, TransactionHandledException {
        return super.getDisposableTransaction()
                .doQueryAndStreamVarargParams(
                        WebDirectory.class, 
                        "SELECT id, name, place, ordering " +
                        "FROM web_directories " +
                        "WHERE " + multipleLowerLIKE("name", patterns.size(), AND), 
                        this.rowToDirectoryConversion, 
                        lowerWildcardList(patterns))
                .sorted()
                .collect(toList());   
    }

    @Override
    public List<WebDirectory> getAllDirectories(
            Initiator initiator) {
        try {
                        
            return super.getDisposableTransaction()
                    .doQueryAndStream(
                            WebDirectory.class, 
                            "SELECT id, name, place, ordering " +
                            "FROM web_directories ", 
                            this.rowToDirectoryConversion)
                    .sorted()
                    .collect(toList());
            
        } catch (TransactionHandledSQLException|TransactionHandledException e) {
            
            return emptyList();
        }
    }

    @Override
    public List<WebDirectory> getAllDirectoriesInPlace(
            Initiator initiator, WebPlace place) {
        try {
                        
            return super.getDisposableTransaction()
                    .doQueryAndStreamVarargParams(
                            WebDirectory.class, 
                            "SELECT id, name, place, ordering " +
                            "FROM web_directories " +
                            "WHERE place IS ? ", 
                            this.rowToDirectoryConversion, 
                            place)
                    .sorted()
                    .collect(toList());
            
        } catch (TransactionHandledSQLException|TransactionHandledException e) {
            
            return emptyList();
        }
    }

    @Override
    public boolean exists(
            Initiator initiator, String directoryName, WebPlace place) {
        try {
            return super.getDisposableTransaction()
                    .doesQueryHaveResultsVarargParams(
                            "SELECT * " +
                            "FROM web_directories " +
                            "WHERE ( LOWER(name) IS ? ) AND ( place IS ? ) ", 
                            lower(directoryName), place);
        } catch (TransactionHandledSQLException|TransactionHandledException e) {
            
            return false;
        }
    }

    @Override
    public boolean updateWebDirectoryOrders(
            Initiator initiator, List<WebDirectory> directories) {
        try (JdbcTransaction transact = super.getTransaction()) {
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
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            
            return false;
        }
    }

    @Override
    public boolean save(
            Initiator initiator, WebDirectory directory) {
        try (JdbcTransaction transact = super.getTransaction()) {
            
            boolean alreadyExists = transact
                    .doesQueryHaveResultsVarargParams(
                            "SELECT * " +
                            "FROM web_directories " +
                            "WHERE ( LOWER(name) IS ? ) AND ( place IS ? ) ", 
                            lower(directory.name()), directory.place());
            
            if ( alreadyExists ) {
                super.ioEngine().report(initiator, "this directory already exists.");
                return false;
            } 
            
            int order = transact
                    .countQueryResultsVarargParams(
                            "SELECT * " +
                            "FROM web_directories " +
                            "WHERE place IS ? ", 
                            directory.place());            
            
            if ( order < 0 ) {
                super.ioEngine().report(initiator, "new order is less than 0, cannot proceed.");
                return false;
            }
            
            directory.setOrder(order);
            
            int saved = transact
                    .doUpdateVarargParams(
                            "INSERT INTO web_directories ( name, ordering, place ) " +
                            "VALUES ( ?, ?, ? ) ", 
                            directory.name(), directory.order(), directory.place());

            if ( saved != 1 ) {
                transact.rollbackAndProceed();
                return false;
            } else {
                return true;
            }
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            
            return false;
        }
    }
    
    @Override
    public boolean save(
            Initiator initiator, String name, WebPlace place) {
        try (JdbcTransaction transact = super.getTransaction()) {
            
            boolean alreadyExists = transact
                    .doesQueryHaveResultsVarargParams(
                            "SELECT * " +
                            "FROM web_directories " +
                            "WHERE ( LOWER(name) IS ? ) AND ( place IS ? ) ", 
                            lower(name), place);
            
            if ( alreadyExists ) {
                super.ioEngine().report(initiator, "this directory already exists.");
                return false;
            } 
            
            int order = transact
                    .countQueryResultsVarargParams(
                            "SELECT * " +
                            "FROM web_directories " +
                            "WHERE place IS ? ", 
                            place);
            
            if ( order < 0 ) {
                super.ioEngine().report(initiator, "new order is less than 0, cannot proceed.");
                return false;
            }
            
            int saved = transact
                    .doUpdateVarargParams(
                            "INSERT INTO web_directories ( name, ordering, place ) " +
                            "VALUES ( ?, ?, ? ) ", 
                            name, order, place);

            if ( saved != 1 ) {
                transact.rollbackAndProceed();
                return false;
            } else {
                return true;
            }
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            
            return false;
        }
    }

    @Override
    public boolean remove(
            Initiator initiator, String name, WebPlace place) {
        try (JdbcTransaction transact = super.getTransaction()) {
            
            Optional<WebDirectory> optDirectory = transact
                    .doQueryAndConvertFirstRowVarargParams(
                            WebDirectory.class, 
                            "SELECT id, name, ordering, place " +
                            "FROM web_directories " +
                            "WHERE ( LOWER(name) IS ? ) AND ( place IS ? ) ", 
                            (row) -> {
                                return Optional.of(this.rowToDirectoryConversion.convert(row));
                            }, 
                            lower(name), place);
            
            if ( ! optDirectory.isPresent() ) {
                super.ioEngine().report(initiator, "cannot find such directory.");
                return false;
            }
            
            int deleted = transact
                    .doUpdateVarargParams(
                            "DELETE FROM web_directories " +
                            "WHERE ( LOWER(name) IS ? ) AND ( place IS ? ) ", 
                            lower(name), place);
            
            if ( deleted != 1 ) {
                transact.rollbackAndProceed();
                return false;
            }
            
            transact
                    .doUpdateVarargParams(
                            "DELETE FROM web_pages " +
                            "WHERE dir_id IS ? ", 
                            optDirectory.get().id());
            
            transact
                    .doUpdateVarargParams(
                            "UPDATE web_directories " +
                            "SET ordering = (ordering - 1) " +
                            "WHERE ( ordering > ? ) AND ( place IS ? )", 
                            optDirectory.get().order(), place);
            
            return true;
            
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            
            return false;
        }
    }
    
    @Override
    public boolean moveDirectoryToPlace(
            Initiator initiator, String name, WebPlace oldPlace, WebPlace newPlace) {
        try (JdbcTransaction transact = super.getTransaction()) {
            
            Optional<WebDirectory> movedDir = transact
                    .doQueryAndConvertFirstRowVarargParams(
                            WebDirectory.class, 
                            "SELECT id, name, place, ordering " +
                            "FROM web_directories " +
                            "WHERE ( LOWER(name) IS ? ) AND ( place IS ? ) ", 
                            (row) -> {
                                return Optional.of(this.rowToDirectoryConversion.convert(row));
                            },
                            lower(name), oldPlace);
            
            if ( ! movedDir.isPresent() ) {
                super.ioEngine().report(initiator, "cannot find such directory.");
                return false;
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
                super.ioEngine().report(initiator, "directory will be moved with name " + name);
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
                transact.rollbackAndProceed();
                super.ioEngine().report(initiator, "cannot move directory.");
                return false;
            }
            
            int oldPlaceReordered = transact
                    .doUpdateVarargParams(
                            "UPDATE web_directories " +
                            "SET ordering = (ordering - 1) " +
                            "WHERE ( ordering > ? ) AND ( place IS ? )", 
                            movedDir.get().order(), oldPlace);
            
            return true;
            
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            
            return false;
        }
    }

    @Override
    public boolean editDirectoryName(
            Initiator initiator, String name, WebPlace place, String newName) {
        try (JdbcTransaction transact = super.getTransaction()) {
            
            boolean alreadyExists = transact
                    .doesQueryHaveResultsVarargParams(
                            "SELECT name " +
                            "FROM web_directories " +
                            "WHERE ( LOWER(name) IS ? ) AND ( place IS ? ) ", 
                            lower(newName), place);
            
            if ( alreadyExists ) {
                super.ioEngine().report(initiator, "directory with this name already exists.");
                return false;
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
            
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            
            return false;
        }
    }

    @Override
    public Optional<WebDirectory> getDirectoryById(
            Initiator initiator, int id) {
        try {
            return super.getDisposableTransaction()
                    .doQueryAndConvertFirstRow(
                            WebDirectory.class,
                            "SELECT id, name, place, ordering " +
                            "FROM web_directories " +
                            "WHERE id IS ? ",
                            (row) -> {
                                return Optional.of(this.rowToDirectoryConversion.convert(row));
                            });
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            
            return Optional.empty();
        }
    }

    @Override
    public Optional<Integer> getDirectoryIdByNameAndPlace(
            Initiator initiator, String name, WebPlace place) {
        try {
            return super.getDisposableTransaction()
                    .doQueryAndConvertFirstRowVarargParams(
                            Integer.class,
                            "SELECT id " +
                                    "FROM web_directories " +
                                    "WHERE ( LOWER(name) IS ? ) AND ( place IS ? )",
                            (row) -> {
                                return Optional.ofNullable((int) row.get("id"));
                            },
                            lower(name), place);
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            
            return Optional.empty();
        }        
    }
    
}
