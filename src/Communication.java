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
    private boolean running = false;

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

        startTask = ses.schedule(() -> {
            running = true;
            urb.start(0);
        }, startTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS);

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
                outputs.remove(out);
                try {
                    socket.close();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                System.err.println(">>> Outgoing node connection ended <<<");
                while (true) {
                    try {
                        Thread.sleep(500);
                        InetAddress addr = socket.getInetAddress();
                        int port = socket.getPort();

                        new OutgoingConnection(new Socket(addr, port)).start();
                        break;
                    } catch (InterruptedException | IOException ignored) {
                    }
                }
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
                    outputs.remove(out);
                    socket.close();
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
                switch (((Message) obj).getType()) {
                    // logic to sync the start time with other nodes
                    case Utils.MessageType.SYNC -> {
                        long otherTime = (long) ((Message) obj).getContent()[0];
                        if (running) {
                            System.out.println("SENDING RECONNECTION INFORMATION");
                            System.out.println("START TIME: " + ((Message) obj).getContent()[0]);
                            System.out.println("EPOCH: " + urb.getEpoch());
                            broadcast(new Message(Utils.MessageType.RECONNECT, new Object[]{startTime, urb.getEpoch()}, nodeId));
                        } else if (Math.max(otherTime, startTime) != startTime) {
                            startTime = otherTime;
                            startTask.cancel(true);
                            startTask = ses.schedule(() -> {
                                running = true;
                                urb.start(0);
                            }, startTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
                        }
                    }
                    // logic to reconnect with other nodes after crash
                    case Utils.MessageType.RECONNECT -> {
                        //ignore if running
                        System.out.println(running);
                        if (!running) {
                            int epoch = (int) ((Message) obj).getContent()[1];
                            running = true;
                            int duration = urb.getEpoch_duration();
                            startTime = (long) ((Message) obj).getContent()[0];
                            System.out.println("RECONNECTING");
                            System.out.println("START TIME: " + startTime);
                            System.out.println("EPOCH: " + epoch);
                            startTask.cancel(true);
                            startTask = ses.schedule(() -> {
                                urb.start(epoch);
                            }, (startTime + ((epoch) * 1000 * duration)) - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
                        }
                    }
                    // normal messages get urb delivered
                    default -> {
                        urb.deliver((Message) obj);
                    }
                }
            }
        }
    }
}
