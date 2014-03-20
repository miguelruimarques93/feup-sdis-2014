package pt.up.fe.sdis.proj1.protocols.initiator;

import pt.up.fe.sdis.proj1.messages.Message;
import pt.up.fe.sdis.proj1.protocols.AbstractProtocol;

public class MainProtocol extends AbstractProtocol {

    public MainProtocol() {
        super(null);
        this.start();
    }

    @Override
    public void ProcessMessage(Message msg) {

    }

}
