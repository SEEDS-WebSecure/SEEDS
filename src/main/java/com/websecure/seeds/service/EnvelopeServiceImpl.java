package com.websecure.seeds.service;

import com.websecure.seeds.domain.Envelope;
import com.websecure.seeds.domain.EnvelopeData;
import com.websecure.seeds.domain.SignatureData;
import com.websecure.seeds.domain.User;
import com.websecure.seeds.dto.SendEnvelopeDTO;
import com.websecure.seeds.repository.EnvelopeRepository;
import com.websecure.seeds.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.io.*;
import java.security.*;

@Service
@RequiredArgsConstructor
public class EnvelopeServiceImpl implements EnvelopeService {
    private final UserRepository userRepository;
    private final EnvelopeRepository envelopeRepository;

    @Override
    public Envelope sendDigitalEnvelope(SendEnvelopeDTO request) {
        try {
            // 송신자
            User sender = userRepository.findByName(request.getSender())
                    .orElseThrow(() -> new RuntimeException("존재하지 않는 송신자 이름입니다."));
            // 수신자
            User receiver = userRepository.findByName(request.getReceiver())
                    .orElseThrow(() -> new RuntimeException("존재하지 않는 수신자 이름입니다."));

            byte[] fileBytes  = request.getFile().getBytes();
            byte[] fileHash = getFileHash(fileBytes);

            PrivateKey senderPrivateKey  = (PrivateKey) loadKeyFromFile(sender.getPrivateKeyFileName());
            PublicKey senderPublicKey = (PublicKey) loadKeyFromFile(sender.getPublicKeyFileName());

            // 전자서명 생성: 원문의 해시값을 송신자의 사설키로 암호화
            byte[] signature = encryptData("RSA", fileHash, senderPrivateKey);

            // [전자서명 + 원문 + 송신자의 공개키]를 SignatureData 객체로 묶음
            SignatureData signatureData = SignatureData.builder()
                    .signature(signature)
                    .file(fileBytes)
                    .publicKey(senderPublicKey)
                    .build();

            // [전자서명 + 원문 + 송신자의 공개키]를 묶은 SignatureData 객체를 직렬화
            byte[] serializedSignatureData = SignatureData.serializeData(signatureData);

            SecretKey secretKey = (SecretKey) loadKeyFromFile(sender.getSecretKeyFileName());
            
            // 직렬화된 데이터를 대칭키로 암호화
            byte[] encryptedData = encryptData("AES", serializedSignatureData, secretKey);

            PublicKey receiverPublicKey = (PublicKey) loadKeyFromFile(receiver.getPublicKeyFileName());
            // 대칭키를 수신자의 공개키로 암호화
            byte[] encryptedKey = encryptData("RSA", secretKey.getEncoded(), receiverPublicKey);

            EnvelopeData envelopeData = EnvelopeData.builder()
                    .encryptedData(encryptedData)
                    .encryptedKey(encryptedKey)
                    .build();

            // 암호화된 문서 + 암호화된 대칭키를 파일에 저장
            EnvelopeData.saveEnvelopeDataToFile(request.getFileName(), envelopeData);

            // DB에 저장
            Envelope envelope = Envelope.builder()
                    .sender(request.getSender())
                    .receiver(request.getReceiver())
                    .fileName(request.getFileName())
                    .build();

            return envelopeRepository.save(envelope);

        } catch (IOException e) {
            throw new RuntimeException("파일 처리 중 오류가 발생했습니다.", e);
        }
    }

    public static byte[] getFileHash(byte[] data) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(data);
            return messageDigest.digest();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("파일 해시 생성 중 오류가 발생했습니다.", e);
        }
    }

    public static byte[] encryptData(String algorithm, byte[] data, Key key) {
       try {
           Cipher cipher = Cipher.getInstance(algorithm);
           cipher.init(Cipher.ENCRYPT_MODE, key);
           return cipher.doFinal(data);
       } catch (Exception e) {
           throw new RuntimeException("데이터 암호화 중 오류가 발생했습니다.", e);
       }
    }

    public static Key loadKeyFromFile(String fileName) {
        try(FileInputStream fis = new FileInputStream(fileName)) {
            try(ObjectInputStream ois = new ObjectInputStream(fis)) {
                return (Key) ois.readObject();
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        } catch (IOException e) {
            throw new RuntimeException("파일 읽기 중 오류가 발생했습니다.", e);
        }
    }
}
