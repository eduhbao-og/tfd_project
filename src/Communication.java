import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Communication {

    private ServerSocket serverSocket;
    private List<ObjectOutputStream> outputs;
    private URBLayer urb;
    private int nodeId;
    private long startTime = 10000 + System.currentTimeMillis();
    private ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> startTask;

    public Communication(int nodeId, List<InetAddress> ips, int serverPort, List<Integer> clientPorts, URBLayer urb) {
        this.nodeId = nodeId;
        this.urb = urb;
        this.outputs = new ArrayList<>();

        try {
            serverSocket = new ServerSocket(serverPort);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        new ServerThread().start();

        startTask = ses.schedule(() -> urb.start(), startTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS);

        for (int i = 0; i < nodeId - 1; i++) {
            while (true) {
                try {
                    Socket socket = new Socket(ips.get(i).getHostAddress(), clientPorts.get(i));
                    new OutgoingConnection(socket).start();
                    break;
                } catch (IOException e) {
                    try {
                        Thread.sleep(200);
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

    private class OutgoingConnection extends Thread {
        private Socket socket;
        private ObjectOutputStream out;

        public OutgoingConnection(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                out = new ObjectOutputStream(socket.getOutputStream());
                out.flush();
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

                // send sync message
                try {
                    out.writeObject(new Message(Utils.MessageType.SYNC, new Object[]{startTime}, nodeId));
                    out.flush();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                outputs.add(out);
                listenIncoming(in);

            } catch (IOException | ClassNotFoundException e) {
                System.err.println(">>> Outgoing node connection ended <<<");
            }
        }
    }

    private class IncomingConnection extends Thread {
        private Socket socket;
        private ObjectOutputStream out;

        public IncomingConnection(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                out = new ObjectOutputStream(socket.getOutputStream());
                out.flush();

                // send sync message
                try {
                    out.writeObject(new Message(Utils.MessageType.SYNC, new Object[]{startTime}, nodeId));
                    out.flush();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                outputs.add(out);
                listenIncoming(in);

            } catch (IOException | ClassNotFoundException e) {
                System.err.println(">>> Incoming node connection ended <<<");
            } finally {
                try {
                    socket.close();
                    outputs.remove(out);
                } catch (IOException e) {
                    System.err.println(">>> Failed to close socket <<<");
                }
            }
        }
    }

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

    private void listenIncoming(ObjectInputStream in) throws IOException, ClassNotFoundException {
        while (true) {
            Object obj = in.readObject();
            if (obj instanceof Message) {
                // logic to sync the start time with other nodes
                if (((Message) obj).getType() == Utils.MessageType.SYNC) {
                    long otherTime = (long)((Message) obj).getContent()[0];
                    if (Math.max(otherTime, startTime) != startTime) {
                        startTime = otherTime;
                        startTask.cancel(true);
                        startTask = ses.schedule(() -> urb.start(), startTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
                    }
                } else {
                    urb.deliver((Message) obj);
                }
            }
        }
    }
}
