package com.websecure.seeds.domain;

import lombok.Builder;
import lombok.Getter;

import java.io.*;
import java.security.PublicKey;

@Builder
@Getter
public class SignatureData implements Serializable {
    private byte[] signature;
    private byte[] file;
    private PublicKey publicKey;

    public static byte[] serializeData(SignatureData data) {
        try(ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(data);

            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("직렬화 중 오류가 발생하였습니다.", e);
        }
    }

    public static SignatureData deserializeData(byte[] data) {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(data);
             ObjectInputStream ois = new ObjectInputStream(bis)) {

            return (SignatureData) ois.readObject();

        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("SignatureData 역직렬화 중 오류가 발생했습니다.", e);
        }
    }
}
