package com.TTT.TTT.Attachment.Repository;

import com.TTT.TTT.Attachment.Domain.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment,Long> {
}
