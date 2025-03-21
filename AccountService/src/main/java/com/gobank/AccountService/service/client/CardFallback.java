package com.gobank.AccountService.service.client;

import com.gobank.AccountService.Dto.CardDto;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class CardFallback implements  CardsFeignClient {
    @Override
    public ResponseEntity<CardDto> getCardWithMobileNumber(String correlationId, String mobileNumber) {
        return null;
    }
}
