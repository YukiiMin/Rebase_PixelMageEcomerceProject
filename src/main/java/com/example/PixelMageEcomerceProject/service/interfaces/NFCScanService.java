package com.example.PixelMageEcomerceProject.service.interfaces;

import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public interface NFCScanService {
    Map<String, Object> scanNFC(String nfcUid, String softwareUuid, Integer userId);

    Map<String, Object> linkCard(String nfcUid, String softwareUuid, Integer userId);

    Map<String, Object> unlinkCard(String nfcUid, Integer userId);
}
