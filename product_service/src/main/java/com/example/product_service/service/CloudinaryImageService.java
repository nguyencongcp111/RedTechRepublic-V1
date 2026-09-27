package com.example.product_service.service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.*;
@Service
public class CloudinaryImageService {
    public record UploadedImage(String url, String publicId) {}
    private final String cloudName, apiKey, apiSecret;
    private final RestClient client;
    public CloudinaryImageService(@Value("${cloudinary.cloud-name:}") String cloudName,
            @Value("${cloudinary.api-key:}") String apiKey, @Value("${cloudinary.api-secret:}") String apiSecret) {
        this.cloudName = cloudName; this.apiKey = apiKey; this.apiSecret = apiSecret;
        var factory = new JdkClientHttpRequestFactory(HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build());
        factory.setReadTimeout(Duration.ofSeconds(30));
        client = RestClient.builder().requestFactory(factory).build();
    }
    public UploadedImage upload(UUID productId, MultipartFile file) {
        if (cloudName.isBlank() || apiKey.isBlank() || apiSecret.isBlank())
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Chưa cấu hình Cloudinary trên server");
        if (!cloudName.matches("[a-zA-Z0-9_-]+")) throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Cấu hình Cloudinary không hợp lệ");
        if (file.isEmpty() || file.getSize() > 5 * 1024 * 1024)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ảnh phải từ 1 byte đến 5 MB");
        byte[] bytes;
        try { bytes = file.getBytes(); } catch (java.io.IOException ex) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Không đọc được ảnh"); }
        String format;
        if (bytes.length >= 3 && (bytes[0] & 255) == 255 && (bytes[1] & 255) == 216 && (bytes[2] & 255) == 255) format = "jpg";
        else if (bytes.length >= 8 && bytes[0] == (byte)137 && bytes[1] == 80 && bytes[2] == 78 && bytes[3] == 71) format = "png";
        else if (bytes.length >= 12 && new String(bytes, 0, 4, java.nio.charset.StandardCharsets.US_ASCII).equals("RIFF")
                && new String(bytes, 8, 4, java.nio.charset.StandardCharsets.US_ASCII).equals("WEBP")) format = "webp";
        else throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chỉ hỗ trợ ảnh JPEG, PNG, WebP");
        final String filename = "product." + format;
        var body = new LinkedMultiValueMap<String, Object>();
        body.add("file", new ByteArrayResource(bytes) { @Override public String getFilename() { return filename; } });
        body.add("public_id", "ecommerce/products/" + productId + "/" + UUID.randomUUID());
        try {
            Map<?, ?> result = client.post().uri("https://api.cloudinary.com/v1_1/" + cloudName + "/image/upload")
                    .headers(h -> h.setBasicAuth(apiKey, apiSecret)).contentType(MediaType.MULTIPART_FORM_DATA).body(body).retrieve().body(Map.class);
            if (result == null || !(result.get("secure_url") instanceof String url) || !(result.get("public_id") instanceof String publicId))
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Cloudinary trả dữ liệu không hợp lệ");
            return new UploadedImage(url, publicId);
        } catch (RestClientException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Không tải được ảnh lên Cloudinary. Kiểm tra cấu hình hoặc thử lại.");
        }
    }
}
