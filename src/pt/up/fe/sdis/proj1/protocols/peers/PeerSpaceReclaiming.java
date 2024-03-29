package pt.up.fe.sdis.proj1.protocols.peers;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import pt.up.fe.sdis.proj1.BackupSystem;
import pt.up.fe.sdis.proj1.Chunk;
import pt.up.fe.sdis.proj1.messages.Message;
import pt.up.fe.sdis.proj1.protocols.AbstractProtocol;
import pt.up.fe.sdis.proj1.protocols.initiator.ChunkBackup;
import pt.up.fe.sdis.proj1.utils.CounterObserver;
import pt.up.fe.sdis.proj1.utils.MessageFilter;
import rx.Scheduler;
import rx.Subscription;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class PeerSpaceReclaiming extends AbstractProtocol {

	public PeerSpaceReclaiming(BackupSystem bs) {
		super(bs.Comm.MC.Publisher);
		_bs = bs;
		
		start(new MessageFilter(Message.Type.REMOVED));
	}

	@Override
	protected void ProcessMessage(Message msg) {
	    System.err.println("Received REMOVED: " + msg.getFileID() + " " + msg.getChunkNo());
		if(_bs.Files.containsPeer(msg.getFileID(), msg.getChunkNo(), msg.Sender.toString())){
			_bs.Files.removePeer(msg.getFileID(), msg.getChunkNo(), msg.Sender.toString());
			int realDegree = _bs.Files.getChunkRealReplicationDegree(msg.getFileID(), msg.getChunkNo());
			int desiredDegree = _bs.Files.getChunkDesiredReplicationDegree(msg.getFileID(), msg.getChunkNo());
			
			if (realDegree < desiredDegree) {
				final CounterObserver co = new CounterObserver();
				
				byte[] chunkArray;
				try {
					chunkArray = _bs.readChunk(msg.getFileID(), msg.getChunkNo());
				} catch(IOException e) {
				    _bs.Files.removeChunk(msg.getFileID(), msg.getChunkNo());
				    _bs.Comm.MC.Sender.Send(Message.makeRemoved(msg.getFileID(), msg.getChunkNo()));
                    return;
				}
				
				final Chunk chunk = new Chunk(msg.getChunkNo(), desiredDegree, msg.getFileID(), chunkArray);
				
				final Subscription sub = _bs.Comm.MDB.Publisher.getObservable()
					.filter(new MessageFilter(Message.Type.PUTCHUNK, msg.getFileID(), msg.getChunkNo()))
					.subscribe(co);
				
		        Schedulers.io().schedule(new Action1<Scheduler.Inner>() {
		            @Override
		            public void call(Scheduler.Inner arg0) {
		                if (!co.received()) new ChunkBackup(_bs, chunk);
		                sub.unsubscribe();
		            }
		        }, rand.nextInt(401), TimeUnit.MILLISECONDS);
			}
		}
	}

	private BackupSystem _bs;
	private static Random rand = new Random();
}
