package com.ssafy.cheket.service.ipfs;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

/**
 * PinataService — IPFS 핀닝 서비스 (Pinata API 연동)
 *
 * [역할] NFT 메타데이터와 이미지를 IPFS에 영구 저장하고 CID(Content Identifier)를 반환한다. CID는 파일의
 * 해시값이므로 내용이 같으면 항상 같은 CID → 변조 불가능.
 *
 * [사용처] ① 공연 민팅 시: 포스터 이미지 → IPFS 업로드 → posterIpfsCid 저장 ② 공연 민팅 시: 메타데이터 JSON
 * → IPFS 업로드 → metadataIpfsCid 저장 ③ EventNFT.createEvent()에 metadataCID 전달 →
 * 온체인에 영구 기록
 *
 * [IPFS URI 형식] ipfs://QmXxx... → 탈중앙 표준 URI (NFT 마켓플레이스 호환)
 * https://gateway.pinata.cloud/ipfs/QmXxx... → HTTP로 직접 조회 가능
 *
 * [Pinata API] - pinFileToIPFS: 파일(이미지) 업로드 - pinJSONToIPFS: JSON 메타데이터 업로드 -
 * 인증: Bearer JWT
 */
@Slf4j
@Service
public class PinataService {

    private static final String PINATA_FILE_URL = "https://api.pinata.cloud/pinning/pinFileToIPFS";
    private static final String PINATA_JSON_URL = "https://api.pinata.cloud/pinning/pinJSONToIPFS";

    @Value("${pinata.jwt}")
    private String pinataJwt;

    @Value("${pinata.gateway}")
    private String pinataGateway;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 이미지 파일을 IPFS에 업로드
     *
     * [흐름] MultipartFile → Pinata pinFileToIPFS API → CID 반환
     *
     * @param file
     *            업로드할 이미지 (포스터, 설명 이미지 등)
     * @param fileName
     *            IPFS에 저장될 파일명 (ex: "show_10_poster.jpg")
     * @return IPFS CID (ex: "QmXxx...")
     */
    public String uploadFile(MultipartFile file, String fileName) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.setBearerAuth(pinataJwt);

        // Pinata pinataMetadata — 파일에 이름 태그 지정 (대시보드에서 식별용)
        String metadata = objectMapper.writeValueAsString(Map.of("name", fileName));

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", file.getResource());
        body.add("pinataMetadata", metadata);

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.exchange(PINATA_FILE_URL, HttpMethod.POST, request,
            String.class);

        String cid = extractCid(response.getBody());
        log.info("[IPFS] 파일 업로드 완료 — fileName={}, CID={}", fileName, cid);
        return cid;
    }

    /**
     * S3 URL의 이미지를 다운로드해서 IPFS에 업로드
     *
     * [왜 필요한가?] 공연 등록 시 포스터는 이미 S3에 업로드됨. 민팅 시점에 S3 이미지를 가져와서 IPFS에도 올려야 함.
     *
     * @param imageUrl
     *            S3 이미지 URL
     * @param fileName
     *            IPFS에 저장될 파일명
     * @return IPFS CID
     */
    public String uploadFileFromUrl(String imageUrl, String fileName) throws IOException {
        // S3에서 이미지 다운로드
        byte[] imageBytes = restTemplate.getForObject(imageUrl, byte[].class);
        if (imageBytes == null) {
            throw new IOException("이미지 다운로드 실패: " + imageUrl);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.setBearerAuth(pinataJwt);

        String metadata = objectMapper.writeValueAsString(Map.of("name", fileName));

        // byte[]를 multipart로 변환
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new org.springframework.core.io.ByteArrayResource(imageBytes) {
            @Override
            public String getFilename() {
                return fileName;
            }
        });
        body.add("pinataMetadata", metadata);

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.exchange(PINATA_FILE_URL, HttpMethod.POST, request,
            String.class);

        String cid = extractCid(response.getBody());
        log.info("[IPFS] URL 이미지 업로드 완료 — url={}, CID={}", imageUrl, cid);
        return cid;
    }

    /**
     * JSON 메타데이터를 IPFS에 업로드
     *
     * [NFT 메타데이터 표준 (ERC-721)] { "name": "공연 제목", "description": "공연 설명", "image":
     * "ipfs://포스터CID", "attributes": { ... } }
     *
     * @param metadata
     *            메타데이터 Map (name, description, image, attributes 등)
     * @param name
     *            Pinata 대시보드 표시용 이름
     * @return IPFS CID
     */
    public String uploadJson(Map<String, Object> metadata, String name) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(pinataJwt);

        // Pinata pinJSONToIPFS 요청 형식
        Map<String, Object> requestBody = Map.of("pinataContent", metadata, "pinataMetadata", Map.of("name", name));

        HttpEntity<String> request = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);

        ResponseEntity<String> response = restTemplate.exchange(PINATA_JSON_URL, HttpMethod.POST, request,
            String.class);

        String cid = extractCid(response.getBody());
        log.info("[IPFS] JSON 업로드 완료 — name={}, CID={}", name, cid);
        return cid;
    }

    /**
     * CID로 IPFS Gateway URL 생성 브라우저에서 직접 접근 가능한 HTTP URL
     *
     * @param cid
     *            IPFS CID
     * @return Gateway URL (ex:
     *         "https://brown-random-gibbon-802.mypinata.cloud/ipfs/QmXxx...")
     */
    public String getGatewayUrl(String cid) {
        return "https://" + pinataGateway + "/ipfs/" + cid;
    }

    /**
     * CID로 IPFS URI 생성 NFT 표준 URI (온체인에 기록하는 형식)
     *
     * @param cid
     *            IPFS CID
     * @return IPFS URI (ex: "ipfs://QmXxx...")
     */
    public String getIpfsUri(String cid) {
        return "ipfs://" + cid;
    }

    /**
     * Pinata API 응답에서 CID(IpfsHash) 추출 응답 예: {"IpfsHash": "QmXxx...", "PinSize":
     * 12345, "Timestamp": "..."}
     */
    private String extractCid(String responseBody) throws IOException {
        JsonNode node = objectMapper.readTree(responseBody);
        return node.get("IpfsHash").asText();
    }
}
