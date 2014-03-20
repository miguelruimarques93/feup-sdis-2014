package pt.up.fe.sdis.proj1.protocols.peers;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

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
        java.io.File dir = new java.io.File("backups/" + msg.getHexFileID());
        if (!dir.exists()) dir.mkdirs();
        
        try {
            java.io.File file = new java.io.File("backups/" + msg.getHexFileID() + "/" + Integer.toString(msg.getChunkNo()) + ".ips");
            
            String hexFileId = msg.getHexFileID();

            ConcurrentHashMap<Integer, HashSet<String>> fileChunksPeers = _bs.PeersWithChunk.get(hexFileId);
            if (fileChunksPeers == null) {
               _bs.PeersWithChunk.put(hexFileId, new ConcurrentHashMap<Integer, HashSet<String>>());
               fileChunksPeers = _bs.PeersWithChunk.get(hexFileId);
            }
                                                         
            HashSet<String> chunksPeers = fileChunksPeers.get(msg.getChunkNo());
            if (chunksPeers == null) {
                fileChunksPeers.put(msg.getChunkNo(), new HashSet<String>());
                chunksPeers = fileChunksPeers.get(msg.getChunkNo());
                
            }

            synchronized (chunksPeers) {
                String uniqueId = msg.Sender.getHostAddress();
                if (!chunksPeers.contains(uniqueId)) {
                    chunksPeers.add(uniqueId);
                    PrintWriter pw = new PrintWriter(new FileOutputStream(file, true));
                    pw.println(msg.Sender);
                    pw.close();
                }
            }                             
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private BackupSystem _bs;

}
