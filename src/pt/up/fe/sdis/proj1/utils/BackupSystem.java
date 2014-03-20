package pt.up.fe.sdis.proj1.utils;

import java.io.IOException;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

public class BackupSystem {
    public BackupSystem(Pair<String, Integer> mc, Pair<String, Integer> mdb, Pair<String, Integer> mdr, String myAddr) throws IOException {
        Comm = new Communicator(mc, mdb, mdr, myAddr);
    }
    
    public final Communicator Comm;
    public ConcurrentHashMap<String, ConcurrentHashMap<Integer, HashSet<String>>> PeersWithChunk = new ConcurrentHashMap<String, ConcurrentHashMap<Integer, HashSet<String>>>();
}
