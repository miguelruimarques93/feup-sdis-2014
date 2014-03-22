package pt.up.fe.sdis.proj1;

import pt.up.fe.sdis.proj1.utils.FileID;

public class Chunk {
	public static final int MAX_CHUNK_SIZE = 64000;
    public Chunk(int cn, int rd, FileID fid, byte[] d) {
        if (cn < 0 || cn > 999999) throw new IllegalArgumentException();
        chunkNo = cn;
        if (rd < 1 || rd > 9) throw new IllegalArgumentException();
        replicationDeg = rd;     
        fileID = fid;
        if (d.length > 64000) throw new IllegalArgumentException();
        data = d;
    }   
    
    public final int chunkNo;
    public final int replicationDeg;
    public final FileID fileID;
    public final byte[] data;
}
