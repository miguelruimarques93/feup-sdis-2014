package pt.up.fe.sdis.proj1;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.FileAlreadyExistsException;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import pt.up.fe.sdis.proj1.config.BackupSystemConfiguration;
import pt.up.fe.sdis.proj1.fileInfo.ChunkInfo;
import pt.up.fe.sdis.proj1.fileInfo.Files;
import pt.up.fe.sdis.proj1.messages.Message;
import pt.up.fe.sdis.proj1.protocols.initiator.ChunkBackup;
import pt.up.fe.sdis.proj1.protocols.initiator.FileBackup;
import pt.up.fe.sdis.proj1.protocols.initiator.FileDeletion;
import pt.up.fe.sdis.proj1.protocols.initiator.FileRestore;
import pt.up.fe.sdis.proj1.protocols.initiator.SpaceReclaiming;
import pt.up.fe.sdis.proj1.protocols.peers.PeerChunkBackup;
import pt.up.fe.sdis.proj1.protocols.peers.PeerChunkRestore;
import pt.up.fe.sdis.proj1.protocols.peers.PeerFileDeletion;
import pt.up.fe.sdis.proj1.protocols.peers.PeerIsDeletedProtocol;
import pt.up.fe.sdis.proj1.protocols.peers.PeerSpaceReclaiming;
import pt.up.fe.sdis.proj1.protocols.peers.PeerStored;
import pt.up.fe.sdis.proj1.utils.Communicator;
import pt.up.fe.sdis.proj1.utils.FileID;
import pt.up.fe.sdis.proj1.utils.FileSystemUtils;
import pt.up.fe.sdis.proj1.utils.MyFile;
import rx.Scheduler;
import rx.Scheduler.Inner;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import com.sun.istack.internal.NotNull;

public class BackupSystem {
    public BackupSystem(BackupSystemConfiguration configs, InetAddress intf) throws IOException {
        this(configs, intf.toString().substring(1));
    }

    public BackupSystem(BackupSystemConfiguration configs, String myAddr) throws IOException {  
        _systemConfigs = configs;
        
        Comm = new Communicator(_systemConfigs.getMC(), _systemConfigs.getMDB(), _systemConfigs.getMDR(), myAddr);
        Files = new Files(getDatabaseFilePath());
        
        _usedSpace = FileSystemUtils.fileSize(getBackupsDir());
        _addr = myAddr;
        initializePeerProtocols();

    }

    private static final String BackupsFolder = "backups";
    private static final String RestoresFolder = "restores";
    private static final String DatabaseFileName = "database.db";
    
    public String getBackupsDir() { 
        return getWorkingDir() + File.separator + BackupsFolder;
    }
    
    public String getRestoresDir() { 
        return getWorkingDir() + File.separator + RestoresFolder;
    }
    
    private String getDatabaseFilePath() {
        return getWorkingDir() + File.separator + DatabaseFileName;
    }
    
    public void shutdown() {
        shutdownPeerProtocols();
        Files.dispose();
    }

    public final Communicator Comm;

    public final Files Files;

    public interface BackupFileListener {
        public void FileVersionAdded(FileVersion filePath);

        public void FileVersionRemoved(FileVersion filePath);
    }

    private void initializePeerProtocols() {
        _chunkBackup = new PeerChunkBackup(this);
        _chunkRestore = new PeerChunkRestore(this);
        _fileDeletion = new PeerFileDeletion(this);
        _spaceReclaiming = new PeerSpaceReclaiming(this);
        _stored = new PeerStored(this);
        _isDeletedProtocol = new PeerIsDeletedProtocol(this);

        Schedulers.newThread().schedulePeriodically(new Action1<Scheduler.Inner>() {
            @Override
            public void call(Scheduler.Inner t1) {
                PriorityQueue<ChunkInfo> chunks = Files.getChunksWithLowRd();
                while (!chunks.isEmpty()) {
                    ChunkInfo info = chunks.remove();
                    try {
                        new ChunkBackup(BackupSystem.this, new Chunk(info.getChunkNo(), info.getDesiredRD(), info.getFileId(), readChunk(info.getFileId(),
                                info.getChunkNo())));
                    } catch (FileNotFoundException e) {
                        Files.removeChunk(info.getFileId(), info.getChunkNo());
                        Comm.MC.Sender.Send(Message.makeRemoved(info.getFileId(), info.getChunkNo()));
                    } catch (IOException e) {
                    }
                }

            }
        }, 1, 1, TimeUnit.MINUTES);

        Schedulers.newThread().schedule(new Action1<Scheduler.Inner>() {

            @Override
            public void call(Inner t1) {
                List<FileID> files = Files.getFiles();
                for (FileID f : files) 
                    Comm.MC.Sender.Send(Message.makeIsDeleted(f));
            }
            
        });
        
        Schedulers.newThread().schedule(new Action1<Scheduler.Inner>() {

            @Override
            public void call(Inner t1) {
                List<FileID> files = Files.getRemovedFiles();
                for (FileID f : files) 
                    Comm.MC.Sender.Send(Message.makeDelete(f));
            }
            
        });
        
    }

    private void shutdownPeerProtocols() {
        _chunkBackup.finish();
        _chunkRestore.finish();
        _fileDeletion.finish();
        _spaceReclaiming.finish();
        _stored.finish();
        _isDeletedProtocol.finish();
    }

    public void writeChunk(Message msg) {
        String filePath = (msg.type == Message.Type.PUTCHUNK ? getBackupsDir() : getRestoresDir()) + File.separator + msg.getHexFileID() + File.separator + msg.getChunkNo().toString();
        long writtenSize = FileSystemUtils.WriteByteArray(filePath, msg.getBody());
        _usedSpace += writtenSize;
    }

    public void deleteChunk(FileID fileId, @NotNull Integer chunkNo) {
        String filePath = getBackupsDir() + File.separator + fileId.toString() + File.separator + chunkNo.toString();
        File file = new File(filePath).getAbsoluteFile();
        long fileSize = FileSystemUtils.fileSize(file);
        FileSystemUtils.deleteFile(file);
        Files.removeChunk(fileId, chunkNo);
        _usedSpace -= fileSize;
    }

    public void deletePhysicalFile(FileID fileId) {
        String dirPath = getBackupsDir() + File.separator + fileId.toString();
        File dir = new File(dirPath);
        long dirSize = FileSystemUtils.fileSize(dir);
        FileSystemUtils.deleteFile(dir);
        Files.removeFile(fileId);
        _usedSpace -= dirSize;
    }

    public FileBackup backupFile(File file, int replicationDegree) {
        try {
            return new FileBackup(this, new MyFile(_addr, file.getAbsolutePath()), replicationDegree);
        } catch (FileAlreadyExistsException e) {
            return null;
        } catch (IOException e) {
            return null;
        }
    }

    public FileBackup backupFile(File file) {
        return backupFile(file, getDefaultReplicationDegree());
    }

    public FileRestore restoreFile(String filepath, String destpath, Long modificationMillis) {
        try {
            return new FileRestore(this, filepath, destpath, modificationMillis);
        } catch (IOException e) {
            return null;
        }
    }

    public FileDeletion deleteFile(String filePath, Long modificationMillis) {
        return new FileDeletion(this, filePath, modificationMillis);
    }

    public long getUsedSpace() {
        return _usedSpace;
    }

    public void setTotalSpace(long totalSpace) {
        _systemConfigs.setAvailableSpace(totalSpace);
        if (getAvailableSpace() < 0) {
            new SpaceReclaiming(this, false);
        }
    }

    public void setDefaultReplicationDegree(int value) {
        _systemConfigs.setDefaultReplicationDeegree(value);
    }

    public int getDefaultReplicationDegree() {
        return _systemConfigs.getDefaultReplicationDeegree();
    }

    public long getTotalSpace() {
        return _systemConfigs.getAvailableSpace();
    }

    public long getAvailableSpace() {
        return getTotalSpace() - _usedSpace;
    }
    
    public byte[] readChunk(FileID fileId, Integer chunkNo) throws IOException {
        File f = new File(getBackupsDir() + File.separator + fileId.toString() + File.separator + chunkNo.toString());
        if (!f.exists()) 
            throw new FileNotFoundException("");
        
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(f));
        byte[] chunk = new byte[(int)f.length()];
        
        bis.read(chunk);        
        
        bis.close();
        return chunk;
    }
    
    public String getWorkingDir() {
        return _systemConfigs.getWorkingDir();
    }

    public void commitSettings() {
        _systemConfigs.save();
    }
    
    private PeerChunkBackup _chunkBackup;
    private PeerChunkRestore _chunkRestore;
    private PeerFileDeletion _fileDeletion;
    private PeerSpaceReclaiming _spaceReclaiming;
    private PeerStored _stored;
    private PeerIsDeletedProtocol _isDeletedProtocol;
    private String _addr;
    private long _usedSpace;

    public static final Logger Log = Logger.getLogger(BackupSystem.class.getName());

    public int getRestorePort() {
        return _systemConfigs.getRestorePort();
    }

    public void setRestorePort(int restorePort) {
        _systemConfigs.setRestorePort(restorePort);
    }

    public InetAddress getAddress() {
        try {
            return InetAddress.getByName(_addr);
        } catch (UnknownHostException e) {
            return null;
        }
    }

    public void setProtocolVersion(int protocolVersion) {
        _systemConfigs.setProtocolVersion(protocolVersion);
    }

    public int getProtocolVersion() {
        return _systemConfigs.getProtocolVersion();
    }
    
    private BackupSystemConfiguration _systemConfigs;
}
