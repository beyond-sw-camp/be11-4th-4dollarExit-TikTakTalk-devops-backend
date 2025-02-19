package com.TTT.TTT.Oauth.dtos;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true) // 없는 필드는 자동 무시
public class GoogleProfile {
    private String sub;
    private String email;
    private String picture;
}


