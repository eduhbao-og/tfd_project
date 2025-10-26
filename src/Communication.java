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
    private int con;
    private int nodes;

    public Communication(int nodeId, List<InetAddress> ips , int serverPort, List<Integer> clientPorts, URBLayer urb){
        this.nodeId = nodeId;
        this.urb = urb;
        con = 0;
        nodes = ips.size();
        outputs = new ArrayList<ObjectOutputStream>(ips.size());
        sockets = new ArrayList<Socket>(ips.size());
        //this.serverSockets = new ArrayList<>(ips.size());
//        try {
//            this.serverSocket = new ServerSocket(serverPort);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//        (new Server(serverSocket, urb)).start();
//        this.sockets = new ArrayList<>(ips.size());
//        outputs = Collections.synchronizedList(new ArrayList<>());
//        urb.setCommunication(this);

        try {
            this.serverSocket = new ServerSocket(serverPort);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        for(int i = 0; i < ips.size(); i++){
            (new Server(serverSocket, urb)).start();
        }

        for(int i = 0; i < nodeId - 1; i++){
            while (true){
                try {
                    Socket s = new Socket(ips.get(i).getHostAddress(),clientPorts.get(i));
                    sockets.add(s);
                    outputs.add(new ObjectOutputStream(s.getOutputStream()));
                    streamlet();
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
        urb.setCommunication(this);
    }

    private void streamlet(){
        con ++;
        System.out.println("CON: " + con);
        System.out.println("NODES: " + nodes);
        if(con == nodes)
            urb.start();
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

    private class Client extends Thread {
        private ObjectInputStream in;
        private Socket socket;

        public Client(Socket socket){
            this.socket = socket;
        }

        @Override
        public void run(){
            while(true) {
                try {
                    outputs.add(new ObjectOutputStream(socket.getOutputStream()));
                    in = new ObjectInputStream(socket.getInputStream());
                    streamlet();
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

    private class Server extends Thread{
        private ObjectInputStream in;
        private ServerSocket ss;

        public Server(ServerSocket ss, URBLayer urb){
            this.ss = ss;
            System.out.println("PORTA BARALHOOOOO!!!!" + ss.getLocalPort());
        }

        @Override
        public void run(){
            while(true) {
                try {
                    Socket socket = ss.accept();
                    outputs.add(new ObjectOutputStream(socket.getOutputStream()));
                    in = new ObjectInputStream(socket.getInputStream());
                    streamlet();
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
