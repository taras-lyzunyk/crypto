package com.example.crypto.service;

import com.example.crypto.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    @Value("${crypto.property.userLimit}")
    private Long userLimit;

    public boolean isUserLimitReached() {
        return userRepository.countAll() >= userLimit;
    }

    public boolean isUserExists(long chatId) {
        return userRepository.findUserByChatId(chatId) != null;
    }
}
