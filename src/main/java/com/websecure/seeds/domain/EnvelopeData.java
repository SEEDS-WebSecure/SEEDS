package com.websecure.seeds.domain;

import lombok.Builder;
import lombok.Getter;

import java.io.*;

@Builder
@Getter
public class EnvelopeData implements Serializable {
    private static final long serialVersionUID = 1L;
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

    public static EnvelopeData loadEnvelopeDataFromFile(String fileName) {
        try(FileInputStream fis = new FileInputStream(fileName)){
            try(ObjectInputStream ois = new ObjectInputStream(fis)){
                return(EnvelopeData) ois.readObject();
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("전자봉투 데이터를 불러오는 중 오류가 발생했습니다.", e);
        }


    }
}
