package pt.up.fe.sdis.proj1.utils;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import pt.up.fe.sdis.proj1.Chunk;
import pt.up.fe.sdis.proj1.protocols.initiator.ChunkBackup;
import pt.up.fe.sdis.proj1.protocols.peers.PeerChunkBackup;

public class Main {

    public static void main(String[] args) throws IOException,
            NoSuchAlgorithmException {
        Communicator comm = new Communicator(Pair.make_pair("239.255.0.1",
                11099), Pair.make_pair("239.255.0.1", 11091), Pair.make_pair(
                "239.255.0.1", 11092), "192.168.0.197");

        if (true) {
            File file = new File("192.168.0.197", "01Contents_Data_Nets.pdf");

            byte[] fileId = file.getFileId();

            int cn = 0;
            byte[] body = null;
            do {
                body = file.getChunk(cn);
                Chunk c = new Chunk(cn, 3, fileId, body);

                new ChunkBackup(comm, c);

                cn++;
            } while (body.length >= 64000);

            System.out.println(cn + " - " + file.getFileSize());
        } else {
            new PeerChunkBackup(comm);
        }
    }

}
