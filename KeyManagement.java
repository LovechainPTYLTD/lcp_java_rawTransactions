package LCP;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicHierarchy;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.UnreadableWalletException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;


public class KeyManagement {
    /**
     * returns a Deterministic Hierarchy with the master key at the top
     * @param mnemonic<String> 12 words
     * @param password<String> passphrase
     * @return DeterministcHierarchy
     * */
    public static DeterministicHierarchy deriveMasterKeyHierarchy(String mnemonic,
                                                            String password)
            throws UnreadableWalletException {

        long unixTime = Instant.now().getEpochSecond();
        //"yard impulse luxury drive today throw farm pepper survey wreck glass federal";
        DeterministicSeed masterSeed = new DeterministicSeed(mnemonic,
                null, password, unixTime);
        DeterministicKey masterKey = HDKeyDerivation.createMasterPrivateKey(masterSeed.getSeedBytes());
        DeterministicHierarchy keyHierarchy = new DeterministicHierarchy(masterKey);
        return keyHierarchy;
    }

    /**
     * derives an indepedent wallet key from the path "m/44'/0'/0'" does not include
     * the master key or any hierarchy. this must be placed in a hierarchy indepedently
     * @param mnemonic<String> 12 word key seed
     * @param password<String> key password
     * @return DeterministicKey
     * */
    public static DeterministicKey deriveWalletKey(String mnemonic, String password)
            throws UnreadableWalletException {

        long unixTime = Instant.now().getEpochSecond();
        //"yard impulse luxury drive today throw farm pepper survey wreck glass federal";
        DeterministicSeed masterSeed = new DeterministicSeed(mnemonic,
                null, password, unixTime);
        DeterministicKey masterKey = HDKeyDerivation.createMasterPrivateKey(masterSeed.getSeedBytes());
        DeterministicHierarchy keyHierarchy = new DeterministicHierarchy(masterKey);
        ChildNumber purpose = new ChildNumber(44,true);
        ChildNumber coinType = new ChildNumber(0,true);
        ChildNumber account = new ChildNumber(0,true);
        List<ChildNumber> path = new ArrayList<>(){{
            add(purpose);
            add(coinType);
            add(account);
        }};
        return keyHierarchy.get(path, false,true);

    }

    /**
     * device keys are used to validate WebSocket connections with the hub
     * @param mnemonic<String> 12 words
     * @param password <String> passphrase
     * */
    public static DeterministicKey deriveDeviceKey(String mnemonic, String password)
        throws UnreadableWalletException{

        long unixTime = Instant.now().getEpochSecond();
        //"yard impulse luxury drive today throw farm pepper survey wreck glass federal";
        DeterministicSeed masterSeed = new DeterministicSeed(mnemonic,
                null, password, unixTime);
        DeterministicKey masterKey = HDKeyDerivation.createMasterPrivateKey(masterSeed.getSeedBytes());
        DeterministicHierarchy keyHierarchy = new DeterministicHierarchy(masterKey);
        ChildNumber purpose = new ChildNumber(1,true);
        List<ChildNumber> path = new ArrayList<>(){{
            add(purpose);
        }};

        return keyHierarchy.get(path,false,true);
    }


    public static String sign(DeterministicKey key, byte[] rawHash) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        Sha256Hash hash = Sha256Hash.wrap(rawHash);
        ECKey.ECDSASignature signature = key.sign(hash);
        byte[] rPointBytes = signature.r.toByteArray();
        byte[] sPointBytes = signature.s.toByteArray();
        byte[] sigBytesHas00 = new byte[rPointBytes.length+ sPointBytes.length];
        System.arraycopy(rPointBytes, 0, sigBytesHas00, 0, rPointBytes.length);
        System.arraycopy(sPointBytes, 0, sigBytesHas00, rPointBytes.length, sPointBytes.length);
        byte[] sigBytes = Arrays.copyOfRange(sigBytesHas00, 1,sigBytesHas00.length);
        System.out.println(Arrays.toString(sigBytesHas00));
        return Base64.getEncoder().encodeToString(sigBytes);
    }


}


