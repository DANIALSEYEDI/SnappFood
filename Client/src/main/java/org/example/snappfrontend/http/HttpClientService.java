package org.example.snappfrontend.http;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.snappfrontend.dto.AuthLoginRequest;
import org.example.snappfrontend.dto.AuthLoginResponse;
import org.example.snappfrontend.dto.AuthRegisterRequest;
import org.example.snappfrontend.utils.ApiClient;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class HttpClientService {
    private static final ObjectMapper mapper = ApiClient.getObjectMapper();
    public static AuthLoginResponse login(AuthLoginRequest request) throws IOException, InterruptedException {
        String body = ApiClient.getObjectMapper().writeValueAsString(request);
        Optional<HttpResponse<String>> responseOpt = ApiClient.post("/auth/login", body, null);

        if (responseOpt.isPresent()) {
            HttpResponse<String> response = responseOpt.get();
            int status = response.statusCode();
            String responseBody = response.body();

            if (status == 200) {
                return ApiClient.getObjectMapper().readValue(responseBody, AuthLoginResponse.class);
            } else {
                throw new RuntimeException(responseBody);
            }
        } else {
            throw new RuntimeException("No response received from server.");
        }
    }


    public static HttpResponseData registerRawResponse(AuthRegisterRequest request) throws IOException, InterruptedException {
        String body = mapper.writeValueAsString(request);
        Optional<HttpResponse<String>> responseOpt = ApiClient.post("/auth/register", body, null);

        if (responseOpt.isPresent()) {
            HttpResponse<String> response = responseOpt.get();
            int status = response.statusCode();
            String responseBody = response.body();
            return new HttpResponseData(status, responseBody);
        } else {
            throw new RuntimeException("No response received from server.");
        }
    }


    public static String adminLogin(String phone, String password) throws IOException {
        URL url = new URL("http://localhost:8080/login");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setDoOutput(true);
        String jsonInput = String.format("{\"phonenumber\": \"%s\", \"password\": \"%s\"}", phone, password);
        try (OutputStream os = con.getOutputStream()) {
            byte[] input = jsonInput.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        int responseCode = con.getResponseCode();
        InputStream is = (responseCode >= 200 && responseCode < 300)
                ? con.getInputStream()
                : con.getErrorStream();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(response.toString());

            if (node.has("token")) {
                return node.get("token").asText();
            } else {
                throw new IOException(response.toString());
            }
        }
    }
}