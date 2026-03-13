package com.example.PixelMageEcomerceProject.service.interfaces;

public interface RedisLockService {

    /**
     * Thử lấy lock với key và TTL cho trước.
     * @return true nếu lấy lock thành công, false nếu key đã tồn tại (bị lock bởi request khác)
     */
    boolean tryLock(String lockKey, long ttlSeconds);

    /**
     * Giải phóng lock (xoá key khỏi Redis).
     */
    void releaseLock(String lockKey);
}
