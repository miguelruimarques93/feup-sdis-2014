package pt.up.fe.sdis.proj1.protocols;

import pt.up.fe.sdis.proj1.messages.Message;
import pt.up.fe.sdis.proj1.utils.MulticastChannelMesssagePublisher;

public class SpaceReclaiming extends AbstractProtocol {
    public SpaceReclaiming(MulticastChannelMesssagePublisher mcmp) {
        super(mcmp);
        this.start();
    }

    @Override
    public void ProcessMessage(Message msg) {
        // TODO Auto-generated method stub
        
    }
}
