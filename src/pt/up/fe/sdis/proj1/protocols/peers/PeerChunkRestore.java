package pt.up.fe.sdis.proj1.protocols.peers;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import pt.up.fe.sdis.proj1.Chunk;
import pt.up.fe.sdis.proj1.messages.Message;
import pt.up.fe.sdis.proj1.protocols.AbstractProtocol;
import pt.up.fe.sdis.proj1.utils.BackupSystem;
import pt.up.fe.sdis.proj1.utils.CounterObserver;
import pt.up.fe.sdis.proj1.utils.MessageFilter;
import pt.up.fe.sdis.proj1.utils.MyFile;
import rx.Scheduler;
import rx.Subscription;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class PeerChunkRestore extends AbstractProtocol {
	
	public PeerChunkRestore(BackupSystem bs) {
		super(bs.Comm.MC.Publisher);
		_bs = bs;
		
		start(new MessageFilter(Message.Type.GETCHUNK));
	}

	@Override
	protected void ProcessMessage(final Message msg) {
	    System.out.println("Received GETCHUNK " + msg.getFileID() + " " + msg.getChunkNo());
		if (_bs.Files.containsChunk(msg.getFileID(), msg.getChunkNo())) {
			final CounterObserver replyCounter = new CounterObserver();
			byte[] chunkArray = null;
			try {
				chunkArray = MyFile.ReadChunk(msg.getFileID(), msg.getChunkNo());
			} catch (FileNotFoundException e) {
	             System.err.println("File not found " + msg.getFileID() + " " + msg.getChunkNo());
			    _bs.Files.removeChunk(msg.getFileID(), msg.getChunkNo());
			} catch (IOException e) {
			}
			if (chunkArray == null) return;
			
			final Chunk chunk = new Chunk(msg.getChunkNo(), msg.getFileID(), chunkArray);
			final Subscription sub = _bs.Comm.MDR.Publisher.getObservable()
					.filter(new MessageFilter(Message.Type.CHUNK, msg.getFileID(), msg.getChunkNo())).subscribe(replyCounter);
			
	        Schedulers.io().schedule(new Action1<Scheduler.Inner>() {
	            @Override
	            public void call(Scheduler.Inner arg0) {
	                if (!replyCounter.received()) _bs.Comm.MDR.Sender.Send(Message.makeChunk(chunk));
	                sub.unsubscribe();
	            }
	        }, rand.nextInt(401), TimeUnit.MILLISECONDS);
		}
	}
	
	private BackupSystem _bs;
	private static Random rand = new Random();
}
