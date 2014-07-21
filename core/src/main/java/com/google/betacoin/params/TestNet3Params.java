/*
 * Copyright 2013 Google Inc.
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

package com.google.betacoin.params;

import com.google.betacoin.core.NetworkParameters;
import com.google.betacoin.core.Utils;
import org.spongycastle.util.encoders.Hex;

import static com.google.common.base.Preconditions.checkState;

/**
 * Parameters for the testnet, a separate public instance of Bitcoin that has relaxed rules suitable for development
 * and testing of applications and new Bitcoin versions.
 */
public class TestNet3Params extends NetworkParameters {
    public TestNet3Params() {
        super();
        id = ID_TESTNET;
        // Genesis hash is 000000000933ea01ad0ee984209779baaec3ced90fa3f408719526f8d77f4943
        packetMagic = 0xa5c07955L;
        interval = INTERVAL;
        targetTimespan = TARGET_TIMESPAN;
        proofOfWorkLimit = Utils.decodeCompactBits(0x1d00ffffL);
        port = 32333;
        addressHeader = 25;
        p2shHeader = 11;
        acceptableAddressCodes = new int[] { addressHeader, p2shHeader };
        dumpedPrivateKeyHeader = 143;
        genesisBlock.setDifficultyTarget(0x1d00ffffL);
        genesisBlock.setTime(1382532797L);
        genesisBlock.setNonce(704106316);
        spendableCoinbaseDepth = 100;
        subsidyDecreaseBlockCount = 126000;
        String genesisHash = genesisBlock.getHashAsString();
        checkState(genesisHash.equals("000000008ef7da946aa3f4dd81b240c6bdedac0dc038cb04e7cf8e60f37d9281"));
        alertSigningKey = Hex.decode("00000000343f91cc401d00d68b123028bf52e5fca1939df127f63c6467cdf9c8e2c14b61104cf817d0b780da337893ecc4aaff1309e536162dabbdb45200ca2b0a");

        /* dnsSeeds = new String[] {
                "testnet-seed.betacoin.petertodd.org",
                "testnet-seed.bluematt.me"
        };*/
    }

    private static TestNet3Params instance;
    public static synchronized TestNet3Params get() {
        if (instance == null) {
            instance = new TestNet3Params();
        }
        return instance;
    }

    public String getPaymentProtocolId() {
        return PAYMENT_PROTOCOL_ID_TESTNET;
    }
}
