package com.example.PixelMageEcomerceProject.event;

import com.example.PixelMageEcomerceProject.enums.PaymentGateway;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;

@Getter
public class PaymentSuccessEvent extends ApplicationEvent {
    private final Integer orderId;
    private final String transactionId;
    private final BigDecimal amount;
    private final PaymentGateway paymentGateway;

    public PaymentSuccessEvent(Object source, Integer orderId, String transactionId, BigDecimal amount, PaymentGateway paymentGateway) {
        super(source);
        this.orderId = orderId;
        this.transactionId = transactionId;
        this.amount = amount;
        this.paymentGateway = paymentGateway;
    }
}
