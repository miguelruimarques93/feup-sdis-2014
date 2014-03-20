package pt.up.fe.sdis.proj1.protocols.initiator;

import pt.up.fe.sdis.proj1.messages.Message;
import pt.up.fe.sdis.proj1.protocols.AbstractProtocol;
import pt.up.fe.sdis.proj1.utils.BackupSystem;

public class SpaceReclaiming extends AbstractProtocol {
    public SpaceReclaiming(BackupSystem bs, byte[] fileId, int chunkNo) {
        super(null);
        Message msg = Message.makeRemoved(fileId, chunkNo);
        bs.Comm.MC.Sender.Send(msg);
    }

    @Override
    public void ProcessMessage(Message msg) {        
    }
}
