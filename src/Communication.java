import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Communication {

    private List<ServerSocket> serverSockets = new ArrayList<ServerSocket>();
    private List<Socket> sockets = new ArrayList<>();
    private List<ObjectOutputStream> outputs ;
    private URBLayer urb;

    public Communication(List<InetAddress> ips , List<Integer> ports, URBLayer urb){
        this.urb = urb;
        this.serverSockets = new ArrayList<ServerSocket>(ips.size());
        this.sockets = new ArrayList<Socket>(ips.size());
        outputs = new ArrayList<ObjectOutputStream>(ips.size());

        for(int i = 0; i != ips.size(); i++){
            try {
                sockets.set(i, new Socket(ips.get(i).getHostAddress(),ports.get(i)));
                outputs.set(i, new ObjectOutputStream(sockets.get(i).getOutputStream()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        for(int i = 0; i != serverSockets.size(); i++){
            try {
                serverSockets.set(i, new ServerSocket(i));
                (new Server(serverSockets.get(i), urb)).start();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

    public void broadcast(Message m){
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
