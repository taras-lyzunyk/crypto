package com.example.crypto.service;

import com.example.crypto.dto.Cryptocurrency;
import com.example.crypto.model.User;
import com.example.crypto.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CryptocurrencyService {
    private final ApiService apiService;
    private final UserRepository userRepository;

    public void startCheck(long chatId) {
        User user = new User();
        user.setChatId(chatId);

        updateUserState(apiService.getAll(), user);
    }

    public void restartCheck(long chatId) {
        List<Cryptocurrency> cryptocurrencies = apiService.getAll();
        updateUserState(cryptocurrencies, userRepository.findUserByChatId(chatId));
    }

    @Transactional
    public void stopCheck(long chatId) {
        userRepository.deleteUserByChatId(chatId);
    }

    private void updateUserState(List<Cryptocurrency> cryptocurrencies, User user) {
        user.setCryptocurrency(cryptocurrencies);
        userRepository.save(user);
    }
}
