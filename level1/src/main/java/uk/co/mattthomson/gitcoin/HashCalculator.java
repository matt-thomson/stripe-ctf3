package uk.co.mattthomson.gitcoin;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

import static com.google.common.base.Charsets.UTF_8;

public class HashCalculator {
    private static final HashFunction HASHER = Hashing.sha1();

    private final String gitObjectPrefix;

    public HashCalculator(String tree, String parent, String timestamp) {
        this.gitObjectPrefix = String.format("commit 254\0tree %s\n" +
                "parent %s\n" +
                "author CTF user <me@example.com> %s +0000\n" +
                "committer CTF user <me@example.com> %s +0000\n" +
                "\n" +
                "Give me a Gitcoin\n" +
                "\n", tree, parent, timestamp, timestamp);
    }

    public byte[] calculateHash(String commitSalt) {
        String gitObject = gitObjectPrefix.concat(commitSalt).concat("\n");
        return HASHER.hashString(gitObject, UTF_8).asBytes();
    }
}
