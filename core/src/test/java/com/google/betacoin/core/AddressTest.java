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
import com.google.betacoin.params.TestNet3Params;
import com.google.betacoin.script.ScriptBuilder;

import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class AddressTest {
    static final NetworkParameters testParams = TestNet3Params.get();
    static final NetworkParameters mainParams = MainNetParams.get();

    @Test
    public void stringification() throws Exception {
        // Test a testnet address.
        Address a = new Address(testParams, Hex.decode("053902DE7AB80BF0B0F1BC282ADA775F24288358"));
        assertEquals("aosHPHjvW6CfJJtk5wxNoH6yfcqgNyBbfU", a.toString());
        assertFalse(a.isP2SHAddress());

        Address b = new Address(mainParams, Hex.decode("FBF3C48D9D9A139208A461AAB51575D3E73456CE"));
        assertEquals("BTRHF9WtJZEy8xhhs3cVNfGjswc2DYXCj2", b.toString());
        assertFalse(b.isP2SHAddress());
    }
    
    @Test
    public void decoding() throws Exception {
        Address a = new Address(testParams, "aosHPHjvW6CfJJtk5wxNoH6yfcqgNyBbfU");
        assertEquals("053902DE7AB80BF0B0F1BC282ADA775F24288358", Utils.bytesToHexString(a.getHash160()));

        Address b = new Address(mainParams, "BTRHF9WtJZEy8xhhs3cVNfGjswc2DYXCj2");
        assertEquals("FBF3C48D9D9A139208A461AAB51575D3E73456CE", Utils.bytesToHexString(b.getHash160()));
    }
    
    @Test
    public void errorPaths() {
        // Check what happens if we try and decode garbage.
        try {
            new Address(testParams, "this is not a valid address!");
            fail();
        } catch (WrongNetworkException e) {
            fail();
        } catch (AddressFormatException e) {
            // Success.
        }

        // Check the empty case.
        try {
            new Address(testParams, "");
            fail();
        } catch (WrongNetworkException e) {
            fail();
        } catch (AddressFormatException e) {
            // Success.
        }

        // Check the case of a mismatched network.
        try {
            new Address(testParams, "BTRHF9WtJZEy8xhhs3cVNfGjswc2DYXCj2");
            fail();
        } catch (WrongNetworkException e) {
            // Success.
            assertEquals(e.verCode, MainNetParams.get().getAddressHeader());
            assertTrue(Arrays.equals(e.acceptableVersions, TestNet3Params.get().getAcceptableAddressCodes()));
        } catch (AddressFormatException e) {
            fail();
        }
    }
    
    @Test
    public void getNetwork() throws Exception {
        NetworkParameters params = Address.getParametersFromAddress("BTRHF9WtJZEy8xhhs3cVNfGjswc2DYXCj2");
        assertEquals(MainNetParams.get().getId(), params.getId());
        params = Address.getParametersFromAddress("aosHPHjvW6CfJJtk5wxNoH6yfcqgNyBbfU");
        assertEquals(TestNet3Params.get().getId(), params.getId());
    }
    
    @Test
    public void p2shAddress() throws Exception {
        // Test that we can construct P2SH addresses
        Address mainNetP2SHAddress = new Address(MainNetParams.get(), "5gwZsKARr3ekip8RRaBbrNfM8QNXbFmnS1");
        assertEquals(mainNetP2SHAddress.version, MainNetParams.get().p2shHeader);
        assertTrue(mainNetP2SHAddress.isP2SHAddress());
        Address testNetP2SHAddress = new Address(TestNet3Params.get(), "6Jgnd62HREwcJ6fvXzeo3GUX1rEeL4arpzR");
        assertEquals(testNetP2SHAddress.version, TestNet3Params.get().p2shHeader);
        assertTrue(testNetP2SHAddress.isP2SHAddress());

        // Test that we can determine what network a P2SH address belongs to
        NetworkParameters mainNetParams = Address.getParametersFromAddress("5gwZsKARr3ekip8RRaBbrNfM8QNXbFmnS1");
        assertEquals(MainNetParams.get().getId(), mainNetParams.getId());
        NetworkParameters testNetParams = Address.getParametersFromAddress("6Jgnd62HREwcJ6fvXzeo3GUX1rEeL4arpzR");
        assertEquals(TestNet3Params.get().getId(), testNetParams.getId());

        // Test that we can convert them from hashes
        byte[] hex = Hex.decode("A6FBFC58363D7257B33B38484BDAEABC441B9A48");
        Address a = Address.fromP2SHHash(mainParams, hex);
        assertEquals("5gwZsKARr3ekip8RRaBbrNfM8QNXbFmnS1", a.toString());
        Address b = Address.fromP2SHHash(testParams, Hex.decode("FCAABD740F5671DC3C634D44B7BAA0A926885043"));
        assertEquals("6Jgnd62HREwcJ6fvXzeo3GUX1rEeL4arpzR", b.toString());
        Address c = Address.fromP2SHScript(mainParams, ScriptBuilder.createP2SHOutputScript(hex));
        assertEquals("5gwZsKARr3ekip8RRaBbrNfM8QNXbFmnS1", c.toString());
    }
}
