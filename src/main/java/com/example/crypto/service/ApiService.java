package com.example.crypto.service;

import com.example.crypto.dto.Cryptocurrency;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ApiService {
    private final RestTemplate restTemplate;

    public List<Cryptocurrency> getAll() {

        String url = "https://api.mexc.com/api/v3/ticker/price";

        ResponseEntity<List<Cryptocurrency>> exchange = this.restTemplate.exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<>() {
        });
        return exchange.getBody();
    }
}
