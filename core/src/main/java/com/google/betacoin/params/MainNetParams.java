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

import com.google.betacoin.core.Sha256Hash;
import com.google.betacoin.core.NetworkParameters;
import com.google.betacoin.core.Utils;

import static com.google.common.base.Preconditions.checkState;

/**
 * Parameters for the main production network on which people trade goods and services.
 */
public class MainNetParams extends NetworkParameters {
    public MainNetParams() {
        super();
        interval = INTERVAL;
        targetTimespan = TARGET_TIMESPAN;
        // Note: proofOfWorkLimit is changed from litecoinj and betacoinj because
        // - betacoin satoshi client's bnProofOfWorkLimit is 1d00ffff, not 1e0fffff
        proofOfWorkLimit = Utils.decodeCompactBits(0x1d00ffffL);
        dumpedPrivateKeyHeader = 143;
        addressHeader = 25;
        p2shHeader = 11;
        acceptableAddressCodes = new int[] { addressHeader, p2shHeader };
        port = 32333;
        packetMagic = 0xa5c07955L;
        genesisBlock.setDifficultyTarget(0x1d00ffffL);
        genesisBlock.setTime(1382532797L);
        genesisBlock.setNonce(704106316);
        id = ID_MAINNET;
        subsidyDecreaseBlockCount = 126000;
        spendableCoinbaseDepth = 100;
        String genesisHash = genesisBlock.getHashAsString();
        checkState(genesisHash.equals("000000008ef7da946aa3f4dd81b240c6bdedac0dc038cb04e7cf8e60f37d9281"),
                genesisHash);

        // This contains (at a minimum) the blocks which are not BIP30 compliant. BIP30 changed how duplicate
        // transactions are handled. Duplicated transactions could occur in the case where a coinbase had the same
        // extraNonce and the same outputs but appeared at different heights, and greatly complicated re-org handling.
        // Having these here simplifies block connection logic considerably.
        checkpoints.put(20325, new Sha256Hash("00000000002c21cba7c1484d368447020d55a33b8dd81ceee0f26629858f6487"));
        checkpoints.put(45095, new Sha256Hash("000000000000140421f951fe8c5614e5a6bcc1b075e553b1b410f303dba2ca64"));
        checkpoints.put(60925, new Sha256Hash("000000000000005e2efa4093448d81a043b586be2ca54c0837118f927db0f941"));
        checkpoints.put(70525, new Sha256Hash("00000000000022abea2d6315e0114570a18240aa5ddd2eee4c8d580ec62aaf47"));
        // TODO: Is BetaCoin post-BIP30? Do we need these?

        dnsSeeds = new String[] {
                "seed.coinsilo.com", "seed1.betacoin.org"
                        };
    }

    private static MainNetParams instance;
    public static synchronized MainNetParams get() {
        if (instance == null) {
            instance = new MainNetParams();
        }
        return instance;
    }

    public String getPaymentProtocolId() {
        return PAYMENT_PROTOCOL_ID_MAINNET;
    }
}
