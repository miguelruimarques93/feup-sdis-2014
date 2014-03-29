package pt.up.fe.sdis.proj1.utils;

import java.util.concurrent.atomic.AtomicInteger;

import pt.up.fe.sdis.proj1.messages.Message;
import rx.Observer;

public class CounterObserver implements Observer<Message> {
	private AtomicInteger numReceived = new AtomicInteger(0);
	
	public boolean received() { return numReceived.get() > 0; }
	
	public int getNumReceived() { return numReceived.get(); }
	
	@Override
	public void onCompleted() { }

	@Override
	public void onError(Throwable arg0) { }

	@Override
	public void onNext(Message arg0) {
		numReceived.incrementAndGet();
	}
}
