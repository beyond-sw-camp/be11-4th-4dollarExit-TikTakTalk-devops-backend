package com.TTT.TTT.Common.Annotation;


import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.io.IOException;
import java.lang.annotation.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// 어노테이션 정의
@Documented
@Constraint(validatedBy = ForbiddenWords.Validator.class)
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ForbiddenWords {

    // 기본 에러 메시지
    String message() default "적절한 단어가 포함 되어있습니다.";

    // 유효성 검사 그룹
    Class<?>[] groups() default {};

    // 추가 메타데이터 전달용
    Class<? extends Payload>[] payload() default {};

    // 어노테이션에 직접 금지 단어를 설정할 수 있도록 옵션 추가
    String[] words() default {};

    /**
     * 실제 검증 로직을 포함하는 Validator 클래스
     */
    class Validator implements ConstraintValidator<ForbiddenWords, String> {

        private List<String> forbiddenWords;

        @Override
        public void initialize(ForbiddenWords annotation) {
            try {
                // forbidden-words.txt에서 단어 읽기
                List<String> fileForbiddenWords = Files.readAllLines(Paths.get("forbidden-words.txt"));

                // 파일 단어와 어노테이션 단어 병합
                forbiddenWords = new ArrayList<>(fileForbiddenWords);
                forbiddenWords.addAll(Arrays.asList(annotation.words()));

            } catch (IOException e) {
                // 파일 읽기 실패 시 기본 단어만 사용
                System.err.println("Failed to load forbidden words: " + e.getMessage());
                forbiddenWords = new ArrayList<>(Arrays.asList(annotation.words()));
            }
        }

        @Override
        public boolean isValid(String value, ConstraintValidatorContext context) {
            // null 또는 공백은 유효
            if (value == null || value.isBlank()) {
                return true;
            }

            // 금지 단어 포함 여부 검사 (대소문자 무시)
            return forbiddenWords.stream()
                    .noneMatch(value.toLowerCase()::contains);
        }
    }
}