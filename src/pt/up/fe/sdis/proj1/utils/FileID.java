package pt.up.fe.sdis.proj1.utils;

import java.util.Arrays;

public class FileID {

    public FileID(byte[] id) {
        if (id.length != 32)
            throw new IllegalArgumentException("FileID must have 32 bytes.");
        _fileID = id.clone();
    }

    @Override
    public boolean equals(Object other) {
        return (other instanceof FileID)
                && Arrays.equals(_fileID, ((FileID) other)._fileID);
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public String toString() {
        if (_hexFileID == null) {
            if (_fileID == null) {
                _hexFileID = "";
            } else {

                StringBuilder sb = new StringBuilder();
                for (int i = _fileID.length - 1; i >= 0; --i)
                    sb.append(String.format("%02X", _fileID[i]));
                _hexFileID = sb.toString();
            }
        }
        return _hexFileID;
    }

    private String _hexFileID = null;
    private byte[] _fileID;
}
