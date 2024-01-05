package com.example.crypto.service;

import com.example.crypto.bot.TelegramBot;
import com.example.crypto.dto.Cryptocurrency;
import com.example.crypto.model.User;
import com.example.crypto.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {
    private final UserRepository userRepository;
    private final TelegramBot telegramBot;
    private final ApiService apiService;

    @Value("${crypto.property.threshold}")
    private double threshold;

    @Async
    @Scheduled(fixedRateString = "${crypto.property.delay}")
    public void process() {
        userRepository.findAll().forEach(user -> detectCryptocurrencyExchangeRate(user.getId()));
    }

    private void detectCryptocurrencyExchangeRate(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();

        List<Cryptocurrency> userCryptocurrencies = user.getCryptocurrency()
                .stream()
                .sorted(Comparator.comparing(Cryptocurrency::getSymbol))
                .toList();

        List<Cryptocurrency> currentCryptocurrencies = apiService.getAll()
                .stream()
                .sorted(Comparator.comparing(Cryptocurrency::getSymbol))
                .toList();

        DecimalFormat dfPercent = new DecimalFormat("#.##");

        for (int i = 0; i < userCryptocurrencies.size(); i++) {
            Cryptocurrency userCrypto = userCryptocurrencies.get(i);
            Cryptocurrency currentCrypto = currentCryptocurrencies.get(i);

            if (userCrypto.getPrice().compareTo(BigDecimal.ZERO) != 0) {
                BigDecimal priceDifference = currentCrypto.getPrice().subtract(userCrypto.getPrice());
                BigDecimal percentageChange = priceDifference.divide(userCrypto.getPrice(), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));

                String cryptoSymbol = userCrypto.getSymbol().endsWith("USDT")
                        ? userCrypto.getSymbol().substring(0, userCrypto.getSymbol().indexOf("USDT")) + " to " + "USDT"
                        : userCrypto.getSymbol();

                if (percentageChange.abs().compareTo(new BigDecimal(threshold)) >= 0) {
                    if (!userCrypto.isNotified()) {
                        StringBuilder message;

                        if (percentageChange.compareTo(BigDecimal.ZERO) > 0) {
                            message = new StringBuilder(buildMessage(currentCrypto, userCrypto, cryptoSymbol,
                                    dfPercent.format(percentageChange), true));
                        } else {
                            message = new StringBuilder(buildMessage(currentCrypto, userCrypto, cryptoSymbol,
                                    dfPercent.format(percentageChange), false));
                        }

                        telegramBot.sendMessage(user.getChatId(), message.toString());

                        user.getCryptocurrency()
                                .stream()
                                .filter(cryptocurrency -> cryptocurrency.getSymbol().equals(userCrypto.getSymbol()))
                                .forEach(cryptocurrency -> cryptocurrency.setNotified(true));
                    }
                }
            }
        }
        userRepository.save(user);
    }

    private String buildMessage(Cryptocurrency currentCryptocurrency, Cryptocurrency userCryptocurrency, String cryptoSymbol,
                                String percentageChange, boolean increase) {
        String change = increase ? "increase" : "dropped";
        String emoji = increase ? "\uD83D\uDFE2" : "\uD83D\uDD34";
        String plot = increase ? "\uD83D\uDCC8" : "\uD83D\uDCC9";
        DecimalFormat dfNumeric = new DecimalFormat("#.##############");

        return plot + cryptoSymbol + " just " + change + " by " + percentageChange + "%! " + emoji +
                "\nFrom: " + dfNumeric.format(userCryptocurrency.getPrice()) + " USDT\n" +
                "To:      " + dfNumeric.format(currentCryptocurrency.getPrice()) + " USDT";
    }
}
