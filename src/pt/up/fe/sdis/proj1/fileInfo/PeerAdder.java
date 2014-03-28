package pt.up.fe.sdis.proj1.fileInfo;

import pt.up.fe.sdis.proj1.utils.FileID;
import pt.up.fe.sdis.proj1.utils.Pair;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteJob;
import com.almworks.sqlite4java.SQLiteStatement;

class PeerAdder extends SQLiteJob<Object> {
    public PeerAdder(FileID file, Integer chunkNo, String peerIp) {
        _file = file;
        _chunkNo = chunkNo;
        _peerIp = peerIp;
    }

    @Override
    protected Object job(SQLiteConnection connection) throws Throwable {
        exec(connection, _file, _chunkNo, _peerIp);
        return null;
    }

    public static void exec(SQLiteConnection connection, FileID file, Integer chunkNo, String peerIp) throws SQLiteException {
        Pair<FileID, Integer> chunk = Pair.make_pair(file, chunkNo);

        SQLiteStatement st1 = connection.prepare("INSERT INTO Ip (chunkId, IP) values (?, ?)");
        try {
            st1.bind(1, chunk.hashCode());
            st1.bind(2, peerIp);
            st1.stepThrough();
        } finally {
            st1.dispose();
        }
    }

    private FileID _file;
    private Integer _chunkNo;
    private String _peerIp;
}