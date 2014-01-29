package uk.co.mattthomson.gitcoin;

import com.google.common.primitives.UnsignedBytes;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import java.util.UUID;
import java.util.concurrent.*;

public class SaltFinder {
    private final HashCalculator calculator;
    private final byte[] difficulty;

    public SaltFinder(HashCalculator calculator, String difficulty) throws DecoderException {
        this.calculator = calculator;
        this.difficulty = Hex.decodeHex(difficulty.toCharArray());
    }

    public String findSalt() throws Exception {
        while (true) {
            String salt = UUID.randomUUID().toString();
            byte[] hash = calculator.calculateHash(salt);

            if (UnsignedBytes.lexicographicalComparator().compare(hash, difficulty) < 0) {
                return salt;
            }
        }
    }

    public static void main(String[] args) throws Exception {
        String tree = args[0];
        String parent = args[1];
        String timestamp = args[2];
        String difficulty = args[3];

        HashCalculator calculator = new HashCalculator(tree, parent, timestamp);
        SaltFinder finder = new SaltFinder(calculator, difficulty);

        System.out.println(finder.findSalt());
    }
}
