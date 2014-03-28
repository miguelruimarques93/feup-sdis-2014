package pt.up.fe.sdis.proj1.protocols.peers;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import pt.up.fe.sdis.proj1.messages.Message;
import pt.up.fe.sdis.proj1.protocols.AbstractProtocol;
import pt.up.fe.sdis.proj1.protocols.initiator.SpaceReclaiming;
import pt.up.fe.sdis.proj1.utils.BackupSystem;
import pt.up.fe.sdis.proj1.utils.Communicator;
import pt.up.fe.sdis.proj1.utils.MessageFilter;
import rx.Scheduler;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class PeerChunkBackup extends AbstractProtocol {

    public PeerChunkBackup(BackupSystem bs) {
        super(bs.Comm.MDB.Publisher);
        _comm = bs.Comm;
        _bs = bs;

        start(new MessageFilter(Message.Type.PUTCHUNK));
    }

    @Override
    protected void ProcessMessage(final Message msg) {
        if(_bs.getAvailableSpace() < msg.getBody().length)
            new SpaceReclaiming(_bs, true);

        if(_bs.getAvailableSpace() >= msg.getBody().length){
            Schedulers.io().schedule(new Action1<Scheduler.Inner>() {
                @Override
                public void call(Scheduler.Inner arg0) {
                    _comm.MC.Sender.Send(Message.makeStored(msg.getFileID(), msg.getChunkNo()));
                }
            }, rand.nextInt(401), TimeUnit.MILLISECONDS);
            
            _bs.writeChunk(msg);

            _bs.Files.addChunk(msg.getFileID(), msg.getChunkNo(), msg.getReplicationDeg());
        }
    }

    Communicator _comm;
    BackupSystem _bs;
    private static Random rand = new Random();
}
