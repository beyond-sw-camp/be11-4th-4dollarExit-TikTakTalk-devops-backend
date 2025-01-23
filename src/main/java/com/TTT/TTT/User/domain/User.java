package com.TTT.TTT.User.domain;

import com.TTT.TTT.Common.Annotation.ForbiddenWords;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(length = 10, nullable = false)
    @Size(max = 10, message = "10자 이내로 작성해주세요.")
    private String name;
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,20}$",
            message = "비밀번호 길이는 8자~20이내, 적어도 하나의 문자와 숫자, 특수기호를 포함해야합니다."
    )
    @Column(length = 20, nullable = false)
    private String password;
    @Size(max = 255)
    @NotNull(message = "이메일은 필수입니다.")
    @Email(message = "이메일 형식에 맞지않습니다.")
    private String email;
    @Column(length = 11, nullable = false)
    @NotNull(message = "휴대폰 번호를 적어주세요.")
    @Pattern(
            regexp = "^(010|011|016|017|018|019)[0-9]{7,8}$",
            message = "형식에 맞지않는 휴대폰 번호 입니다."
    )
    private String phoneNumber;     //api 예정
    @Size(max = 15, min = 3, message = "닉네임은 3자 이상, 15자 이하여야 합니다.")
    @ForbiddenWords(words = {"admin", "시발", "씨발"}, message = "닉네임에 금지된 단어가 포함되어 있습니다.")
    @Column(nullable = false, unique = true)
    private String nickname;
    @Column(nullable = false)
    private String delYN;
    @Column(nullable = false)
    private String adminYN;
    @Column(length = 5, nullable = false)
    private int batch;//랭크
    @Column(length = 20, nullable = false)
    private String blogLink;

    private String createdTime;
}
