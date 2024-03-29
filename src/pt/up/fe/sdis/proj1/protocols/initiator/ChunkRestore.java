package pt.up.fe.sdis.proj1.protocols.initiator;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

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

        final Message msg = Message.makeGetChunk(fileID, chunkNo, bs.getProtocolVersion());
        bs.Comm.MC.Sender.Send(msg);
        start(new MessageFilter(Message.Type.CHUNK, fileID, chunkNo));
        bs.Comm.MC.Publisher.getObservable().filter(new MessageFilter(Message.Type.HAVECHUNK, fileID, chunkNo)).subscribe(this);
        
        Schedulers.io().schedule(new Action1<Scheduler.Inner>(){
            int NumTimes = 0;
            int Time = 2;
            
            @Override
            public void call(Inner t1) {
                NumTimes++;
                
                if (_waiting.get())
                    try { _waiting.wait(); } catch (InterruptedException e) { }
                
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

    private AtomicBoolean _waiting = new AtomicBoolean(false);
    
    @Override
    public void ProcessMessage(Message msg) {
        switch(msg.type){
        case CHUNK:
            _bs.writeChunk(msg);
            _sub.onCompleted();
            finish();
            break;
        case HAVECHUNK:
            InetAddress addr = _bs.getAddress();
            Message listeningMsg = null;
            try {
            listeningMsg = Message.makeListeningFor(msg.getFileID(), msg.getChunkNo(), _bs.getRestorePort(), msg.getUniqueID());
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
            if (addr == null)
                return;
            
            boolean success = true;
            _waiting.set(true);
            DatagramSocket ds = null;
            try {
                ds = new DatagramSocket(_bs.getRestorePort());
                ds.setSoTimeout(1000);
                byte[] buffer = new byte[65536];
                DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
                _bs.Comm.MC.Sender.Send(listeningMsg);
                ds.receive(dp);
                Message chunkMsg = Message.fromByteArray(Arrays.copyOf(dp.getData(), dp.getLength()));
                _bs.writeChunk(chunkMsg);
            } catch (SocketTimeoutException e) {
                
            } catch (IOException e) {
                e.printStackTrace();
                success = false;
            } finally {
                if (ds != null) 
                    ds.close();
            }
            if (success) {
                _sub.onCompleted();
                finish();
            }
            
            _waiting.notifyAll();
            break;
        default:
            break;
        }
    }

    public Observable<Object> getObservable() {
        return _sub.asObservable();
    }

    private AsyncSubject<Object> _sub = AsyncSubject.create();
    private BackupSystem _bs;
}
