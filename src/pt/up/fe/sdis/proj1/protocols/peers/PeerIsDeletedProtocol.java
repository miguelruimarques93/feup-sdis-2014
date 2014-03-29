package pt.up.fe.sdis.proj1.protocols.peers;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import pt.up.fe.sdis.proj1.BackupSystem;
import pt.up.fe.sdis.proj1.messages.Message;
import pt.up.fe.sdis.proj1.protocols.AbstractProtocol;
import pt.up.fe.sdis.proj1.utils.MessageFilter;
import rx.Scheduler;
import rx.Scheduler.Inner;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class PeerIsDeletedProtocol extends AbstractProtocol {

    public PeerIsDeletedProtocol(BackupSystem bs) {
        super(bs.Comm.MC.Publisher);
        _bs = bs;
        start(new MessageFilter(Message.Type.ISDELETED));
    }

    @Override
    protected void ProcessMessage(final Message msg) {
        if (_bs.Files.containsRemovedFile(msg.getFileID())) {
            Schedulers.newThread().schedule(new Action1<Scheduler.Inner>() {

                @Override
                public void call(Inner t1) {
                    _bs.Comm.MC.Sender.Send(Message.makeDelete(msg.getFileID()));
                }
                
            }, rand.nextInt(401), TimeUnit.MILLISECONDS);
        }
    }
    
    private BackupSystem _bs;
    private static Random rand = new Random();

}
