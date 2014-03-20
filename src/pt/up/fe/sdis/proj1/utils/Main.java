package pt.up.fe.sdis.proj1.utils;

import java.io.IOException;
import java.util.Arrays;

import pt.up.fe.sdis.proj1.Chunk;
import pt.up.fe.sdis.proj1.messages.Message;
import rx.Observer;

public class Main {

    public static void main(String[] args) throws IOException {
        Channel channel = new Channel("239.255.0.1", 11099, "192.168.0.197");
        
        channel.Publisher.getObservable().subscribe(new Observer<Message>() {
            long i = 1L;

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
                        + j + " : " + arg0.type + " from " + arg0.Sender);
            }
        });
        
        byte[] fileId = new byte[]{ 0x4a, 0x2f, 0x3e, 0x4a, 0x4e, 0x43, 0x34, 0x33, 0x33, 0x34, 0x4e, 0x1c, 0x04, 0x1c, 0x04, 0x1c, 0x55, 0x4e, 0x38, 0x2d, 0x00, 0x6f, 0x1c, 0x04, 0x1c, 0x3c, 0x55, 0x56, 0x50, 0x53, 0x70,0x1a };
        byte[] body = new byte[64000];
        Arrays.fill(body, (byte)10);
        
        Chunk c = new Chunk(4526, 
                3, 
                fileId, 
                body);
        
        Message msg = Message.makePutChunk(c);
        Message msg1 = Message.makeChunk(c);
        
        long i = 0;
        
        for (i = 0; i < 100; ++i) {
            channel.Sender.Send(msg);
            channel.Sender.Send(msg1);
        }
        
        System.out.println(i * 2L);
    }

}
