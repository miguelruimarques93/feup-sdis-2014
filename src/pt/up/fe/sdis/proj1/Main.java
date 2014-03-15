package pt.up.fe.sdis.proj1;

import pt.up.fe.sdis.proj1.messages.Message;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

public class Main {

    public static void hello(String... names) {
        Observable.from(names).subscribe(new Observer<String>() {

            @Override
            public void onCompleted() {
                // TODO Auto-generated method stub
                
            }

            @Override
            public void onError(Throwable arg0) {
                // TODO Auto-generated method stub
                
            }

            @Override
            public void onNext(String s) {
                System.out.println("Hello " + s + "!");
            }
        });
    }
    
    private static Runnable serverMockUp = new Runnable() {
        
        @Override
        public void run() {
            Message msg = new Message(Message.Type.DELETE);
            
            System.out.println("sender: " + Thread.currentThread().getId());
            
            while (!done) {
                ps.onNext(msg);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            
        }
    };
    private static boolean done = false;
    private static PublishSubject<Message> ps = PublishSubject.create();
    
    static class MyObserver implements Observer<Message> {

        public MyObserver(String name) {
            _name = name;
        }
        
        private final String _name;
        
        @Override
        public void onCompleted() {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void onError(Throwable arg0) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void onNext(Message arg0) {
            System.out.println("obs '"+ _name +"': " + Thread.currentThread().getId());
            
        }
        
    }
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("main: " + Thread.currentThread().getId());
        
        Observable<Message> obsvb = ps.observeOn(Schedulers.io());
        
        new Thread(serverMockUp).start();
        
        
        for (int i = 0; i < 10; ++i) {
            Subscription s1 = obsvb.subscribe(new MyObserver("s" + Integer.toString(i + 1)));
            Subscription s2 = obsvb.subscribe(new MyObserver("s" + Integer.toString(i + 11)));
            Thread.sleep(200);
            s1.unsubscribe();
            s2.unsubscribe();
        }        
        Thread.sleep(200);
        System.out.println("Done!");
        done = true;
    }

}
