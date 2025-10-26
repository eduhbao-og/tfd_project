import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.lang.Thread.sleep;

public class Communication {

    //private List<ServerSocket> serverSockets;
    private ServerSocket serverSocket;
    private List<Socket> sockets;
    private List<ObjectOutputStream> outputs;
    private URBLayer urb;
    private int nodeId;

    public Communication(int nodeId, List<InetAddress> ips , List<Integer> serverPorts, List<Integer> clientPorts, URBLayer urb){
        this.nodeId = nodeId;
        this.urb = urb;
        //this.serverSockets = new ArrayList<>(ips.size());
        try {
            this.serverSocket = new ServerSocket(serverPorts.get(nodeId));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        (new Server(serverSocket, urb)).start();
        this.sockets = new ArrayList<>(ips.size());
        outputs = Collections.synchronizedList(new ArrayList<>());
        urb.setCommunication(this);

//        for(int i = 0; i < ips.size(); i++){
//            try {
//                serverSockets.add(new ServerSocket(serverPorts.get(i)));
//                (new Server(serverSockets.get(i), urb)).start();
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        }

        for(int i = 0; i < nodeId; i++){
            while (true){
                try {
                    sockets.add(new Socket(ips.get(i).getHostAddress(),clientPorts.get(i)));
                    outputs.add(new ObjectOutputStream(sockets.get(i).getOutputStream()));
                    break;
                } catch (IOException e) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        }
    }

    public void broadcast(Message m){
        for (ObjectOutputStream o : outputs){
            try {
                o.writeObject(m);
                o.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private class Server extends Thread{
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
            while(true) {
                try {
                    Socket socket = ss.accept();
                    outputs.add(new ObjectOutputStream(socket.getOutputStream()));
                    in = new ObjectInputStream(socket.getInputStream());
                    serve();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        private void serve(){
            try{
                while(true) {
                    Object message = in.readObject();
                    if (message instanceof Message) {
                        urb.deliver((Message) message);
                    }
                }
            } catch (ClassNotFoundException | IOException e){
                e.printStackTrace();
            }
        }

    }

}
