package com.TTT.TTT.Oauth.Service;

import com.TTT.TTT.Oauth.dtos.KakaoProfile;
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

@Service
@Transactional
public class KakaoService {

    private final UserRepository userRepository;
    @Value("${oauth.kakao.client-id}")
    private String authKakaoClientId;
    @Value("${oauth.kakao.redirect-url}")
    private String redirectUrl;


    public KakaoService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    public OAuthToken getAccessToken(String code) throws JsonProcessingException {
        System.out.println("카카오 인증서버에 token 요청");

        // 요청 생성 및 전송
        RestClient restClient = RestClient.create();
        ResponseEntity<String> response = restClient.post()
                .uri("https://kauth.kakao.com/oauth/token")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Accept", "application/json")
                .body("grant_type=authorization_code&redirect_uri=" + redirectUrl + "&code=" + code+"&client_id="+authKakaoClientId)
                .retrieve()
                .toEntity(String.class); // 응답을 String 형태로 받음
        // JSON 응답 출력 (디버깅 용)
        System.out.println("응답 JSON: " + response.getBody());
        // 응답을 OAuthToken 객체로 변환
        ObjectMapper objectMapper = new ObjectMapper();
        OAuthToken oAuthToken = objectMapper.readValue(response.getBody(), OAuthToken.class);

        return oAuthToken;
    }



    public KakaoProfile getKakaoProfile(String accessToken) throws JsonProcessingException {
        RestClient restClient = RestClient.create();

        // 응답을 먼저 String으로 받기
        ResponseEntity<String> response = restClient.get()
                .uri("https://kapi.kakao.com/v2/user/me")
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/x-www-form-urlencoded;charset=utf-8")
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        (kakaoRequest, kakaoResponse) -> {
                            throw new IllegalArgumentException("member is not found");
                        })
                .toEntity(String.class); // 응답을 String 형태로 받음

        // JSON 응답 출력 (디버깅 용)
        System.out.println("응답 JSON: " + response.getBody());

        // 응답을 kakaoProfile 객체로 변환
        ObjectMapper objectMapper = new ObjectMapper();
        KakaoProfile kakaoProfile = objectMapper.readValue(response.getBody(), KakaoProfile.class);

        System.out.println("profile : " + kakaoProfile);
        return kakaoProfile;
    }



}
