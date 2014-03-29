package pt.up.fe.sdis.proj1.protocols.initiator;

import pt.up.fe.sdis.proj1.BackupSystem;
import pt.up.fe.sdis.proj1.messages.Message;
import pt.up.fe.sdis.proj1.protocols.AbstractProtocol;
import pt.up.fe.sdis.proj1.utils.FileID;
import pt.up.fe.sdis.proj1.utils.Pair;

public class FileDeletion extends AbstractProtocol {    
    public FileDeletion(BackupSystem bs, String filePath, Long modificationMillis) {
        super(null);
        Pair<FileID, Integer> fileInfo = bs.Files.getOwnFileVersionInfo(filePath, modificationMillis);
        
        if (fileInfo == null) return;
        
        Message msg = Message.makeDelete(fileInfo.first);
        bs.Comm.MC.Sender.Send(msg);
        bs.Files.addRemovedFile(fileInfo.first);
        bs.Files.removeOwnFile(filePath, modificationMillis);
    }

    @Override
    public void ProcessMessage(Message msg) {        
    }
}
