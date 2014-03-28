package pt.up.fe.sdis.proj1.fileInfo;

import pt.up.fe.sdis.proj1.utils.FileID;
import pt.up.fe.sdis.proj1.utils.Pair;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteJob;
import com.almworks.sqlite4java.SQLiteStatement;

class ChunkRemover extends SQLiteJob<Object> {
    public ChunkRemover(FileID file, Integer chunkNo) {
        _file = file;
        _chunkNo = chunkNo;
    }

    @Override
    protected Object job(SQLiteConnection connection) throws Throwable {
        exec(connection, _file, _chunkNo);
        return null;
    }

    public static void exec(SQLiteConnection connection, FileID file, Integer chunkNo) throws SQLiteException {
        Pair<FileID, Integer> chunk = Pair.make_pair(file, chunkNo);

        SQLiteStatement st1 = connection.prepare("DELETE FROM Chunk WHERE id = ?");
        try {
            st1.bind(1, chunk.hashCode());
            st1.stepThrough();
        } finally {
            st1.dispose();
        }
    }

    private FileID _file;
    private Integer _chunkNo;
}