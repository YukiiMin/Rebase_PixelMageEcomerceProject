package com.example.PixelMageEcomerceProject.service.interfaces;

import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public interface NFCScanService {
    Map<String, Object> scanNFC(String nfcUid, Integer userId);

    Map<String, Object> linkCard(String nfcUid, Integer userId);

    Map<String, Object> unlinkCard(String nfcUid, Integer userId);
}
