import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Communication {

    private ServerSocket serverSocket;
    private List<ObjectOutputStream> outputs;
    private URBLayer urb;
    private int nodeId;
    private int nodes;
    private int readyConnections = 0;

    public Communication(int nodeId, List<InetAddress> ips, int serverPort, List<Integer> clientPorts, URBLayer urb) {
        this.nodeId = nodeId;
        this.nodes = ips.size();
        this.urb = urb;
        this.outputs = new ArrayList<>();

        // Start server socket to accept incoming connections
        try {
            serverSocket = new ServerSocket(serverPort);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        new ServerThread().start();

        // Connect to nodes with lower IDs
        for (int i = 0; i < nodeId - 1; i++) {
            while (true) {
                try {
                    Socket socket = new Socket(ips.get(i).getHostAddress(), clientPorts.get(i));
                    new OutgoingConnection(socket).start();
                    break;
                } catch (IOException e) {
                    try {
                        Thread.sleep(200); // retry after a short delay
                    } catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        }

        urb.setCommunication(this);
    }

    public synchronized void broadcast(Message m) {
        for (ObjectOutputStream out : outputs) {
            try {
                out.writeObject(m);
                out.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private synchronized void connectionReady(ObjectOutputStream out) {
        outputs.add(out);
        readyConnections++;

        System.out.println("CON: " + readyConnections);

        if (readyConnections == nodes) {
            System.out.println("STARTING...");
            urb.start();
        }
    }

    // Handles outgoing connections to nodes with lower IDs
    private class OutgoingConnection extends Thread {
        private Socket socket;

        public OutgoingConnection(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                // Outgoing connection: write first, then read
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                out.flush();
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

                connectionReady(out);
                listenIncoming(in);

            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    // Handles incoming connections from nodes with higher IDs
    private class IncomingConnection extends Thread {
        private Socket socket;

        public IncomingConnection(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                // Incoming connection: read first, then write
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                out.flush();

                connectionReady(out);
                listenIncoming(in);

            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    // Thread to accept incoming connections
    private class ServerThread extends Thread {
        @Override
        public void run() {
            while (true) {
                try {
                    Socket socket = serverSocket.accept();
                    new IncomingConnection(socket).start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // Listen for messages from a connected node
    private void listenIncoming(ObjectInputStream in) throws IOException, ClassNotFoundException {
        while (true) {
            Object obj = in.readObject();
            if (obj instanceof Message) {
                urb.deliver((Message) obj);
            }
        }
    }
}
