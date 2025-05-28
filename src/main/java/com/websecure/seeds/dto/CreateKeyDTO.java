package com.websecure.seeds.dto;

import com.websecure.seeds.domain.User;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateKeyDTO {
    @NotBlank(message = "이름 입력은 필수입니다.")
    private String name;

    @NotBlank(message = "비밀키 파일명 입력은 필수입니다.")
    private String secretKeyFileName;

    @NotBlank(message = "공개키 파일명 입력은 필수입니다.")
    private String publicKeyFileName;

    @NotBlank(message = "사설키 파일명 입력은 필수입니다.")
    private String privateKeyFileName;

    public User toUser() {
        return User.builder()
                .name(name)
                .secretKeyFileName(secretKeyFileName)
                .publicKeyFileName(publicKeyFileName)
                .privateKeyFileName(privateKeyFileName)
                .build();
    }
}
