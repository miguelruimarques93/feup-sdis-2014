package pt.up.fe.sdis.proj1.protocols.peers;

import pt.up.fe.sdis.proj1.messages.Message;
import pt.up.fe.sdis.proj1.protocols.AbstractProtocol;
import pt.up.fe.sdis.proj1.utils.Communicator;
import rx.functions.Func1;

public class PeerChunkBackup extends AbstractProtocol {

    public PeerChunkBackup(Communicator comm) {
        super(comm.MDB.Publisher);
        _comm = comm;
        this.start(new Func1<Message, Boolean>() {
            
            @Override
            public Boolean call(Message arg0) {
                return arg0.type == Message.Type.PUTCHUNK;
            }
        });
    }

    @Override
    protected void ProcessMessage(Message msg) {
        _comm.MC.Sender.Send(Message.makeStored(msg.getFileID(), msg.getChunkNo()));
        System.out.println("Received : " + msg.getChunkNo());
    }

    Communicator _comm;
}
