import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

public class Blockchain {

    List<Block> chain = new ArrayList<>();

    public Blockchain() {
        chain.add(Block.createGenesisBlock());
    }

    public void addBlock(Block b) {
        chain.add(b);
    }

    public Block getBlock(int length) {
        for (Block b : chain) {
            if (b.getLength() == length)
                return b;
        }
        throw new NoSuchElementException("Block with length " + length + " doesn't exist.");
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
        throw new NoSuchElementException("Block with hash \"" + hash + "\" doesn't exist.");
    }

    public void setBlockStatus(int length, Utils.BlockStatus status) {
        for (Block b : chain) {
            if (b.getLength() == length)
                b.setStatus(status);
        }
    }

    public List<Block> getBlockChain(Block b) {
        List<Block> chain = new ArrayList<>();
        Block current = b;
        while(!current.getHash().equals("0")) {
            chain.add(current);
            current = getBlock(current.getPrevHash());
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
}
