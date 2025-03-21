package com.gobank.AccountService.service.Impl;

import com.gobank.AccountService.Dto.AccountDto;
import com.gobank.AccountService.Dto.AccountMessgingDto;
import com.gobank.AccountService.Dto.CustomerDto;
import com.gobank.AccountService.Exception.CustomerAlreadyPresent;
import com.gobank.AccountService.Exception.ResourceNotFoundException;
import com.gobank.AccountService.constant.AccountConstant;
import com.gobank.AccountService.entity.Account;
import com.gobank.AccountService.entity.Customer;
import com.gobank.AccountService.mapper.AccountMapper;
import com.gobank.AccountService.mapper.CustomerMapper;
import com.gobank.AccountService.repository.AccountsRepository;
import com.gobank.AccountService.repository.CustomerRepository;
import com.gobank.AccountService.service.IAccountService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Random;

@Service
@AllArgsConstructor
public class AccountServiceImpl implements IAccountService {
    @Autowired
    private AccountsRepository accountsRepository;
    private static final Logger log = LoggerFactory.getLogger(AccountServiceImpl.class);
    private final StreamBridge streamBridge;

    @Autowired
    private CustomerRepository customerRepository;
    @Override
    public void createAccount(CustomerDto customerDto) {
        Customer customer= CustomerMapper.mapToCustomer(customerDto,new Customer());
        Optional<Customer>optionalCustomer=customerRepository.findByMobileNumber(customerDto.getMobileNumber());
        if (optionalCustomer.isPresent()){
            throw  new CustomerAlreadyPresent("you enter customerd is already present in the data"+customerDto.getMobileNumber());
        }
        Customer saveCustomer=customerRepository.save(customer);
       Account savedAccounts= accountsRepository.save(createNewAccount(saveCustomer));
        sendCommunication(savedAccounts, saveCustomer);

    }
    private void sendCommunication(Account account, Customer customer) {
        var accountsMsgDto = new AccountMessgingDto(account.getAccountNumber(), customer.getName(),
                customer.getEmail(), customer.getMobileNumber());
        log.info("Sending Communication request for the details: {}", accountsMsgDto);
        var result = streamBridge.send("sendCommunication-out-0", accountsMsgDto);
        log.info("Is the Communication request successfully triggered ? : {}", result);
    }



    private Account createNewAccount(Customer customer){
        Account newAccount=new Account();
        newAccount.setCustomerId(customer.getCustomerId());
        long randomAccNUmber=1000000000L+new Random().nextInt(900000000);
        newAccount.setAccountNumber(randomAccNUmber);
        newAccount.setAccountType(AccountConstant.SAVINGS);
        newAccount.setBranchAddress(AccountConstant.ADDRESS);
        return  newAccount;
    }
    @Override
    public CustomerDto fetchAccount(String mobileNumber) {
        Customer customer=customerRepository.findByMobileNumber(mobileNumber).orElseThrow(()-> new ResourceNotFoundException(" customer ","mobileNumber",mobileNumber));
        Account account=accountsRepository.findByCustomerId(customer.getCustomerId()).orElseThrow(()-> {
            return new ResourceNotFoundException("Account", "customerID", customer.getCustomerId().toString());
        });
        CustomerDto customerDto=CustomerMapper.mapToCustomerDto(customer,new CustomerDto());
        customerDto.setAccountDto(AccountMapper.mapToAccountDto(account, new AccountDto()));
        return  customerDto;
    }

    @Override
    public boolean upadtedAccount(CustomerDto customerDto) {
        boolean isupdated=false;
        AccountDto accountDto=customerDto.getAccountDto();
        if (accountDto!=null){
            Account account=accountsRepository.findById(accountDto.getAccountNumber()).orElseThrow(()-> new ResourceNotFoundException("Account","Accountdto",accountDto.getAccountNumber().toString()));
            AccountMapper.mapToAccounts(accountDto,account);
            account=accountsRepository.save(account);
          Long customerID=account.getCustomerId();
          Customer customer=customerRepository.findById(customerID).orElseThrow(()-> new ResourceNotFoundException("Customer","customerId",customerID.toString()));
          CustomerMapper.mapToCustomer(customerDto,customer);
          customerRepository.save(customer);
          isupdated=true;

        }
        return isupdated;
    }

    @Override
    public boolean deleteAccount(String mobileNumber) {
       Customer customer= customerRepository.findByMobileNumber(mobileNumber).orElseThrow(()-> new ResourceNotFoundException("Customer","mobileNumber",mobileNumber));
       customerRepository.deleteById(customer.getCustomerId());
       accountsRepository.deleteByCustomerId(customer.getCustomerId());
       return  true;
    }

    /**
     * @param accountNumber - Long
     * @return boolean indicating if the update of communication status is successful or not
     */
    @Override
    public boolean updateCommunicationStatus(Long accountNumber) {
        boolean isUpdated = false;
        if(accountNumber !=null ){
            Account accounts = accountsRepository.findById(accountNumber).orElseThrow(
                    () -> new ResourceNotFoundException("Account", "AccountNumber", accountNumber.toString())
            );
            accounts.setCommunicationSw(true);
            accountsRepository.save(accounts);
            isUpdated = true;
        }
        return  isUpdated;
    }


}
