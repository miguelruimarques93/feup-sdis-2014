package pt.up.fe.sdis.proj1.protocols;

import pt.up.fe.sdis.proj1.messages.Message;
import pt.up.fe.sdis.proj1.utils.MulticastChannelMesssagePublisher;

public class FileDeletion extends AbstractProtocol {
    public FileDeletion(MulticastChannelMesssagePublisher mcmp) {
        super(mcmp);
    }

    @Override
    public void ProcessMessage(Message msg) {
        // TODO Auto-generated method stub
        
    }
}
