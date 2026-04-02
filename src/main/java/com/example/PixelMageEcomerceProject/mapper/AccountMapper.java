package com.example.PixelMageEcomerceProject.mapper;

import com.example.PixelMageEcomerceProject.dto.response.AccountResponse;

import com.example.PixelMageEcomerceProject.entity.Account;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AccountMapper {

    @Mapping(target = "guestReadingUsedToday", source = "account", qualifiedByName = "isGuestReadingUsedToday")
    @Mapping(target = "role", source = "role.roleName")
    AccountResponse toAccountResponse(Account account);

    @Mapping(target = "role", source = "role.roleName")
    AccountResponse.Summary toAccountSummaryResponse(Account account);

    List<AccountResponse> toAccountResponses(List<Account> accounts);

    @Named("isGuestReadingUsedToday")
    default Boolean isGuestReadingUsedToday(Account account) {
        if (account == null || account.getGuestReadingUsedAt() == null) return false;
        return account.getGuestReadingUsedAt().toLocalDate().isEqual(java.time.LocalDate.now());
    }
}
