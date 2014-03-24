package pt.up.fe.sdis.proj1.utils;

import pt.up.fe.sdis.proj1.messages.Message;
import rx.Observer;

public class CounterObserver implements Observer<Message> {
	private Integer numReceived = 0;
	
	public boolean received() { return numReceived > 0; }
	
	public int getNumReceived() { return numReceived; }
	
	@Override
	public void onCompleted() { }

	@Override
	public void onError(Throwable arg0) { }

	@Override
	public void onNext(Message arg0) {
		synchronized (numReceived) {
			numReceived++;
		}
	}
}
