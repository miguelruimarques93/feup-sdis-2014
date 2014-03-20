package pt.up.fe.sdis.proj1;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import pt.up.fe.sdis.proj1.utils.File;

public class Main {

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
        File f = new File("192.168.0.197", "Readme.md");
        File f1 = new File("192.168.0.197", "LICENSE");
        
        byte[] fileID = f.getFileId();
        StringBuilder sb = new StringBuilder();
        for (int i = fileID.length - 1; i >= 0; --i)
            sb.append(String.format("%02X", fileID[i]));
        
        sb.append('\n');
        
        fileID = f1.getFileId();
        for (int i = fileID.length - 1; i >= 0; --i)
            sb.append(String.format("%02X", fileID[i]));
        
        System.out.println(sb.toString());
        
    }

}
