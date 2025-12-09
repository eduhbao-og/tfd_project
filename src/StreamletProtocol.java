import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class StreamletProtocol {

    private int num_nodes;
    private int epoch_duration;
    private int epoch = 0;
    private int node_id;
    private TransactionGenerator tg;
    private Blockchain blockchain = new Blockchain();
    private URBLayer urb;
    private int leader_id;
    private long seed;
    private ConcurrentHashMap<Block, Integer> proposed_blocks = new ConcurrentHashMap<>();
    private boolean byzantine;
    private int confusion_start = 1;
    private int confusion_duration = 2;

    public StreamletProtocol(int num_nodes, int duration, int node_id, long seed, URBLayer urb, boolean byzantine) {
        this.urb = urb;
        tg = new TransactionGenerator(num_nodes);
        this.node_id = node_id;
        this.num_nodes = num_nodes;
        this.seed = seed;
        epoch_duration = 2 * duration;
        this.byzantine = byzantine;
        urb.setStreamlet(this);
    }

    public void start(){
        Timer timer = new Timer(true);
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                compute();
            }
        };
        timer.scheduleAtFixedRate(task, 0, epoch_duration * 1000L);
    }

    private void selectLeader() {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] seedBytes = ByteBuffer.allocate(Long.BYTES).putLong(seed).array();
            byte[] hashBytes = digest.digest(seedBytes);
            int hashInt = ByteBuffer.wrap(hashBytes).getInt();
            hashInt = Math.abs(hashInt);
            leader_id = hashInt % num_nodes + 1;
            seed++;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private void compute() {
        epoch++;
        proposed_blocks = new ConcurrentHashMap<>();

        if (byzantine && !(epoch < confusion_start || epoch >= confusion_start + confusion_duration)) {
            leader_id = epoch % num_nodes;
        } else {
            selectLeader();
        }

        System.out.println("=====================================================================================");
        System.out.println("EPOCH: " + epoch + "; LEADER: " + leader_id);
        System.out.println("---------------------------------------------");
        System.out.println(blockchain);
        if (leader_id == node_id) {
            Block previous_block = blockchain.getBestChainBlock();
            List<Transaction> transactions = blockchain.getPreviousTransactions(previous_block);
            transactions.addAll(tg.getTransactions(2));
            Block proposed = Block.createNewBlock(previous_block.getHash(), epoch, previous_block.getLength() + 1, transactions);
            proposed_blocks.put(proposed, 1);
            URB_broadcast(new Message(Utils.MessageType.PROPOSE, proposed, node_id));
        }
    }

    private void URB_broadcast(Message m) {
        urb.broadcast(m);
    }

    public synchronized void URB_deliver(Message m) {
        if (!byzantine || (epoch < confusion_start || epoch >= confusion_start + confusion_duration)) {
            switch (m.getType()) {
                case PROPOSE -> {
                    // logic for deciding if the proposed block is voted or not
                    Block proposed = (Block) m.getContent();
                    List<Block> longestChain = blockchain.getLongestNotarizedChain();
                    if (longestChain.getLast().getHash().equals(proposed.getPrevHash())) {
                        Block block = getBlock(proposed);
                        if (proposed_blocks.containsKey(block)) {
                            proposed_blocks.put(block, proposed_blocks.get(block) + 1);
                        } else {
                            proposed_blocks.put(block, 2);
                        }
                        notarize(block);
                        URB_broadcast(new Message(Utils.MessageType.VOTE, Block.createBlock(Utils.BlockStatus.PROPOSED,
                                block.getPrevHash(), block.getHash(),
                                block.getEpoch(), longestChain.getLast().getLength() + 1,
                                new ArrayList<>()), node_id));
                    }
                }
                case VOTE -> {
                    // add to vote counter to notarize block
                    Block proposed = (Block) m.getContent();
                    Block block = getBlock(proposed);
                    if (proposed_blocks.containsKey(block)) {
                        proposed_blocks.put(block, proposed_blocks.get(block) + 1);
                    } else {
                        proposed_blocks.put(block, 1);
                    }
                    notarize(block);
                }
            }
        } else {

        }
    }

    private Block getBlock(Block b) {
        for(Block block : proposed_blocks.keySet()) {
            if(block.isEqual(b)) {
                return block;
            }
        }
        return b;
    }

    private void notarize(Block b) {
        if (proposed_blocks.get(b) > num_nodes / 2 && blockchain.getBlock(b.getHash()) == null) {
            b.setStatus(Utils.BlockStatus.NOTARIZED);
            blockchain.addBlock(b);

            //check for finalization
            List<Block> chain = blockchain.getBlockChain(b);
            int past_epoch = 0;
            int count = 0;
            for (Block block1 : chain) {
                if (count == 3) {
                    for (Block block2 : chain) {
                        if (block2.getStatus() != Utils.BlockStatus.FINALIZED && !block2.equals(b))
                            block2.setStatus(Utils.BlockStatus.FINALIZED);
                    }
                    b.removeTransactions(blockchain.getBlock(b.getPrevHash()).getTransactions());
                    break;
                }
                if (!block1.getHash().equals("0")) {
                    if (block1.getEpoch() - past_epoch == 1) {
                        count++;
                    } else {
                        count = 0;
                    }
                }
                past_epoch = block1.getEpoch();
            }
        }
    }

    public int getNode_id() {
        return node_id;
    }
}
