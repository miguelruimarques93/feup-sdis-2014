package pt.up.fe.sdis.proj1.fileInfo;

import pt.up.fe.sdis.proj1.utils.FileID;

public class ChunkInfo implements Comparable<ChunkInfo> {

    ChunkInfo(String fileId, Integer chunkNo, Integer desiredRD, Integer actualRD) {
        _fileId = new FileID(fileId);
        _desiredRD = desiredRD;
        _actualRD = actualRD;
        _chunkNo = chunkNo;
    }

    public Integer getExcessDegree() {
        return _actualRD - _desiredRD;
    }

    public FileID getFileId() {
        return _fileId;
    }

    public Integer getChunkNo() {
        return _chunkNo;
    }

    @Override
    public int compareTo(ChunkInfo arg0) {
        return arg0.getExcessDegree() - getExcessDegree();
    }

    public Integer getDesiredRD() {
        return _desiredRD;
    }
    
    private Integer _chunkNo;
    private FileID _fileId;
    private Integer _desiredRD;
    private Integer _actualRD;
}