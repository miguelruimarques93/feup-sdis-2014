package pt.up.fe.sdis.proj1.protocols.peers;

import pt.up.fe.sdis.proj1.BackupSystem;
import pt.up.fe.sdis.proj1.messages.Message;
import pt.up.fe.sdis.proj1.protocols.AbstractProtocol;
import pt.up.fe.sdis.proj1.utils.MessageFilter;

public class PeerIsDeletedProtocol extends AbstractProtocol {

    public PeerIsDeletedProtocol(BackupSystem bs) {
        super(bs.Comm.MC.Publisher);
        _bs = bs;
        start(new MessageFilter(Message.Type.ISDELETED));
    }

    @Override
    protected void ProcessMessage(Message msg) {
        if (_bs.Files.containsDeletedFile(msg.getFileID())) {
            _bs.Comm.MC.Sender.Send(Message.makeDelete(msg.getFileID()));
        }

    }
    
    private BackupSystem _bs;

}
