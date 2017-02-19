/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.data.daos.sql;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.domain.entities.Task;
import diarsid.beam.core.domain.entities.TaskRepeat;
import diarsid.beam.core.domain.inputparsing.time.AllowedTimePeriod;
import diarsid.beam.core.modules.data.DaoTasks;
import diarsid.beam.core.modules.data.DataBase;
import diarsid.beam.core.modules.data.daos.BeamCommonDao;


class H2DaoTasks 
        extends BeamCommonDao 
        implements DaoTasks {
    
    H2DaoTasks(DataBase dataBase, InnerIoEngine ioEngine) {
        super(dataBase, ioEngine);
    }

    @Override
    public Optional<LocalDateTime> getTimeOfFirstActiveTask(
            Initiator initiator) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<Task> getActiveTasksOfTypeBetweenDates(
            Initiator initiator, LocalDateTime from, LocalDateTime to, TaskRepeat... type) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<Task> getActiveTasksBeforeTime(
            Initiator initiator, LocalDateTime fromNow) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<Task> getFirstActiveTasks(
            Initiator initiator) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean updateTasks(
            Initiator initiator, List<Task> tasks) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean saveTask(
            Initiator initiator, Task task) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean deleteTaskById(
            Initiator initiator, int id) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean editTaskText(
            Initiator initiator, int taskId, List<String> newText) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean editTaskTime(
            Initiator initiator, int taskId, LocalDateTime newTime) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<Task> findTasksByTextPattern(Initiator initiator, String textPattern) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean editTaskTime(Initiator initiator, int taskId, LocalDateTime newTime, AllowedTimePeriod timePeriod) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
