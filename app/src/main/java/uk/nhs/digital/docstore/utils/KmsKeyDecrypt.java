package uk.nhs.digital.docstore.utils;

import com.amazonaws.services.kms.AWSKMS;
import com.amazonaws.services.kms.AWSKMSClientBuilder;
import com.amazonaws.services.kms.model.DecryptRequest;
import com.amazonaws.services.kms.model.DecryptResult;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class KmsKeyDecrypt {

    public static String decryptCiphertextWithKey(String ciphertext) {
        AWSKMS kmsClient = AWSKMSClientBuilder.standard().build();

        byte[] encryptedBytes = java.util.Base64.getDecoder().decode(ciphertext);
        ByteBuffer byteBuffer = ByteBuffer.allocate(encryptedBytes.length);
        byteBuffer.put(encryptedBytes);
        byteBuffer.flip();

        DecryptRequest decryptRequest = new DecryptRequest().withCiphertextBlob(byteBuffer);
        DecryptResult decryptResult = kmsClient.decrypt(decryptRequest);

        ByteBuffer decryptedByteBuffer = decryptResult.getPlaintext();
        return StandardCharsets.UTF_8.decode(decryptedByteBuffer).toString();
    }
}
