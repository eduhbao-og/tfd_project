import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Thread.sleep;

public class Communication {

    private List<ServerSocket> serverSockets;
    private List<Socket> sockets;
    private List<ObjectOutputStream> outputs ;
    private URBLayer urb;

    public Communication(List<InetAddress> ips , List<Integer> ports, int port, URBLayer urb){
        this.urb = urb;
        this.serverSockets = new ArrayList<>(ips.size());
        this.sockets = new ArrayList<>(ips.size());
        outputs = new ArrayList<>(ips.size());
        urb.setCommunication(this);

        for(int i = 0; i < ips.size(); i++){
            try {
                serverSockets.add(new ServerSocket(ports.get(i)));
                (new Server(serverSockets.get(i), urb)).start();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        for(int i = 0; i < ips.size(); i++){
            while (true){
                try {
                    System.out.println(ips.get(i).getHostAddress() + " port: " + port);
                    sockets.add(new Socket(ips.get(i).getHostAddress(),port));
                    outputs.add(new ObjectOutputStream(sockets.get(i).getOutputStream()));
                    break;
                } catch (IOException e) {
                    continue;
                }
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
            System.out.println("PORTA BARALHOOOOO!!!!" + ss.getLocalPort());
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
                        urb.deliver((Message) message);
                        System.out.println("message received: " + message);
                    }
                }
            }catch (ClassNotFoundException | IOException e){
                e.printStackTrace();
            }
        }

    }

}
