/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.data.daos.sql;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import diarsid.beam.core.control.io.base.Initiator;
import diarsid.beam.core.control.io.base.InnerIoEngine;
import diarsid.beam.core.domain.entities.SchedulableType;
import diarsid.beam.core.domain.entities.Task;
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
            Initiator initiator, LocalDateTime from, LocalDateTime to, SchedulableType... type) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<Task> getExpiredTasks(
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
            Initiator initiator, int taskId, String[] newText) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean editTaskTime(
            Initiator initiator, int taskId, LocalDateTime newTime) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
