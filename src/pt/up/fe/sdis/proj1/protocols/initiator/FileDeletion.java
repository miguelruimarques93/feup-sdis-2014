package pt.up.fe.sdis.proj1.protocols.initiator;

import pt.up.fe.sdis.proj1.messages.Message;
import pt.up.fe.sdis.proj1.protocols.AbstractProtocol;
import pt.up.fe.sdis.proj1.utils.BackupSystem;
import pt.up.fe.sdis.proj1.utils.FileID;
import pt.up.fe.sdis.proj1.utils.MyFile;
import pt.up.fe.sdis.proj1.utils.Pair;

public class FileDeletion extends AbstractProtocol {
    public FileDeletion(BackupSystem bs, MyFile file) {
        super(null);
        
        Message msg = Message.makeDelete(file.getFileId());
        bs.Comm.MC.Sender.Send(msg);
        bs.Files.removeOwnFile(file.getPath());
    }
    
    public FileDeletion(BackupSystem bs, String filePath) {
        super(null);
        Pair<FileID, Integer> fileInfo = bs.Files.getOwnFileInfo(filePath);
        
        if (fileInfo == null) return;
        
        Message msg = Message.makeDelete(fileInfo.first);
        bs.Comm.MC.Sender.Send(msg);
        bs.Files.removeOwnFile(filePath);
    }

    @Override
    public void ProcessMessage(Message msg) {        
    }
}
