package pt.up.fe.sdis.proj1.protocols.peers;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import pt.up.fe.sdis.proj1.messages.Message;
import pt.up.fe.sdis.proj1.protocols.AbstractProtocol;
import pt.up.fe.sdis.proj1.utils.BackupSystem;
import pt.up.fe.sdis.proj1.utils.Communicator;
import pt.up.fe.sdis.proj1.utils.MyFile;
import rx.Scheduler;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class PeerChunkBackup extends AbstractProtocol {

    public PeerChunkBackup(BackupSystem bs) {
        super(bs.Comm.MDB.Publisher);
        _comm = bs.Comm;
        this.start(new Func1<Message, Boolean>() {

            @Override
            public Boolean call(Message arg0) {
                return arg0.type == Message.Type.PUTCHUNK;
            }
        });
    }

    @Override
    protected void ProcessMessage(final Message msg) {
        System.out.println("Received : " + msg.getChunkNo());

        MyFile.WriteChunk(msg);

        Schedulers.io().schedule(new Action1<Scheduler.Inner>() {
            @Override
            public void call(Scheduler.Inner arg0) {
                _comm.MC.Sender.Send(Message.makeStored(msg.getFileID(),
                        msg.getChunkNo()));
            }
        }, rand.nextInt(401), TimeUnit.MILLISECONDS);
    }

    Communicator _comm;

    private static Random rand = new Random();
}
