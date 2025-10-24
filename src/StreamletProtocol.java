import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class StreamletProtocol {

    private int num_nodes;
    private int epoch_duration;
    private int node_id;
    private TransactionGenerator tg;
    private Blockchain blockchain = new Blockchain();
    private URBLayer urb;
    private int leader_id;
    private long seed;

    public StreamletProtocol(int num_nodes, int duration, int node_id, long seed, URBLayer urb) {
        this.urb = urb;
        tg = new TransactionGenerator(num_nodes);
        this.node_id = node_id;
        this.num_nodes = num_nodes;
        this.seed = seed;
        epoch_duration = 2 * duration;
        urb.setStreamlet(this);
        start();
    }

    private void selectLeader() {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] seedBytes = ByteBuffer.allocate(Long.BYTES).putLong(seed).array();
            byte[] hashBytes = digest.digest(seedBytes);
            int hashInt = ByteBuffer.wrap(hashBytes).getInt();
            hashInt = Math.abs(hashInt);
            leader_id = hashInt % num_nodes;
            seed++;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private void start() {
        for (int i = 0; i < Utils.EPOCHS; i++) {
            selectLeader();
            if(leader_id == node_id) {
                Block previous_block = blockchain.getBestChainBlock();
                List<Transaction> transactions = blockchain.getPreviousTransactions(previous_block);
                transactions.addAll(tg.getTransactions(num_nodes));
                URB_broadcast(new Message(Utils.MessageType.PROPOSE, Block.createBlock(previous_block.getHash(), i, previous_block.getLength() + 1, transactions), node_id));
            }
            //TODO
        }
    }

    private void URB_broadcast(Message m) {
        urb.broadcast(m);
    }

    public void URB_deliver(Message m) {
        switch (m.getType()) {
            case PROPOSE -> {
                // logic for deciding if the proposed block is voted or not
            }
            case VOTE -> {
                // add to vote counter to notarize block
            }
            case ECHO -> {
                // logic for message echo
            }
        }
    }

    public int getLeader_id() {
        return leader_id;
    }
}
