package pt.up.fe.sdis.proj1.utils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.logging.Level;

import pt.up.fe.sdis.proj1.messages.Message;

public class MulticastChannelMessageSender {
    public MulticastChannelMessageSender(String mCastAddr, int mCastPort)
            throws IOException {
        _address = InetAddress.getByName(mCastAddr);
        _port = mCastPort;
        _mCastSocket = new MulticastSocket();
        _mCastSocket.setTimeToLive(1);
    }
    
    public MulticastChannelMessageSender(String mCastAddr, int mCastPort, String myAddr)
            throws IOException {
        _address = InetAddress.getByName(mCastAddr);
        _port = mCastPort;
        _mCastSocket = new MulticastSocket();
        _mCastSocket.setTimeToLive(1);
        _mCastSocket.setInterface(InetAddress.getByName(myAddr));
    }
    
    public boolean Send(Message msg) {
        byte[] arr = msg.toByteArray();
        DatagramPacket dp = new DatagramPacket(arr, arr.length, _address, _port);
        synchronized (_mCastSocket) {
            try {
                _mCastSocket.send(dp);
                BackupSystem.Log.log(Level.INFO, "Sending " + msg.type + ": " + msg.getFileID() + (msg.getChunkNo() != null ? " " + msg.getChunkNo() : ""));
                return true;
            } catch (IOException e) {
                return false;
            }
        }
    }

    private MulticastSocket _mCastSocket;
    private InetAddress _address;
    private int _port;
}
