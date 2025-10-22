import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class Communication {

    private int id;
    private List<ServerSocket> serverSockets = new ArrayList<ServerSocket>();
    private List<Socket> sockets = new ArrayList<>();
    private List<ObjectOutputStream> outputs ;
    private URBLayer urb;

    public Communication(List<ServerSocket> serverSockets, List<Socket> sockets, URBLayer urb){
        this.serverSockets = serverSockets;
        this.sockets = sockets;
        new ArrayList<ObjectOutputStream>(sockets.size());

        for(int i = 0; i != sockets.size(); i++){
            try {
                outputs.set(i, new ObjectOutputStream(sockets.get(i).getOutputStream()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        for(int i = 0; i != serverSockets.size(); i++){
            (new Server(serverSockets.get(i), urb)).start();
        }

    }

    public void send(Message m){
        for (ObjectOutputStream o : outputs){
            try {
                o.writeObject(m);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static class Server extends Thread{
//        private ObjectOutputStream out;
        private ObjectInputStream in;
        private ServerSocket ss;
        private URBLayer urb;

        public Server(ServerSocket ss, URBLayer urb){
            this.ss = ss;
            this.urb = urb;
        }

        @Override
        public void run(){
            try {
                Socket socket = ss.accept();
//                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());
                serve();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }

        private void serve(){
            try{
                while(true) {
                    Object message = in.readObject();
                    if (message instanceof Message) {
                        urb.receive((Message) message);
                    }
                }
            }catch (ClassNotFoundException | IOException e){
                e.printStackTrace();
            }
        }

    }

}
