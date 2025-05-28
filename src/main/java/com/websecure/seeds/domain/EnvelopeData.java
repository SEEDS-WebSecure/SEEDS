package com.websecure.seeds.domain;

import lombok.Builder;
import lombok.Getter;

import java.io.*;

@Builder
@Getter
public class EnvelopeData {
    private byte[] encryptedData;
    private byte[] encryptedKey;

    public static void saveEnvelopeDataToFile(String fileName, EnvelopeData data) {
        try(FileOutputStream fos = new FileOutputStream(fileName)) {
            try(ObjectOutputStream oos = new ObjectOutputStream(fos)) {
                oos.writeObject(data);
            }
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 중 오류가 발생했습니다.", e);
        }
    }
}
