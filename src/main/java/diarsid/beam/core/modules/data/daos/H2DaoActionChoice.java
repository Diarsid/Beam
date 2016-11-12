/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.data.daos;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;

import diarsid.beam.core.exceptions.NullDependencyInjectionException;
import diarsid.beam.core.modules.IoInnerModule;
import diarsid.beam.core.modules.data.DaoActionChoice;
import diarsid.beam.core.modules.data.DataBase;
import diarsid.beam.core.modules.data.HandledTransactSQLException;
import diarsid.beam.core.modules.data.JdbcTransaction;
import diarsid.beam.core.modules.executor.commandscache.ActionChoice;
import diarsid.beam.core.modules.executor.commandscache.ActionInfo;
import diarsid.beam.core.modules.executor.commandscache.ActionRequest;
import diarsid.beam.core.util.CommandsStringsComparator;

import static java.lang.String.join;
import static java.util.Collections.sort;

import static diarsid.beam.core.util.Logs.logError;


class H2DaoActionChoice implements DaoActionChoice {
    
    /*
     * action_choices
     * 
     * +------------+--------------------------------------------+---------------+
     * | action_arg | action_variants                            | action_choice |
     * +------------+--------------------------------------------+---------------+
     * | webst      | run webstorm::::open webstorm-projects     | run webstorm  |
     * +------------+--------------------------------------------+---------------+
     * | tomc       | call tomcat::::open tomcat::::see tomcat   | NO_CHOICE     |
     * +------------+--------------------------------------------+---------------+
     * | fb         | see fb::::run fbreader                     | see fb        |
     * +------------+--------------------------------------------+---------------+
     * 
     */
    
    private static final String DATA_BASE_STRING_DELIMITER;
    private static final Comparator STRING_LENGTH_COMPARATOR;
    static {
        DATA_BASE_STRING_DELIMITER = "::::";
        STRING_LENGTH_COMPARATOR = new CommandsStringsComparator();
    }
    
    private static final String INSERT_NEW_CHOICE = 
            "INSERT INTO action_choices (action_arg, action_variants, action_choice) " +
            "VALUES ( ?, ?, ? ) ";
    private static final String SELECT_COUNT_ACTION_ARG = 
            "SELECT COUNT(action_arg) AS result " +
            "FROM action_choices " +
            "WHERE ( action_arg IS ? ) ";
    private static final String UPDATE_CHOICE_WHERE_ACTION_ARG_IS = 
            "UPDATE action_choices " +
            "SET " +
                " action_variants = ? , " +
                " action_choice = ? " +
            "WHERE ( action_arg IS ? ) ";
    private static final String DELETE_CHOICE_WHERE_ACTION_ARG_IS = 
            "DELETE " +
            "FROM action_choices " +
            "WHERE ( action_arg IS ? ) ";
    private static final String SELECT_CHOICE_WHERE_ACTION_ARG_AND_VARIANTS_ARE = 
            "SELECT action_choice " +
            "FROM action_choices " +
            "WHERE " +
                " ( action_arg IS ? ) " +
                " AND " +
                " ( action_variants IS ? ) ";
    
    private final DataBase data;
    private final IoInnerModule ioEngine;    
    
    H2DaoActionChoice(final IoInnerModule io, final DataBase data) {
        if (io == null) {
            throw new NullDependencyInjectionException(
                    H2DaoActionChoice.class.getSimpleName(), 
                    IoInnerModule.class.getSimpleName());
        }
        if (data == null) {
            throw new NullDependencyInjectionException(
                    H2DaoActionChoice.class.getSimpleName(), 
                    DataBase.class.getSimpleName());
        }
        this.data = data;
        this.ioEngine = io;
    }
    
    private static String transformVariantsToDataBaseRepresentation(ActionInfo action) {
        sort(action.getActionVariants(), STRING_LENGTH_COMPARATOR);
        return join(DATA_BASE_STRING_DELIMITER, action.getActionVariants());
    }
        
    @Override
    public boolean saveChoice(ActionChoice actionChoice) {
        JdbcTransaction transact = this.data.beginTransaction();
        try {
            boolean saved;
            if ( this.ifChoiceWithArgumentExists(transact, actionChoice) ) {
                saved = this.rewriteExistedEntryWithNewVariantsAndChoice(transact, actionChoice);
            } else {
                saved = this.insertNewEntry(transact, actionChoice);
            }
            transact.commitThemAll();
            return saved;
        } catch (HandledTransactSQLException e) {
            logError(this.getClass(), "Transact Exception during action choice saving: ", e);
            this.ioEngine.reportException(e, "SQL Exception during action choice saving: " + 
                    actionChoice.toString());
            return false;
        } catch (SQLException e) {
            logError(this.getClass(), "SQL Exception during action choice saving: ", e);
            transact.rollbackAllAndReleaseResources();
            this.ioEngine.reportException(e, "SQL Exception during action choice saving: " + 
                    actionChoice.toString());
            return false;
        }
    }

    private boolean insertNewEntry(JdbcTransaction transact, ActionChoice actionChoice) throws
                SQLException, HandledTransactSQLException {
        PreparedStatement insertNew = transact.getPreparedStatement(INSERT_NEW_CHOICE);
        insertNew.setString(1, actionChoice.getActionArgument());
        insertNew.setString(2, transformVariantsToDataBaseRepresentation(actionChoice));
        insertNew.setString(3, actionChoice.getMadeChoice());
        return ( transact.executePreparedUpdate(insertNew) > 0 );
    }

    private boolean rewriteExistedEntryWithNewVariantsAndChoice(JdbcTransaction transact, ActionChoice actionChoice)
            throws SQLException, HandledTransactSQLException {
        PreparedStatement rewriteExisted = transact.getPreparedStatement(UPDATE_CHOICE_WHERE_ACTION_ARG_IS);
        rewriteExisted.setString(1, transformVariantsToDataBaseRepresentation(actionChoice));
        rewriteExisted.setString(2, actionChoice.getMadeChoice());
        rewriteExisted.setString(3, actionChoice.getActionArgument());
        return ( transact.executePreparedUpdate(rewriteExisted) > 0 );
    }

    private boolean ifChoiceWithArgumentExists(JdbcTransaction transact, ActionChoice actionChoice)
            throws HandledTransactSQLException, SQLException {
        PreparedStatement ifExists = transact.getPreparedStatement(SELECT_COUNT_ACTION_ARG);
        ifExists.setString(1, actionChoice.getActionArgument());
        ResultSet ifExistsResult = transact.executePreparedQuery(ifExists);
        if ( ifExistsResult.first() ) {
            int count = ifExistsResult.getInt("result");
            return ( count > 0 );
        } else {
            return true;
        }
    }

    @Override
    public String getChoiceFor(ActionRequest actionRequest) {
        JdbcTransaction transact = this.data.beginTransaction();
        try {
            PreparedStatement getChoice = transact.getPreparedStatement(
                    SELECT_CHOICE_WHERE_ACTION_ARG_AND_VARIANTS_ARE);
            getChoice.setString(1, actionRequest.getActionArgument());
            getChoice.setString(2, transformVariantsToDataBaseRepresentation(actionRequest));
            ResultSet result = transact.executePreparedQuery(getChoice);
            String choice;
            if ( result.first() ) {
                choice = result.getString("action_choice");
            } else {
                choice = "";
            }
            transact.commitThemAll();
            return choice;
        } catch (HandledTransactSQLException e) {
            logError(this.getClass(), "SQLException during action choice searching: ", e);
            this.ioEngine.reportException(e, "SQLException during action choice searching: "
                    + actionRequest.toString());
            return "";
        } catch (SQLException e) {
            transact.rollbackAllAndReleaseResources();
            logError(this.getClass(), "SQLException during action choice searching: ", e);
            this.ioEngine.reportException(e, "SQLException during action choice searching: "
                    + actionRequest.toString());
            return "";
        }
    }

    @Override
    public boolean deleteChoiceFor(String actionArgument) {
        JdbcTransaction transact = this.data.beginTransaction();
        try {
            PreparedStatement delete = transact.getPreparedStatement(
                    DELETE_CHOICE_WHERE_ACTION_ARG_IS);
            delete.setString(1, actionArgument);
            int qty = transact.executePreparedUpdate(delete);
            
            transact.commitThemAll();
            return ( qty > 0 );
        } catch (HandledTransactSQLException e) {
            logError(this.getClass(), "SQLException during action choice removing: ", e);
            this.ioEngine.reportException(e, "SQLException during action choice removing: "
                    + actionArgument);
            return false;
        } catch (SQLException e) {
            transact.rollbackAllAndReleaseResources();
            logError(this.getClass(), "SQLException during action choice removing: ", e);
            this.ioEngine.reportException(e, "SQLException during action choice removing: "
                    + actionArgument);
            return false;
        }
    }
}
