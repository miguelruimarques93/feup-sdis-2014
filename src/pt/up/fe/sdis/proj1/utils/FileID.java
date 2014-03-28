package pt.up.fe.sdis.proj1.utils;

import java.util.Arrays;

public class FileID {

    public FileID(String hexString) {
        String[] chars = hexString.split("(?<=\\G..)");
        
        if (chars.length != 32) 
            throw new IllegalArgumentException("FileID must have 32 bytes.");
        
        _fileID = new byte[32];
        
        for (int i = 0; i < _fileID.length; ++i)
            _fileID[i] = (byte) Short.parseShort(chars[i], 16);
        
        _hexFileID = hexString.toLowerCase();
    }
    
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
                for (int i = 0; i < _fileID.length; ++i)
                    sb.append(String.format("%02X", _fileID[i]));
                _hexFileID = sb.toString().toLowerCase();
            }
        }
        return _hexFileID;
    }

    public byte[] toArray() { return _fileID; }
    
    private String _hexFileID = null;
    private byte[] _fileID;
}
