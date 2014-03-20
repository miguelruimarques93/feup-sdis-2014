package pt.up.fe.sdis.proj1.protocols.initiator;

import pt.up.fe.sdis.proj1.messages.Message;
import pt.up.fe.sdis.proj1.protocols.AbstractProtocol;
import pt.up.fe.sdis.proj1.utils.Communicator;

public class FileDeletion extends AbstractProtocol {
    public FileDeletion(Communicator comm, byte[] fileID) {
        super(comm.MC.Publisher);
        Message msg = Message.makeDelete(fileID);
        comm.MC.Sender.Send(msg);
    }

    @Override
    public void ProcessMessage(Message msg) {        
    }
}
