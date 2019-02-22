/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.data.sql.daos;

import java.util.concurrent.atomic.AtomicBoolean;

import diarsid.beam.core.base.data.DataBase;
import diarsid.beam.core.base.data.DataExtractionException;
import diarsid.beam.core.modules.data.DaoNamedRectangles;
import diarsid.beam.core.modules.io.gui.geometry.MutableNamedRectangle;
import diarsid.beam.core.modules.io.gui.geometry.NamedRectangle;
import diarsid.jdbc.transactions.JdbcTransaction;
import diarsid.jdbc.transactions.exceptions.TransactionHandledException;
import diarsid.jdbc.transactions.exceptions.TransactionHandledSQLException;
import diarsid.jdbc.transactions.exceptions.TransactionTerminationException;

import static java.lang.String.format;

import static diarsid.support.strings.StringUtils.lower;


class H2DaoNamedRectangles 
        extends BeamCommonDao 
        implements DaoNamedRectangles {

    H2DaoNamedRectangles(DataBase dataBase) {
        super(dataBase);
    }

    @Override
    public boolean fetchDataInto(MutableNamedRectangle rectangleToFill) 
            throws DataExtractionException {
        try (JdbcTransaction transact = super.openTransaction()) {
            
            AtomicBoolean found = new AtomicBoolean(false);
            
            transact
                    .doQueryAndProcessFirstRowVarargParams(
                            (row) -> {
                                rectangleToFill.anchor().set(
                                        row.get("x", Double.class), 
                                        row.get("y", Double.class));
                                rectangleToFill.size().set(
                                        row.get("width", Double.class), 
                                        row.get("height", Double.class));
                                found.set(true);
                            }, 
                            "SELECT name, x, y, width, height " +
                            "FROM named_rectangles " +
                            "WHERE LOWER(name) IS ? ", 
                            lower(rectangleToFill.name()));
            
            return found.get();
            
        } catch (TransactionHandledException | TransactionHandledSQLException e) {
            throw super.logAndWrap(e);
        }
    }

    @Override
    public boolean save(NamedRectangle rectangle) throws DataExtractionException {
        try (JdbcTransaction transact = super.openTransaction()) {
            
            int updated = transact
                    .doUpdateVarargParams(
                            "MERGE INTO named_rectangles (name, x, y, width, height) " +
                            "KEY(name) " +
                            "VALUES( ?, ?, ?, ?, ? )", 
                            rectangle.name(), 
                            rectangle.anchor().x(), 
                            rectangle.anchor().y(), 
                            rectangle.size().width(), 
                            rectangle.size().height());
            
            if ( updated == 1 ) {
                return true;
            } else {
                throw transact.rollbackAndTermination(
                        format("SQL merge of %s have updated %s rows!", rectangle, updated));
            }
            
        } catch (TransactionHandledException | TransactionHandledSQLException e) {
            throw super.logAndWrap(e);
        } catch (TransactionTerminationException e) {
            throw super.wrap(e.getMessage());
        }
    }
    
}
