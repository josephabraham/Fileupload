package com.jaka.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
public class ChunkUploadController {
    private static final Logger log = Logger.getLogger(ChunkUploadController.class.getName());
    private static final String TEMP_DIR = "C:\\Personal\\Projects\\files\\tmp\\";
    private static final String FINAL_DIR = "C:\\Personal\\Projects\\files\\final\\";

    @PostMapping("/chunk-upload")
    public ResponseEntity<String> handleChunkUpload(
            @RequestParam("file") MultipartFile chunk,
            @RequestParam("chunkIndex") int chunkIndex,
            @RequestParam("totalChunks") int totalChunks,
            @RequestParam("fileName") String fileName) throws IOException {

        log.log(Level.INFO, "Input File name :"+fileName + "\n Chunk index :"+chunkIndex +" ,\n Total Chunks " +totalChunks);
        File dir = new File(TEMP_DIR + fileName);
        if (!dir.exists()) dir.mkdirs();

        File chunkFile = new File(dir, "chunk_" + chunkIndex);
        chunk.transferTo(chunkFile);

        // Check if all chunks are uploaded
        if (allChunksUploaded(dir, totalChunks)) {
            mergeChunks(dir, fileName, totalChunks);
            deleteDirectory(dir); // Clean up temp chunks
            return ResponseEntity.ok("File uploaded and merged successfully.");
        }

        return ResponseEntity.ok("Chunk received.");
    }

    private boolean allChunksUploaded(File dir, int totalChunks) {
        String[] files = dir.list((d, name) -> name.startsWith("chunk_"));
        return files != null && files.length == totalChunks;
    }

    private void mergeChunks(File dir, String fileName, int totalChunks) throws IOException {
        File finalDir = new File(FINAL_DIR);
        if (!finalDir.exists()) finalDir.mkdirs();

        File mergedFile = new File(finalDir, fileName);
        try (BufferedOutputStream mergedStream = new BufferedOutputStream(new FileOutputStream(mergedFile))) {
            for (int i = 0; i < totalChunks; i++) {
                File chunkFile = new File(dir, "chunk_" + i);
                Files.copy(chunkFile.toPath(), mergedStream);
            }
        }
    }

    private void deleteDirectory(File directory) throws IOException {
        Files.walk(directory.toPath())
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }
}
