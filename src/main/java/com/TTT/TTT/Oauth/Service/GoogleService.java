package com.TTT.TTT.Oauth.Service;

import com.TTT.TTT.Oauth.dtos.GoogleProfile;
import com.TTT.TTT.Oauth.dtos.OAuthToken;
import com.TTT.TTT.User.UserRepository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.util.Base64;

@Service
@Transactional
public class GoogleService {

    private final UserRepository userRepository;
    @Value("${oauth.google.client-id}")
    private String authGoogleClientId;
    @Value("${oauth.google.client-secret}")
    private String authGoogleClientSecret;

    @Value("${oauth.google.redirect-url}")
    private String redirectUrl;


    public GoogleService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }



    public OAuthToken getAccessToken(String code) throws JsonProcessingException {
        System.out.println("구글 인증서버에 token 요청");

        // client_id와 client_secret을 Base64로 인코딩
        String credentials = authGoogleClientId + ":" + authGoogleClientSecret;
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());

        // 요청 생성 및 전송
        RestClient restClient = RestClient.create();
        ResponseEntity<String> response = restClient.post()
                .uri("https://oauth2.googleapis.com/token")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Accept", "application/json")
                .header("Authorization", "Basic " + encodedCredentials)
                .body("grant_type=authorization_code&redirect_uri=" + redirectUrl + "&code=" + code)
                .retrieve()
                .toEntity(String.class); // 응답을 String 형태로 받음
        // JSON 응답 출력 (디버깅 용)
        System.out.println("응답 JSON: " + response.getBody());
        // 응답을 OAuthToken 객체로 변환
        ObjectMapper objectMapper = new ObjectMapper();
        OAuthToken oAuthToken = objectMapper.readValue(response.getBody(), OAuthToken.class);

        return oAuthToken;
    }


    public GoogleProfile getGoogleProfile(String accessToken) throws JsonProcessingException {
        RestClient restClient = RestClient.create();

        // 응답을 먼저 String으로 받기
        ResponseEntity<String> response = restClient.get()
                .uri("https://openidconnect.googleapis.com/v1/userinfo")
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/x-www-form-urlencoded;charset=utf-8")
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        (googleRequest, googleResponse) -> {
                            throw new IllegalArgumentException("member is not found");
                        })
                .toEntity(String.class); // 응답을 String 형태로 받음

        // JSON 응답 출력 (디버깅 용)
        System.out.println("응답 JSON: " + response.getBody());

        // 응답을 GoogleProfile 객체로 변환
        ObjectMapper objectMapper = new ObjectMapper();
        GoogleProfile googleProfile = objectMapper.readValue(response.getBody(), GoogleProfile.class);

        System.out.println("profile : " + googleProfile);
        return googleProfile;
    }

}
