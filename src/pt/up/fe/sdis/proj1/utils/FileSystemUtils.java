package pt.up.fe.sdis.proj1.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import pt.up.fe.sdis.proj1.messages.Message;

public class FileSystemUtils {
    
    public static long fileSize(File directory) {
        if (!directory.exists())
            return 0L;

        if (!directory.isDirectory())
            directory.length();

        long size = 0L;

        File[] files = directory.listFiles();
        if (files == null) { // null if security restricted
            return 0L;
        }
        for (int i = 0; i < files.length; i++) {
            File file = files[i];

            if (file.isDirectory())
                size += fileSize(file);
            else
                size += file.length();
        }

        return size;
    }
    
    public static void CreateDirectory(String dirPath) {
        File dir = new File(dirPath);
        dir.mkdirs();
    }
    
    public static boolean deleteFile(File path) {
        if (path.exists() && path.isDirectory()) {
            File[] files = path.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteFile(files[i]);
                } else {
                    files[i].delete();
                }
            }
        }
        return (path.delete());
    }
    
    public static long WriteByteArray(String path, byte[] data) {        
        java.io.File file = new java.io.File(path).getAbsoluteFile();
        CreateDirectory(file.getParent());
        
        if (file.exists()) return 0L;
        try {
            FileOutputStream f = new FileOutputStream(file);
            f.write(data);
            f.close();
            return file.length();
        } catch (IOException e) {
            e.printStackTrace();
            return 0L;
        }
    }
}
