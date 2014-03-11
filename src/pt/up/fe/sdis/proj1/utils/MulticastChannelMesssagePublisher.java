package pt.up.fe.sdis.proj1.utils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import pt.up.fe.sdis.proj1.messages.Message;
import rx.Observable;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

public class MulticastChannelMesssagePublisher extends Thread {
    private static final int MAX_UDP_SIZE = 65536;
    
    public MulticastChannelMesssagePublisher(String mCastAddr, int mCastPort) throws IOException {
        _mCastSocket = new MulticastSocket(mCastPort);
        _mCastSocket.joinGroup(InetAddress.getByName(mCastAddr));
    }
    
    public MulticastChannelMesssagePublisher(String mCastAddr, int mCastPort, int packetSize) throws IOException {
        _packetSize = packetSize;
        _mCastSocket = new MulticastSocket(mCastPort);
        _mCastSocket.joinGroup(InetAddress.getByName(mCastAddr));
    }
    
    @Override
    public void run() {
        byte[] buffer = new byte[_packetSize];
        DatagramPacket dp = new DatagramPacket(buffer, _packetSize);
        
        while (true) {
            try {
                _mCastSocket.receive(dp);
                _subject.onNext(dp.getData());
            } catch (IOException e) {
            }
        }
    }    
    
    public Observable<Message> getObservable() {
        return _observable;
    }
    
    private PublishSubject<byte[]> _subject = PublishSubject.create();
    private Observable<Message> _observable = _subject
            .observeOn(Schedulers.io())
            .map(new Func1<byte[], Message>() {
                @Override
                public Message call(byte[] arg0) {
                    try {
                        return Message.fromByteArray(arg0);
                    } catch (IOException e) {
                        return null;
                    }
                }
            }).filter(new Func1<Message, Boolean>() {
                @Override
                public Boolean call(Message arg0) {
                    return arg0 != null;
                }
            });
    
    private MulticastSocket _mCastSocket;
    private int _packetSize = MAX_UDP_SIZE;
}
