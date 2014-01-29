package uk.co.mattthomson.gitcoin;

import com.google.common.base.Stopwatch;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

public class SaltFinderTest {
    private static final String TREE = "ddf2858599b40f510e7969c9d63ac2147eda5e80";
    private static final String PARENT = "0000003420348303e0bd0e3de4a087d6286b3d09";
    private static final String TIMESTAMP = "1390509399";
    private static final String DIFFICULTY = "01";

    @Test
    public void shouldMineCoin() throws Exception {
        HashCalculator calculator = new HashCalculator(TREE, PARENT, TIMESTAMP);
        SaltFinder finder = new SaltFinder(calculator, DIFFICULTY);

        Stopwatch stopwatch = Stopwatch.createStarted();
        String salt = finder.call();
        stopwatch.stop();

        System.out.println(stopwatch);

        byte[] difference = calculator.calculateHash(salt);

        assertThat(difference[0] == 0);
        assertThat(difference[1] == 0);
    }
}
