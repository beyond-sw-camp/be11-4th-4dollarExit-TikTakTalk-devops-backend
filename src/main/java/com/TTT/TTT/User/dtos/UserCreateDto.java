package com.TTT.TTT.User.dtos;

import com.TTT.TTT.User.domain.DelYN;
import com.TTT.TTT.User.domain.Role;
import com.TTT.TTT.User.domain.User;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UserCreateDto {
    @Size(max = 10, message = "10자 이내로 작성해주세요.")
    // @NotBlank : 문자열에만 사용가능하며 null, 빈문자열(""), 공백문자만 있는 문자열("  ")모두 불가.
    @NotBlank(message = "이름이 비어있습니다.")
    private String name;

    @Size(max = 255, message = "255자 이내로 작성해주세요.")
    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "이메일 형식에 맞지않습니다.")
    private String email;

    @Size(max = 50, message = "50자 이내로 작성해주세요.")
    @NotBlank(message = "아이디는 필수입니다.")
    @Pattern(
            regexp = "^[A-Za-z0-9]+$",
            message = "아이디는 영어대소문자와 숫자만 가능합니다."
    )
    private String loginId;

    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,20}$",
            message = "비밀번호 길이는 8자~20이내, 적어도 하나의 문자와 숫자, 특수기호를 포함해야합니다."
    )
    @NotBlank(message = "비밀번호는 필수입니다.")
    private String password;

    @NotBlank(message = "휴대폰 번호는 필수입니다.")
    @Pattern(
            regexp = "^(010|011|016|017|018|019)[0-9]{7,8}$",
            message = "올바르지 않은 휴대폰 번호 입니다."
    )
    private String phoneNumber;

    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(max = 15, min = 3, message = "닉네임은 3자 이상, 15자 이하여야 합니다.")
    private String nickName;

    @NotBlank(message = "블로그링크는 필수입니다.")
    private String blogLink;

    @NotNull(message = "기수는 필수입니다.")
    private Integer batch;

    @Builder.Default
    private Role role = Role.USER;

    @Builder.Default
    private DelYN delYN = DelYN.N;

    public User toEntity(String password) {
        return User.builder().batch(this.batch).blogLink(this.blogLink)
                            .email(this.email).name(this.name).nickName(this.nickName)
                            .password(password).phoneNumber(this.phoneNumber)
                            .loginId(this.loginId).delYN(this.delYN).role(this.role).build();
    }
}
