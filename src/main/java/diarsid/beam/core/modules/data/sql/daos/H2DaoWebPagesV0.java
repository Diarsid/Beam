/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.data.sql.daos;

import java.util.List;
import java.util.Optional;

import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.data.DataBase;
import diarsid.beam.core.domain.entities.WebPage;
import diarsid.beam.core.modules.data.BeamCommonDao;
import diarsid.beam.core.modules.data.DaoWebPages;
import diarsid.jdbc.transactions.JdbcTransaction;
import diarsid.jdbc.transactions.exceptions.TransactionHandledException;
import diarsid.jdbc.transactions.exceptions.TransactionHandledSQLException;

import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import static diarsid.beam.core.base.util.Logs.logError;
import static diarsid.beam.core.base.util.StringUtils.lower;
import static diarsid.beam.core.modules.data.sql.daos.RowToEntityConversions.ROW_TO_WEBDIRECTORY;
import static diarsid.beam.core.modules.data.sql.daos.RowToEntityConversions.ROW_TO_WEBPAGE;
import static diarsid.jdbc.transactions.core.Params.params;



abstract class H2DaoWebPagesV0 
        extends BeamCommonDao 
        implements DaoWebPages {
    
    H2DaoWebPagesV0(DataBase dataBase, InnerIoEngine ioEngine) {
        super(dataBase, ioEngine);
    }
    
    protected final void setLoadableDirectoryFor(Initiator initiator, WebPage webPage) {
        webPage.setLoadableDirectory(()-> {
            try {
                return super.openDisposableTransaction()
                        .doQueryAndConvertFirstRowVarargParams(
                                ROW_TO_WEBDIRECTORY,
                                "SELECT id, name, place, ordering " +
                                "FROM web_directories " +
                                "WHERE ( id IS ? ) ", 
                                webPage.directoryId());
            } catch (TransactionHandledSQLException|TransactionHandledException e) {
                logError(H2DaoWebPagesV0.class, e);
                super.ioEngine().report(
                        initiator, "Cannot find WebDirectory by id " + webPage.directoryId());
                return Optional.empty();
            }
        });
    }
    
    protected final void setLoadableDirectoryFor(Initiator initiator, Optional<WebPage> webPage) {
        if ( webPage.isPresent() ) {
            this.setLoadableDirectoryFor(initiator, webPage.get());
        }
    }

    @Override
    public Optional<Integer> findFreeNameNextIndex(
            Initiator initiator, String name) {
        try (JdbcTransaction transact = super.openTransaction()) {
            
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
    public Optional<WebPage> getByExactName(
            Initiator initiator, String name) {
        try {
            Optional<WebPage> page = super.openDisposableTransaction()
                    .doQueryAndConvertFirstRowVarargParams(
                            ROW_TO_WEBPAGE,
                            "SELECT name, shortcuts, url, ordering, dir_id " +
                            "FROM web_pages " +
                            "WHERE LOWER(name) IS ? ",
                            lower(name));
            this.setLoadableDirectoryFor(initiator, page);
            return page;
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            
            return Optional.empty();
        }        
    }

    @Override
    public List<WebPage> getAllFromDirectory(
            Initiator initiator, int directoryId) {
        try {
            return super.openDisposableTransaction()
                    .doQueryAndStreamVarargParams(
                            ROW_TO_WEBPAGE,
                            "SELECT name, shortcuts, url, ordering, dir_id " +
                            "FROM web_pages " +
                            "WHERE dir_id IS ? ",
                            directoryId)
                    .sorted()
                    .peek(page -> this.setLoadableDirectoryFor(initiator, page))
                    .collect(toList());
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            
            return emptyList();
        }
    }

    @Override
    public boolean save(
            Initiator initiator, WebPage page) {
        try (JdbcTransaction transact = super.openTransaction()) {
            
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
            
            boolean directoryExists = transact
                    .doesQueryHaveResultsVarargParams(
                            "SELECT * " +
                            "FROM web_directories " +
                            "WHERE id IS ? ", 
                            page.directoryId());
            
            if ( ! directoryExists ) {
                super.ioEngine().report(initiator, "Not found directory to save this page.");
                return false;
            }
            
            int newPageOrder = transact
                    .countQueryResultsVarargParams(
                            "SELECT ordering " +
                            "FROM web_pages " +
                            "WHERE dir_id IS ? ", 
                            page.directoryId());
            page.setOrder(newPageOrder);
            
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
        try (JdbcTransaction transact = super.openTransaction()) {
            
            Optional<WebPage> optPage = transact
                    .doQueryAndConvertFirstRowVarargParams(
                            ROW_TO_WEBPAGE,
                            "SELECT name, shortcuts, url, ordering, dir_id " +
                            "FROM web_pages " +
                            "WHERE LOWER(name) IS ? ",
                            lower(name));
            
            if ( ! optPage.isPresent() ) {
                return false;
            }
            WebPage page = optPage.get();
            
            int removed = transact
                    .doUpdateVarargParams(
                            "DELETE FROM web_pages " +
                            "WHERE ( LOWER(name) IS ? ) AND ( ordering IS ? ) AND ( dir_id IS ? ) ", 
                            lower(page.name()), page.order(), page.directoryId());
            
            if ( removed == 1 ) {
                transact
                        .doUpdateVarargParams(
                                "UPDATE web_pages " +
                                "SET ordering = (ordering - 1) " +
                                "WHERE ( ordering > ? ) AND ( dir_id IS ? )", 
                                page.order(), page.directoryId());
            }
            
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
        try (JdbcTransaction transact = super.openTransaction()) {
            
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
        try (JdbcTransaction transact = super.openTransaction()) {
            
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
        try (JdbcTransaction transact = super.openTransaction()) {
            
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
        try (JdbcTransaction transact = super.openTransaction()) {
            
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
        try (JdbcTransaction transact = super.openTransaction()) {
            
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

    @Override
    public List<WebPage> getAll(Initiator initiator) {
        try {
            return super.openDisposableTransaction()
                    .doQueryAndStream(
                            ROW_TO_WEBPAGE,
                            "SELECT name, shortcuts, url, ordering, dir_id " +
                            "FROM web_pages ")
                    .collect(toList());
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            
            return emptyList();
        }
    }
    
}
