package com.example.PixelMageEcomerceProject.mapper;

import com.example.PixelMageEcomerceProject.dto.response.WalletResponse;
import com.example.PixelMageEcomerceProject.entity.PmPointWallet;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper
public interface WalletMapper {

    @Mapping(target = "canRedeemVoucher", source = "wallet", qualifiedByName = "canRedeem")
    @Mapping(target = "pointsToNextVoucher", source = "wallet", qualifiedByName = "calculatePointsToNext")
    WalletResponse toWalletResponse(PmPointWallet wallet);

    @Named("canRedeem")
    default boolean canRedeem(PmPointWallet wallet) {
        return wallet.getBalance() != null && wallet.getBalance() >= 100;
    }

    @Named("calculatePointsToNext")
    default Integer calculatePointsToNext(PmPointWallet wallet) {
        if (wallet.getBalance() == null) return 100;
        return Math.max(0, 100 - (wallet.getBalance() % 100));
    }
}
