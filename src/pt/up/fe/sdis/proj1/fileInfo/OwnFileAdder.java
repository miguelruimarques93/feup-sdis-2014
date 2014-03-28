package pt.up.fe.sdis.proj1.fileInfo;

import pt.up.fe.sdis.proj1.utils.FileID;
import pt.up.fe.sdis.proj1.utils.Pair;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteJob;
import com.almworks.sqlite4java.SQLiteStatement;

class OwnFileAdder extends SQLiteJob<Object> {
    public OwnFileAdder(String filePath, FileID fileId, Integer numberOfChunks, Long modificationMillis) {
        _filePath = filePath;
        _fileId = fileId;
        _numberOfChunks = numberOfChunks;
        _modificationMillis = modificationMillis;
    }

    @Override
    protected Object job(SQLiteConnection connection) throws Throwable {
        exec(connection, _filePath, _fileId, _numberOfChunks, _modificationMillis);
        return null;
    }

    public static void exec(SQLiteConnection connection, String filePath, FileID fileId, Integer numberOfChunks, Long modificationMillis) throws SQLiteException {
        String fileID = fileId.toString();

        SQLiteStatement st = connection.prepare("INSERT INTO OwnFile (id, filePath, fileId, numberChunks, modificationMillis) values(?, ?, ?, ?, ?)");
        try {
            st.bind(1, Pair.make_pair(filePath, modificationMillis).hashCode());
            st.bind(2, filePath);
            st.bind(3, fileID);
            st.bind(4, numberOfChunks);
            st.bind(5, modificationMillis);
            st.stepThrough();
        } finally {
            st.dispose();
        }
    }

    private String _filePath;
    private FileID _fileId;
    private Integer _numberOfChunks;
    private Long _modificationMillis;
}