package com.TTT.TTT.User.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class BatchRankDto {
    private Integer batch;
    private Integer averageRankingPoint; // 소수점에서 반올림.

    // Hibernate가 사용할 수 있도록 명시적 생성자 추가
    public BatchRankDto(Integer batch, Long averageRankingPoint) {
        this.batch = batch;
        this.averageRankingPoint = averageRankingPoint.intValue(); // Long → Integer 변환
    }
}
