package com.websecure.seeds.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class SendEnvelopeDTO {
    @NotBlank(message = "송신자 이름 입력은 필수입니다.")
    private String sender;

    @NotBlank(message = "수신자 이름 입력은 필수입니다.")
    private String receiver;

    @NotBlank(message = "파일명 입력은 필수입니다.")
    private String fileName;

    @NotNull(message = "파일은 필수입니다.")
    private MultipartFile file;
}
