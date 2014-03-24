package pt.up.fe.sdis.proj1.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import pt.up.fe.sdis.proj1.Chunk;
import pt.up.fe.sdis.proj1.messages.Message;

public class MyFile {
    public MyFile(String myAddr, String path) throws IOException,
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
        _fileId = new FileID(digest.digest(text.getBytes(StandardCharsets.UTF_8)));
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
        return (int) Math.ceil(getFileSize() / (double)Chunk.MAX_CHUNK_SIZE);
    }
    
    public byte[] getChunk(int chunkNo) throws IOException {
        long chunkPos = chunkNo * Chunk.MAX_CHUNK_SIZE;
        long arrSize = Math.min(Chunk.MAX_CHUNK_SIZE, _fileSize - chunkPos);
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
    
    public static byte[] ReadChunk(FileID fileId, Integer chunkNo) throws IOException {
    	File f = new File("backups/" + fileId.toString() + "/" + chunkNo.toString());
    	if (!f.exists()) 
    		return null;
    	
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(f));
		byte[] chunk = new byte[(int)f.length()];
		
		bis.read(chunk);    	
    	
		bis.close();
    	return chunk;
    }
    
    public static void WriteChunk(Message msg) {
        String directory = msg.type == Message.Type.PUTCHUNK ? "backups/" : "restores/";
        
        java.io.File dir = new java.io.File(directory + msg.getHexFileID());
        if (!dir.exists()) dir.mkdirs();
        java.io.File file = new java.io.File(directory + msg.getHexFileID() + "/" + Integer.toString(msg.getChunkNo()));
        
        if (file.exists()) return;
        try {
            FileOutputStream f = new FileOutputStream(file);
            f.write(msg.getBody());
            f.close();
        } catch (IOException e) {
            e.printStackTrace();
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
    
    FileID _fileId;

    private String _absolutePath;
    private java.io.File _file;
    private String _lastModifiedTime;
    private String _ownerIP;
    
    private long _fileSize;
    
    private RandomAccessFile raf = null;
}
