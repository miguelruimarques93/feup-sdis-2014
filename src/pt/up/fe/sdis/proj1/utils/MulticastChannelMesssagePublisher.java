package pt.up.fe.sdis.proj1.utils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import pt.up.fe.sdis.proj1.messages.Message;
import rx.Observable;
import rx.Observer;
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

    @Override
    public void run() {
        byte[] buffer = new byte[_packetSize];
        DatagramPacket dp = new DatagramPacket(buffer, _packetSize);
        while (true) {
            buffer = new byte[_packetSize];
            try {
                _mCastSocket.receive(dp);
                // System.out.println("Something received from " + dp.getAddress());
                _subject.onNext(dp.getData().clone());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public Observable<Message> getObservable() {
        return _observable;
    }

    private PublishSubject<byte[]> _subject = PublishSubject.create();
    private Observable<Message> _observable = _subject
            .observeOn(Schedulers.io()).map(new Func1<byte[], Message>() {
                @Override
                public Message call(byte[] arg0) {
                    try {
                        Message result = Message.fromByteArray(arg0);
                        // System.out.println("Message received");
                        return result;
                    } catch (Exception e) {
                        // e.printStackTrace();
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

    public static void main(String[] args) {
        String addr = "239.255.0.1";
        int port = 11099;

        
        try {
            MulticastChannelMesssagePublisher mcmp = new MulticastChannelMesssagePublisher(
                    addr, port);

            mcmp.start();

            mcmp.getObservable()/*.filter(new Func1<Message, Boolean>() {

                @Override
                public Boolean call(Message arg0) {
                    return arg0.type == Message.Type.CHUNK;
                }
            })*/.subscribe(new Observer<Message>() {

                long i = 0L;

                @Override
                public void onCompleted() {
                    System.out.println("Done");
                }

                @Override
                public void onError(Throwable arg0) {
                    arg0.printStackTrace();
                }

                @Override
                public void onNext(Message arg0) {
                    long j = i++;
                    System.out.println(Thread.currentThread().getId() + " : "
                            + j + " : " + arg0.type);
                }
            });
            
            /*mcmp.getObservable().filter(new Func1<Message, Boolean>() {

                @Override
                public Boolean call(Message arg0) {
                    return arg0.type == Message.Type.PUTCHUNK;
                }
            }).subscribe(new Observer<Message>() {

                long i = 0L;

                @Override
                public void onCompleted() {
                    System.out.println("Done");
                }

                @Override
                public void onError(Throwable arg0) {
                    arg0.printStackTrace();
                }

                @Override
                public void onNext(Message arg0) {
                    System.out.println(Thread.currentThread().getId() + " : "
                            + i++ + " : " + arg0.type);
                }
            });*/
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
