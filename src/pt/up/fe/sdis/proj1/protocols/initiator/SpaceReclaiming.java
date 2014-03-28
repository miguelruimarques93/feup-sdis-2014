package pt.up.fe.sdis.proj1.protocols.initiator;

import java.io.IOException;
import java.util.PriorityQueue;

import pt.up.fe.sdis.proj1.BackupSystem;
import pt.up.fe.sdis.proj1.Chunk;
import pt.up.fe.sdis.proj1.fileInfo.ChunkInfo;
import pt.up.fe.sdis.proj1.messages.Message;
import pt.up.fe.sdis.proj1.protocols.AbstractProtocol;
import pt.up.fe.sdis.proj1.utils.FileID;

public class SpaceReclaiming extends AbstractProtocol {
    public SpaceReclaiming(BackupSystem bs, boolean clearExcess) {
        super(null);
        _bs = bs;
        
       PriorityQueue<ChunkInfo> chunksToRemove = _bs.Files.getChunksToRemove();
       
       System.out.println(chunksToRemove.size());
       
       if(clearExcess){
           while(!chunksToRemove.isEmpty() && (chunksToRemove.peek().getExcessDegree() > 0 || _bs.getAvailableSpace() < 0)){
               ChunkInfo ci = chunksToRemove.remove();
               _bs.deleteChunk(ci.getFileId(), ci.getChunkNo());
               sendRemovedNotification(ci.getFileId(), ci.getChunkNo());
           }
       }
       else{
           while(!chunksToRemove.isEmpty() && _bs.getAvailableSpace() < 0){
               ChunkInfo ci = chunksToRemove.remove();
               _bs.deleteChunk(ci.getFileId(), ci.getChunkNo());
               sendRemovedNotification(ci.getFileId(), ci.getChunkNo());
           }
       }
    }

    private void sendRemovedNotification(FileID fileId, int chunkNo){  
        int rrd = _bs.Files.getChunkRealReplicationDegree(fileId, chunkNo);
        
        if (rrd == 1) {
            try {
                int drd = _bs.Files.getChunkDesiredReplicationDegree(fileId, chunkNo);
                byte[] chunkArray = _bs.readChunk(fileId, chunkNo);
                Chunk chunk = new Chunk(chunkNo, drd, fileId, chunkArray);
                new ChunkBackup(_bs, chunk);
            } catch (IOException e) {
            }
        }
        Message msg = Message.makeRemoved(fileId, chunkNo);
        _bs.Comm.MC.Sender.Send(msg);
    }
    @Override
    public void ProcessMessage(Message msg) {        
    }
    
    private BackupSystem _bs;
}
