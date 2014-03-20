package pt.up.fe.sdis.proj1.protocols.initiator;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import pt.up.fe.sdis.proj1.Chunk;
import pt.up.fe.sdis.proj1.messages.Message;
import pt.up.fe.sdis.proj1.protocols.AbstractProtocol;
import pt.up.fe.sdis.proj1.utils.BackupSystem;
import rx.Scheduler;
import rx.Scheduler.Inner;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class ChunkBackup extends AbstractProtocol {
    private Chunk _chunk;
    
    public ChunkBackup(final BackupSystem bs, final Chunk chunk) {
        super(bs.Comm.MC.Publisher);
        _chunk = chunk;
        final Message msg = Message.makePutChunk(chunk);

        start(new Func1<Message, Boolean>() {
            @Override
            public Boolean call(Message arg0) {
                return arg0.type == Message.Type.STORED && Arrays.equals(msg.getFileID(), _chunk.fileID) && msg.getChunkNo().equals(_chunk.chunkNo);
            }
        });

        bs.Comm.MDB.Sender.Send(msg);

        Action1<Scheduler.Inner> act = new Action1<Scheduler.Inner>() {
            int TimeInterval = 500;
            int NumTimes = 0;

            @Override
            public void call(Inner arg0) {
                synchronized (_repliers) {
                    NumTimes++;

                    int numRepliers = _repliers.size();

                    if (numRepliers < chunk.replicationDeg) {
                        if (NumTimes == 5) {
                            // TODO replace with some kind of warning
                            finish();
                        } else {
                            _repliers.clear();
                            TimeInterval *= 2;
                            bs.Comm.MDB.Sender.Send(msg);
                            arg0.schedule(this, TimeInterval,
                                    TimeUnit.MILLISECONDS);
                        }
                    } else {
                        finish();
                    }
                }
            }
        };

        Schedulers.newThread().schedule(act, 500, TimeUnit.MILLISECONDS);
    }

    @Override
    protected void ProcessMessage(Message msg) {
        synchronized (_repliers) {
            System.err.println("STORED Received.");
            _repliers.add(msg.Sender);
        }
    }

    private Set<InetAddress> _repliers = new HashSet<InetAddress>();
}
