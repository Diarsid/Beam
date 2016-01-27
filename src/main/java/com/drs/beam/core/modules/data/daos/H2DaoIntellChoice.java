/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.drs.beam.core.modules.data.daos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.drs.beam.core.exceptions.NullDependencyInjectionException;
import com.drs.beam.core.modules.IoInnerModule;
import com.drs.beam.core.modules.data.DaoIntellChoice;
import com.drs.beam.core.modules.data.DataBase;

/**
 *
 * @author Diarsid
 */
class H2DaoIntellChoice implements DaoIntellChoice {
    
    private final DataBase data;
    private final IoInnerModule ioEngine;
    
    H2DaoIntellChoice(final IoInnerModule io, final DataBase data) {
        if (io == null){
            throw new NullDependencyInjectionException(
                    H2DaoIntellChoice.class.getSimpleName(), 
                    IoInnerModule.class.getSimpleName());
        }
        if (data == null){
            throw new NullDependencyInjectionException(
                    H2DaoIntellChoice.class.getSimpleName(), 
                    DataBase.class.getSimpleName());
        }
        this.data = data;
        this.ioEngine = io;
    }
    
    /* 
     * SQL Table illustration for remembered command choices.
     *  +----------------------------------------+
     *  | command_choices                        |
     *  +------------+------------+--------------+
     *  | choice_id  | command    | choice       |
     *  +------------+------------+--------------+
     *  | 123        | see vk     | vkontakt     |
     *  +------------+------------+--------------+
     *  | 451        | op java    | java_lang    |
     *  +------------+------------+--------------+
     */
    
    private final String SELECT_ALL_CHOICES = 
            "SELECT command, choice " +
            "FROM command_choices";
    private final String SELECT_ALL_WHERE_COMMAND_NAME_LIKE = 
            "SELECT command, choice " +
            "FROM command_choices " +
            "WHERE command LIKE ? ";
    private final String SELECT_CHOICE_WHERE_COMMAND_NAME_LIKE = 
            "SELECT choice " +
            "FROM command_choices " +
            "WHERE command LIKE ? ";
    private final String INSERT_NEW_CHOICE = 
            "INSERT INTO command_choices (command, choice) " +
            "VALUES (?, ?) ";
    private final String DELETE_CHOICE_WHERE_NAME_LIKE = 
            "DELETE FROM command_choices " +
            "WHERE command LIKE ? ";
    
    @Override
    public Map<String, String> getAllChoices() {
        try (Connection con = this.data.connect();
                PreparedStatement ps = con.prepareStatement(SELECT_ALL_CHOICES)) {
            
            ResultSet rs = ps.executeQuery();
            Map<String, String> choices = new HashMap<>();
            while ( rs.next() ) {
                choices.put(
                        rs.getString(1), 
                        rs.getString(2));
            }
            return choices;            
        } catch (SQLException e) {
            this.ioEngine.reportException(e, 
                    "SQLException: get choice for command: ");
            return Collections.emptyMap();
        }
    }
    
    @Override
    public String getChoiceFor(String command){
        try (Connection con = this.data.connect();
                PreparedStatement ps = con.prepareStatement(
                        SELECT_CHOICE_WHERE_COMMAND_NAME_LIKE)) {
            
            ps.setString(1, command);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString(1);
            } else {
                return "";
            }
        } catch (SQLException e) {
            this.ioEngine.reportException(e, 
                    "SQLException: get choice for command: "+command);
            return "";
        }
    }
    
    @Override
    public List<String> getChoicesLike(String command){
        try (Connection con = this.data.connect();
                PreparedStatement ps = con.prepareStatement(
                        SELECT_ALL_WHERE_COMMAND_NAME_LIKE)) {
            
            List<String> commands = new ArrayList<>();
            ps.setString(1, "%"+command+"%");
            ResultSet rs = ps.executeQuery();
            StringBuilder sb = new StringBuilder();
            while(rs.next()) {
                sb.append(rs.getString(1)).append(" -> ").append(rs.getString(2));
                commands.add(sb.toString());
                sb.delete(0, sb.length());
            }
            return commands;
        } catch (SQLException e) {
            this.ioEngine.reportException(e, 
                    "SQLException: get commands from choices where commands like: "
                            +command);
            return Collections.emptyList();
        }
    }
    
    @Override
    public boolean deleteChoiceForCommand(String command){
        try (Connection con = this.data.connect();
                PreparedStatement ps = con.prepareStatement(
                        DELETE_CHOICE_WHERE_NAME_LIKE)) {
            
            ps.setString(1, command);
            int qty = ps.executeUpdate();
            return (qty > 0); 
        } catch (SQLException e) {
            this.ioEngine.reportException(e, 
                    "SQLException: delete choice for command: "+command);
            return false;
        }
    }
    
    @Override
    public boolean newChoice(String command, String choice){
        try (Connection con = this.data.connect();
                PreparedStatement ps = con.prepareStatement(INSERT_NEW_CHOICE)) {
            
            ps.setString(1, command);
            ps.setString(2, choice);
            int qty = ps.executeUpdate();
            return (qty > 0);             
        } catch (SQLException e) {
            this.ioEngine.reportException(e, 
                    "SQLException: new choice '"+choice+"' for command: "+command);
            return false;
        }
    }
}
