package com.gobank.AccountService.Dto;

/**
 * @param accountNumber
 * @param name
 * @param email
 * @param mobileNumber
 */
public record AccountMessgingDto(Long accountNumber, String name, String email, String mobileNumber) {
}
