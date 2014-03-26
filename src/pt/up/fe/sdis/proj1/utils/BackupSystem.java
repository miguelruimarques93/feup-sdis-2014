package pt.up.fe.sdis.proj1.utils;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import pt.up.fe.sdis.proj1.messages.Message;
import pt.up.fe.sdis.proj1.protocols.initiator.FileBackup;
import pt.up.fe.sdis.proj1.protocols.peers.PeerChunkBackup;
import pt.up.fe.sdis.proj1.protocols.peers.PeerChunkRestore;
import pt.up.fe.sdis.proj1.protocols.peers.PeerFileDeletion;
import pt.up.fe.sdis.proj1.protocols.peers.PeerSpaceReclaiming;
import pt.up.fe.sdis.proj1.protocols.peers.PeerStored;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteJob;
import com.almworks.sqlite4java.SQLiteQueue;
import com.almworks.sqlite4java.SQLiteStatement;
import com.sun.istack.internal.NotNull;

public class BackupSystem {
    public BackupSystem(Pair<String, Integer> mc, Pair<String, Integer> mdb, Pair<String, Integer> mdr, InetAddress myAddr) throws IOException {
        this(mc, mdb, mdr, myAddr.toString().substring(1));
    }

    public BackupSystem(Pair<String, Integer> mc, Pair<String, Integer> mdb, Pair<String, Integer> mdr, String myAddr) throws IOException {
        Comm = new Communicator(mc, mdb, mdr, myAddr);
        Files = new Files(new File("database.db"));
        initializePeerProtocols();
        _usedSpace = FileSystemUtils.fileSize(new File("backups"));
        _addr = myAddr;
    }

    public void shutdown() {
        shutdownPeerProtocols();
        Files.dispose();
    }

    public final Communicator Comm;

    public final Files Files;

    public interface BackupFileListener {
        public void FileAdded(String filePath);
        public void FileRemoved(String filePath);
    }
    
    public static class Files {
        private static class FileAdder extends SQLiteJob<Object> {
            public FileAdder(FileID file) {
                _fileId = file;
            }

            @Override
            protected Object job(SQLiteConnection connection) throws Throwable {
                exec(connection, _fileId);
                return null;
            }

            public static void exec(SQLiteConnection connection, FileID fileId) throws SQLiteException {
                String fileID = fileId.toString();

                SQLiteStatement st = connection.prepare("INSERT INTO File (id, fileID) values(?, ?)");
                try {
                    st.bind(1, fileID.hashCode());
                    st.bind(2, fileID);
                    st.stepThrough();
                } finally {
                    st.dispose();
                }
            }
            
            private FileID _fileId;
        }

        private static class FileRemover extends SQLiteJob<Object> {
            public FileRemover(FileID file) {
                _file = file;
            }

            @Override
            protected Object job(SQLiteConnection connection) throws Throwable {
                exec(connection, _file);
                return null;
            }

            public static void exec(SQLiteConnection connection, FileID fileId) throws SQLiteException {
                String fileID = fileId.toString();

                SQLiteStatement st = connection.prepare("DELETE FROM File WHERE id = ?");
                try {
                    st.bind(1, fileID.hashCode());
                    st.stepThrough();
                } finally {
                    st.dispose();
                }
            }
            
            private FileID _file;
        }

        private static class OwnFileAdder extends SQLiteJob<Object> {
            public OwnFileAdder(String filePath, FileID fileId, Integer numberOfChunks) {
                _filePath = filePath;
                _fileId = fileId;
                _numberOfChunks = numberOfChunks;
            }
            
            @Override
            protected Object job(SQLiteConnection connection) throws Throwable {
                exec(connection, _filePath, _fileId, _numberOfChunks);
                return null;
            }

            public static void exec(SQLiteConnection connection, String filePath, FileID fileId, Integer numberOfChunks) throws SQLiteException {
                String fileID = fileId.toString();

                SQLiteStatement st = connection.prepare("INSERT INTO OwnFile (id, filePath, fileId, numberChunks) values(?, ?, ?, ?)");
                try {
                    st.bind(1, filePath.hashCode());
                    st.bind(2, filePath);
                    st.bind(3, fileID);
                    st.bind(4, numberOfChunks);
                    st.stepThrough();
                } finally {
                    st.dispose();
                }
            }

            private String _filePath;
            private FileID _fileId;
            private Integer _numberOfChunks;
        }

        private static class OwnFileRemover extends SQLiteJob<Object> {
            public OwnFileRemover(String filePath) {
                _filePath = filePath;
            }
            
            @Override
            protected Object job(SQLiteConnection connection) throws Throwable {
                exec(connection, _filePath);
                return null;
            }

            public static void exec(SQLiteConnection connection, String filePath) throws SQLiteException {
                SQLiteStatement st = connection.prepare("DELETE FROM OwnFile WHERE id = ?");
                try {
                    st.bind(1, filePath.hashCode());
                    st.stepThrough();
                } finally {
                    st.dispose();
                }
            }

            private String _filePath;
        }

        private static class ChunkAdder extends SQLiteJob<Object> {
            public ChunkAdder(FileID file, Integer chunkNo, Integer replicationDegree) {
                _file = file;
                _chunkNo = chunkNo;
                _replicationDeegree = replicationDegree;
            }
            
            @Override
            protected Object job(SQLiteConnection connection) throws Throwable {
                exec(connection, _file, _chunkNo, _replicationDeegree);
                return null;
            }

            public static void exec(SQLiteConnection connection, FileID file, Integer chunkNo, Integer replicationDegree) throws SQLiteException {
                Pair<FileID, Integer> chunk = Pair.make_pair(file, chunkNo);

                SQLiteStatement st1 = null;
                try {
                    st1 = connection.prepare("INSERT INTO Chunk (id, fileId, chunkNo, replicationDegree) VALUES (?, ?, ?, ?)");
                    st1.bind(1, chunk.hashCode());
                    st1.bind(2, chunk.first.hashCode());
                    st1.bind(3, chunk.second);
                    st1.bind(4, replicationDegree);
                    st1.stepThrough();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    st1.dispose();
                }
            }

            private FileID _file;
            private Integer _chunkNo;
            private Integer _replicationDeegree;
        }

        private static class ChunkRemover extends SQLiteJob<Object> {
            public ChunkRemover(FileID file, Integer chunkNo) {
                _file = file;
                _chunkNo = chunkNo;
            }

            @Override
            protected Object job(SQLiteConnection connection) throws Throwable {
                exec(connection, _file, _chunkNo);
                return null;
            }

            public static void exec(SQLiteConnection connection, FileID file, Integer chunkNo) throws SQLiteException {
                Pair<FileID, Integer> chunk = Pair.make_pair(file, chunkNo);

                SQLiteStatement st1 = connection.prepare("DELETE FROM Chunk WHERE id = ?");
                try {
                    st1.bind(1, chunk.hashCode());
                    st1.stepThrough();
                } finally {
                    st1.dispose();
                }
            }
            
            private FileID _file;
            private Integer _chunkNo;
        }

        private static class PeerAdder extends SQLiteJob<Object> {
            public PeerAdder(FileID file, Integer chunkNo, String peerIp) {
                _file = file;
                _chunkNo = chunkNo;
                _peerIp = peerIp;
            }

            @Override
            protected Object job(SQLiteConnection connection) throws Throwable {
                exec(connection, _file, _chunkNo, _peerIp);
                return null;
            }

            public static void exec(SQLiteConnection connection, FileID file, Integer chunkNo, String peerIp) throws SQLiteException {
                Pair<FileID, Integer> chunk = Pair.make_pair(file, chunkNo);

                SQLiteStatement st1 = connection.prepare("INSERT INTO Ip (chunkId, IP) values (?, ?)");
                try {
                    st1.bind(1, chunk.hashCode());
                    st1.bind(2, peerIp);
                    st1.stepThrough();
                } finally {
                    st1.dispose();
                }
            }
            
            private FileID _file;
            private Integer _chunkNo;
            private String _peerIp;
        }

        private static class PeerRemover extends SQLiteJob<Object> {
            public PeerRemover(FileID file, Integer chunkNo, String peerIp) {
                _file = file;
                _chunkNo = chunkNo;
                _peerIp = peerIp;
            }

            @Override
            protected Object job(SQLiteConnection connection) throws Throwable {
                exec(connection, _file, _chunkNo, _peerIp);
                return null;
            }

            public static void exec(SQLiteConnection connection, FileID file, Integer chunkNo, String peerIp) throws SQLiteException {
                Pair<FileID, Integer> chunk = Pair.make_pair(file, chunkNo);

                SQLiteStatement st1 = connection.prepare("DELETE FROM Ip WHERE chunkId = ? AND IP = ?");
                try {
                    st1.bind(1, chunk.hashCode());
                    st1.bind(2, peerIp);
                    st1.stepThrough();
                } finally {
                    st1.dispose();
                }
            }
            
            private FileID _file;
            private Integer _chunkNo;
            private String _peerIp;
        }

        private void createDatabase(SQLiteConnection db) throws SQLiteException {
            db.exec("BEGIN TRANSACTION;                                                                            "
                    + "PRAGMA foreign_keys = ON;                                                                   "
                    + "                                                                                            "
                    + "DROP TABLE IF EXISTS OwnFile;                                                               "
                    + "DROP TABLE IF EXISTS File;                                                                  "
                    + "DROP TABLE IF EXISTS Chunk;                                                                 "
                    + "DROP TABLE IF EXISTS Ip;                                                                    "
                    + "                                                                                            "
                    + "CREATE TABLE OwnFile (                                                                      "
                    + "    id INTEGER NOT NULL,                                                                    "
                    + "    filePath TEXT NOT NULL,                                                                 "
                    + "    fileId TEXT NOT NULL,                                                                   "
                    + "    numberChunks INTEGER NOT NULL,                                                          "
                    + "                                                                                            "
                    + "    CONSTRAINT ownFile_PK PRIMARY KEY (id),                                                 "
                    + "    CONSTRAINT ownFileId_Unique UNIQUE(fileId),                                             "
                    + "    CONSTRAINT ownFilePath_Unitque UNIQUE(filePath),                                        "
                    + "    CONSTRAINT ownFileId_Size64 CHECK(length(fileId) = 64)                                  "
                    + ");                                                                                          "
                    + "                                                                                            "
                    + "CREATE TABLE File (                                                                         "
                    + "    id INTEGER NOT NULL,                                                                    "
                    + "    fileId TEXT NOT NULL,                                                                   "
                    + "                                                                                            "
                    + "    CONSTRAINT file_PK PRIMARY KEY (id),                                                    "
                    + "    CONSTRAINT fileId_Unique UNIQUE(fileId),                                                "
                    + "    CONSTRAINT fileId_Size64 CHECK(length(fileId) = 64)                                     "
                    + ");                                                                                          "
                    + "                                                                                            "
                    + "CREATE TABLE Chunk (                                                                        "
                    + "    id INTEGER NOT NULL,                                                                    "
                    + "    fileId INTEGER NOT NULL,                                                                "
                    + "    chunkNo INTEGER NOT NULL,                                                               "
                    + "    replicationDegree INTEGER NOT NULL,     			              						   "
                    + "                                                                                            "
                    + "    CONSTRAINT chunk_PK PRIMARY KEY (id),                                                   "
                    + "    CONSTRAINT fileId_chunkNo_Unique UNIQUE(fileId, chunkNo),                               "
                    + "    CONSTRAINT chunk_file_FK FOREIGN KEY (fileId) REFERENCES File(id) ON DELETE CASCADE     "
                    + "    CONSTRAINT chunk_replication_degree_CHECK CHECK(replicationDegree >= 0 AND replicationDegree <= 9) "
                    + ");                                                                                          "
                    + "                                                                                            "
                    + "CREATE TABLE Ip (                                                                           "
                    + "    chunkId INTEGER NOT NULL,                                                               "
                    + "    IP TEXT NOT NULL,                                                                       "
                    + "                                                                                            "
                    + "    CONSTRAINT Ip_PK PRIMARY KEY(chunkId, IP),                                              "
                    + "    CONSTRAINT ip_chunk_FK FOREIGN KEY (chunkId) REFERENCES Chunk(id) ON DELETE CASCADE     "
                    + ");                                                                                          "
                    + "                                                                                            "
                    + "DROP VIEW IF EXISTS FileChunk;                                                              "
                    + "DROP VIEW IF EXISTS FileChunkIp;                                                            "
                    + "                                                                                            "
                    + "CREATE VIEW FileChunkIp AS                                                                  "
                    + "    SELECT File.fileId, Chunk.chunkNo, Ip.IP                                                "
                    + "    FROM Ip JOIN Chunk ON Ip.chunkId = Chunk.id                                             "
                    + "            JOIN File ON File.id = Chunk.fileId;                                            "
                    + "                                                                                            "
                    + "CREATE VIEW FileChunk AS                                                                    "
                    + "    SELECT File.fileId, Chunk.chunkNo, Chunk.replicationDegree                              "
                    + "    FROM Chunk JOIN File ON File.id = Chunk.fileId;                                         "
                    + "                                                                                            "
                    + "CREATE VIEW FileChunkReplicationDegree AS                                                   "
                    + "SELECT fileId, chunkNo, COUNT(*) AS replicationDegree                                       "
                    + "FROM FileChunkIp                                                                            "
                    + "GROUP BY fileId, chunkNo;                                                                   "
                    + "COMMIT;                                                                                     ");
        }

        private void loadDatabase(SQLiteConnection db) {
            SQLiteStatement fileSt = null;
            try {
                fileSt = db.prepare("SELECT fileId FROM File");
                while (fileSt.step()) {
                    String fileId = fileSt.columnString(0);
                    _internalMap.put(fileId, new ConcurrentHashMap<Integer, Pair<Integer, HashSet<String>>>());
                }
            } catch (SQLiteException e) {
                e.printStackTrace();
            } finally {
                if (fileSt != null)
                    fileSt.dispose();
            }

            SQLiteStatement ownfileSt = null;
            try {
                ownfileSt = db.prepare("SELECT filePath, fileId, numberChunks FROM OwnFile");
                while (ownfileSt.step()) {
                    String filePath = ownfileSt.columnString(0);
                    FileID fileId = new FileID(ownfileSt.columnString(1));
                    Integer numberChunks = ownfileSt.columnInt(2);
                    _ownFiles.put(filePath, Pair.make_pair(fileId, numberChunks));
                }
            } catch (SQLiteException e) {
                e.printStackTrace();
            } finally {
                if (ownfileSt != null)
                    ownfileSt.dispose();
            }

            SQLiteStatement chunkSt = null;
            try {
                chunkSt = db.prepare("SELECT * FROM FileChunk");
                while (chunkSt.step()) {
                    String fileId = chunkSt.columnString(0);
                    Integer chunkNo = chunkSt.columnInt(1);
                    Integer replDegree = chunkSt.columnInt(2);
                    _internalMap.get(fileId).put(chunkNo, Pair.make_pair(replDegree, new HashSet<String>()));
                }
            } catch (SQLiteException e) {
                e.printStackTrace();
            } finally {
                if (chunkSt != null)
                    chunkSt.dispose();
            }

            SQLiteStatement ipSt = null;
            try {
                ipSt = db.prepare("SELECT * FROM FileChunkIp");
                while (ipSt.step()) {
                    String fileId = ipSt.columnString(0);
                    Integer chunkNo = ipSt.columnInt(1);
                    String ip = ipSt.columnString(2);
                    _internalMap.get(fileId).get(chunkNo).second.add(ip);
                }
            } catch (SQLiteException e) {
                e.printStackTrace();
            } finally {
                if (ipSt != null)
                    ipSt.dispose();
            }
        }

        SQLiteConnection db;
        
        public Files(File databaseFile) {
            Logger.getLogger("com.almworks.sqlite4java").setLevel(Level.OFF);

            try {

                db = new SQLiteConnection(databaseFile.getAbsoluteFile());
                db.open(true);

                int numResults = 0;
                SQLiteStatement testSt = null;
                try {
                    testSt = db.prepare("SELECT 1 FROM sqlite_master WHERE type='table' AND name='File'");
                    while (testSt.step())
                        numResults++;
                } finally {
                    if (testSt != null)
                        testSt.dispose();
                }

                if (numResults > 0)
                    loadDatabase(db);
                else
                    createDatabase(db);

                db.dispose();
                queue = new SQLiteQueue(databaseFile.getAbsoluteFile());
                queue.start();

                queue.execute(new SQLiteJob<Object>() {

                    @Override
                    protected Object job(SQLiteConnection connection) throws Throwable {
                        connection.exec("PRAGMA foreign_keys = ON;");
                        return null;
                    }
                });
                
                // db.exec("PRAGMA foreign_keys = ON;");
            } catch (SQLiteException e) {

            }
        }

        public void dispose() {
            try {
                queue.stop(true).join();
            } catch (InterruptedException e) {
            }
        }

        public boolean containsOwnFile(String filePath) {
            return _ownFiles.containsKey(filePath);
        }

        public Pair<FileID, Integer> getOwnFileInfo(String filePath) {
            return _ownFiles.get(filePath);
        }

        public boolean addOwnFile(MyFile file) {
            if (!containsOwnFile(file.getPath())) {
                _ownFiles.put(file.getPath(), Pair.make_pair(file.getFileId(), file.getNumberOfChunks()));
                queue.execute(new OwnFileAdder(file.getPath(), file.getFileId(), file.getNumberOfChunks()));
//                try {
//                    OwnFileAdder.exec(db, filePath, fileId, numberOfChunks);
//                } catch (SQLiteException e) {
//                    e.printStackTrace();
//                }
                if (fileListener != null) {
                    fileListener.FileAdded(file.getPath());
                }
            }
            return true;
        }

        public void removeOwnFile(String filePath) {
            if (containsOwnFile(filePath)) {
                _ownFiles.remove(filePath);
                queue.execute(new OwnFileRemover(filePath));
//                try {
//                    OwnFileRemover.exec(db, filePath);
//                } catch (SQLiteException e) {
//                    e.printStackTrace();
//                }
                if (fileListener != null) fileListener.FileRemoved(filePath);
            }
        }

        public boolean containsFile(FileID fileId) {
            return _internalMap.containsKey(fileId.toString());
        }

        public boolean addFile(FileID file) {
            if (!containsFile(file)) {
                _internalMap.put(file.toString(), new ConcurrentHashMap<Integer, Pair<Integer, HashSet<String>>>());
                queue.execute(new FileAdder(file));
//                try {
//                    FileAdder.exec(db, file);
//                } catch (SQLiteException e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                }
            }
            return true;
        }

        public void removeFile(FileID file) {
            if (containsFile(file)) {
                _internalMap.remove(file.toString());
                queue.execute(new FileRemover(file));
//                try {
//                    FileRemover.exec(db, file);
//                } catch (SQLiteException e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                }
            }
        }

        public boolean containsChunk(FileID fileId, Integer chunkNo) {
            ConcurrentHashMap<Integer, Pair<Integer, HashSet<String>>> fileEntry = _internalMap.get(fileId.toString());
            return fileEntry != null && fileEntry.containsKey(chunkNo);
        }

        public int getChunkRealReplicationDegree(FileID fileId, Integer chunkNo) {
            if (containsChunk(fileId, chunkNo))
                return _internalMap.get(fileId).get(chunkNo).second.size();
            return 0;
        }

        public int getChunkDesiredReplicationDegree(FileID fileId, Integer chunkNo) {
            if (containsChunk(fileId, chunkNo))
                return _internalMap.get(fileId).get(chunkNo).first;
            return 0;
        }

        public boolean addChunk(FileID file, Integer chunkNo, Integer replDegree) {
            addFile(file);
            ConcurrentHashMap<Integer, Pair<Integer, HashSet<String>>> fileEntry = _internalMap.get(file.toString());
            if (fileEntry == null)
                return false;

            if (!fileEntry.containsKey(chunkNo)) {
                fileEntry.put(chunkNo, Pair.make_pair(replDegree, new HashSet<String>()));
                queue.execute(new ChunkAdder(file, chunkNo, replDegree));
//                try {
//                    ChunkAdder.exec(db, file, chunkNo, replDegree);
//                } catch (SQLiteException e) {
//                    e.printStackTrace();
//                }
            }
            return true;
        }

        public void removeChunk(FileID file, Integer chunkNo) {
            if (containsChunk(file, chunkNo)) {
                _internalMap.get(file.toString()).remove(chunkNo);
                queue.execute(new ChunkRemover(file, chunkNo));
//                try {
//                    ChunkRemover.exec(db, file, chunkNo);
//                } catch (SQLiteException e) {
//                    e.printStackTrace();
//                }
            }
        }

        public boolean containsPeer(FileID file, Integer chunkNo, String peerIp) {
            ConcurrentHashMap<Integer, Pair<Integer, HashSet<String>>> fileEntry = _internalMap.get(file.toString());
            if (fileEntry == null)
                return false;

            Pair<Integer, HashSet<String>> chunkPeers = fileEntry.get(chunkNo);

            if (chunkPeers != null)
                synchronized (chunkPeers.second) {
                    return chunkPeers.second.contains(peerIp);
                }

            return false;
        }

        public boolean addPeer(FileID file, Integer chunkNo, String peerIp) {
            if (!containsChunk(file, chunkNo))
                return false;

            ConcurrentHashMap<Integer, Pair<Integer, HashSet<String>>> fileEntry = _internalMap.get(file.toString());
            if (fileEntry == null)
                return false;

            Pair<Integer, HashSet<String>> chunkPeers = fileEntry.get(chunkNo);
            if (chunkPeers == null)
                return false;

            synchronized (chunkPeers.second) {
                if (!chunkPeers.second.contains(peerIp)) {
                    chunkPeers.second.add(peerIp);
                     queue.execute(new PeerAdder(file, chunkNo, peerIp));
//                    try {
//                        peeradder.exec(db, file, chunkno, peerip);
//                    } catch (sqliteexception e) {
//                        e.printstacktrace();
//                    }
                }
            }
            return true;
        }

        public void removePeer(FileID file, Integer chunkNo, String peerIp) {
            if (containsPeer(file, chunkNo, peerIp)) {
                Pair<Integer, HashSet<String>> chunkPeers = _internalMap.get(file.toString()).get(chunkNo);
                synchronized (chunkPeers.second) {
                    chunkPeers.second.remove(peerIp);
                }
                 queue.execute(new PeerRemover(file, chunkNo, peerIp));
//                try {
//                    PeerRemover.exec(db, file, chunkNo, peerIp);
//                } catch (SQLiteException e) {
//                    e.printStackTrace();
//                }
            }
        }

        @Override
        public String toString() {
            return _internalMap.toString() + "\n" + _ownFiles.toString();
        }

        public void setFileListener(BackupFileListener l) {
            fileListener = l;
        }
        
        /**
         * Iterates through the backed up chunks and places them in a priority queue
         * 
         * @return returns a priority queue with the backedup chunks ordered descendingly by (ActualReplicationDegree - DesiredReplicationDegree)
         */
        public PriorityQueue<ChunkInfo> getChunksToRemove() {
            
            PriorityQueue<ChunkInfo> result = new PriorityQueue<ChunkInfo>();
            
            for (Map.Entry<String, ConcurrentHashMap<Integer, Pair<Integer, HashSet<String>>>> file : _internalMap.entrySet()) {
                
                for(Map.Entry<Integer, Pair<Integer, HashSet<String>>> chunk : file.getValue().entrySet()){
                    Integer actualDegree = chunk.getValue().second.size();
                    ChunkInfo info = new ChunkInfo(file.getKey(), chunk.getKey(), chunk.getValue().first, actualDegree);
                    result.offer(info);
                }
                
            }
            
            return result;
        }
        
        public static class ChunkInfo implements Comparable<ChunkInfo>{
            
            ChunkInfo(String fileId, Integer chunkNo, Integer desiredRD, Integer actualRD){
                _fileId = new FileID(fileId);
                _desiredRD = desiredRD;
                _actualRD = actualRD;
                _chunkNo = chunkNo;
            }
            
            public Integer getExcessDegree(){
                return _actualRD-_desiredRD;
            }
            
            public FileID getFileId(){
                return _fileId;
            }
            
            public Integer getChunkNo(){
                return _chunkNo;
            }
            
            @Override
            public int compareTo(ChunkInfo arg0) {
                return arg0.getExcessDegree() - getExcessDegree();
            }
            
            private Integer _chunkNo;
            private FileID _fileId;
            private Integer _desiredRD;
            private Integer _actualRD;
        }
        
        private SQLiteQueue queue;
        
        private BackupFileListener fileListener;

        private ConcurrentHashMap<String, Pair<FileID, Integer>> _ownFiles = new ConcurrentHashMap<String, Pair<FileID, Integer>>();
        
        /*
         * FileID -> ChunkNo -> [ReplicationDegree (Desired), {Peers}] 
         */
        private ConcurrentHashMap<String, ConcurrentHashMap<Integer, Pair<Integer, HashSet<String>>>> _internalMap = new ConcurrentHashMap<String, ConcurrentHashMap<Integer, Pair<Integer, HashSet<String>>>>();
    }

    private void initializePeerProtocols() {
        _chunkBackup = new PeerChunkBackup(this);
        _chunkRestore = new PeerChunkRestore(this);
        _fileDeletion = new PeerFileDeletion(this);
        _spaceReclaiming = new PeerSpaceReclaiming(this);
        _stored = new PeerStored(this);
    }

    private void shutdownPeerProtocols() {
        _chunkBackup.finish();
        _chunkRestore.finish();
        _fileDeletion.finish();
        _spaceReclaiming.finish();
        _stored.finish();
    }

    public void writeChunk(Message msg) {
        String filePath = (msg.type == Message.Type.PUTCHUNK ? "backups/" : "restores/") + msg.getHexFileID() + "/" + msg.getChunkNo().toString();
        long writtenSize = FileSystemUtils.WriteByteArray(filePath, msg.getBody());
        _usedSpace += writtenSize;
    }

    public void deleteChunk(FileID fileId, @NotNull Integer chunkNo) {
        String filePath = "backups/" + fileId.toString() + "/" + chunkNo.toString();
        File file = new File(filePath);
        long fileSize = FileSystemUtils.fileSize(file);
        FileSystemUtils.deleteFile(file);
        Files.removeChunk(fileId, chunkNo);
        _usedSpace -= fileSize;
    }

    public void deleteFile(FileID fileId) {
        String dirPath = "backups/" + fileId.toString();
        File dir = new File(dirPath);
        long dirSize = FileSystemUtils.fileSize(dir);
        FileSystemUtils.deleteFile(dir);
        Files.removeFile(fileId);
        _usedSpace -= dirSize;
    }

    public FileBackup backupFile(File file) {
        try {
            return new FileBackup(this, new MyFile(_addr, file.getAbsolutePath()), 1);
        } catch (IOException e) {
            return null;
        }
    }
    

    
    

    public long getUsedSpace() {
        return _usedSpace;
    }

    public void setTotalSpace(long totalSpace){
        _totalSpace = totalSpace;
    }
    
    public long getTotalSpace(){
        return _totalSpace;
    }
    
    public long getAvailableSpace(){
        return _totalSpace-_usedSpace;
    }
    
    private PeerChunkBackup _chunkBackup;
    private PeerChunkRestore _chunkRestore;
    private PeerFileDeletion _fileDeletion;
    private PeerSpaceReclaiming _spaceReclaiming;
    private PeerStored _stored;
    private String _addr;
    private long _usedSpace;
    private long _totalSpace;

}
