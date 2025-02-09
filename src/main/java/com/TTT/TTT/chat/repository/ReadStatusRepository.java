package com.TTT.TTT.chat.repository;

import com.TTT.TTT.chat.domain.ReadStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReadStatusRepository extends JpaRepository<ReadStatus, Long> {
}
