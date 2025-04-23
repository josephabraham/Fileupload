package com.jaka;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.web.multipart.MultipartFile;

@Data
@Getter
@Setter
public class FileTransferRequest {
    private String userName;
    private ByteArrayResource file2;
}
