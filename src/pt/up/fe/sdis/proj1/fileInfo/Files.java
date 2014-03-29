package pt.up.fe.sdis.proj1.fileInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import pt.up.fe.sdis.proj1.BackupSystem.BackupFileListener;
import pt.up.fe.sdis.proj1.FileVersion;
import pt.up.fe.sdis.proj1.utils.FileID;
import pt.up.fe.sdis.proj1.utils.MyFile;
import pt.up.fe.sdis.proj1.utils.Pair;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteJob;
import com.almworks.sqlite4java.SQLiteQueue;
import com.almworks.sqlite4java.SQLiteStatement;

public class Files {
    private static final String _databaseCreationString  = "BEGIN TRANSACTION;                                                                                     "
            + "PRAGMA foreign_keys = ON;                                                                              "
            + "                                                                                                       "
            + "DROP TABLE IF EXISTS OwnFile;                                                                          "
            + "DROP TABLE IF EXISTS File;                                                                             "
            + "DROP TABLE IF EXISTS Chunk;                                                                            "
            + "DROP TABLE IF EXISTS Ip;                                                                               "
            + "                                                                                                       "
            + "CREATE TABLE OwnFile (                                                                                 "
            + "    id INTEGER NOT NULL,                                                                               "
            + "    filePath TEXT NOT NULL,                                                                            "
            + "    fileId TEXT NOT NULL,                                                                              "
            + "    numberChunks INTEGER NOT NULL,                                                                     "
            + "    modificationMillis INTEGER NOT NULL,                                                               "
            + "                                                                                                       "
            + "    CONSTRAINT ownFile_PK PRIMARY KEY (id),                                                            "
            + "    CONSTRAINT ownFileId_Unique UNIQUE(fileId),                                                        "
            + "    CONSTRAINT ownFilePath_Unitque UNIQUE(filePath),                                                   "
            + "    CONSTRAINT ownFileId_Size64 CHECK(length(fileId) = 64)                                             "
            + ");                                                                                                     "
            + "                                                                                                       "
            + "CREATE TABLE RemovedFile (                                                                             "
            + "    fileId TEXT NOT NULL,                                                                              "
            + "                                                                                                       "
            + "    CONSTRAINT removedFiles_PK PRIMARY KEY (fileId),                                                   "
            + "    CONSTRAINT ownFileId_Size64 CHECK(length(fileId) = 64)                                             "
            + ");                                                                                                     "
            + "                                                                                                       "
            + "CREATE TABLE File (                                                                                    "
            + "    id INTEGER NOT NULL,                                                                               "
            + "    fileId TEXT NOT NULL,                                                                              "
            + "                                                                                                       "
            + "    CONSTRAINT file_PK PRIMARY KEY (id),                                                               "
            + "    CONSTRAINT fileId_Unique UNIQUE(fileId),                                                           "
            + "    CONSTRAINT fileId_Size64 CHECK(length(fileId) = 64)                                                "
            + ");                                                                                                     "
            + "                                                                                                       "
            + "CREATE TABLE Chunk (                                                                                   "
            + "    id INTEGER NOT NULL,                                                                               "
            + "    fileId INTEGER NOT NULL,                                                                           "
            + "    chunkNo INTEGER NOT NULL,                                                                          "
            + "    replicationDegree INTEGER NOT NULL,                                                                "
            + "                                                                                                       "
            + "    CONSTRAINT chunk_PK PRIMARY KEY (id),                                                              "
            + "    CONSTRAINT fileId_chunkNo_Unique UNIQUE(fileId, chunkNo),                                          "
            + "    CONSTRAINT chunk_file_FK FOREIGN KEY (fileId) REFERENCES File(id) ON DELETE CASCADE                "
            + "    CONSTRAINT chunk_replication_degree_CHECK CHECK(replicationDegree >= 0 AND replicationDegree <= 9) "
            + ");                                                                                                     "
            + "                                                                                                       "
            + "CREATE TABLE Ip (                                                                                      "
            + "    chunkId INTEGER NOT NULL,                                                                          "
            + "    IP TEXT NOT NULL,                                                                                  "
            + "                                                                                                       "
            + "    CONSTRAINT Ip_PK PRIMARY KEY(chunkId, IP),                                                         "
            + "    CONSTRAINT ip_chunk_FK FOREIGN KEY (chunkId) REFERENCES Chunk(id) ON DELETE CASCADE                "
            + ");                                                                                                     "
            + "                                                                                                       "
            + "DROP VIEW IF EXISTS FileChunk;                                                                         "
            + "DROP VIEW IF EXISTS FileChunkIp;                                                                       "
            + "                                                                                                       "
            + "CREATE VIEW FileChunkIp AS                                                                             "
            + "    SELECT File.fileId, Chunk.chunkNo, Ip.IP                                                           "
            + "    FROM Ip JOIN Chunk ON Ip.chunkId = Chunk.id                                                        "
            + "            JOIN File ON File.id = Chunk.fileId;                                                       "
            + "                                                                                                       "
            + "CREATE VIEW FileChunk AS                                                                               "
            + "    SELECT File.fileId, Chunk.chunkNo, Chunk.replicationDegree                                         "
            + "    FROM Chunk JOIN File ON File.id = Chunk.fileId;                                                    "
            + "                                                                                                       "
            + "CREATE VIEW FileChunkReplicationDegree AS                                                              "
            + "SELECT fileId, chunkNo, COUNT(*) AS replicationDegree                                                  "
            + "FROM FileChunkIp                                                                                       "
            + "GROUP BY fileId, chunkNo;                                                                              "
            + "COMMIT;                                                                                                ";

    public Files(String databaseFilePath) {
        this(new File(databaseFilePath));
    }
    
    public Files(File databaseFile) {
        Logger.getLogger("com.almworks.sqlite4java").setLevel(Level.OFF);

        try {

            SQLiteConnection db = new SQLiteConnection(databaseFile.getAbsoluteFile());
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
        } catch (SQLiteException e) { }
    }
    
    private void createDatabase(SQLiteConnection db) throws SQLiteException {
        db.exec(_databaseCreationString);
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
        
        SQLiteStatement removedFileSt = null;
        try {
            removedFileSt = db.prepare("SELECT fileId FROM RemovedFile");
            while (removedFileSt.step()) {
                String fileId = removedFileSt.columnString(0);
                _removedFiles.add(new FileID(fileId));
            }
        } catch (SQLiteException e) {
            e.printStackTrace();
        } finally {
            if (removedFileSt != null)
                removedFileSt.dispose();
        }

        SQLiteStatement ownfileSt = null;
        try {
            ownfileSt = db.prepare("SELECT filePath, fileId, numberChunks, modificationMillis FROM OwnFile");
            while (ownfileSt.step()) {
                String filePath = ownfileSt.columnString(0);
                FileID fileId = new FileID(ownfileSt.columnString(1));
                Integer numberChunks = ownfileSt.columnInt(2);
                Long modificationMillis = ownfileSt.columnLong(3);
                
                ConcurrentHashMap<Long, Pair<FileID, Integer>> fileVersions = _ownFiles.get(filePath);
                if (fileVersions == null) {
                    _ownFiles.put(filePath, new ConcurrentHashMap<Long, Pair<FileID, Integer>>());
                    fileVersions = _ownFiles.get(filePath);
                }
                fileVersions.put(modificationMillis, Pair.make_pair(fileId, numberChunks));
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

    public void dispose() {
        try { queue.stop(true).join(); } catch (InterruptedException e) { }
    }

    public boolean containsOwnFile(String filePath, Long modificationMillis) {
        ConcurrentHashMap<Long, Pair<FileID, Integer>> fileVersions = _ownFiles.get(filePath);
        if (fileVersions == null) return false;
        
        for (Long currentMillis : fileVersions.keySet())
            if (modificationMillis.equals(currentMillis))
                return true;
        
        return false;
    }

    public Pair<FileID, Integer> getOwnFileVersionInfo(String filePath, Long modificationMillis) {
        ConcurrentHashMap<Long, Pair<FileID, Integer>> fileVersions = _ownFiles.get(filePath);
        if (fileVersions == null) return null;
        return fileVersions.get(modificationMillis);
    }
    
    public Map<Long, Pair<FileID, Integer>> getOwnFileVersions(String filePath) {
        return _ownFiles.get(filePath);
    }

    public boolean addOwnFile(MyFile file) {
        if (!containsOwnFile(file.getPath(), file.getLastModifiedDate())) {
            ConcurrentHashMap<Long, Pair<FileID, Integer>> fileVersions = _ownFiles.get(file.getPath());
            if (fileVersions == null) {
                _ownFiles.put(file.getPath(), new ConcurrentHashMap<Long, Pair<FileID, Integer>>());
                fileVersions = _ownFiles.get(file.getPath());
            }
            
            fileVersions.put(file.getLastModifiedDate(), Pair.make_pair(file.getFileId(), file.getNumberOfChunks()));
            queue.execute(new OwnFileAdder(file.getPath(), file.getFileId(), file.getNumberOfChunks(), file.getLastModifiedDate()));
            
            if (fileListener != null)
                fileListener.FileVersionAdded(new FileVersion(file.getPath(), file.getLastModifiedDate()));
        }
        return true;
    }

    public void removeOwnFile(String filePath, Long modificationMillis) {
        if (containsOwnFile(filePath, modificationMillis)) {
            ConcurrentHashMap<Long, Pair<FileID, Integer>> fileVersions = _ownFiles.get(filePath);
            fileVersions.remove(modificationMillis);
            if (fileVersions.isEmpty())
                _ownFiles.remove(filePath);
            
            queue.execute(new OwnFileRemover(filePath, modificationMillis));

            if (fileListener != null)
                fileListener.FileVersionRemoved(new FileVersion(filePath, modificationMillis));
        }
    }

    public List<FileID> getFiles() {
        List<FileID> result = new ArrayList<FileID>();
        for (Enumeration<String> enumStr = _internalMap.keys(); enumStr.hasMoreElements();) {
            String str = enumStr.nextElement();
            result.add(new FileID(str));
        }
        return result;
    }
    
    public boolean containsFile(FileID fileId) {
        return _internalMap.containsKey(fileId.toString());
    }

    public boolean addFile(FileID file) {
        if (!containsFile(file)) {
            _internalMap.put(file.toString(), new ConcurrentHashMap<Integer, Pair<Integer, HashSet<String>>>());
            queue.execute(new FileAdder(file));
        }
        return true;
    }

    public void removeFile(FileID file) {
        if (containsFile(file)) {
            _internalMap.remove(file.toString());
            queue.execute(new FileRemover(file));
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
        }
        return true;
    }

    public void removeChunk(FileID file, Integer chunkNo) {
        if (containsChunk(file, chunkNo)) {
            ConcurrentHashMap<Integer, Pair<Integer, HashSet<String>>> m = _internalMap.get(file.toString());
            m.remove(chunkNo);
            if (m.isEmpty())
                removeFile(file);
            queue.execute(new ChunkRemover(file, chunkNo));
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
        }
    }

    public void addRemovedFile(FileID file) {
        if (!containsRemovedFile(file)) {
            _removedFiles.add(file);
            queue.execute(new RemovedFileAdder(file));
        }
    }
    
    public void removeRemovedFile(FileID file) {
        if (containsRemovedFile(file)) {
            _removedFiles.remove(file);
            queue.execute(new RemovedFileRemover(file));
        }
    }
    
    public boolean containsRemovedFile(FileID file) {
        return _removedFiles.contains(file);
    }
    
    @Override
    public String toString() {
        return _internalMap.toString() + "\n" + _ownFiles.toString();
    }

    public void setFileListener(BackupFileListener l) {
        fileListener = l;
    }

    /**
     * Iterates through the backed up chunks and places them in a priority
     * queue
     * 
     * @return returns a priority queue with the backedup chunks ordered
     *         descendingly by (ActualReplicationDegree -
     *         DesiredReplicationDegree)
     */
    public PriorityQueue<ChunkInfo> getChunksToRemove() {

        PriorityQueue<ChunkInfo> result = new PriorityQueue<ChunkInfo>();

        for (Map.Entry<String, ConcurrentHashMap<Integer, Pair<Integer, HashSet<String>>>> file : _internalMap.entrySet()) {

            for (Map.Entry<Integer, Pair<Integer, HashSet<String>>> chunk : file.getValue().entrySet()) {
                Integer actualDegree = chunk.getValue().second.size();
                ChunkInfo info = new ChunkInfo(file.getKey(), chunk.getKey(), chunk.getValue().first, actualDegree);
                result.offer(info);
            }

        }

        return result;
    }

    public PriorityQueue<ChunkInfo> getChunksWithLowRd() {

        PriorityQueue<ChunkInfo> result = new PriorityQueue<ChunkInfo>(11, new Comparator<ChunkInfo>() {
            @Override
            public int compare(ChunkInfo arg0, ChunkInfo arg1) {
                return arg0.getExcessDegree() - arg1.getExcessDegree();
            }
        });

        for (Map.Entry<String, ConcurrentHashMap<Integer, Pair<Integer, HashSet<String>>>> file : _internalMap.entrySet()) {

            for (Map.Entry<Integer, Pair<Integer, HashSet<String>>> chunk : file.getValue().entrySet()) {
                Integer actualDegree = chunk.getValue().second.size();
                ChunkInfo info = new ChunkInfo(file.getKey(), chunk.getKey(), chunk.getValue().first, actualDegree);
                if (info.getExcessDegree() < 0)
                    result.offer(info);
            }

        }

        return result;
    }

    public List<FileVersion> getOwnFileVersions() {
        List<FileVersion> result = new ArrayList<FileVersion>();
        for (Entry<String, ConcurrentHashMap<Long, Pair<FileID, Integer>>> psi : _ownFiles.entrySet()) {
            for (Long modm : psi.getValue().keySet()) 
                result.add(new FileVersion(psi.getKey(), modm));
        }
            
        return result;
    }

    public List<FileID> getRemovedFiles() {
        return new ArrayList<FileID>(_removedFiles);
    }
    
    private SQLiteQueue queue;

    private BackupFileListener fileListener;
    
    /*
     * FilePath -> ModificationMillis -> [FileID, NoChunks]
     */
    private ConcurrentHashMap<String, ConcurrentHashMap<Long, Pair<FileID, Integer>>> _ownFiles = new ConcurrentHashMap<String, ConcurrentHashMap<Long, Pair<FileID, Integer>>>();

    /*
     * FileID -> ChunkNo -> [ReplicationDegree (Desired), {Peers}]
     */
    private ConcurrentHashMap<String, ConcurrentHashMap<Integer, Pair<Integer, HashSet<String>>>> _internalMap = new ConcurrentHashMap<String, ConcurrentHashMap<Integer, Pair<Integer, HashSet<String>>>>();
    
    private Set<FileID> _removedFiles = java.util.Collections.newSetFromMap(new ConcurrentHashMap<FileID, Boolean>());
}
