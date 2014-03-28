package pt.up.fe.sdis.proj1;

import java.text.SimpleDateFormat;
import java.util.Date;

public class FileVersion {
    
    public FileVersion(String filePath, Long modificationMillis) {
        _filePath = filePath;
        _modificationMillis = modificationMillis;
    }

    public String getFilePath() {
        return _filePath;
    }

    public Long getModificationMillis() {
        return _modificationMillis;
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs instanceof FileVersion) {
            FileVersion other = (FileVersion) rhs;
            return other._filePath.equals(_filePath) && other._modificationMillis.equals(_modificationMillis);
        }
        return false;
    }
    
    @Override
    public String toString() {
        return _dateFormat.format(new Date(_modificationMillis)) + " - " + _filePath;
    }
    
    private static SimpleDateFormat _dateFormat =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
    
    private String _filePath;
    private Long _modificationMillis;
}
