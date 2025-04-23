package com.jaka.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jaka.FileTransferRequest;
import com.jaka.InputRequest;
import com.jaka.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Files;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
public class FilaManagerController {
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FileStorageService fileStorageService;
    private static final Logger log = Logger.getLogger(FilaManagerController.class.getName());

    @PostMapping("/sendFile")
    public boolean transferTheFile(@RequestParam  MultipartFile file2, @RequestParam String userName){
        log.log(Level.INFO, "Transfer File");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        RestTemplate restTemplate = new RestTemplate();
        String serverUrl = "http://localhost:8080/upload-file";

        MultiValueMap<String, Object> body  = new LinkedMultiValueMap<>();
        body.add("userName", userName);
        try {
            body.add("file", new ByteArrayResource(file2.getBytes()) {
                @Override
                public String getFilename() {
                    return file2.getOriginalFilename();
                }
            });
        }catch (Exception e ){
            e.printStackTrace();
        }
         HttpEntity<MultiValueMap<String, Object>> requestEntity  = new HttpEntity<>(body, headers);
       ResponseEntity<String> response = restTemplate.postForEntity(serverUrl, requestEntity, String.class);


        log.log(Level.INFO, "Sucessfuly transfered the file "+ file2.getOriginalFilename() + " to the file storage service." );
        return true;
}



    @PostMapping("/sendFile2")
    public boolean transferTheFile2( @ModelAttribute InputRequest input){
        log.log(Level.INFO, "Transfer File - First API");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);


        MultiValueMap<String, Object> body  = new LinkedMultiValueMap<>();
        body.add("userName", input.getUserName());
        try {
            body.add("file", new ByteArrayResource(input.getFile2().getBytes()) {
                @Override
                public String getFilename() {
                    return input.getFile2().getOriginalFilename();
                }
            });
        }catch (Exception e ){
            e.printStackTrace();
        }


        RestTemplate restTemplate = new RestTemplate();
        String serverUrl = "http://localhost:8080/upload-file";

        HttpEntity<MultiValueMap<String, Object>>  requestEntity  = new HttpEntity<>( body, headers);
        UriComponentsBuilder uriBuilder =UriComponentsBuilder.fromUriString(serverUrl);
//        List<HttpMessageConverter<?>> messageConverters = restTemplate.getMessageConverters();
//        messageConverters.add( new MultiPartMessageConverter(objectMapper));
        ResponseEntity<String> response = restTemplate.exchange(uriBuilder
                                .build()
                                .encode()
                                .toUri(),
                HttpMethod.POST,requestEntity, new ParameterizedTypeReference<>() {

                });

        log.log(Level.INFO, "Sucessfuly transfered the file "+ input.getFile2().getOriginalFilename() + " to the file storage service." );
        return true;
    }






    @PostMapping(value = "/upload-file",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public boolean uploadFile(@RequestParam("file") MultipartFile file) {
        log.log(Level.INFO, "Entering the SECOND API File Upload");
        try {
            fileStorageService.saveFile(file);

            log.log(Level.INFO, "Saved the file in  SECOND API File Upload");
            return true;
        } catch (IOException e) {
            log.log(Level.SEVERE, "Exception during upload", e);
        }
        return false;
    }

    @GetMapping("/download")
    public ResponseEntity<Resource> downloadFile(@RequestParam("fileName") String filename) {
        log.log(Level.INFO, "[NORMAL] Download with /download");
        try {
            var fileToDownload = fileStorageService.getDownloadFile(filename);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentLength(fileToDownload.length())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(new InputStreamResource(Files.newInputStream(fileToDownload.toPath())));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/download-faster")
    public ResponseEntity<Resource> downloadFileFaster(@RequestParam("fileName") String filename) {
        log.log(Level.INFO, "[FASTER] Download with /download-faster");
        try {
            var fileToDownload = fileStorageService.getDownloadFile(filename);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentLength(fileToDownload.length())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(new FileSystemResource(fileToDownload));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }







}
