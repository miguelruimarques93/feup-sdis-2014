package pt.up.fe.sdis.proj1.protocols.peers;

import pt.up.fe.sdis.proj1.messages.Message;
import pt.up.fe.sdis.proj1.protocols.AbstractProtocol;
import pt.up.fe.sdis.proj1.utils.BackupSystem;
import rx.functions.Func1;

public class PeerStored extends AbstractProtocol {

    public PeerStored(BackupSystem bs) {
        super(bs.Comm.MC.Publisher);

        _bs = bs;
        
        this.start(new Func1<Message, Boolean>() {
            @Override
            public Boolean call(Message arg0) {
                return arg0.type == Message.Type.STORED;
            }
        });
    }

    @Override
    protected void ProcessMessage(Message msg) {
        if (_bs.Files.containsChunk(msg.getFileID(), msg.getChunkNo())) {
            _bs.Files.addPeer(msg.getFileID(), msg.getChunkNo(), msg.Sender.toString());
        }
    }
    
    private BackupSystem _bs;

}
