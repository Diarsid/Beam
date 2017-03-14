/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.data.daos.sql;

import java.util.List;
import java.util.Optional;

import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.domain.entities.WebPage;
import diarsid.beam.core.modules.data.DaoWebPages;
import diarsid.beam.core.modules.data.DataBase;
import diarsid.beam.core.modules.data.daos.BeamCommonDao;
import diarsid.jdbc.transactions.JdbcTransaction;
import diarsid.jdbc.transactions.PerRowConversion;
import diarsid.jdbc.transactions.exceptions.TransactionHandledException;
import diarsid.jdbc.transactions.exceptions.TransactionHandledSQLException;

import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import static diarsid.beam.core.base.control.io.interpreter.ControlKeys.hasWildcard;
import static diarsid.beam.core.base.util.SqlUtil.SqlOperator.AND;
import static diarsid.beam.core.base.util.SqlUtil.lowerWildcard;
import static diarsid.beam.core.base.util.SqlUtil.lowerWildcardList;
import static diarsid.beam.core.base.util.SqlUtil.multipleLowerLIKE;
import static diarsid.beam.core.base.util.StringUtils.lower;
import static diarsid.beam.core.base.util.StringUtils.splitByWildcard;
import static diarsid.beam.core.domain.entities.WebPages.restorePage;
import static diarsid.jdbc.transactions.core.Params.params;



class H2DaoWebPages 
        extends BeamCommonDao 
        implements DaoWebPages {
    
    private final PerRowConversion<WebPage> rowToPageConversion;

    H2DaoWebPages(DataBase dataBase, InnerIoEngine ioEngine) {
        super(dataBase, ioEngine);
        this.rowToPageConversion = (row) -> {
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
            Initiator initiator, String name) {
        try (JdbcTransaction transact = super.getTransaction()) {
            
            // test if original name is free
            boolean exists = transact
                    .doesQueryHaveResultsVarargParams(
                            "SELECT name " +
                            "FROM web_pages " +
                            "WHERE ( LOWER(name) IS ? ) ", 
                            lower(name));
            
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
                                "FROM web_pages " +
                                "WHERE ( LOWER(name) IS ? ) ", 
                                lower(name));
            } while ( exists ) ;
            return Optional.of(nameCounter);
        } catch (TransactionHandledSQLException|TransactionHandledException e) {
            
            return Optional.empty();
        }
    }

    @Override
    public Optional<WebPage> getByName(
            Initiator initiator, String name) {
        try {
            return super.getDisposableTransaction()
                    .doQueryAndConvertFirstRowVarargParams(
                            WebPage.class,
                            "SELECT name, shortcuts, url, ordering, dir_id " +
                            "FROM web_pages " +
                            "WHERE LOWER(name) IS ? ",
                            (row) -> {
                                return Optional.of(this.rowToPageConversion.convert(row));
                            },
                            lower(name));
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            
            return Optional.empty();
        }        
    }

    @Override
    public List<WebPage> findByPattern(
            Initiator initiator, String pattern) {
        try {
            if ( hasWildcard(pattern) ) {
                return this.getPagesByPatternParts(splitByWildcard(pattern));
            } else {
                return this.getPagesBySinglePattern(pattern);
            }
        } catch (TransactionHandledException|TransactionHandledSQLException e) {
            
            return emptyList();
        }
    }
    
    private List<WebPage> getPagesByPatternParts(List<String> patterns) 
            throws TransactionHandledException, TransactionHandledSQLException {
        return super.getDisposableTransaction()
                .doQueryAndStreamVarargParams(
                        WebPage.class, 
                        "SELECT name, shortcuts, url, ordering, dir_id " +
                        "FROM web_pages " +
                        "WHERE " + 
                                multipleLowerLIKE("name", patterns.size(), AND) + 
                                " OR " + 
                                multipleLowerLIKE("shorcuts", patterns.size(), AND), 
                        this.rowToPageConversion, 
                        lowerWildcardList(patterns))
                .sorted()
                .collect(toList());
    }
    
    private List<WebPage> getPagesBySinglePattern(String pattern)
            throws TransactionHandledException, TransactionHandledSQLException {
        return super.getDisposableTransaction()
                .doQueryAndStreamVarargParams(
                        WebPage.class, 
                        "SELECT name, shortcuts, url, ordering, dir_id " +
                        "FROM web_pages " +
                        "WHERE ( LOWER(name) LIKE ? ) OR ( LOWER(shortcuts) LIKE ? ) ", 
                        this.rowToPageConversion, 
                        lowerWildcard(pattern), lowerWildcard(pattern))
                .sorted()
                .collect(toList());
    }

    @Override
    public List<WebPage> allFromDirectory(
            Initiator initiator, int directoryId) {
        try {
            return super.getDisposableTransaction()
                    .doQueryAndStreamVarargParams(
                            WebPage.class,
                            "SELECT name, shortcuts, url, ordering, dir_id " +
                                    "FROM web_pages " +
                                    "WHERE dir_id IS ? ",
                            this.rowToPageConversion,
                            directoryId)
                    .sorted()
                    .collect(toList());
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            
            return emptyList();
        }
    }

    @Override
    public boolean save(
            Initiator initiator, WebPage page) {
        try (JdbcTransaction transact = super.getTransaction()) {
            
            boolean exists = transact
                    .doesQueryHaveResultsVarargParams(
                            "SELECT * " +
                            "FROM web_pages " +
                            "WHERE LOWER(name) IS ? ", 
                            lower(page.name()));
            
            if ( exists ) {
                super.ioEngine().report(initiator, "this page already exists.");
                return false;
            } 
            
            int inserted = transact
                    .doUpdateVarargParams(
                            "INSERT INTO web_pages (name, url, shortcuts, ordering, dir_id) " +
                            "VALUES ( ?, ?, ?, ?, ? ) ", 
                            page.name(), 
                            page.url(), 
                            page.shortcuts(), 
                            page.order(), 
                            page.directoryId());
            
            if ( inserted == 1 ) {
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
    public boolean remove(
            Initiator initiator, String name) {
        try (JdbcTransaction transact = super.getTransaction()) {
            
            int removed = transact
                    .doUpdateVarargParams(
                            "DELETE FROM web_pages " +
                            "WHERE LOWER(name) IS ? ", 
                            lower(name));
            
            transact
                    .ifTrue( removed > 1 )
                    .rollbackAndProceed();
            
            return ( removed == 1 );
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            
            return false;
        }
    }

    @Override
    public boolean editName(
            Initiator initiator, String oldName, String newName) {
        try (JdbcTransaction transact = super.getTransaction()) {
            
             boolean exists = transact
                    .doesQueryHaveResultsVarargParams(
                            "SELECT * " +
                            "FROM web_pages " +
                            "WHERE LOWER(name) IS ? ", 
                            lower(newName));
             
            if ( exists ) {
                super.ioEngine().report(initiator, "this name already exists.");
                return false;
            }
            
            int renamed = transact
                    .doUpdateVarargParams(
                            "UPDATE web_pages " +
                            "SET name = ? " +
                            "WHERE LOWER(name) IS ? ", 
                            newName, lower(oldName));
            
            transact
                    .ifTrue( renamed > 1 )
                    .rollbackAndProceed();
            
            return ( renamed == 1 );
            
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            
            return false;
        }
    }

    @Override
    public boolean editShortcuts(
            Initiator initiator, String name, String newShortcuts) {
        try (JdbcTransaction transact = super.getTransaction()) {
            
            int edited = transact
                    .doUpdateVarargParams(
                            "UPDATE web_pages " +
                            "SET shortcuts = ? " +
                            "WHERE LOWER(name) IS ? ",
                            newShortcuts, lower(name));
            
            transact
                    .ifTrue( edited > 1 )
                    .rollbackAndProceed();
            
            return ( edited == 1 );
            
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            
            return false;
        }
    }

    @Override
    public boolean editUrl(
            Initiator initiator, String name, String newUrl) {
        try (JdbcTransaction transact = super.getTransaction()) {
            
            int edited = transact
                    .doUpdateVarargParams(
                            "UPDATE web_pages " +
                            "SET url = ? " +
                            "WHERE LOWER(name) IS ? ", 
                            newUrl, name);
            
            if ( edited == 1 ) {
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
    public boolean movePageFromDirToDir(
            Initiator initiator, WebPage page, int newDirId) {
        try (JdbcTransaction transact = super.getTransaction()) {
            
            boolean exists = transact
                    .doesQueryHaveResultsVarargParams(
                            "SELECT * " +
                            "FROM web_directories " +
                            "WHERE id IS ? ", 
                            newDirId);
            
            if ( ! exists ) {
                super.ioEngine().report(initiator, "target directory does not exist.");
                return false;
            }
            
            int newOrder = transact
                    .countQueryResultsVarargParams(
                            "SELECT ordering " +
                            "FROM web_pages " +
                            "WHERE dir_id IS ? ", 
                            newDirId);
            
            if ( newOrder < 0 ) {
                super.ioEngine().report(initiator, "cannot define new page order.");
                return false;
            }
            
            int moved = transact
                    .doUpdateVarargParams(
                            "UPDATE web_pages " +
                            "SET ordering = ?, dir_id = ? " +
                            "WHERE ( LOWER(name) IS ? ) AND ( dir_id IS ? )", 
                            newOrder, newDirId, lower(page.name()), page.directoryId());
            
            if ( moved != 1 ) {
                transact.rollbackAndProceed();
                return false;
            }
            
            transact
                    .doUpdateVarargParams(
                            "UPDATE web_pages " +
                            "SET ordering = (ordering - 1) " +
                            "WHERE ( ordering > ? ) AND ( dir_id IS ? ) ", 
                            page.order(), page.directoryId());
            
            return true;
            
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            
            return false;
        }
    }

    @Override
    public boolean updatePageOrdersInDir(
            Initiator initiator, List<WebPage> pagesToReorder) {
        try (JdbcTransaction transact = super.getTransaction()) {
            
            int[] reordered = transact
                    .doBatchUpdate(
                            "UPDATE web_pages " +
                            "SET ordering = ? " +
                            "WHERE ( LOWER(name) IS ? ) AND ( dir_id IS ? ) ", 
                            pagesToReorder
                                    .stream()
                                    .sorted()
                                    .map(page -> params(
                                            page.order(), 
                                            lower(page.name()), 
                                            page.directoryId()))
                                    .collect(toSet()));
            
            int sum = stream(reordered).sum();
            
            if ( sum > pagesToReorder.size() ) {
                super.ioEngine().report(initiator, "unpredicatble pages order modifications.");
                transact.rollbackAndProceed();
                return false;
            } else {
                return true;
            }
            
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            
            return false;
        }
    }
    
}
