package pt.up.fe.sdis.proj1.protocols;

import pt.up.fe.sdis.proj1.messages.Message;
import pt.up.fe.sdis.proj1.utils.Communicator;

public class FileDeletion extends AbstractProtocol {
    public FileDeletion(Communicator comm, byte[] fileID) {
        super(comm.MC.Publisher);
        Message msg = Message.delete(fileID);
        comm.MC.Sender.Send(msg);
    }

    @Override
    public void ProcessMessage(Message msg) {        
    }
}
