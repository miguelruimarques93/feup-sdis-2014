package pt.up.fe.sdis.proj1.protocols.initiator;

import java.util.Arrays;

import pt.up.fe.sdis.proj1.Chunk;
import pt.up.fe.sdis.proj1.messages.Message;
import pt.up.fe.sdis.proj1.protocols.AbstractProtocol;
import pt.up.fe.sdis.proj1.utils.BackupSystem;
import rx.Observable;
import rx.functions.Func1;
import rx.subjects.AsyncSubject;

public class ChunkRestore extends AbstractProtocol {
    public ChunkRestore(final BackupSystem bs, final byte[] fileID,
            final int chunkNo) {
        super(bs.Comm.MDR.Publisher);

        Message msg = Message.makeGetChunk(fileID, chunkNo);

        bs.Comm.MC.Sender.Send(msg);

        this.start(new Func1<Message, Boolean>() {
            @Override
            public Boolean call(Message arg0) {
                return arg0.type == Message.Type.CHUNK
                        && Arrays.equals(arg0.getFileID(), fileID)
                        && arg0.getChunkNo().equals(chunkNo);
            }
        });
    }

    @Override
    public void ProcessMessage(Message msg) {
        Chunk c = new Chunk(msg.getChunkNo(), 0, msg.getFileID(), msg.getBody());
        _sub.onNext(c);
        _sub.onCompleted();
        finish();
    }

    public Observable<Chunk> getObservable() {
        return _sub.asObservable();
    }

    private AsyncSubject<Chunk> _sub = AsyncSubject.create();
}
