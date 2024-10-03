package bg.sofia.uni.fmi.mjt.spacetest.rijndaeltest;

import bg.sofia.uni.fmi.mjt.space.algorithm.Rijndael;
import bg.sofia.uni.fmi.mjt.space.exception.CipherException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;

public class RijndaelTest {

    private static Rijndael rijndael;
    private static final String ENCRYPTION_ALGORITHM = "AES";
    private static final int KEY_SIZE_IN_BITS = 128;


    @BeforeAll
    static void setUp() throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(ENCRYPTION_ALGORITHM);
        keyGenerator.init(KEY_SIZE_IN_BITS);
        SecretKey secretKey = keyGenerator.generateKey();

        rijndael = new Rijndael(secretKey);
    }

    @Test
    void testDecryptAndEncrypt() throws CipherException {
        String example = "example";
        InputStream inputStream = new ByteArrayInputStream(example.getBytes());
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        rijndael.encrypt(inputStream, outputStream);

        byte[] encryptedString = outputStream.toByteArray();
        InputStream inputStream1 = new ByteArrayInputStream(encryptedString);
        ByteArrayOutputStream outputStream1 = new ByteArrayOutputStream();

        rijndael.decrypt(inputStream1, outputStream1);

        String decryptedString = outputStream1.toString(StandardCharsets.UTF_8);

        Assertions.assertEquals(example, decryptedString, "An original string should be the same as the output when we have it decrypted and then encrypted again.");
    }
}
