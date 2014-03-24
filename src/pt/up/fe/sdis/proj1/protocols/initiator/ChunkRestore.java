package pt.up.fe.sdis.proj1.protocols.initiator;

import pt.up.fe.sdis.proj1.Chunk;
import pt.up.fe.sdis.proj1.messages.Message;
import pt.up.fe.sdis.proj1.protocols.AbstractProtocol;
import pt.up.fe.sdis.proj1.utils.BackupSystem;
import pt.up.fe.sdis.proj1.utils.FileID;
import pt.up.fe.sdis.proj1.utils.MessageFilter;
import pt.up.fe.sdis.proj1.utils.MyFile;
import rx.Observable;
import rx.subjects.AsyncSubject;

public class ChunkRestore extends AbstractProtocol {
    public ChunkRestore(final BackupSystem bs, final byte[] fileID,
            final int chunkNo) {
        super(bs.Comm.MDR.Publisher);

        Message msg = Message.makeGetChunk(fileID, chunkNo);

        bs.Comm.MC.Sender.Send(msg);

        start(new MessageFilter(Message.Type.CHUNK, new FileID(fileID), chunkNo));
    }

    @Override
    public void ProcessMessage(Message msg) {
        Chunk c = new Chunk(msg.getChunkNo(), 0, msg.getFileID(), msg.getBody());
        MyFile.WriteChunk(msg);
        _sub.onNext(c);
        _sub.onCompleted();
        finish();
    }

    public Observable<Chunk> getObservable() {
        return _sub.asObservable();
    }

    private AsyncSubject<Chunk> _sub = AsyncSubject.create();
}
