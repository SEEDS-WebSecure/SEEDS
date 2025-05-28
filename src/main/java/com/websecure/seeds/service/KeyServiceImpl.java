package com.websecure.seeds.service;

import com.websecure.seeds.domain.User;
import com.websecure.seeds.dto.CreateKeyDTO;
import com.websecure.seeds.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.KeyGenerator;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.*;

@Service
@RequiredArgsConstructor
public class KeyServiceImpl implements KeyService {
    private final UserRepository userRepository;

    @Override
    public User createKey(CreateKeyDTO request) {
        // 대칭키 생성
        Key secretKey = createKey();

        // 대칭키를 파일에 저장
        saveKeyToFile(request.getSecretKeyFileName(), secretKey);
        
        // 공개키, 사설키 생성
        KeyPair keyPair = createKeyPair();
        PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();

        // 비대칭키를 파일에 저장
        saveKeyToFile(request.getPublicKeyFileName(), publicKey);
        saveKeyToFile(request.getPrivateKeyFileName(), privateKey);

        User user = request.toUser();
        return userRepository.save(user);
    }

    private Key createKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(128);
            return keyGen.generateKey();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("대칭키 생성 중 오류가 발생했습니다.");
        }
    }

    private KeyPair createKeyPair() {
        try {
            KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
            keyPairGen.initialize(1024);
            return keyPairGen.generateKeyPair();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("비대칭키 생성 중 오류가 발생했습니다.");
        }
    }

    private void saveKeyToFile(String fileName, Key key) {
        try(FileOutputStream fos = new FileOutputStream(fileName)) {
            try(ObjectOutputStream oos = new ObjectOutputStream(fos)) {
                oos.writeObject(key);
            }
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 중 오류가 발생했습니다.");
        }
    }
}
