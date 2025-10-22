public class StreamletProtocol {

    private int num_nodes;
    private int epoch_duration;

    public StreamletProtocol(int num_nodes, int duration) {
        this.num_nodes = num_nodes;
        epoch_duration = 2*duration;

        for(int i = 0; i < Utils.EPOCHS; i++) {
            /*TODO:
            *
            * - epoch loop logic
            * - using a hash function, decide which node is the next leader
            *
            * */
        }

    }

    private void URB_broadcast(Message m) {

    }

}
