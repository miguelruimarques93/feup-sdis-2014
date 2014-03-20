package pt.up.fe.sdis.proj1.protocols.initiator;

import pt.up.fe.sdis.proj1.messages.Message;
import pt.up.fe.sdis.proj1.protocols.AbstractProtocol;
import pt.up.fe.sdis.proj1.utils.Communicator;

public class SpaceReclaiming extends AbstractProtocol {
    public SpaceReclaiming(Communicator comm, byte[] fileId, int chunkNo) {
        super(null);
        Message msg = Message.makeRemoved(fileId, chunkNo);
        comm.MC.Sender.Send(msg);
    }

    @Override
    public void ProcessMessage(Message msg) {        
    }
}
