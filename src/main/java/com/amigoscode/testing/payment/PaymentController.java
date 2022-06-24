package com.amigoscode.testing.payment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("api/v1/payment")
public class PaymentController {

    private final PaymentService paymentService;

    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("{id}")
    public Optional<Payment> getPayment(@PathVariable("id") String paymentId) {
        try {
            Long id = Long.valueOf(paymentId);
            return paymentService.findPaymentById(id);
        } catch (NumberFormatException e) {
            throw new NumberFormatException(String.format("Not a Long value: ", paymentId));
        }
    }

    @PostMapping
    public void makePayment(@RequestBody PaymentRequest paymentRequest) {
        paymentService.chargeCard(paymentRequest.getPayment().getCustomerId(), paymentRequest);
    }
}
