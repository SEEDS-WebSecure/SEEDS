package com.websecure.seeds.service;

import com.websecure.seeds.domain.*;
import com.websecure.seeds.dto.SendEnvelopeDTO;
import com.websecure.seeds.dto.VerifySignDTO;
import com.websecure.seeds.exception.CryptoOperationException;
import com.websecure.seeds.exception.SignatureVerificationException;
import com.websecure.seeds.repository.EnvelopeRepository;
import com.websecure.seeds.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.*;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EnvelopeServiceImpl implements EnvelopeService {
    private final UserRepository userRepository;
    private final EnvelopeRepository envelopeRepository;
    private static final String ALGO_RSA = "RSA";
    private static final String ALGO_AES = "AES";
    private static final String ALGO_SIGN = "SHA1withRSA";

    @Override
    public Envelope sendDigitalEnvelope(SendEnvelopeDTO request) {
        try {
            // 송신자
            User sender = userRepository.findByName(request.getSender())
                    .orElseThrow(() -> new RuntimeException("존재하지 않는 송신자 이름입니다."));
            // 수신자
            User receiver = userRepository.findByName(request.getReceiver())
                    .orElseThrow(() -> new RuntimeException("존재하지 않는 수신자 이름입니다."));

            PrivateKey senderPrivateKey = (PrivateKey) loadKeyFromFile(sender.getPrivateKeyFileName());
            PublicKey senderPublicKey = (PublicKey) loadKeyFromFile(sender.getPublicKeyFileName());

            // 전자서명 생성
            byte[] fileBytes = request.getFile().getBytes();
            byte[] signature = createSignature(fileBytes, senderPrivateKey);

            // [전자서명 + 원문 + 송신자의 공개키]를 SignatureData 객체로 묶음
            SignatureData signatureData = SignatureData.builder()
                    .signature(signature)
                    .file(fileBytes)
                    .publicKey(senderPublicKey)
                    .build();

            // [전자서명 + 원문 + 송신자의 공개키]를 묶은 SignatureData 객체를 직렬화
            byte[] serializedSignatureData = SignatureData.serializeData(signatureData);
            Arrays.fill(fileBytes, (byte) 0);

            SecretKey secretKey = (SecretKey) loadKeyFromFile(sender.getSecretKeyFileName());

            // 직렬화된 데이터를 대칭키로 암호화
            byte[] encryptedData = encryptData(ALGO_AES, serializedSignatureData, secretKey);
            Arrays.fill(serializedSignatureData, (byte) 0);

            PublicKey receiverPublicKey = (PublicKey) loadKeyFromFile(receiver.getPublicKeyFileName());

            // 대칭키를 수신자의 공개키로 암호화
            byte[] encryptedKey = encryptData(ALGO_RSA, secretKey.getEncoded(), receiverPublicKey);

            EnvelopeData envelopeData = EnvelopeData.builder()
                    .encryptedData(Arrays.copyOf(encryptedData, encryptedData.length))
                    .encryptedKey(Arrays.copyOf(encryptedKey, encryptedKey.length))
                    .build();

            // 암호화된 문서 + 암호화된 대칭키를 파일에 저장
            EnvelopeData.saveEnvelopeDataToFile(request.getFileName(), envelopeData);

            Arrays.fill(encryptedData, (byte) 0);
            Arrays.fill(encryptedKey, (byte) 0);

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

    private static byte[] createSignature(byte[] data, PrivateKey privateKey) {
        try {
            Signature sig = Signature.getInstance(ALGO_SIGN);
            sig.initSign(privateKey);
            sig.update(data);
            return sig.sign();
        } catch (InvalidKeyException e) {
            throw new RuntimeException("전자서명 생성 중 오류가 발생했습니다.", e);
        } catch (NoSuchAlgorithmException | SignatureException e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] encryptData(String algorithm, byte[] data, Key key) {
        try {
            Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return cipher.doFinal(data);
        } catch (Exception e) {
            throw new RuntimeException("데이터 암호화 중 오류가 발생했습니다.", e);
        }
    }

    private static Key loadKeyFromFile(String fileName) {
        try (FileInputStream fis = new FileInputStream(fileName)) {
            try (ObjectInputStream ois = new ObjectInputStream(fis)) {
                return (Key) ois.readObject();
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        } catch (IOException e) {
            throw new RuntimeException("파일 읽기 중 오류가 발생했습니다.", e);
        }
    }

    @Override
    public List<Envelope> findEnvelopeList(String receiver) {
        User user = userRepository.findByName(receiver)
                .orElseThrow(() -> new IllegalStateException("사용자를 찾을 수 없습니다."));

        return envelopeRepository.findByReceiver(user.getName());
    }

    //검증하기
    @Override
    @Transactional
    public VerifySignDTO verifySign(Long envelopeId, String receiver) throws CryptoOperationException, SignatureVerificationException {
        User user = userRepository.findByName(receiver)
                .orElseThrow(() -> new IllegalStateException("사용자를 찾을 수 없습니다."));
        Envelope envelope = envelopeRepository.findById(envelopeId)
                .orElseThrow(() -> new IllegalStateException("전자봉투를 찾을 수 없습니다."));

        // EnvelopeData(암호화된 데이터) 로드
        EnvelopeData envelopeData = EnvelopeData.loadEnvelopeDataFromFile(envelope.getFileName());

        //수험생의 개인키 가져오기(대칭키 복호화용)
        Key receiverPrivateKey = loadKeyFromFile(user.getPrivateKeyFileName());

        /*AES 키 복원*/
        //대칭키 복호화
        byte[] secretKeyBytes = decryptData(ALGO_RSA, envelopeData.getEncryptedKey(), receiverPrivateKey);
        //AES SecretKey 객체 생성
        SecretKey secretKey = new SecretKeySpec(secretKeyBytes,ALGO_AES);

        //직렬화 된 데이터 복호화(대칭키 AES)로
        byte[] serializedSignatureData = decryptData(ALGO_AES, envelopeData.getEncryptedData(), secretKey);

        //역직렬화
        SignatureData signatureData  = SignatureData.deserializeData(serializedSignatureData);

        //해시 비교
        boolean isVerified = verifySign(signatureData.getSignature(), signatureData.getFile(), signatureData.getPublicKey());

        // 8. DTO 반환
        return VerifySignDTO.builder()
                .sender(envelope.getSender())
                .receiver(envelope.getReceiver())
                .content(new String(signatureData.getFile()))
                .isVerified(isVerified)
                .build();
    }

    private static byte[] decryptData(String algorithm, byte[] data, Key key) throws CryptoOperationException {
        try {
            Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.DECRYPT_MODE, key);
            return cipher.doFinal(data);
        } catch (Exception e) {
            throw new CryptoOperationException("데이터 복호화 중 오류가 발생했습니다.", e);
        }
    }

    private boolean verifySign(byte[] signature, byte[] data, PublicKey publicKey) {
        boolean isVerified = false;
        try {
            Signature sig = Signature.getInstance(ALGO_SIGN);
            sig.initVerify(publicKey);
            sig.update(data);
            isVerified = sig.verify(signature); // 서명 검증
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            throw new RuntimeException("서명 검증 중 오류가 발생하였습니다.", e);
        }
        return isVerified;
    }
}
