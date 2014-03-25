package pt.up.fe.sdis.proj1.protocols.initiator;

import pt.up.fe.sdis.proj1.messages.Message;
import pt.up.fe.sdis.proj1.protocols.AbstractProtocol;
import pt.up.fe.sdis.proj1.utils.BackupSystem;
import pt.up.fe.sdis.proj1.utils.MyFile;

public class FileDeletion extends AbstractProtocol {
    public FileDeletion(BackupSystem bs, MyFile file) {
        super(null);
        Message msg = Message.makeDelete(file.getFileId().toArray());
        bs.Comm.MC.Sender.Send(msg);
        bs.Files.removeOwnFile(file.getPath());
    }

    @Override
    public void ProcessMessage(Message msg) {        
    }
}
