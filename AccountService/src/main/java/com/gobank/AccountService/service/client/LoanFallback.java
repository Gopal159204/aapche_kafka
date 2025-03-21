package com.gobank.AccountService.service.client;

import com.gobank.AccountService.Dto.LoanDto;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class LoanFallback implements  LoanFeignClient {
    @Override
    public ResponseEntity<LoanDto> fetchLoan(String correlationId, String mobileNumber) {
        return null;
    }
}
