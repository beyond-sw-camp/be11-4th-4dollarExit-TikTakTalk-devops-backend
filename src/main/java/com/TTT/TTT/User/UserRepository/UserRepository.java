package com.TTT.TTT.User.UserRepository;


import com.TTT.TTT.Common.BaseTimeEntity;
import com.TTT.TTT.User.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailAndDelYN(String email, Enum delYN);

    Optional<User> findByNickNameAndDelYN(String nickName, Enum delYN);

    Optional<User> findByIdAndDelYN(Long id, Enum delYN);

    Optional<User> findByPhoneNumberAndDelYN(String phoneNumber,Enum delYN);

    Optional<User> findByLoginIdAndDelYN(String username,Enum delYN);
}