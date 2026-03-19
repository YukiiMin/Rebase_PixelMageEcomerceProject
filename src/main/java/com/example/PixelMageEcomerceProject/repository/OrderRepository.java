package com.example.PixelMageEcomerceProject.repository;

import com.example.PixelMageEcomerceProject.entity.Order;
import com.example.PixelMageEcomerceProject.enums.OrderStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    List<Order> findByAccountCustomerId(Integer customerId);
    List<Order> findByStatus(OrderStatus status);
}
