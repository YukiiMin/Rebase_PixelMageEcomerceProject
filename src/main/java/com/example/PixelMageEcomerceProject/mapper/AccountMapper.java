package com.example.PixelMageEcomerceProject.mapper;

import java.time.LocalDate;
import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import com.example.PixelMageEcomerceProject.dto.request.UpdateProfileRequestDTO;
import com.example.PixelMageEcomerceProject.dto.response.AccountResponse;
import com.example.PixelMageEcomerceProject.entity.Account;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AccountMapper {

    @Mapping(target = "guestReadingUsedToday", source = "account", qualifiedByName = "isGuestReadingUsedToday")
    @Mapping(target = "role", source = "role.roleName")
    AccountResponse toAccountResponse(Account account);

    @Mapping(target = "role", source = "role.roleName")
    AccountResponse.Summary toAccountSummaryResponse(Account account);

    List<AccountResponse> toAccountResponses(List<Account> accounts);

    /**
     * Partial update: Chỉ update field nào có giá trị (khác null) từ DTO
     * MapStruct 1.5+ best practice cho update profile
     */
    @Mapping(target = "customerId", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "emailVerified", ignore = true)
    @Mapping(target = "authProvider", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "guestReadingUsedAt", ignore = true)
    // Explicit mapping for new profile fields
    @Mapping(target = "gender", source = "gender")
    @Mapping(target = "dateOfBirth", source = "dateOfBirth")
    @Mapping(target = "address", source = "address")
    void updateEntityFromDto(UpdateProfileRequestDTO dto, @MappingTarget Account entity);

    @Named("isGuestReadingUsedToday")
    default Boolean isGuestReadingUsedToday(Account account) {
        if (account == null || account.getGuestReadingUsedAt() == null)
            return false;
        return account.getGuestReadingUsedAt().toLocalDate().isEqual(LocalDate.now());
    }
}
