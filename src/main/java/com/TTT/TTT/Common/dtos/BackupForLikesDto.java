package com.TTT.TTT.Common.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class BackupForLikesDto {
    private String UserId;
    private Long PostId;
}
