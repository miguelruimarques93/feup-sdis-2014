package pt.up.fe.sdis.proj1.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import pt.up.fe.sdis.proj1.Chunk;

public class MyFile {
    public MyFile(String myAddr, String path) throws IOException {
        _file = new java.io.File(path);
        _absolutePath = _file.getAbsolutePath();
        
        Path filePath = FileSystems.getDefault()
                .getPath(_absolutePath);
        
        BasicFileAttributes attr = Files.readAttributes(filePath,
                BasicFileAttributes.class);

        
        _lastModifiedTime = attr.lastModifiedTime().toMillis();
        _ownerIP = myAddr;
        _fileSize = attr.size();

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String text = _ownerIP + _absolutePath + _lastModifiedTime.toString() + new Date().getTime();
            _fileId = new FileID(digest.digest(text.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            _fileId = null;
        }
    }

    public FileID getFileId() {
        return _fileId;
    }
    
    public long getFileSize() { 
        return _fileSize;
    }

    public String getPath() { 
        return _absolutePath;
    }
    
    public int getNumberOfChunks() { 
        return (int)(getFileSize() / Chunk.MAX_CHUNK_SIZE) + 1;
    }
    
    public byte[] getChunk(int chunkNo) throws IOException {
        long chunkPos = chunkNo * Chunk.MAX_CHUNK_SIZE;
        long arrSize = Math.min(Chunk.MAX_CHUNK_SIZE, _fileSize - chunkPos);
        if (chunkPos > _fileSize || raf == null) {
            return new byte[0];
        }
        
        byte[] result = new byte[(int) arrSize];;
        synchronized (raf) {
            raf.seek(chunkPos);
            raf.read(result);
        }
        
        return result == null ? new byte[0] : result;
    }
    
    public void open() throws FileNotFoundException {
        if (raf == null) raf = new RandomAccessFile(_file, "r");
    }
    
    public void close() {
        if (raf != null) {
            try { raf.close(); } catch (IOException e) { }
            raf = null;
        }
    }

    @Override
    public String toString() {
        return _fileId.toString();
    }
    
    @Override
    public int hashCode() {
        return _fileId.hashCode();
    }
    
    public Long getLastModifiedDate() { return _lastModifiedTime; }
    
    FileID _fileId;

    private String _absolutePath;
    private java.io.File _file;
    private Long _lastModifiedTime;
    private String _ownerIP;
    
    private long _fileSize;
    
    private RandomAccessFile raf = null;
}
