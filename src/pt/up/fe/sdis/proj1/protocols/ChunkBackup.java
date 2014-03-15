package pt.up.fe.sdis.proj1.protocols;

import pt.up.fe.sdis.proj1.messages.Message;
import pt.up.fe.sdis.proj1.utils.MulticastChannelMesssagePublisher;
import rx.functions.Func1;

public class ChunkBackup extends AbstractProtocol {
    public ChunkBackup(MulticastChannelMesssagePublisher mcmp) {
        super(mcmp, new Func1<Message, Boolean>() {
            @Override
            public Boolean call(Message arg0) { return arg0.type == Message.Type.STORED; }
        });
    }

    @Override
    public void ProcessMessage(Message msg) {
        // TODO Auto-generated method stub
        
    }
}
