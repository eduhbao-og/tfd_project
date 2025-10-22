import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class Blockchain {

    List<Block> chain = new ArrayList<>();

    public Blockchain() {
        chain.add(new Block("0", 0, 0));
    }

    public void addBlock(Block b) {
        chain.add(b);
    }

    public Block getBlock(int length) {
        for(Block b : chain) {
            if(b.getLength() == length)
                return b;
        }
        throw new NoSuchElementException("Block with length " + length + " doesn't exist.");
    }

    public Block getBlock(String hash) {
        for(Block b : chain) {
            if(b.getHash().equals(hash))
                return b;
        }
        throw new NoSuchElementException("Block with hash \"" + hash + "\" doesn't exist.");
    }

    public void setBlockStatus(int length, Utils.BlockStatus status) {
        for(Block b : chain) {
            if(b.getLength() == length)
                b.setStatus(status);
        }
    }

}
