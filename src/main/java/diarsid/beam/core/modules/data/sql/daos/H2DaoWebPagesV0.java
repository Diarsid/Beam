/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.data.sql.daos;

import java.util.List;
import java.util.Optional;

import diarsid.beam.core.base.data.DataBase;
import diarsid.beam.core.base.data.DataExtractionException;
import diarsid.beam.core.domain.entities.WebDirectory;
import diarsid.beam.core.domain.entities.WebPage;
import diarsid.beam.core.modules.data.DaoWebPages;
import diarsid.jdbc.transactions.JdbcTransaction;
import diarsid.jdbc.transactions.exceptions.TransactionHandledException;
import diarsid.jdbc.transactions.exceptions.TransactionHandledSQLException;
import diarsid.jdbc.transactions.exceptions.TransactionTerminationException;

import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import static diarsid.beam.core.base.control.flow.Flows.valueFlowCompletedWith;
import static diarsid.beam.core.base.control.flow.Flows.valueFlowFail;
import static diarsid.beam.core.base.util.StringUtils.lower;
import static diarsid.beam.core.modules.data.sql.daos.RowToEntityConversions.ROW_TO_WEBDIRECTORY;
import static diarsid.beam.core.modules.data.sql.daos.RowToEntityConversions.ROW_TO_WEBPAGE;
import static diarsid.jdbc.transactions.core.Params.params;
import static diarsid.support.log.Logging.logFor;



abstract class H2DaoWebPagesV0 
        extends BeamCommonDao 
        implements DaoWebPages {
    
    H2DaoWebPagesV0(DataBase dataBase) {
        super(dataBase);
    }
    
    protected final void setLoadableDirectoryFor(WebPage webPage) {
        webPage.setLoadableDirectory(()-> {
            try {
                Optional<WebDirectory> dir = super.openDisposableTransaction()
                        .doQueryAndConvertFirstRowVarargParams(
                                ROW_TO_WEBDIRECTORY,
                                "SELECT id, name, place, ordering " +
                                "FROM web_directories " +
                                "WHERE ( id IS ? ) ", 
                                webPage.directoryId());
                return valueFlowCompletedWith(dir);
            } catch (TransactionHandledSQLException | TransactionHandledException e) {
                logFor(this).error(e.getMessage(), e);
                String message = "Cannot find WebDirectory by id " + webPage.directoryId();
                return valueFlowFail(message);
            }
        });
    }
    
    protected final void setLoadableDirectoryFor(Optional<WebPage> webPage) {
        if ( webPage.isPresent() ) {
            this.setLoadableDirectoryFor(webPage.get());
        }
    }

    @Override
    public Optional<Integer> findFreeNameNextIndex(String name) throws DataExtractionException {
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
        } catch (TransactionHandledSQLException | TransactionHandledException e) {
            throw super.logAndWrap(e);
        }
    }

    @Override
    public Optional<WebPage> getByExactName(String name) throws DataExtractionException {
        try {
            Optional<WebPage> page = super
                    .openDisposableTransaction()
                    .doQueryAndConvertFirstRowVarargParams(
                            ROW_TO_WEBPAGE,
                            "SELECT name, shortcuts, url, ordering, dir_id " +
                            "FROM web_pages " +
                            "WHERE LOWER(name) IS ? ",
                            lower(name));
            this.setLoadableDirectoryFor(page);
            return page;
        } catch (TransactionHandledSQLException | TransactionHandledException e) {
            throw super.logAndWrap(e);
        }        
    }
    
    @Override
    public Optional<WebPage> getByUrl(String url) throws DataExtractionException {
        try {
            Optional<WebPage> page = super
                    .openDisposableTransaction()
                    .doQueryAndConvertFirstRowVarargParams(
                            ROW_TO_WEBPAGE,
                            "SELECT name, shortcuts, url, ordering, dir_id " +
                            "FROM web_pages " +
                            "WHERE LOWER(url) IS ? ",
                            lower(url));
            this.setLoadableDirectoryFor(page);
            return page;
        } catch (TransactionHandledSQLException | TransactionHandledException e) {
            throw super.logAndWrap(e);
        }    
    }

    @Override
    public List<WebPage> getAllFromDirectory(int directoryId) throws DataExtractionException {
        try {
            return super.openDisposableTransaction()
                    .doQueryAndStreamVarargParams(
                            ROW_TO_WEBPAGE,
                            "SELECT name, shortcuts, url, ordering, dir_id " +
                            "FROM web_pages " +
                            "WHERE dir_id IS ? ",
                            directoryId)
                    .sorted()
                    .peek(page -> this.setLoadableDirectoryFor(page))
                    .collect(toList());
        } catch (TransactionHandledSQLException | TransactionHandledException e) {
            throw super.logAndWrap(e);
        }
    }

    @Override
    public boolean save(WebPage page) throws DataExtractionException {
        try (JdbcTransaction transact = super.openTransaction()) {
            
            boolean exists = transact
                    .doesQueryHaveResultsVarargParams(
                            "SELECT * " +
                            "FROM web_pages " +
                            "WHERE LOWER(name) IS ? ", 
                            lower(page.name()));
            
            if ( exists ) {
                throw transact.rollbackAndTermination("this page already exists.");
            } 
            
            boolean directoryExists = transact
                    .doesQueryHaveResultsVarargParams(
                            "SELECT * " +
                            "FROM web_directories " +
                            "WHERE id IS ? ", 
                            page.directoryId());
            
            if ( ! directoryExists ) {
                throw transact.rollbackAndTermination("Not found directory to save this page.");
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
            
        } catch (TransactionHandledSQLException | TransactionHandledException e) {
            throw super.logAndWrap(e);
        } catch (TransactionTerminationException e) {
            throw super.wrap(e.getMessage());
        }
    }

    @Override
    public boolean remove(String name) throws DataExtractionException {
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
        } catch (TransactionHandledSQLException | TransactionHandledException e) {
            throw super.logAndWrap(e);
        }
    }

    @Override
    public boolean editName(String oldName, String newName) throws DataExtractionException {
        try (JdbcTransaction transact = super.openTransaction()) {
            
             boolean exists = transact
                    .doesQueryHaveResultsVarargParams(
                            "SELECT * " +
                            "FROM web_pages " +
                            "WHERE LOWER(name) IS ? ", 
                            lower(newName));
             
            if ( exists ) {
                throw transact.rollbackAndTermination("this name already exists.");
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
            
        } catch (TransactionHandledSQLException | TransactionHandledException e) {
            throw super.logAndWrap(e);
        } catch (TransactionTerminationException e) {
            throw super.wrap(e.getMessage());
        }
    }

    @Override
    public boolean editShortcuts(String name, String newShortcuts) throws DataExtractionException {
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
            
        } catch (TransactionHandledSQLException | TransactionHandledException e) {
            throw super.logAndWrap(e);
        }
    }

    @Override
    public boolean editUrl(String name, String newUrl) throws DataExtractionException {
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
        } catch (TransactionHandledSQLException | TransactionHandledException e) {
            throw super.logAndWrap(e);
        }
    }

    @Override
    public boolean movePageFromDirToDir(WebPage page, int newDirId) throws DataExtractionException {
        try (JdbcTransaction transact = super.openTransaction()) {
            
            boolean exists = transact
                    .doesQueryHaveResultsVarargParams(
                            "SELECT * " +
                            "FROM web_directories " +
                            "WHERE id IS ? ", 
                            newDirId);
            
            if ( ! exists ) {
                throw transact.rollbackAndTermination("target directory does not exist.");
            }
            
            int newOrder = transact
                    .countQueryResultsVarargParams(
                            "SELECT ordering " +
                            "FROM web_pages " +
                            "WHERE dir_id IS ? ", 
                            newDirId);
            
            if ( newOrder < 0 ) {
                throw transact.rollbackAndTermination("cannot define new page order.");
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
            
        } catch (TransactionHandledSQLException | TransactionHandledException e) {
            throw super.logAndWrap(e);
        } catch (TransactionTerminationException e) {
            throw super.wrap(e.getMessage());
        }
    }

    @Override
    public boolean updatePageOrdersInDir(List<WebPage> pagesToReorder) 
            throws DataExtractionException {
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
                throw transact.rollbackAndTermination("unpredicatble pages order modifications.");
            } else {
                return true;
            }
            
        } catch (TransactionHandledSQLException | TransactionHandledException e) {
            throw super.logAndWrap(e);
        } catch (TransactionTerminationException e) {
            throw super.wrap(e.getMessage());
        }
    }

    @Override
    public List<WebPage> getAll() throws DataExtractionException {
        try {
            return super.openDisposableTransaction()
                    .doQueryAndStream(
                            ROW_TO_WEBPAGE,
                            "SELECT name, shortcuts, url, ordering, dir_id " +
                            "FROM web_pages ")
                    .collect(toList());
        } catch (TransactionHandledSQLException | TransactionHandledException e) {
            throw super.logAndWrap(e);
        }
    }
    
}
