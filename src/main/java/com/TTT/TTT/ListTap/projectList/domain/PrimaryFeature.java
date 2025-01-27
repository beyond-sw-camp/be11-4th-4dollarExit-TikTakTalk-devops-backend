package com.TTT.TTT.ListTap.projectList.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class PrimaryFeature {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String utilityName;
    @JoinColumn(name = "project_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Project project;

    @Override
    public String toString() {
        return utilityName;
    }
}
