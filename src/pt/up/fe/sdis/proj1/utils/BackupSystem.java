package pt.up.fe.sdis.proj1.utils;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteJob;
import com.almworks.sqlite4java.SQLiteQueue;
import com.almworks.sqlite4java.SQLiteStatement;
import com.sun.istack.internal.NotNull;

public class BackupSystem {
    public BackupSystem(Pair<String, Integer> mc, Pair<String, Integer> mdb,
            Pair<String, Integer> mdr, String myAddr) throws IOException {
        Comm = new Communicator(mc, mdb, mdr, myAddr);
    }

    public final Communicator Comm;
    
    public static class Files {
        private class FileAdder extends SQLiteJob<Object> {
            public FileAdder(MyFile file) { 
                _file = file;
            }

            @Override
            protected Object job(SQLiteConnection connection) throws Throwable {
                String fileID = _file.getFileId().toString();
                
                SQLiteStatement st = connection.prepare("INSERT INTO File (id, fileID) values(?, ?)");
                try {
                    st.bind(1, fileID.hashCode());
                    st.bind(2, fileID);
                    st.stepThrough();
                } finally {
                    st.dispose();
                }
                
                return null;
            }
            
            private MyFile _file;
        }

        private class FileRemover extends SQLiteJob<Object> {
            public FileRemover(MyFile file) { 
                _file = file;
            }

            @Override
            protected Object job(SQLiteConnection connection) throws Throwable {
                String fileID = _file.getFileId().toString();
                
                SQLiteStatement st = connection.prepare("DELETE FROM File WHERE id = ?");
                try {
                    st.bind(1, fileID.hashCode());
                    st.stepThrough();
                } finally {
                    st.dispose();
                }
                
                return null;
            }
            
            private MyFile _file;
        }
        
        private class ChunkAdder extends SQLiteJob<Object> {
            public ChunkAdder(MyFile file, Integer chunkNo) {
                _file = file;
                _chunkNo = chunkNo;
            }
            
            @Override
            protected Object job(SQLiteConnection connection) throws Throwable {
              Pair<FileID, Integer> chunk = Pair.make_pair(_file.getFileId(), _chunkNo);
              
              SQLiteStatement st1 = null;
              try {
                  st1 = connection.prepare("INSERT INTO Chunk (id, fileId, chunkNo) VALUES (?, ?, ?)");
                  st1.bind(1, chunk.hashCode());
                  st1.bind(2, chunk.first.hashCode());
                  st1.bind(3, chunk.second);
                  st1.stepThrough();
              } catch (Exception e) {
                  e.printStackTrace();
              } finally {
                  st1.dispose();
              }
              
              return null;
            }
            
            private MyFile _file;
            private Integer _chunkNo;
        }

        private class ChunkRemover extends SQLiteJob<Object> {
            public ChunkRemover(MyFile file, Integer chunkNo) {
                _file = file;
                _chunkNo = chunkNo;
            }
            
            @Override
            protected Object job(SQLiteConnection connection) throws Throwable {
              Pair<FileID, Integer> chunk = Pair.make_pair(_file.getFileId(), _chunkNo);
              
              SQLiteStatement st1 = connection.prepare("DELETE FROM Chunk WHERE id = ?");
              try {
                  st1.bind(1, chunk.hashCode());
                  st1.stepThrough();
              } finally {
                  st1.dispose();
              }
              
              return null;
            }
            
            private MyFile _file;
            private Integer _chunkNo;
        }

        private class PeerAdder extends SQLiteJob<Object> {
            public PeerAdder(MyFile file, Integer chunkNo, String peerIp) {
                _file = file;
                _chunkNo = chunkNo;
                _peerIp = peerIp;
            }
            
            @Override
            protected Object job(SQLiteConnection connection) throws Throwable {
              Pair<FileID, Integer> chunk = Pair.make_pair(_file.getFileId(), _chunkNo);
              
              SQLiteStatement st1 = connection.prepare("INSERT INTO Ip (chunkId, IP) values (?, ?)");
              try {
                  st1.bind(1, chunk.hashCode());
                  st1.bind(2, _peerIp);
                  st1.stepThrough();
              } finally {
                  st1.dispose();
              }
              
              return null;
            }
            
            private MyFile _file;
            private Integer _chunkNo;
            private String _peerIp;
        }

        private class PeerRemover extends SQLiteJob<Object> {
            public PeerRemover(MyFile file, Integer chunkNo, String peerIp) {
                _file = file;
                _chunkNo = chunkNo;
                _peerIp = peerIp;
            }
            
            @Override
            protected Object job(SQLiteConnection connection) throws Throwable {
              Pair<FileID, Integer> chunk = Pair.make_pair(_file.getFileId(), _chunkNo);
              
              SQLiteStatement st1 = connection.prepare("DELETE FROM Ip WHERE chunkId = ? AND IP = ?");
              try {
                  st1.bind(1, chunk.hashCode());
                  st1.bind(2, _peerIp);
                  st1.stepThrough();
              } finally {
                  st1.dispose();
              }
              
              return null;
            }
            
            private MyFile _file;
            private Integer _chunkNo;
            private String _peerIp;
        }
        
        private void createDatabase(SQLiteConnection db) throws SQLiteException {
            db.exec("BEGIN TRANSACTION;                                                                          "
                    + "PRAGMA foreign_keys = ON;                                                                   "
                    + "                                                                                            "
                    + "DROP TABLE IF EXISTS File;                                                                  "
                    + "DROP TABLE IF EXISTS Chunk;                                                                 "
                    + "DROP TABLE IF EXISTS Ip;                                                                    "
                    + "                                                                                            "
                    + "CREATE TABLE File (                                                                         "
                    + "    id INTEGER NOT NULL,                                                                    "
                    + "    fileId TEXT NOT NULL,                                                                   "
                    + "                                                                                            "
                    + "    CONSTRAINT file_PK Primary KEY (id),                                                    "
                    + "    CONSTRAINT fileId_Unique UNIQUE(fileId),                                                "
                    + "    CONSTRAINT fileId_Size64 CHECK(length(fileId) = 64)                                     "
                    + ");                                                                                          "
                    + "                                                                                            "
                    + "CREATE TABLE Chunk (                                                                        "
                    + "    id INTEGER NOT NULL,                                                                    "
                    + "    fileId INTEGER NOT NULL,                                                                "
                    + "    chunkNo INTEGER NOT NULL,                                                               "
                    + "                                                                                            "
                    + "    CONSTRAINT chunk_PK PRIMARY KEY (id),                                                   "
                    + "    CONSTRAINT fileId_chunkNo_Unique UNIQUE(fileId, chunkNo),                               "
                    + "    CONSTRAINT chunk_file_FK FOREIGN KEY (fileId) REFERENCES File(id) ON DELETE CASCADE     "
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
                    + "    SELECT File.fileId, Chunk.chunkNo                                                       "
                    + "    FROM Chunk JOIN File ON File.id = Chunk.fileId;                                         "
                    + "                                                                                            "
                    + "COMMIT;                                                                                     ");
        }
        
        private void loadDatabase(SQLiteConnection db) {
            SQLiteStatement fileSt = null;
            try {
                fileSt = db.prepare("SELECT fileId FROM File");
                while (fileSt.step()) {
                    String fileId = fileSt.columnString(0);
                    _internalMap.put(fileId, new ConcurrentHashMap<Integer, HashSet<String>>());
                }
            } catch (SQLiteException e) {
                e.printStackTrace();
            } finally {
                if (fileSt != null) fileSt.dispose();
            }
            
            SQLiteStatement chunkSt = null;
            try {
                chunkSt = db.prepare("SELECT * FROM FileChunk");
                while (chunkSt.step()) {
                    String fileId = chunkSt.columnString(0);
                    Integer chunkNo = chunkSt.columnInt(1);
                    _internalMap.get(fileId).put(chunkNo, new HashSet<String>());
                }
            } catch (SQLiteException e) {
                e.printStackTrace();
            } finally {
                if (chunkSt != null) chunkSt.dispose();
            }
            
            SQLiteStatement ipSt = null;
            try {
                ipSt = db.prepare("SELECT * FROM FileChunkIp");
                while (ipSt.step()) {
                    String fileId = ipSt.columnString(0);
                    Integer chunkNo = ipSt.columnInt(1);
                    String ip = ipSt.columnString(2);
                    _internalMap.get(fileId).get(chunkNo).add(ip);
                }
            } catch (SQLiteException e) {
                e.printStackTrace();
            } finally {
                if (ipSt != null) ipSt.dispose();
            }
        }
        
        public Files(File databaseFile) throws SQLiteException {   
            Logger.getLogger("com.almworks.sqlite4java").setLevel(Level.OFF);
            
            SQLiteConnection db = new SQLiteConnection(databaseFile.getAbsoluteFile());
            db.open(true);
            
            int numResults = 0;
            SQLiteStatement testSt = null;
            try {
                testSt = db.prepare("SELECT 1 FROM sqlite_master WHERE type='table' AND name='File'");
                while (testSt.step()) numResults++;
            } finally {
                if(testSt != null) testSt.dispose();
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
                protected Object job(SQLiteConnection connection)
                        throws Throwable {
                    connection.exec("PRAGMA foreign_keys = ON;");
                    return null;
                }
            });
        }
        
        public void dispose() throws InterruptedException {
            queue.stop(true).join();
        }
        
        public boolean containsFile(MyFile file) {
            return _internalMap.containsKey(file.getFileId().toString());
        }
        
        public boolean addFile(@NotNull final MyFile file) {
            if (!containsFile(file)) {
                _internalMap.put(file.getFileId().toString(), new ConcurrentHashMap<Integer, HashSet<String>>());
                queue.execute(new FileAdder(file));
            }
            return true;
        }
        
        public void removeFile(@NotNull final MyFile file) {
            if (containsFile(file)) {
                _internalMap.remove(file.getFileId().toString());
                queue.execute(new FileRemover(file));
            }
        }
        
        public boolean containsChunk(MyFile file, Integer chunkNo) {
            ConcurrentHashMap<Integer, HashSet<String>> fileEntry = _internalMap.get(file.getFileId().toString());            
            return fileEntry != null && fileEntry.containsKey(chunkNo);
        }
        
        public boolean addChunk(MyFile file, Integer chunkNo) {
            addFile(file);
            ConcurrentHashMap<Integer, HashSet<String>> fileEntry = _internalMap.get(file.getFileId().toString());
            if (fileEntry == null) 
                return false;
            
            if (!fileEntry.containsKey(chunkNo)) {
                fileEntry.put(chunkNo, new HashSet<String>());
                queue.execute(new ChunkAdder(file, chunkNo));
            }
            return true;
        }
        
        public void removeChunk(@NotNull MyFile file, Integer chunkNo) {
            if (containsChunk(file, chunkNo)) {
                _internalMap.get(file.getFileId().toString()).remove(chunkNo);
                queue.execute(new ChunkRemover(file, chunkNo));
            }
        }
        
        public boolean containsPeer(MyFile file, Integer chunkNo, String peerIp) {
            ConcurrentHashMap<Integer, HashSet<String>> fileEntry = _internalMap.get(file.getFileId().toString());  
            if (fileEntry == null) 
                return false;
            
            HashSet<String> chunkPeers = fileEntry.get(chunkNo);
            
            return chunkPeers != null && chunkPeers.contains(peerIp);
        }
        
        public boolean addPeer(MyFile file, Integer chunkNo, String peerIp) {
            addChunk(file, chunkNo);
            ConcurrentHashMap<Integer, HashSet<String>> fileEntry = _internalMap.get(file.getFileId().toString());
            if (fileEntry == null)
                return false;
            
            HashSet<String> chunkPeers = fileEntry.get(chunkNo);
            if (chunkPeers == null)
                return false;
            
            if (!chunkPeers.contains(peerIp)) {
                chunkPeers.add(peerIp);
                queue.execute(new PeerAdder(file, chunkNo, peerIp));
            }
            return true;
        }
        
        public void removePeer(@NotNull MyFile file, Integer chunkNo, String peerIp) {
            if (containsPeer(file, chunkNo, peerIp)) {
                _internalMap.get(file.getFileId().toString()).get(chunkNo).remove(peerIp);
                queue.execute(new PeerRemover(file, chunkNo, peerIp));
            }
        }
        
        
        @Override
        public String toString() { 
            return _internalMap.toString();
        }
        
        private SQLiteQueue queue;
        
        private ConcurrentHashMap<String, ConcurrentHashMap<Integer, HashSet<String>>> _internalMap = new ConcurrentHashMap<String, ConcurrentHashMap<Integer, HashSet<String>>>();
    }

    public ConcurrentHashMap<String, ConcurrentHashMap<Integer, HashSet<String>>> PeersWithChunk = new ConcurrentHashMap<String, ConcurrentHashMap<Integer, HashSet<String>>>();
}
