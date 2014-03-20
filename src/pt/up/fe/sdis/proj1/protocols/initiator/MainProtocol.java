package pt.up.fe.sdis.proj1.protocols.initiator;

import pt.up.fe.sdis.proj1.messages.Message;
import pt.up.fe.sdis.proj1.protocols.AbstractProtocol;
import pt.up.fe.sdis.proj1.utils.MulticastChannelMesssagePublisher;

public class MainProtocol extends AbstractProtocol {

    public MainProtocol(MulticastChannelMesssagePublisher mcmp) {
        super(mcmp);
        this.start();
    }

    @Override
    public void ProcessMessage(Message msg) {

    }

}
