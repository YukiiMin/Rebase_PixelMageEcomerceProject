package com.example.PixelMageEcomerceProject.mapper;

import com.example.PixelMageEcomerceProject.dto.response.AccountResponse;
import com.example.PixelMageEcomerceProject.dto.response.AccountSummaryResponse;
import com.example.PixelMageEcomerceProject.entity.Account;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper
public interface AccountMapper {

    @Mapping(target = "guestReadingUsedToday", source = "account", qualifiedByName = "isGuestReadingUsedToday")
    @Mapping(target = "role", source = "role.roleName")
    AccountResponse toAccountResponse(Account account);

    @Mapping(target = "role", source = "role.roleName")
    AccountSummaryResponse toAccountSummaryResponse(Account account);

    @Named("isGuestReadingUsedToday")
    default boolean isGuestReadingUsedToday(Account account) {
        if (account.getGuestReadingUsedAt() == null) return false;
        return account.getGuestReadingUsedAt().toLocalDate().isEqual(java.time.LocalDate.now());
    }
}
