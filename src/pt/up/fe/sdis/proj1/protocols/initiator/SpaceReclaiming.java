package pt.up.fe.sdis.proj1.protocols.initiator;

import java.util.PriorityQueue;

import pt.up.fe.sdis.proj1.messages.Message;
import pt.up.fe.sdis.proj1.protocols.AbstractProtocol;
import pt.up.fe.sdis.proj1.utils.BackupSystem;
import pt.up.fe.sdis.proj1.utils.FileID;

public class SpaceReclaiming extends AbstractProtocol {
    public SpaceReclaiming(BackupSystem bs, boolean clearExcess) {
        super(null);
        _bs = bs;
        
       PriorityQueue<BackupSystem.Files.ChunkInfo> chunksToRemove = _bs.Files.getChunksToRemove();
       
       if(clearExcess){
           while(!chunksToRemove.isEmpty() && (chunksToRemove.peek().getExcessDegree() > 0 || _bs.getAvailableSpace() < 0)){
               BackupSystem.Files.ChunkInfo ci = chunksToRemove.remove();
               _bs.deleteChunk(ci.getFileId(), ci.getChunkNo());
               sendRemovedNotification(ci.getFileId(), ci.getChunkNo());
           }
       }
       else{
           while(!chunksToRemove.isEmpty() && _bs.getAvailableSpace() < 0){
               BackupSystem.Files.ChunkInfo ci = chunksToRemove.remove();
               _bs.deleteChunk(ci.getFileId(), ci.getChunkNo());
               sendRemovedNotification(ci.getFileId(), ci.getChunkNo());
           }
       }
    }

    private void sendRemovedNotification(FileID fileId, int chunkNo){    
        Message msg = Message.makeRemoved(fileId.toArray(), chunkNo);
        _bs.Comm.MC.Sender.Send(msg);
    }
    @Override
    public void ProcessMessage(Message msg) {        
    }
    
    private BackupSystem _bs;
}
