package pt.up.fe.sdis.proj1.utils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Arrays;

import pt.up.fe.sdis.proj1.messages.Message;
import rx.Observable;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

public class MulticastChannelMesssagePublisher extends Thread {
    private static final int MAX_UDP_SIZE = 65536;

    public MulticastChannelMesssagePublisher(String mCastAddr, int mCastPort)
            throws IOException {
        _mCastSocket = new MulticastSocket(mCastPort);
        _mCastSocket.joinGroup(InetAddress.getByName(mCastAddr));
    }

    public MulticastChannelMesssagePublisher(String mCastAddr, int mCastPort,
            int packetSize) throws IOException {
        _packetSize = packetSize;
        _mCastSocket = new MulticastSocket(mCastPort);
        _mCastSocket.joinGroup(InetAddress.getByName(mCastAddr));
    }
    
    public MulticastChannelMesssagePublisher(String mCastAddr, int mCastPort, String myAddr)
            throws IOException {
        _mCastSocket = new MulticastSocket(mCastPort);
        _mCastSocket.joinGroup(InetAddress.getByName(mCastAddr));
        _mCastSocket.setInterface(InetAddress.getByName(myAddr));
    }

    public MulticastChannelMesssagePublisher(String mCastAddr, int mCastPort, String myAddr,
            int packetSize) throws IOException {
        _packetSize = packetSize;
        _mCastSocket = new MulticastSocket(mCastPort);
        _mCastSocket.joinGroup(InetAddress.getByName(mCastAddr));
        _mCastSocket.setInterface(InetAddress.getByName(myAddr));
    }

    @Override
    public void run() {
        byte[] buffer = new byte[_packetSize];
        DatagramPacket dp = new DatagramPacket(buffer, _packetSize);
        while (true) {
            buffer = new byte[_packetSize];
            try {
                _mCastSocket.receive(dp);
                _subject.onNext(Pair.make_pair(Arrays.copyOf(dp.getData(), dp.getLength()), dp.getAddress()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public Observable<Message> getObservable() {
        return _observable;
    }

    private PublishSubject<Pair<byte[], InetAddress>> _subject = PublishSubject.create();
    private Observable<Message> _observable = _subject
            .observeOn(Schedulers.io()).map(new Func1<Pair<byte[], InetAddress>, Message>() {
                @Override
                public Message call(Pair<byte[], InetAddress> arg0) {
                    try {
                        Message result = Message.fromByteArray(arg0.first);
                        result.Sender = arg0.second;
                        return result;
                    } catch (Exception e) {
                        return null;
                    }
                }
            }).filter(new Func1<Message, Boolean>() {
                @Override
                public Boolean call(Message arg0) {
                    return arg0 != null;
                }
            }).observeOn(Schedulers.computation());

    private MulticastSocket _mCastSocket;
    private int _packetSize = MAX_UDP_SIZE;
}
