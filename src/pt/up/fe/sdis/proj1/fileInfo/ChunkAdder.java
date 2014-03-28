package pt.up.fe.sdis.proj1.fileInfo;

import pt.up.fe.sdis.proj1.utils.FileID;
import pt.up.fe.sdis.proj1.utils.Pair;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteJob;
import com.almworks.sqlite4java.SQLiteStatement;

class ChunkAdder extends SQLiteJob<Object> {
    public ChunkAdder(FileID file, Integer chunkNo, Integer replicationDegree) {
        _file = file;
        _chunkNo = chunkNo;
        _replicationDeegree = replicationDegree;
    }

    @Override
    protected Object job(SQLiteConnection connection) throws Throwable {
        exec(connection, _file, _chunkNo, _replicationDeegree);
        return null;
    }

    public static void exec(SQLiteConnection connection, FileID file, Integer chunkNo, Integer replicationDegree) throws SQLiteException {
        Pair<FileID, Integer> chunk = Pair.make_pair(file, chunkNo);

        SQLiteStatement st1 = null;
        try {
            st1 = connection.prepare("INSERT INTO Chunk (id, fileId, chunkNo, replicationDegree) VALUES (?, ?, ?, ?)");
            st1.bind(1, chunk.hashCode());
            st1.bind(2, chunk.first.hashCode());
            st1.bind(3, chunk.second);
            st1.bind(4, replicationDegree);
            st1.stepThrough();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            st1.dispose();
        }
    }

    private FileID _file;
    private Integer _chunkNo;
    private Integer _replicationDeegree;
}