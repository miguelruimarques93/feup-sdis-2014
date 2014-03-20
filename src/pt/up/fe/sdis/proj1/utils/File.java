package pt.up.fe.sdis.proj1.utils;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class File {
    public File(String myAddr, String path) throws IOException,
            NoSuchAlgorithmException {
        _file = new java.io.File(path);
        _absolutePath = _file.getAbsolutePath();
        
        Path filePath = FileSystems.getDefault()
                .getPath(_absolutePath);
        
        BasicFileAttributes attr = Files.readAttributes(filePath,
                BasicFileAttributes.class);

        
        _lastModifiedTime = attr.lastModifiedTime().toString();
        _ownerIP = myAddr;
        _fileSize = attr.size();

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        String text = _ownerIP + _absolutePath + _lastModifiedTime;
        _fileId = digest.digest(text.getBytes(StandardCharsets.UTF_8));
    }

    public byte[] getFileId() {
        return _fileId;
    }
    
    public long getFileSize() { 
        return _fileSize;
    }

    public byte[] getChunk(int chunkNo) throws IOException {
        long chunkPos = chunkNo * 64000;
        long arrSize = Math.min(64000, _fileSize - chunkPos);
        if (chunkPos > _fileSize) {
            if (raf != null) {
                raf.close();
                raf = null;
            }
            return new byte[0];
        }
        
        if (raf == null) 
            raf = new RandomAccessFile(_file, "r");
        
        raf.seek(chunkPos);
        byte[] result = new byte[(int) arrSize];
        raf.read(result);
        
        return result;
    }
    
    byte[] _fileId;

    private String _absolutePath;
    private java.io.File _file;
    private String _lastModifiedTime;
    private String _ownerIP;
    
    private long _fileSize;
    
    private RandomAccessFile raf = null;
}
