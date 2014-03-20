package pt.up.fe.sdis.proj1.protocols.initiator;

import pt.up.fe.sdis.proj1.messages.Message;
import pt.up.fe.sdis.proj1.protocols.AbstractProtocol;
import pt.up.fe.sdis.proj1.utils.BackupSystem;

public class FileDeletion extends AbstractProtocol {
    public FileDeletion(BackupSystem bs, byte[] fileID) {
        super(null);
        Message msg = Message.makeDelete(fileID);
        bs.Comm.MC.Sender.Send(msg);
    }

    @Override
    public void ProcessMessage(Message msg) {        
    }
}
