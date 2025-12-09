import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Blockchain {

    List<Block> chain = new CopyOnWriteArrayList<>();
    List<Block> proposed_notarized_chain = new CopyOnWriteArrayList<>();

    public Blockchain() {
        chain.add(Block.createGenesisBlock());
    }

    public Blockchain(List<Block> chain) {
        this.chain = chain;
    }

    public void addBlock(Block b) {
        chain.add(b);
    }

    public Block getBestChainBlock() {
        Block bestBlock = chain.getLast();
        int numNotarized = 0;
        for (Block b : chain.reversed()) {
            int count = numberOfNotarized(getBlockChain(b));
            if (count > numNotarized) {
                numNotarized = count;
                bestBlock = b;
            }
        }
        return bestBlock;
    }

    private int numberOfNotarized(List<Block> blockchain) {
        int count = 0;
        for(Block b : blockchain) {
            if (b.getStatus() == Utils.BlockStatus.NOTARIZED || b.getStatus() == Utils.BlockStatus.FINALIZED) {
                count++;
            }
        }
        return count;
    }

    public List<Block> getLongestNotarizedChain() {
        List<Block> longest = new ArrayList<>();
        int numNotarized = 0;
        for (Block b : chain.reversed()) {
            List<Block> current = getBlockChain(b);
            int count = numberOfNotarized(current);
            if (count > numNotarized) {
                numNotarized = count;
                longest = current;
            }
        }
        return longest;
    }

    public Block getBlock(String hash) {
        for (Block b : chain) {
            if (b.getHash().equals(hash))
                return b;
        }
        return null;
    }

    public List<Block> getBlockChain(Block b) {
        List<Block> chain = new ArrayList<>();
        Block current = b;
        while(!current.getHash().equals("0")) {
            chain.add(current);
            current = getBlock(current.getPrevHash());
            if(current == null) {
                //TODO - look at received parent list
            }
        }
        chain.add(current);
        return chain.reversed();
    }

    // returns the previous unconfirmed transactions, as in, the transactions from non-final blocks
    public List<Transaction> getPreviousTransactions(Block previousBlock) {
        List<Transaction> transactions = new ArrayList<>(previousBlock.getTransactions());
        List<Block> blockchain = getBlockChain(previousBlock).reversed();
        for(Block b : blockchain) {
            if (b.getStatus() == Utils.BlockStatus.FINALIZED) {
                transactions.removeAll(b.getTransactions());
                break;
            }
        }
        return transactions;
    }

    public void setProposedNotarizedChain(List<Block> proposed_notarized_chain) {
        this.proposed_notarized_chain = proposed_notarized_chain;
    }

    @Override
    public String toString() {
        String res = ">>>> CHAIN >>>>";
        for(Block b: chain){
            res += "\n-> " + b;
        }
        return res;
    }
}
