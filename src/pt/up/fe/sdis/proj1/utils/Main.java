package pt.up.fe.sdis.proj1.utils;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import pt.up.fe.sdis.proj1.Chunk;
import pt.up.fe.sdis.proj1.protocols.initiator.ChunkBackup;
import pt.up.fe.sdis.proj1.protocols.peers.PeerChunkBackup;
import pt.up.fe.sdis.proj1.protocols.peers.PeerStored;

public class Main {

    public static void main(String[] args) throws IOException,
            NoSuchAlgorithmException {
        BackupSystem bs = new BackupSystem(Pair.make_pair("239.255.0.1",
                11099), Pair.make_pair("239.255.0.1", 11091), Pair.make_pair(
                "239.255.0.1", 11092), "192.168.0.198");

        
        
        if (true) {
            MyFile file = new MyFile("192.168.0.198", "[Raghu_Ramakrishnan,_Johannes_Gehrke]_Database_Man(BookFi.org).pdf");

            FileID fileId = file.getFileId();

            int cn = 0;
            byte[] body = null;
            do {
                body = file.getChunk(cn);
                Chunk c = new Chunk(cn, 1, fileId, body);

                new ChunkBackup(bs, c);

                cn++;
            } while (body.length >= 64000);

            System.out.println(cn + " - " + file.getFileSize());
        } else {
            new PeerChunkBackup(bs);
            new PeerStored(bs);
        }
    }

}
