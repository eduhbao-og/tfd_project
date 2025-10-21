import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Node {

    private int id;
    private ServerSocket server_socket;
    //list of sockets corresponding to the other nodes' sockets for broadcasting
    private List<Socket> sockets = new ArrayList<>();

}
