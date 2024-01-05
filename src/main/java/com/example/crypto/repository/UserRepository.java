package com.example.crypto.repository;

import com.example.crypto.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findUserByChatId(Long chatId);

    @Query("""
            SELECT count(*) from User
            """)
    Long countAll();

    void deleteUserByChatId(Long chatId);
}
