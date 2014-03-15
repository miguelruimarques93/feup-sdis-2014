package pt.up.fe.sdis.proj1.utils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Arrays;

import pt.up.fe.sdis.proj1.messages.Message;

public class Main {

    public static void main(String[] args) throws IOException, InterruptedException {
        
        MulticastSocket mCastSocket = new MulticastSocket();
        mCastSocket.setTimeToLive(1);
        
        byte[] body = new byte[64000];
        Arrays.fill(body, (byte)10);
        
        Message msg = new Message(Message.Type.PUTCHUNK);
        msg.setVersion(1, 0);
        msg.setBody(body);
        msg.setChunkNo(4526);
        msg.setReplicationDeg(3);
        msg.setFileID(new byte[]{ 0x4a, 0x2f, 0x3e, 0x4a, 0x4e, 0x43, 0x34, 0x33, 0x33, 0x34, 0x4e, 0x1c, 0x04, 0x1c, 0x04, 0x1c, 0x55, 0x4e, 0x38, 0x2d, 0x00, 0x6f, 0x1c, 0x04, 0x1c, 0x3c, 0x55, 0x56, 0x50, 0x53, 0x70,0x1a });
        
        Message msg1 = new Message(Message.Type.CHUNK);
        msg1.setVersion(1, 0);
        msg1.setBody(body);
        msg1.setChunkNo(4526);
        // msg1.setReplicationDeg(3);
        msg1.setFileID(new byte[]{ 0x4a, 0x2f, 0x3e, 0x4a, 0x4e, 0x43, 0x34, 0x33, 0x33, 0x34, 0x4e, 0x1c, 0x04, 0x1c, 0x04, 0x1c, 0x55, 0x4e, 0x38, 0x2d, 0x00, 0x6f, 0x1c, 0x04, 0x1c, 0x3c, 0x55, 0x56, 0x50, 0x53, 0x70,0x1a });
        
        
        byte[] arr = msg.toByteArray();
        DatagramPacket dp = new DatagramPacket(arr, arr.length, InetAddress.getByName("239.255.0.1"), 11099);
        byte[] arr1 = msg1.toByteArray();
        DatagramPacket dp1 = new DatagramPacket(arr1, arr1.length, InetAddress.getByName("239.255.0.1"), 11099);
        
        long i = 0;
        
        for (i = 0; i < 100; ++i) {
            mCastSocket.send(dp);
            mCastSocket.send(dp1);
        }
        System.out.println(i * 2L);
        mCastSocket.close();

    }

}
