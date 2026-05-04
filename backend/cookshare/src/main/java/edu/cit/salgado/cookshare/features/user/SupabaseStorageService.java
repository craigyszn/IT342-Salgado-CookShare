package edu.cit.salgado.cookshare.features.user;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service
public class SupabaseStorageService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.anon-key}")
    private String supabaseAnonKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public String uploadRecipeImage(MultipartFile file) throws Exception {
        return uploadFile(file, "recipe-images");
    }

    public String uploadProfilePhoto(MultipartFile file) throws Exception {
        return uploadFile(file, "profile-photos");
    }

    private String uploadFile(MultipartFile file, String bucket) throws Exception {
        String fileName = UUID.randomUUID() + "-" + file.getOriginalFilename();
        String uploadUrl = supabaseUrl + "/storage/v1/object/" + bucket + "/" + fileName;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + supabaseAnonKey);
        headers.setContentType(MediaType.parseMediaType(
            file.getContentType() != null ? file.getContentType() : "application/octet-stream"
        ));

        HttpEntity<byte[]> entity = new HttpEntity<>(file.getBytes(), headers);

        ResponseEntity<String> response = restTemplate.exchange(
            uploadUrl, HttpMethod.POST, entity, String.class
        );

        if (response.getStatusCode().is2xxSuccessful()) {
            return supabaseUrl + "/storage/v1/object/public/" + bucket + "/" + fileName;
        } else {
            throw new Exception("Failed to upload to Supabase: " + response.getBody());
        }
    }
}