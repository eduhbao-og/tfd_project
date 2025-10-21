import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class StreamletProtocol {

    //list of sockets corresponding to the middleware sockets for broadcasting
    private List<Socket> sockets = new ArrayList<>();

    public StreamletProtocol() {

        for(int i = 0; i < Utils.EPOCHS; i++) {
            /*TODO:
            *
            * - epoch loop logic
            * - using a hash function, decide which node is the next leader
            *
            * */
        }

    }

}
