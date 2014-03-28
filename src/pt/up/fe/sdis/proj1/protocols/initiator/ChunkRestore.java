package pt.up.fe.sdis.proj1.protocols.initiator;

import java.util.concurrent.TimeUnit;

import pt.up.fe.sdis.proj1.BackupSystem;
import pt.up.fe.sdis.proj1.messages.Message;
import pt.up.fe.sdis.proj1.protocols.AbstractProtocol;
import pt.up.fe.sdis.proj1.utils.FileID;
import pt.up.fe.sdis.proj1.utils.MessageFilter;
import rx.Observable;
import rx.Scheduler;
import rx.Scheduler.Inner;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subjects.AsyncSubject;

public class ChunkRestore extends AbstractProtocol {
    public ChunkRestore(final BackupSystem bs, final FileID fileID,
            final int chunkNo) {
        super(bs.Comm.MDR.Publisher);
        
        _bs = bs;

        final Message msg = Message.makeGetChunk(fileID, chunkNo);
        bs.Comm.MC.Sender.Send(msg);
        start(new MessageFilter(Message.Type.CHUNK, fileID, chunkNo));
        
        Schedulers.io().schedule(new Action1<Scheduler.Inner>(){
            int NumTimes = 0;
            int Time = 2;
            
            @Override
            public void call(Inner t1) {
                NumTimes++;
                
                if (!isFinished()) {
                    if (NumTimes >= 3) {
                        finish();
                        _sub.onError(new Error("Timeout: " + fileID + " " + chunkNo));
                    } else {
                        Time += 2;
                        bs.Comm.MC.Sender.Send(msg);
                        t1.schedule(this, Time, TimeUnit.SECONDS);
                    }
                }
            }
        }, 2, TimeUnit.SECONDS);
    }

    @Override
    public void ProcessMessage(Message msg) {
        _bs.writeChunk(msg);
        _sub.onCompleted();
        finish();
    }

    public Observable<Object> getObservable() {
        return _sub.asObservable();
    }

    private AsyncSubject<Object> _sub = AsyncSubject.create();
    private BackupSystem _bs;
}
