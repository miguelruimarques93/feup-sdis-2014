package pt.up.fe.sdis.proj1.messages;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import pt.up.fe.sdis.proj1.messages.Message.Type;

public class HashTest {
    public static void main(String[] args) throws NoSuchAlgorithmException, IOException {
        String text = "Hello World";
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(text.getBytes("UTF-8"));
        System.out.println(Arrays.toString(hash));
        
        Message msg = new Message(Type.PUTCHUNK);
        msg.setVersion(1, 0);
        msg.setBody(new byte[]{ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 });
        msg.setChunkNo(4526);
        msg.setReplicationDeg(3);
        msg.setFileID(hash);
        byte[] b = msg.toByteArray();
        
        System.out.println(Arrays.toString(b));
        
        Message msg1 = Message.fromByteArray(b);
        
        System.out.println(Arrays.toString(msg1.toByteArray()));
        
        byte[] fileID = msg.getFileID();
        
        StringBuilder sb = new StringBuilder();
        
        for (int i = fileID.length - 1; i >= 0; --i) sb.append(String.format("%02X", fileID[i]));
        
        System.out.println(sb.toString());
        
        fileID = msg1.getFileID();
        
        sb = new StringBuilder();
        
        for (int i = fileID.length - 1; i >= 0; --i) sb.append(String.format("%02X", fileID[i]));
        
        System.out.println(sb.toString());
        
    }
}
