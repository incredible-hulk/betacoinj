/**
 * Copyright 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.betacoin.core;

import com.google.betacoin.params.MainNetParams;
import com.google.betacoin.params.RegTestParams;
import com.google.betacoin.params.TestNet2Params;
import com.google.betacoin.params.TestNet3Params;
import com.google.betacoin.params.UnitTestParams;
import com.google.betacoin.script.Script;
import com.google.betacoin.script.ScriptOpCodes;
import com.google.common.base.Objects;

import org.spongycastle.util.encoders.Hex;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import static com.google.betacoin.core.Utils.COIN;

/**
 * <p>NetworkParameters contains the data needed for working with an instantiation of a Bitcoin chain.</p>
 *
 * <p>This is an abstract class, concrete instantiations can be found in the params package. There are four:
 * one for the main network ({@link MainNetParams}), one for the public test network, and two others that are
 * intended for unit testing and local app development purposes. Although this class contains some aliases for
 * them, you are encouraged to call the static get() methods on each specific params class directly.</p>
 */
public abstract class NetworkParameters implements Serializable {
    /**
     * The protocol version this library implements.
     */
    public static final int PROTOCOL_VERSION = 70001;

    /**
     * The alert signing key originally owned by Satoshi, and now passed on to Gavin along with a few others.
     */
    //public static final byte[] SATOSHI_KEY = Hex.decode("000000000007840aaf100de8442ebecedf5b095cdbb9bc716bda9110971b28a49e0ead8564ff0db22209e0374782c093bb899692d524e9d6a6956e7c5ecbcd68284");
    //public static final byte[] SATOSHI_KEY = Hex.decode("000000000007840aaf100de8442ebecedf5b095cdbb9bc716bda9110971b28a49e0ead8564ff0db22209e0374782c093bb899692d524e9d6a6956e7c5ecbcd68284");
    public static final byte[] SATOSHI_KEY = Hex.decode("04fc9702847840aaf195de8442ebecedf5b095cdbb9bc716bda9110971b28a49e0ead8564ff0db22209e0374782c093bb899692d524e9d6a6956e7c5ecbcd68284");
    /** The string returned by getId() for the main, production network where people trade things. */
    public static final String ID_MAINNET = "org.betacoin.production";
    /** The string returned by getId() for the testnet. */
    public static final String ID_TESTNET = "org.betacoin.test";
    /** Unit test network. */
    public static final String ID_UNITTESTNET = "com.google.betacoin.unittest";

    /** The string used by the payment protocol to represent the main net. */
    public static final String PAYMENT_PROTOCOL_ID_MAINNET = "main";
    /** The string used by the payment protocol to represent the test net. */
    public static final String PAYMENT_PROTOCOL_ID_TESTNET = "test";

    // TODO: Seed nodes should be here as well.

    protected Block genesisBlock;
    protected BigInteger proofOfWorkLimit;
    protected int port;
    protected long packetMagic;
    protected int addressHeader;
    protected int p2shHeader;
    protected int dumpedPrivateKeyHeader;
    protected int interval;
    protected int averagingInterval = AVERAGING_INTERVAL;
    protected int averagingInterval2 = AVERAGING_INTERVAL2;
    protected int targetTimespan;
    protected int minActualTimespan = MIN_ACTUAL_TIMESPAN;
    protected int maxActualTimespan = MAX_ACTUAL_TIMESPAN;
    protected int minActualTimespan2 = MIN_ACTUAL_TIMESPAN2;
    protected int maxActualTimespan2 = MAX_ACTUAL_TIMESPAN2;
    protected int averatingTargetTimespan = AVERAGING_TARGET_TIMESPAN;
    protected int averatingTargetTimespan2 = AVERAGING_TARGET_TIMESPAN2;
    protected byte[] alertSigningKey;

    /**
     * See getId(). This may be null for old deserialized wallets. In that case we derive it heuristically
     * by looking at the port number.
     */
    protected String id;

    /**
     * The depth of blocks required for a coinbase transaction to be spendable.
     */
    protected int spendableCoinbaseDepth;
    protected int subsidyDecreaseBlockCount;
    
    protected int[] acceptableAddressCodes;
    protected String[] dnsSeeds;
    protected Map<Integer, Sha256Hash> checkpoints = new HashMap<Integer, Sha256Hash>();

    protected NetworkParameters() {
        alertSigningKey = SATOSHI_KEY;
        genesisBlock = createGenesis(this);
    }

    private static Block createGenesis(NetworkParameters n) {
        Block genesisBlock = new Block(n);
        Transaction t = new Transaction(n);
        try {
            // A script containing the difficulty bits and the following message:
            //
            //   "3 Aug 2013 - M&G - Mugabe wins Zim election with more than 60% of votes"
            byte[] bytes = Hex.decode("04ffff001d010445434e4e2032332f31302f3230313320536369656e74697374732066696e6420676f6c642067726f77696e67206f6e20747265657320696e204175737472616c6961");
            t.addInput(new TransactionInput(n, t, bytes));
            ByteArrayOutputStream scriptPubKeyBytes = new ByteArrayOutputStream();
            Script.writeBytes(scriptPubKeyBytes, Hex.decode("04678afdb0fe5548271967f1a67130b7105cd6a828e03909a67962e0ea1f61deb649f6bc3f4cef38c4f35504e51ec112de5c384df7ba0b8d578a4c702b6bf11d5f"));
            scriptPubKeyBytes.write(ScriptOpCodes.OP_CHECKSIG);
            t.addOutput(new TransactionOutput(n, t, Utils.toNanoCoins(50, 0), scriptPubKeyBytes.toByteArray()));
        } catch (Exception e) {
            // Cannot happen.
            throw new RuntimeException(e);
        }
        genesisBlock.addTransaction(t);
        // Unable to figure out the exact transaction input script therefore taking the shortcut by setting merkle root directly
        genesisBlock.setMerkleRoot(new Sha256Hash("d25dbe3a2852926fc2ec6591a95983bbcde80c449f30ced37fd657361073fa96"));
        return genesisBlock;
    }

    // Difficulty calculation parameters
    public static final int TARGET_TIMESPAN = 24 * 60;  // 24 minutes per difficulty cycle, on average.
    public static final int TARGET_SPACING = 4 * 60;  // 4 min per block.
    public static final int INTERVAL = TARGET_TIMESPAN / TARGET_SPACING; // 6 blocks
    
    public static final int AVERAGING_INTERVAL = INTERVAL * 8; // 48 blocks
    public static final int AVERAGING_TARGET_TIMESPAN = AVERAGING_INTERVAL * TARGET_SPACING;
    public static final int AVERAGING_INTERVAL2 = INTERVAL * 8; // 48 blocks
    public static final int AVERAGING_TARGET_TIMESPAN2 = AVERAGING_INTERVAL2 * TARGET_SPACING;
 
    public static final int MAX_ADJUST_DOWN = 2; // 2% adjustment down
    public static final int MAX_ADJUST_UP = 1; // 1% adjustment up
    public static final int MAX_ADJUST_DOWN2 = 2; // 2% adjustment down
    public static final int MAX_ADJUST_UP2 = 1; // 1% adjustment up
    
    public static final int TARGET_TIMESPAN_ADJ_DOWN = TARGET_TIMESPAN * (100 + MAX_ADJUST_DOWN) / 100;
    public static final int TARGET_TIMESPAN_ADJ_DOWN2 = TARGET_TIMESPAN * (100 + MAX_ADJUST_DOWN2) / 100;

    public static final int MIN_ACTUAL_TIMESPAN = AVERAGING_TARGET_TIMESPAN * (100 - MAX_ADJUST_UP) / 100;
    public static final int MAX_ACTUAL_TIMESPAN = AVERAGING_TARGET_TIMESPAN * (100 + MAX_ADJUST_DOWN) / 100;

    public static final int MIN_ACTUAL_TIMESPAN2 = AVERAGING_TARGET_TIMESPAN2 * (100 - MAX_ADJUST_UP2) / 100;
    public static final int MAX_ACTUAL_TIMESPAN2 = AVERAGING_TARGET_TIMESPAN2 * (100 + MAX_ADJUST_DOWN2) / 100;

    /**
     * Blocks with a timestamp after this should enforce BIP 16, aka "Pay to script hash". This BIP changed the
     * network rules in a soft-forking manner, that is, blocks that don't follow the rules are accepted but not
     * mined upon and thus will be quickly re-orged out as long as the majority are enforcing the rule.
     */
//    public static final int BIP16_ENFORCE_TIME = 1333238400;
    public static final int BIP16_ENFORCE_TIME = 1380608826;
    
    /**
     * The maximum money to be generated
     */
    public static final BigInteger MAX_MONEY = new BigInteger("64000000", 10).multiply(COIN);

    /** Alias for TestNet3Params.get(), use that instead. */
    @Deprecated
    public static NetworkParameters testNet() {
        return TestNet3Params.get();
    }

    /** Alias for TestNet2Params.get(), use that instead. */
    @Deprecated
    public static NetworkParameters testNet2() {
        return TestNet2Params.get();
    }

    /** Alias for TestNet3Params.get(), use that instead. */
    @Deprecated
    public static NetworkParameters testNet3() {
        return TestNet3Params.get();
    }

    /** Alias for MainNetParams.get(), use that instead */
    @Deprecated
    public static NetworkParameters prodNet() {
        return MainNetParams.get();
    }

    /** Returns a testnet params modified to allow any difficulty target. */
    @Deprecated
    public static NetworkParameters unitTests() {
        return UnitTestParams.get();
    }

    /** Returns a standard regression test params (similar to unitTests) */
    @Deprecated
    public static NetworkParameters regTests() {
        return RegTestParams.get();
    }

    /**
     * A Java package style string acting as unique ID for these parameters
     */
    public String getId() {
        return id;
    }

    public abstract String getPaymentProtocolId();

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof NetworkParameters)) return false;
        NetworkParameters o = (NetworkParameters) other;
        return o.getId().equals(getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    /** Returns the network parameters for the given string ID or NULL if not recognized. */
    @Nullable
    public static NetworkParameters fromID(String id) {
        if (id.equals(ID_MAINNET)) {
            return MainNetParams.get();
        } else if (id.equals(ID_TESTNET)) {
            return TestNet3Params.get();
        } else if (id.equals(ID_UNITTESTNET)) {
            return UnitTestParams.get();
        } else {
            return null;
        }
    }

    /** Returns the network parameters for the given string paymentProtocolID or NULL if not recognized. */
    @Nullable
    public static NetworkParameters fromPmtProtocolID(String pmtProtocolId) {
        if (pmtProtocolId.equals(PAYMENT_PROTOCOL_ID_MAINNET)) {
            return MainNetParams.get();
        } else if (pmtProtocolId.equals(PAYMENT_PROTOCOL_ID_TESTNET)) {
            return TestNet3Params.get();
        } else {
            return null;
        }
    }

    public int getSpendableCoinbaseDepth() {
        return spendableCoinbaseDepth;
    }

    /**
     * Returns true if the block height is either not a checkpoint, or is a checkpoint and the hash matches.
     */
    public boolean passesCheckpoint(int height, Sha256Hash hash) {
        Sha256Hash checkpointHash = checkpoints.get(height);
        return checkpointHash == null || checkpointHash.equals(hash);
    }

    /**
     * Returns true if the given height has a recorded checkpoint.
     */
    public boolean isCheckpoint(int height) {
        Sha256Hash checkpointHash = checkpoints.get(height);
        return checkpointHash != null;
    }

    public int getSubsidyDecreaseBlockCount() {
        return subsidyDecreaseBlockCount;
    }

    /** Returns DNS names that when resolved, give IP addresses of active peers. */
    public String[] getDnsSeeds() {
        return dnsSeeds;
    }

    /**
     * <p>Genesis block for this chain.</p>
     *
     * <p>The first block in every chain is a well known constant shared between all Bitcoin implemenetations. For a
     * block to be valid, it must be eventually possible to work backwards to the genesis block by following the
     * prevBlockHash pointers in the block headers.</p>
     *
     * <p>The genesis blocks for both test and prod networks contain the timestamp of when they were created,
     * and a message in the coinbase transaction. It says, <i>"The Times 03/Jan/2009 Chancellor on brink of second
     * bailout for banks"</i>.</p>
     */
    public Block getGenesisBlock() {
        return genesisBlock;
    }

    /** Default TCP port on which to connect to nodes. */
    public int getPort() {
        return port;
    }

    /** The header bytes that identify the start of a packet on this network. */
    public long getPacketMagic() {
        return packetMagic;
    }

    /**
     * First byte of a base58 encoded address. See {@link com.google.betacoin.core.Address}. This is the same as acceptableAddressCodes[0] and
     * is the one used for "normal" addresses. Other types of address may be encountered with version codes found in
     * the acceptableAddressCodes array.
     */
    public int getAddressHeader() {
        return addressHeader;
    }

    /**
     * First byte of a base58 encoded P2SH address.  P2SH addresses are defined as part of BIP0013.
     */
    public int getP2SHHeader() {
        return p2shHeader;
    }

    /** First byte of a base58 encoded dumped private key. See {@link com.google.betacoin.core.DumpedPrivateKey}. */
    public int getDumpedPrivateKeyHeader() {
        return dumpedPrivateKeyHeader;
    }

    /**
     * How much time in seconds is supposed to pass between "interval" blocks. If the actual elapsed time is
     * significantly different from this value, the network difficulty formula will produce a different value. Both
     * test and production Betacoin networks use 2 minutes (120 seconds).
     */
    public int getTargetTimespan() {
        return targetTimespan;
    }

    /**
     * Documentation to be added. Used in difficulty transition calculation.
     */
    public int getMinActualTimespan() {
        return minActualTimespan;
    }

    /**
     * Documentation to be added. Used in difficulty transition calculation.
     */
    public int getMaxActualTimespan() {
        return maxActualTimespan;
    }

    /**
     * Documentation to be added. Used in difficulty transition calculation.
     */
    public int getAveratingTargetTimespan() {
        return averatingTargetTimespan;
    }

    /**
     * The version codes that prefix addresses which are acceptable on this network. Although Satoshi intended these to
     * be used for "versioning", in fact they are today used to discriminate what kind of data is contained in the
     * address and to prevent accidentally sending coins across chains which would destroy them.
     */
    public int[] getAcceptableAddressCodes() {
        return acceptableAddressCodes;
    }

    /**
     * If we are running in testnet-in-a-box mode, we allow connections to nodes with 0 non-genesis blocks.
     */
    public boolean allowEmptyPeerChain() {
        return true;
    }

    /** How many blocks pass between difficulty adjustment periods. Betacoin standardises this to be 4. */
    public int getInterval() {
        return interval;
    }

    /** How many blocks whose average should the difficulty adjustment be based on. */
    public int getAveragingInterval() { return averagingInterval; }
    public int getAveragingInterval2() { return averagingInterval2; }

    /** What the easiest allowable proof of work should be. */
    public BigInteger getProofOfWorkLimit() {
        return proofOfWorkLimit;
    }

    /**
     * The key used to sign {@link com.google.betacoin.core.AlertMessage}s. You can use {@link com.google.betacoin.core.ECKey#verify(byte[], byte[], byte[])} to verify
     * signatures using it.
     */
    public byte[] getAlertSigningKey() {
        return alertSigningKey;
    }
}
