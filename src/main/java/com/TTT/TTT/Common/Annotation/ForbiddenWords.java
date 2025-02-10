package com.TTT.TTT.Common.Annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;

import java.io.*;
import java.lang.annotation.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

// 어노테이션 정의
@Documented
@Constraint(validatedBy = ForbiddenWords.Validator.class) // Validator 클래스 연결
@Target({ElementType.METHOD, ElementType.FIELD}) // 필드에도 적용 가능
@Retention(RetentionPolicy.RUNTIME)
public @interface ForbiddenWords {

    String message() default "적절하지 않은 단어가 포함되어 있습니다.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    String[] words() default {}; // 추가 금지어 지정 가능

    /**
     * 실제 검증 로직을 포함하는 Validator 클래스
     */
    class Validator implements ConstraintValidator<ForbiddenWords, String> {

        private List<String> forbiddenWords;

        @Override
        public void initialize(ForbiddenWords annotation) {
            forbiddenWords = new ArrayList<>();

            // 1. 클래스패스에서 금지어 파일 로드
            try {
                Resource resource = new ClassPathResource("forbidden-words.txt");
                InputStream inputStream = resource.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

                forbiddenWords.addAll(reader.lines()
                        .map(String::trim)
                        .filter(line -> !line.startsWith("#") && !line.isEmpty()) // 주석 및 빈 줄 제거
                        .collect(Collectors.toList()));

            } catch (IOException e) {
                System.err.println("금지어 파일을 읽는 데 실패했습니다: " + e.getMessage());
            }

            // 2. 어노테이션에서 설정한 금지어 추가
            forbiddenWords.addAll(Arrays.asList(annotation.words()));
        }

        @Override
        public boolean isValid(String value, ConstraintValidatorContext context) {
            if (value == null || value.isBlank()) {
                return true; // null 또는 빈 값은 허용
            }

            String lowerCaseValue = value.toLowerCase(); // 영어는 소문자로 변환하여 검사

            boolean bool = forbiddenWords.stream()
                    .noneMatch(word -> lowerCaseValue.contains(word.toLowerCase()));
            if(!bool)
            {
                throw new IllegalArgumentException("적절하지 않은 단어가 포함되어 있습니다.");

            }
            return bool;
        }
    }
}
