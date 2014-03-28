package pt.up.fe.sdis.proj1.fileInfo;

import pt.up.fe.sdis.proj1.utils.Pair;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteJob;
import com.almworks.sqlite4java.SQLiteStatement;

class OwnFileRemover extends SQLiteJob<Object> {
    public OwnFileRemover(String filePath, Long modificationMillis) {
        _filePath = filePath;
        _modificationMillis = modificationMillis;
    }

    @Override
    protected Object job(SQLiteConnection connection) throws Throwable {
        exec(connection, _filePath, _modificationMillis);
        return null;
    }

    public static void exec(SQLiteConnection connection, String filePath, Long modificationMillis) throws SQLiteException {
        SQLiteStatement st = connection.prepare("DELETE FROM OwnFile WHERE id = ?");
        try {
            st.bind(1, Pair.make_pair(filePath, modificationMillis).hashCode());
            st.stepThrough();
        } finally {
            st.dispose();
        }
    }

    private String _filePath;
    private Long _modificationMillis;
}