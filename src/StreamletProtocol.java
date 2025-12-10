import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class StreamletProtocol {

    private int num_nodes;
    private int epoch_duration;
    private int epoch = 0;
    private int node_id;
    private TransactionGenerator transactionGenerator;
    private Blockchain blockchain = new Blockchain();
    private URBLayer urb;
    private int leader_id;
    private long seed;
    private ConcurrentHashMap<Block, Integer> proposed_blocks = new ConcurrentHashMap<>();
    private boolean byzantine;
    private int confusion_start;
    private int confusion_duration;
    private final long initialSeed;

    public StreamletProtocol(int num_nodes, int duration, int node_id, long seed, URBLayer urb, boolean crashing, int confusion_start, int confusion_duration) {
        this.urb = urb;
        transactionGenerator = new TransactionGenerator(num_nodes);
        this.node_id = node_id;
        this.num_nodes = num_nodes;
        initialSeed = seed;
        this.seed = seed;
        epoch_duration = 2 * duration;
        this.byzantine = crashing;
        this.confusion_start = confusion_start;
        this.confusion_duration = confusion_duration;
        urb.setStreamlet(this);
    }

    public void start(int ep){
        this.epoch = ep;
        Timer timer = new Timer(true);
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (epoch > 0) {
                    System.out.println(Utils.ANSI_WHITE + "=====================================================================================" + Utils.ANSI_RESET);
                    System.out.print(Utils.ANSI_BLUE + "EPOCH" + Utils.ANSI_RESET + " - " + epoch +
                            " | " + Utils.ANSI_BLUE + "LEADER" + Utils.ANSI_RESET + " - " + leader_id +
                            " | " + Utils.ANSI_BLUE + "TIMESTAMP" + Utils.ANSI_RESET +  " - ");
                    System.out.println((new SimpleDateFormat("HH:mm:ss")).format(new Date(System.currentTimeMillis())));
                    System.out.println(blockchain);
                }
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
            seed = initialSeed + epoch;
            selectLeader();
        }

        if (leader_id == node_id) {
            List<Block> parent_chain = blockchain.getLongestNotarizedChain();

            Block previous_block = parent_chain.getLast();
            List<Transaction> transactions = blockchain.getPreviousTransactions(previous_block);
            transactions.addAll(transactionGenerator.getTransactions(2));
            Block proposed = Block.createNewBlock(previous_block.getHash(), epoch, previous_block.getLength() + 1, transactions);
            proposed_blocks.put(proposed, 1);

            Object[] content = new Object[2];
            content[0] = proposed;
            content[1] = Utils.deepCopy(parent_chain);

            URB_broadcast(new Message(Utils.MessageType.PROPOSE, content, node_id));
        }
    }

    private void URB_broadcast(Message m) {
        urb.broadcast(m);
    }

    public synchronized void URB_deliver(Message m) {
        // checks if process is out of confusion period/is correct
        if (!byzantine || (epoch < confusion_start || epoch >= confusion_start + confusion_duration)) {
            switch (m.getType()) {
                case PROPOSE -> {
                    // extracting contents from message
                    Block proposed = (Block) m.getContent()[0];
                    List<Block> proposed_notarized_chain = (List<Block>) m.getContent()[1];
                    blockchain.setProposedNotarizedChain(proposed_notarized_chain);

                    // fix holes in blockchain
                    blockchain.getBlockChain(proposed);

                    List<Block> longestChain = blockchain.getLongestNotarizedChain();

                    // logic for deciding if the proposed block is voted or not
                    if (longestChain.getLast().getHash().equals(proposed.getPrevHash())) {
                        Block block = getBlock(proposed);
                        if (proposed_blocks.containsKey(block)) {
                            proposed_blocks.put(block, proposed_blocks.get(block) + 1);
                        } else {
                            proposed_blocks.put(block, 2);
                        }
                        notarize(block);

                        Object[] content = new Object[2];
                        content[0] = Block.createBlock(Utils.BlockStatus.PROPOSED,
                                block.getPrevHash(), block.getHash(),
                                block.getEpoch(), longestChain.getLast().getLength() + 1,
                                new ArrayList<>());
                        content[1] = Utils.deepCopy(proposed_notarized_chain);
                        URB_broadcast(new Message(Utils.MessageType.VOTE, content, node_id));
                    }
                }
                case VOTE -> {
                    // add to vote counter to notarize block
                    Block proposed = (Block) m.getContent()[0];
                    Block block = getBlock(proposed);
                    List<Block> proposed_notarized_chain = (List<Block>) m.getContent()[1];
                    blockchain.setProposedNotarizedChain(proposed_notarized_chain);
                    if (proposed_blocks.containsKey(block)) {
                        proposed_blocks.put(block, proposed_blocks.get(block) + 1);
                    } else {
                        proposed_blocks.put(block, 1);
                    }
                    notarize(block);
                }
            }
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
            int count = 1;
            for (Block block1 : chain) {
                if (!block1.getHash().equals("0")) {
                    if (block1.getEpoch() - past_epoch == 1) {
                        count++;
                        if (count == 3) {
                            for (Block block2 : chain) {
                                if (block2.getStatus() != Utils.BlockStatus.FINALIZED && !block2.equals(b)){
                                    block2.setStatus(Utils.BlockStatus.FINALIZED);
                                }
                            }
                            b.removeTransactions(blockchain.getBlock(b.getPrevHash()).getTransactions());
                            break;
                        }
                    } else {
                        count = 1;
                    }
                }
                past_epoch = block1.getEpoch();
            }
        }
    }

    public int getNode_id() {
        return node_id;
    }

    public int getEpoch() {
        return epoch;
    }

    public int getEpoch_duration() {
        return epoch_duration;
    }

}
