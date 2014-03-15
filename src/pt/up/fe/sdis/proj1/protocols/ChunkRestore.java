package pt.up.fe.sdis.proj1.protocols;

import pt.up.fe.sdis.proj1.messages.Message;
import pt.up.fe.sdis.proj1.utils.MulticastChannelMesssagePublisher;

public class ChunkRestore extends AbstractProtocol {
    public ChunkRestore(MulticastChannelMesssagePublisher mcmp) {
        super(mcmp);
    }

    @Override
    public void ProcessMessage(Message msg) {
        // TODO Auto-generated method stub
        
    }
}
