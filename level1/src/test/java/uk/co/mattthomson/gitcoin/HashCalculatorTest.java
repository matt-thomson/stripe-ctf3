package uk.co.mattthomson.gitcoin;

import com.google.common.base.Stopwatch;
import org.apache.commons.codec.binary.Hex;
import org.junit.Test;

import java.util.Arrays;

import static org.fest.assertions.Assertions.assertThat;

public class HashCalculatorTest {
    private static final String TREE = "4561428216f0e66bea9a64d0dfd9922f9a2b6b95";
    private static final String PARENT = "0000005f9227ced308b4dd345075ff123887e8b0";
    private static final String TIMESTAMP = "1390512433";

    @Test
    public void shouldCalculateCorrectHash() {
        HashCalculator calculator = new HashCalculator(TREE, PARENT, TIMESTAMP);
        byte[] hash = calculator.calculateHash("b191b703-194a-4fb3-9038-097f2cdc0250");

        assertThat(Hex.encodeHexString(hash)).startsWith("da4ded");
    }

    @Test
    public void shouldBeFast() {
        HashCalculator calculator = new HashCalculator(TREE, PARENT, TIMESTAMP);
        Stopwatch stopwatch = Stopwatch.createStarted();

        for (int i = 0; i < 10000; i++) {
            calculator.calculateHash("b191b703-194a-4fb3-9038-097f2cdc0250");
        }

        stopwatch.stop();
        System.out.println(stopwatch);
    }
}
