package bg.sofia.uni.fmi.mjt.space.algorithm;

import bg.sofia.uni.fmi.mjt.space.exception.CipherException;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class Rijndael implements SymmetricBlockCipher{
    private SecretKey secretKey;
    public Rijndael(SecretKey secretKey){
        this.secretKey=secretKey;
    }
    @Override
    public void encrypt(InputStream inputStream, OutputStream outputStream) throws CipherException {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] buffer = new byte[1024];
            try (var cipherOutputStream = new CipherOutputStream(outputStream, cipher)) {
                int bytesRead;
                while ((bytesRead = inputStream.read()) != -1) {
                    cipherOutputStream.write(buffer, 0, bytesRead);
                }
            }
        } catch (IOException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {

        }
    }

    @Override
    public void decrypt(InputStream inputStream, OutputStream outputStream) throws CipherException {
        try {
            Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            try (OutputStream decryptedOutputStream = new CipherOutputStream(outputStream, cipher)) {
                byte[] buffer = new byte[1024];
                int bytesRead;

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    decryptedOutputStream.write(buffer, 0, bytesRead);
                }
            }

        }
        catch(IOException|NoSuchAlgorithmException|NoSuchPaddingException|InvalidKeyException e){

        }
    }
}
