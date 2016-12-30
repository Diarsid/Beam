/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.systemconsole;

/**
 *
 * @author Diarsid
 */
class ConsolePassport {
    
    private String name;
    private String initiatorId;
    private int port;
    
    ConsolePassport() {
        this.name = "name is not set";
        this.initiatorId = "initiator id is not set";
        this.port = 0;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInitiatorId() {
        return this.initiatorId;
    }

    public void setInitiatorId(String initiatorId) {
        this.initiatorId = initiatorId;
    }

    public int getPort() {
        return this.port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
