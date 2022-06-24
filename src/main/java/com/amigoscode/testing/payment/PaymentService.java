package com.amigoscode.testing.payment;

import com.amigoscode.testing.customer.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentService {

    private final CustomerRepository customerRepository;
    private final PaymentRepository paymentRepository;
    private final CardPaymentCharger cardPaymentCharger;
    private static final List<Currency> ACCEPTED_CURRENCIES = List.of(Currency.USD, Currency.GBP);

    @Autowired
    public PaymentService(CustomerRepository customerRepository,
                          PaymentRepository paymentRepository,
                          CardPaymentCharger cardPaymentCharger) {
        this.customerRepository = customerRepository;
        this.paymentRepository = paymentRepository;
        this.cardPaymentCharger = cardPaymentCharger;
    }

    void chargeCard(UUID customerId, PaymentRequest paymentRequest) {
        // 1. Does customer exist, if not throw
        boolean customerExists = customerRepository.findById(customerId).isPresent();
        if (!customerExists) { throw new IllegalStateException(String.format("Not a customer", customerId)); }
        // 2. Do we support the currency, if not throw
        Payment payment = paymentRequest.getPayment();
        boolean isCurrencySupported = ACCEPTED_CURRENCIES
                .stream()
                .anyMatch(c -> c.equals(payment.getCurrency()));

        if (!isCurrencySupported) { throw new IllegalStateException(String.format("This currency is not supported", payment.getCurrency())); }
        // 3. Charge card
        boolean isDebitSuccess = cardPaymentCharger
                .chargeCard(
                        payment.getSource(),
                        payment.getAmount(),
                        payment.getCurrency(),
                        payment.getDescription()
                )
                .isCardDebited();

        // 4. If not debited throw
        if (!isDebitSuccess) { throw new IllegalStateException("The card was not successfully debited"); }
        // 5. Insert payment
        payment.setCustomerId(customerId);
        paymentRepository.save(payment);
        // 6. TODO: send sms
    }

    Optional<Payment> findPaymentById(Long payment) {
        return paymentRepository.findById(payment);
    }
}
