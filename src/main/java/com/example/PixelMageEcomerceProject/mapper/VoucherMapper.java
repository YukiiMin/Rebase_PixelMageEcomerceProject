package com.example.PixelMageEcomerceProject.mapper;

import com.example.PixelMageEcomerceProject.dto.response.VoucherResponse;
import com.example.PixelMageEcomerceProject.entity.Voucher;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface VoucherMapper {

    @Mapping(target = "voucherId", source = "id", qualifiedByName = "longToInt")
    @Mapping(target = "isExpired", source = "voucher", qualifiedByName = "checkExpired")
    @Mapping(target = "daysUntilExpiry", source = "voucher", qualifiedByName = "calculateDays")
    VoucherResponse toVoucherResponse(Voucher voucher);

    @Named("longToInt")
    default Integer longToInt(Long id) {
        return id != null ? id.intValue() : null;
    }

    @Named("checkExpired")
    default Boolean checkExpired(Voucher voucher) {
        if (voucher == null || voucher.getExpiresAt() == null) return false;
        return voucher.getExpiresAt().isBefore(LocalDateTime.now());
    }

    @Named("calculateDays")
    default Integer calculateDays(Voucher voucher) {
        if (voucher == null || voucher.getExpiresAt() == null) return 0;
        long days = ChronoUnit.DAYS.between(LocalDateTime.now(), voucher.getExpiresAt());
        return (int) Math.max(0, (int) days);
    }
}
