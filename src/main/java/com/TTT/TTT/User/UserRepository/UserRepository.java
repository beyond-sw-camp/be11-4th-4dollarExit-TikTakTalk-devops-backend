package com.TTT.TTT.User.UserRepository;


import com.TTT.TTT.User.domain.User;
import com.TTT.TTT.User.dtos.BatchRankDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    Optional<User> findByEmailAndDelYN(String email, Enum delYN);

    Optional<User> findByNickNameAndDelYN(String nickName, Enum delYN);

    Optional<User> findByIdAndDelYN(Long id, Enum delYN);

    Optional<User> findByPhoneNumberAndDelYN(String phoneNumber,Enum delYN);

    Optional<User> findByLoginIdAndDelYN(String username,Enum delYN);

    Page<User> findAll(Pageable pageable);

    List<User> findTop5ByOrderByRankingPointDesc();

    Optional<User> findBySocialId(String socialId);

//    jpql new키워드를 사용하여 jpql에서 직접 DTO 객체를 생성
//    batchRankDto의 생성자를 호출하여 데이터를 DTO로 매핑시킴.
    @Query("SELECT new com.TTT.TTT.User.dtos.BatchRankDto(u.batch, CAST(AVG(u.rankingPoint) AS Integer)) " +
            "FROM User u " +
            "GROUP BY u.batch " +
            "ORDER BY AVG(u.rankingPoint) DESC")
    Page<BatchRankDto> findTopBatchesWithAvgRankingPoint(Pageable pageable);
}