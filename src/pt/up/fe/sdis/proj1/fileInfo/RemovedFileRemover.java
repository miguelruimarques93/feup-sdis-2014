package pt.up.fe.sdis.proj1.fileInfo;

import pt.up.fe.sdis.proj1.utils.FileID;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteJob;
import com.almworks.sqlite4java.SQLiteStatement;

public class RemovedFileRemover extends SQLiteJob<Object> {
    public RemovedFileRemover(FileID file) {
        _fileId = file;
    }

    @Override
    protected Object job(SQLiteConnection connection) throws Throwable {
        exec(connection, _fileId);
        return null;
    }

    public static void exec(SQLiteConnection connection, FileID fileId) throws SQLiteException {
        String fileID = fileId.toString();

        SQLiteStatement st = connection.prepare("DELETE FROM RemovedFile WHERE fileID = ?");
        try {
            st.bind(1, fileID);
            st.stepThrough();
        } finally {
            st.dispose();
        }
    }

    private FileID _fileId;
}
