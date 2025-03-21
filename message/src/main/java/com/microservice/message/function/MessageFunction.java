package com.microservice.message.function;

import com.microservice.message.dto.AccountMessgingDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Function;

@Configuration
public class MessageFunction {
    private  static  final Logger log= LoggerFactory.getLogger(MessageFunction.class);
    @Bean
    public Function<AccountMessgingDto, AccountMessgingDto>email(){
        return  accountMessgingDto -> {
            log.info("sending email with detals: "+accountMessgingDto.toString());
            return accountMessgingDto;
        };
    }
    @Bean
    public Function<AccountMessgingDto, Long>sms(){
        return  accountMessgingDto -> {
            log.info("sending sms with detals: "+accountMessgingDto.toString());
            return accountMessgingDto.accountNumber();
        };
    }

}
