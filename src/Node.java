import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Node {

    private int id;
    //list of sockets corresponding to the middleware sockets for broadcasting
    private List<Socket> sockets = new ArrayList<>();

}
