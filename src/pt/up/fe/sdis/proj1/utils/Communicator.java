package pt.up.fe.sdis.proj1.utils;

import java.io.IOException;

public class Communicator {
    
    public Communicator(Pair<String, Integer> mc, Pair<String, Integer> mdb, Pair<String, Integer> mdr) throws IOException {
        MC = new Channel(mc.first, mc.second);
        MDB = new Channel(mdb.first, mdb.second);
        MDR = new Channel(mdr.first, mdr.second);
    }
    
    public final Channel MC;
    public final Channel MDB;
    public final Channel MDR;
}
