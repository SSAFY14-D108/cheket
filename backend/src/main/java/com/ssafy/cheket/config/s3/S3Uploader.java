package com.ssafy.cheket.config.s3;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class S3Uploader {

    private final S3Client s3Client;
    private final S3Properties s3Properties;

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("png", "jpg", "jpeg", "webp");

    public String upload(MultipartFile file, Long showId) {
        validateFile(file);

        String key = createFileKey(file.getOriginalFilename(), showId);

        try {
            PutObjectRequest request = PutObjectRequest.builder().bucket(s3Properties.getS3().getBucket()).key(key)
                .contentType(file.getContentType()).build();

            s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));

        } catch (IOException e) {
            throw new RuntimeException("S3 업로드 실패", e);
        }

        return getFileUrl(key);
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("파일이 존재하지 않습니다.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new RuntimeException("파일 크기는 5MB 이하만 가능합니다.");
        }

        String originalFilename = file.getOriginalFilename();

        String ext = extractExt(originalFilename).toLowerCase();

        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new RuntimeException("지원하지 않는 이미지 형식입니다.");
        }
    }

    public void deleteAllByShowId(Long showId) {
        String prefix = "shows/" + showId + "/";
        String continuationToken = null;

        do {
            ListObjectsV2Request.Builder listRequestBuilder = ListObjectsV2Request.builder()
                .bucket(s3Properties.getS3().getBucket()).prefix(prefix);

            if (continuationToken != null) {
                listRequestBuilder.continuationToken(continuationToken);
            }

            var listResponse = s3Client.listObjectsV2(listRequestBuilder.build());

            List<ObjectIdentifier> deleteObjects = listResponse.contents().stream()
                .map(object -> ObjectIdentifier.builder().key(object.key()).build()).toList();

            if (!deleteObjects.isEmpty()) {
                DeleteObjectsRequest deleteRequest = DeleteObjectsRequest.builder()
                    .bucket(s3Properties.getS3().getBucket()).delete(Delete.builder().objects(deleteObjects).build())
                    .build();

                s3Client.deleteObjects(deleteRequest);
            }

            continuationToken = listResponse.nextContinuationToken();
        } while (continuationToken != null);
    }

    private String createFileKey(String originalFilename, Long showId) {
        String ext = extractExt(originalFilename);
        String uuid = UUID.randomUUID().toString();

        return "shows/" + showId + "/images/" + uuid + "." + ext;
    }

    private String getFileUrl(String key) {
        return "https://" + s3Properties.getS3().getBucket() + ".s3."
            + s3Properties.getRegion().getStaticRegion() + ".amazonaws.com/" + key;
    }

    private String extractExt(String filename) {
        if (filename == null || !filename.contains(".")) {
            throw new RuntimeException("잘못된 파일 형식입니다.");
        }

        int pos = filename.lastIndexOf(".");
        return filename.substring(pos + 1);
    }

}
