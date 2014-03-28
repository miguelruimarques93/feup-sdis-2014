package pt.up.fe.sdis.proj1.protocols.initiator;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;

import pt.up.fe.sdis.proj1.BackupSystem;
import pt.up.fe.sdis.proj1.Chunk;
import pt.up.fe.sdis.proj1.FileVersion;
import pt.up.fe.sdis.proj1.utils.MyFile;
import rx.Observable;
import rx.Observer;
import rx.subjects.PublishSubject;

public class FileBackup implements Observer<Object> {
    public FileBackup(final BackupSystem bs, final MyFile file, int replicationDegree) throws FileAlreadyExistsException {
        if (replicationDegree < 1 || replicationDegree > 9)
            throw new IllegalArgumentException("Replication Degree must be between 1 and 9.");

        _bs = bs;
        _replicationDegree = replicationDegree;
        _file = file;
        
        System.out.println(new FileVersion(file.getPath(), file.getLastModifiedDate()).toString());
        
        if (_bs.Files.containsOwnFile(file.getPath(), file.getLastModifiedDate()))
            throw new FileAlreadyExistsException(new FileVersion(file.getPath(), file.getLastModifiedDate()).toString());
        
        
        ps.subscribe(new Observer<Double>() {

            @Override
            public void onCompleted() {
                bs.Files.addOwnFile(file);
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onNext(Double t) {
            } 
            
        });
    }
    
    public void Send() {
        try {
            _numChunks = _file.getNumberOfChunks();
            int numChunksInitiallySent = Math.min(1, _numChunks);
            _numChunksToBeSent = _numChunks - numChunksInitiallySent;
            _file.open();
            for (int i = 0; i < numChunksInitiallySent; ++i) {
                System.out.println("Sending chunk " + i);
                byte[] chunkArray = _file.getChunk(i);
                Chunk chunk = new Chunk(i, _replicationDegree, _file.getFileId(), chunkArray);
                new ChunkBackup(_bs, chunk).getObservable().subscribe(this);
            }
        } catch (IOException e) {
            new FileDeletion(_bs, _file.getPath(), _file.getLastModifiedDate());
            ps.onError(e);
        }
    }

    @Override
    public void onCompleted() {
        _numChunksSent++;
        ps.onNext(_numChunksSent / (double)_numChunks);
        if (_numChunks == _numChunksSent) {
            ps.onCompleted();
            _file.close();
        }
        else if (_numChunksToBeSent > 0) {
            int i;
            synchronized (_numChunksToBeSent) {
                i = _numChunks - _numChunksToBeSent;
                --_numChunksToBeSent;
            }
            
            try {
                byte[] chunkArray;
                chunkArray = _file.getChunk(i);
                Chunk chunk = new Chunk(i, _replicationDegree, _file.getFileId(), chunkArray);
                System.out.println("Sending chunk " + i);
                new ChunkBackup(_bs, chunk).getObservable().subscribe(this);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onError(Throwable e) {
        new FileDeletion(_bs, _file.getPath(), _file.getLastModifiedDate());
        ps.onError(e);
    }

    @Override
    public void onNext(Object t) { }
    
    public Observable<Double> getProgressionObservable() { return ps.asObservable(); }
    
    private int _numChunks;
    private Integer _numChunksToBeSent;
    private int _numChunksSent = 0;
    private BackupSystem _bs;
    private MyFile _file;
    private int _replicationDegree;
    
    PublishSubject<Double> ps = PublishSubject.create();
}
