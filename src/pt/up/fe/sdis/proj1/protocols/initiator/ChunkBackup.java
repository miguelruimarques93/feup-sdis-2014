package pt.up.fe.sdis.proj1.protocols.initiator;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import pt.up.fe.sdis.proj1.Chunk;
import pt.up.fe.sdis.proj1.messages.Message;
import pt.up.fe.sdis.proj1.protocols.AbstractProtocol;
import pt.up.fe.sdis.proj1.utils.BackupSystem;
import pt.up.fe.sdis.proj1.utils.FileID;
import pt.up.fe.sdis.proj1.utils.MessageFilter;
import rx.Observable;
import rx.Scheduler;
import rx.Scheduler.Inner;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subjects.AsyncSubject;

public class ChunkBackup extends AbstractProtocol {
    
    public class ChunkBackupException extends IOException {
        private static final long serialVersionUID = 1L;
        
        public ChunkBackupException(FileID fileId, Integer chunkNo) {
            super("Timeout sending chunk '" + chunkNo + "' of file '" + fileId + "'");
        }
    }
    
    public ChunkBackup(final BackupSystem bs, final Chunk chunk) {
        super(bs.Comm.MC.Publisher);
        final Message msg = Message.makePutChunk(chunk);

        start(new MessageFilter(Message.Type.STORED, chunk.fileID, chunk.chunkNo));

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
                            resultPublisher.onError(new ChunkBackupException(chunk.fileID, chunk.chunkNo));
                            finish();
                        } else {
                            _repliers.clear();
                            TimeInterval *= 2;
                            bs.Comm.MDB.Sender.Send(msg);
                            arg0.schedule(this, TimeInterval,
                                    TimeUnit.MILLISECONDS);
                        }
                    } else {
                        resultPublisher.onCompleted();
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
            _repliers.add(msg.Sender);
        }
    }

    private AsyncSubject<Object> resultPublisher = AsyncSubject.create();
    
    public Observable<Object> getObservable() { return resultPublisher.asObservable(); }
    
    private Set<InetAddress> _repliers = new HashSet<InetAddress>();
}
