package pt.up.fe.sdis.proj1.protocols;

import pt.up.fe.sdis.proj1.messages.Message;
import pt.up.fe.sdis.proj1.utils.MulticastChannelMesssagePublisher;

public class MainProtocol extends AbstractProtocol {

    public MainProtocol(MulticastChannelMesssagePublisher mcmp) {
        super(mcmp);
    }

    @Override
    public void ProcessMessage(Message msg) {

    }

}
