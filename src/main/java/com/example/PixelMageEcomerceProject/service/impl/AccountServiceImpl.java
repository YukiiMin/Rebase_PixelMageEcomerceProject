package com.example.PixelMageEcomerceProject.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.PixelMageEcomerceProject.dto.request.AccountRequestDTO;
import com.example.PixelMageEcomerceProject.dto.request.LoginRequestDTO;
import com.example.PixelMageEcomerceProject.entity.Account;
import com.example.PixelMageEcomerceProject.entity.Role;
import com.example.PixelMageEcomerceProject.repository.AccountRepository;
import com.example.PixelMageEcomerceProject.repository.RoleRepository;
import com.example.PixelMageEcomerceProject.security.service.AuthenticationService;
import com.example.PixelMageEcomerceProject.service.interfaces.AccountService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationService authenticationService;

    @Override
    @Transactional
    public Account createAccount(AccountRequestDTO account) {
        // Validate email
        if (existsByEmail(account.getEmail())) {
            throw new RuntimeException("Email already exists: " + account.getEmail());
        }

        // Validate roleId is provided
        if (account.getRoleId() == null) {
            throw new RuntimeException("Role ID is required");
        }

        // Get role from database and validate it exists
        Role role = roleRepository.findById(account.getRoleId())
                .orElseThrow(() -> new RuntimeException(
                        "Role not found with id: " + account.getRoleId() +
                                ". Please create the role first using POST /api/roles"));

        Account newAccount = new Account();
        newAccount.setEmail(account.getEmail());
        newAccount.setPassword(authenticationService.encodePassword(account.getPassword()));
        newAccount.setName(account.getName());
        newAccount.setPhoneNumber(account.getPhoneNumber());
        newAccount.setRole(role);

        return accountRepository.save(newAccount);
    }

    @Override
    @Transactional
    public Account updateAccount(Integer customerId, Account account) {
        Account existingAccount = accountRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Account not found with id: " + customerId));

        // Check if email is being changed and if new email already exists
        if (!existingAccount.getEmail().equals(account.getEmail()) &&
                existsByEmail(account.getEmail())) {
            throw new RuntimeException("Email already exists: " + account.getEmail());
        }

        // Update fields
        existingAccount.setEmail(account.getEmail());
        existingAccount.setPassword(authenticationService.encodePassword(account.getPassword()));
        existingAccount.setName(account.getName());
        existingAccount.setPhoneNumber(account.getPhoneNumber());

        if (account.getRole() != null) {
            existingAccount.setRole(account.getRole());
        }

        return accountRepository.save(existingAccount);
    }

    @Override
    @Transactional
    public void deleteAccount(Integer customerId) {
        Account account = accountRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Account not found with id: " + customerId));
        account.setIsActive(false);
        accountRepository.save(account);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Account> getAccountById(Integer customerId) {
        return accountRepository.findById(customerId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Account> getAccountByEmail(String email) {
        return accountRepository.findByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return accountRepository.existsByEmail(email);
    }

    @Override
    public Map<String, Object> loginAccount(LoginRequestDTO loginRequestDTO) {
        Account account = accountRepository.findByEmail(loginRequestDTO.getEmail()).orElseThrow(
                () -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(loginRequestDTO.getPassword(), account.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }
        String token = authenticationService.generateToken(account);

        return Map.of(
                "accessToken", token,
                "account", account);
    }
}
