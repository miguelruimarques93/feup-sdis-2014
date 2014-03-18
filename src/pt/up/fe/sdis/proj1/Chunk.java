package pt.up.fe.sdis.proj1;

public class Chunk {

    public Chunk(int cn, int rd, byte[] fid, byte[] d) {
        if (cn < 1 || cn > 999999) throw new IllegalArgumentException();
        chunkNo = cn;
        if (rd < 1 || rd > 9) throw new IllegalArgumentException();
        replicationDeg = rd;     
        if (fid.length != 32) throw new IllegalArgumentException();
        fileID = fid;
        if (d.length > 64000) throw new IllegalArgumentException();
        data = d;
    }   
    
    public final int chunkNo;
    public final int replicationDeg;
    public final byte[] fileID;
    public final byte[] data;
}
