package pt.up.fe.sdis.proj1.utils;

import java.io.IOException;

public class Channel {

    public Channel(String addr, int port) throws IOException {
        Publisher = new MulticastChannelMesssagePublisher(addr, port);
        Sender = new MulticastChannelMessageSender(addr, port);
        Publisher.start();
    }
    
    public Channel(String addr, int port, String myAddr) throws IOException {
        Publisher = new MulticastChannelMesssagePublisher(addr, port, myAddr);
        Sender = new MulticastChannelMessageSender(addr, port, myAddr);
        Publisher.start();
    }
    
    public final MulticastChannelMesssagePublisher Publisher;
    public final MulticastChannelMessageSender Sender;
    
}
