import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
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
    private HashMap<Block, Integer> proposed_blocks = new HashMap<>();

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
            proposed_blocks = new HashMap<>();
            selectLeader();
            if (leader_id == node_id) {
                Block previous_block = blockchain.getBestChainBlock();
                List<Transaction> transactions = blockchain.getPreviousTransactions(previous_block);
                transactions.addAll(tg.getTransactions(num_nodes));
                URB_broadcast(new Message(Utils.MessageType.PROPOSE, Block.createBlock(previous_block.getHash(), i, previous_block.getLength() + 1, transactions), node_id));
            }
            //TODO - logic for waiting epoch duration and advancing to next iteration (maybe timer)
        }
    }

    private void URB_broadcast(Message m) {
        urb.broadcast(m);
    }

    public void URB_deliver(Message m) {
        switch (m.getType()) {
            case PROPOSE -> {
                // logic for deciding if the proposed block is voted or not
                Block proposed = (Block) m.getContent();
                List<Block> longestChain = blockchain.getLongestNotarizedChain();
                if (longestChain.getLast().getHash().equals(proposed.getPrevHash())) {
                    if (proposed_blocks.containsKey(proposed)) {
                        proposed_blocks.put(proposed, proposed_blocks.get(proposed) + 1);
                    } else {
                        proposed_blocks.put(proposed, 1);
                    }
                    notarize(proposed);
                    URB_broadcast(new Message(Utils.MessageType.VOTE, Block.createBlock(proposed.getPrevHash(),
                            proposed.getEpoch(), longestChain.getLast().getLength() + 1, new ArrayList<>()), node_id));
                }
            }
            case VOTE -> {
                // add to vote counter to notarize block
                Block proposed = (Block) m.getContent();
                if (proposed_blocks.containsKey(proposed)) {
                    proposed_blocks.put(proposed, proposed_blocks.get(proposed) + 1);
                } else {
                    proposed_blocks.put(proposed, 1);
                }
                notarize(proposed);
            }
        }
    }

    private void notarize(Block b) {
        if (proposed_blocks.get(b) > num_nodes/2) {
            b.setStatus(Utils.BlockStatus.NOTARIZED);
            blockchain.addBlock(b);
            List<Block> chain = blockchain.getBlockChain(b);
            int past_epoch = 0;
            int count = 0;
            for(Block block : chain) {
                if(count == 3) {
                    for(Block block1 : chain) {
                        if(block1.getStatus() != Utils.BlockStatus.FINALIZED)
                            block1.setStatus(Utils.BlockStatus.FINALIZED);
                    }
                    break;
                }
                if(!block.getHash().equals("0")) {
                    if (block.getEpoch() - past_epoch == 1) {
                        count++;
                    } else {
                        count = 0;
                    }
                }
                past_epoch = block.getEpoch();
            }
        }
    }

    public int getLeader_id() {
        return leader_id;
    }

    public int getNode_id(){
        return node_id;
    }
}
