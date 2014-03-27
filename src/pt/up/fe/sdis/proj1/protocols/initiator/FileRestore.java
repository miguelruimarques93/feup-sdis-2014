package pt.up.fe.sdis.proj1.protocols.initiator;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.Arrays;
import java.util.Comparator;

import pt.up.fe.sdis.proj1.Chunk;
import pt.up.fe.sdis.proj1.utils.BackupSystem;
import pt.up.fe.sdis.proj1.utils.FileID;
import pt.up.fe.sdis.proj1.utils.FileSystemUtils;
import pt.up.fe.sdis.proj1.utils.Pair;
import rx.Observable;
import rx.Observer;
import rx.subjects.PublishSubject;

public class FileRestore implements Observer<Object> {

	public FileRestore(BackupSystem bs, String filePath, String destPath) throws FileNotFoundException{
	    _bs = bs;
	    Pair<FileID, Integer> fileInfo = _bs.Files.getOwnFileInfo(filePath);
	    
	    if (fileInfo == null) 
	        throw new FileNotFoundException();
	    
		_fileId = fileInfo.first;
		_destPath = destPath;
		_numChunks = fileInfo.second;
	}

	public void Restore() {	    
	    int numChunksInitiallyReceived = /*_numChunks*/Math.min(1, _numChunks);
	    _numChunksToBeReceived = _numChunks - numChunksInitiallyReceived;
        for (int i = 0; i < numChunksInitiallyReceived; ++i) {
            new ChunkRestore(_bs, _fileId, i).getObservable().subscribe(this);
        }
    }
	
	/**
	 * Attempts to restore the file by reading chunks in the directory "restores/{fileHexId}"
	 * 
	 * If any chunks are invalid/missing, throws a NoSuchFileException with the number of the first invalid chunk.
	 * 
	 * Throws an IOException in case of other generic errors handling files.
	 * @throws IOException
	 */
	private void restoreFile() throws IOException{
		File dir = new File("restores/" + _fileId.toString());
		if (!dir.exists()){
			throw new NoSuchFileException("0"); //first chunk is missing
		}

		//get all chunk files from directory in lexicographical order
		File[] fileListing = getSortedChunks(dir);
		//ensure all chunks are available and valid, if not, throw exception
		validateChunks(fileListing);
		//create destination file and append all chunks to it
		writeChunksToFile(fileListing);
		
		FileSystemUtils.deleteFile(dir);
	}

	private File[] getSortedChunks(File dir) {
		File[] fileListing = dir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.matches("^[0-9]+$");
			}
		});
		Arrays.sort(fileListing, new Comparator<File>() {
			@Override
			public int compare(File arg0, File arg1) {
				Integer arg0Num = Integer.parseInt(arg0.getName());
				Integer arg1Num = Integer.parseInt(arg1.getName());
				return arg0Num.compareTo(arg1Num);
			}
		});
		return fileListing;
	}

	private void writeChunksToFile(File[] fileListing) throws IOException {
		File file = new File(_destPath);
		if(!file.exists()){
			file.createNewFile();
		}
		try{
			FileOutputStream output=new FileOutputStream(file);
			byte[] chunk = new byte[64000];
			for(File f: fileListing)
			{
				BufferedInputStream bis = new BufferedInputStream(new FileInputStream(f));
				bis.read(chunk);
				output.write(chunk, 0, (int)f.length());
				bis.close();
			}
			output.close();
		}
		catch(IOException e){ //if anything goes wrong, delete file if created
			if(file.exists())
				file.delete();
			throw e;
		}
	}

	private void validateChunks(File[] fileListing) throws NoSuchFileException {
		if(fileListing.length < 1){
			throw new NoSuchFileException("0");
		}
		for(int i=0; i < fileListing.length; i++){
			if(Integer.parseInt(fileListing[i].getName()) != i){
				throw new NoSuchFileException(Integer.toString(i));
			}
			if(i < fileListing.length-1 && fileListing[i].length() != Chunk.MAX_CHUNK_SIZE)
				throw new NoSuchFileException(Integer.toString(i));
			else if(i == fileListing.length-1 && fileListing[i].length() >= Chunk.MAX_CHUNK_SIZE)
				throw new NoSuchFileException(Integer.toString(fileListing.length));
		}
	}

    
	@Override
    public void onCompleted() {
	    _numChunksReceived++;
	    
	    ps.onNext(_numChunksReceived / (double)_numChunks);
	    
	    if (_numChunks == _numChunksReceived)
	        try { restoreFile(); ps.onCompleted(); } catch (IOException e) { ps.onError(e);}
	    else if (_numChunksToBeReceived > 0) {
	        int i;
            synchronized (_numChunksToBeReceived) {
                i = _numChunks - _numChunksToBeReceived;
                --_numChunksToBeReceived;
            }
            new ChunkRestore(_bs, _fileId, i).getObservable().subscribe(this);
	    }
    }

    @Override
    public void onError(Throwable e) {
        ps.onError(e);
    }

    @Override
    public void onNext(Object t) { }
    
    public Observable<Double> getProgressionObservable() { return ps.asObservable(); }
    
    private BackupSystem _bs;
    private FileID _fileId;
    private String _destPath;
    private int _numChunks;
    private int _numChunksReceived = 0;
    private Integer _numChunksToBeReceived;
    
    private PublishSubject<Double> ps = PublishSubject.create();
}
