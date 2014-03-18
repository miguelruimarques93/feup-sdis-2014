package pt.up.fe.sdis.proj1.protocols;

import pt.up.fe.sdis.proj1.Chunk;
import pt.up.fe.sdis.proj1.messages.Message;
import pt.up.fe.sdis.proj1.utils.Communicator;
import rx.Observable;
import rx.functions.Func1;
import rx.subjects.AsyncSubject;

public class ChunkRestore extends AbstractProtocol {
    public ChunkRestore(final Communicator comm, final byte[] fileID,
            final int chunkNo) {
        super(comm.MDR.Publisher);

        Message msg = Message.getChunk(fileID, chunkNo);

        comm.MC.Sender.Send(msg);

        this.start(new Func1<Message, Boolean>() {
            @Override
            public Boolean call(Message arg0) {
                return arg0.type == Message.Type.CHUNK
                        && arg0.getFileID().equals(fileID)
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
