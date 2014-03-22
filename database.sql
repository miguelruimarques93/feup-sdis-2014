BEGIN TRANSACTION;
PRAGMA foreign_keys = ON;

DROP TABLE IF EXISTS OwnFile;
DROP TABLE IF EXISTS File;
DROP TABLE IF EXISTS Chunk;
DROP TABLE IF EXISTS Ip;

CREATE TABLE OwnFile (
    id INTEGER NOT NULL,
    filePath TEXT NOT NULL,
    fileId TEXT NOT NULL,
    numberChunks INTEGER NOT NULL,
    
    CONSTRAINT ownFile_PK PRIMARY KEY (id),
    CONSTRAINT ownFileId_Unique UNIQUE(fileId),
    CONSTRAINT ownFilePath_Unitque UNIQUE(filePath),
    CONSTRAINT ownFileId_Size64 CHECK(length(fileId) = 64)
);

CREATE TABLE File (
    id INTEGER NOT NULL,
    fileId TEXT NOT NULL, 
    
    CONSTRAINT file_PK PRIMARY KEY (id),
    CONSTRAINT fileId_Unique UNIQUE(fileId),
    CONSTRAINT fileId_Size64 CHECK(length(fileId) = 64)
);

CREATE TABLE Chunk (
    id INTEGER NOT NULL, 
    fileId INTEGER NOT NULL, 
    chunkNo INTEGER NOT NULL, 
    
    CONSTRAINT chunk_PK PRIMARY KEY (id),
    CONSTRAINT fileId_chunkNo_Unique UNIQUE(fileId, chunkNo),
    CONSTRAINT chunk_file_FK FOREIGN KEY (fileId) REFERENCES File(id) ON DELETE CASCADE
);

CREATE TABLE Ip (
    chunkId INTEGER NOT NULL, 
    IP TEXT NOT NULL, 
    
    CONSTRAINT Ip_PK PRIMARY KEY(chunkId, IP),
    CONSTRAINT ip_chunk_FK FOREIGN KEY (chunkId) REFERENCES Chunk(id) ON DELETE CASCADE
);

DROP VIEW IF EXISTS FileChunk;
DROP VIEW IF EXISTS FileChunkIp;

CREATE VIEW FileChunkIp AS 
    SELECT File.fileId, Chunk.chunkNo, Ip.IP 
    FROM Ip JOIN Chunk ON Ip.chunkId = Chunk.id
            JOIN File ON File.id = Chunk.fileId;
            
CREATE VIEW FileChunk AS
    SELECT File.fileId, Chunk.chunkNo
    FROM Chunk JOIN File ON File.id = Chunk.fileId;
    
CREATE VIEW FileChunkReplicationDegree AS
    SELECT fileId, chunkNo, COUNT(*) AS replicationDegree
    FROM FileChunkIp
    GROUP BY fileId, chunkNo;
    
COMMIT;