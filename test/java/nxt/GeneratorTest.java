/*
 * Copyright © 2013-2016 The Nxt Core Developers.
 * Copyright © 2016-2017 Jelurida IP B.V.
 * Copyright © 2017-2018 Rya Developers.
 *
 * See the LICENSE.txt file at the top-level directory of this distribution
 * for licensing information.
 *
 * Unless otherwise agreed in a custom licensing agreement with Jelurida B.V.,
 * no part of the Nxt software, including this file, may be copied, modified,
 * propagated, or distributed except according to the terms contained in the
 * LICENSE.txt file.
 *
 * Removal or modification of this copyright notice is prohibited.
 *
 */

package nxt;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigInteger;

public class GeneratorTest extends BlockchainTest {

    /**
     * Simulate the forging process calculations
     */
    @Ignore
    @Test
    public void forge() {
        byte[] publicKey = ALICE.getPublicKey();
        BlockImpl lastBlock = blockchain.getLastBlock();
        BigInteger hit = Generator.getHit(publicKey, lastBlock);
        Account account = Account.getAccount(publicKey);
        BigInteger effectiveTrust = BigInteger.valueOf(account == null || account.getEffectiveTrust() <= 0 ? 0 : account.getEffectiveTrust());
        long hitTime = Generator.getHitTime(effectiveTrust, hit, lastBlock);
        long deadline = hitTime - lastBlock.getTimestamp();
        Generator generator = Generator.startForging(ALICE.getSecretPhrase());
        int i=1;
        try {
            while (i<deadline) {
                Assert.assertFalse(generator.forge(lastBlock, lastBlock.getTimestamp() + i));
                i += 100;
            }
            Assert.assertEquals(true, generator.forge(lastBlock, (int)hitTime + 1));
        } catch (BlockchainProcessor.BlockNotAcceptedException e) {
            e.printStackTrace();
        }

        // Now the block is broadcast to all peers
        // This is what the peer which receives the block does
        lastBlock = blockchain.getLastBlock();
        Assert.assertEquals(hitTime + 1, lastBlock.getTimestamp());
        try {
            Assert.assertTrue(lastBlock.verifyGenerationSignature());
        } catch (BlockchainProcessor.BlockOutOfOrderException e) {
            e.printStackTrace();
        }
    }

}
