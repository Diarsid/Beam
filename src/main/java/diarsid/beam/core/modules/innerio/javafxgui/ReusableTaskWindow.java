/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.innerio.javafxgui;

import diarsid.beam.core.modules.tasks.TaskMessage;

/**
 *
 * @author Diarsid
 */
public interface ReusableTaskWindow extends Runnable {
    
    void reuseWithNewTask(TaskMessage task);
}