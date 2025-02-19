package com.TTT.TTT.Oauth.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true) // 없는 필드는 자동 무시
public class OAuthToken {
//    api요청시 인증값으로 사용되는 토큰
    private String access_token;
    private String token_type;
    private String scope;
    private int expires_in;
    private String id_token;
}
