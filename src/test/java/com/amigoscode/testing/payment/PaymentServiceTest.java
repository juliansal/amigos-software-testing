package com.amigoscode.testing.payment;

import com.amigoscode.testing.customer.Customer;
import com.amigoscode.testing.customer.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

class PaymentServiceTest {
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private CardPaymentCharger cardPaymentCharger;

    private PaymentService underTest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        underTest = new PaymentService(customerRepository, paymentRepository, cardPaymentCharger);
    }

    @Test
    void itShouldChargeCardSuccessfully() {
        // Given
        UUID customerId = UUID.randomUUID();
        given(customerRepository.findById(customerId))
                .willReturn(Optional.of(mock(Customer.class)));

        PaymentRequest paymentRequest = new PaymentRequest(
                new Payment(
                        null,
                        null,
                        new BigDecimal("100.00"),
                        Currency.USD,
                        "card123xx",
                        "Donation"
                )
        );

        Payment payment = paymentRequest.getPayment();
        given(cardPaymentCharger
                .chargeCard(
                        payment.getSource(),
                        payment.getAmount(),
                        payment.getCurrency(),
                        payment.getDescription()
                ))
                .willReturn(new CardPaymentCharge(true));
        // When
        underTest.chargeCard(customerId, paymentRequest);
        // Then
        ArgumentCaptor<Payment>
                paymentArgumentCaptor = ArgumentCaptor.forClass(Payment.class);

        then(paymentRepository)
                .should()
                .save(paymentArgumentCaptor.capture());

        Payment paymentArgumentCaptorValue = paymentArgumentCaptor.getValue();

        assertThat(paymentArgumentCaptorValue)
                .isEqualToIgnoringGivenFields(paymentRequest.getPayment(), "customerId");
        assertThat(paymentArgumentCaptorValue.getCustomerId())
                .isEqualTo(customerId);
    }

    @Test
    void itShouldThrowWhenCardIsNotCharged() {
        // Given
        UUID customerId = UUID.randomUUID();
        given(customerRepository.findById(customerId))
                .willReturn(Optional.of(mock(Customer.class)));

        PaymentRequest paymentRequest = new PaymentRequest(
                new Payment(
                        null,
                        null,
                        new BigDecimal("100.00"),
                        Currency.USD,
                        "card123xx",
                        "Donation"
                )
        );

        Payment payment = paymentRequest.getPayment();
        given(cardPaymentCharger
                .chargeCard(
                        payment.getSource(),
                        payment.getAmount(),
                        payment.getCurrency(),
                        payment.getDescription()
                ))
                .willReturn(new CardPaymentCharge(false));
        // When
        assertThatThrownBy(() -> underTest.chargeCard(customerId, paymentRequest))
                .isInstanceOf(IllegalStateException.class);
        // Then
        then(paymentRepository)
                .should(never())
                .save(any(Payment.class));
    }

    @Test
    void itShouldThrowWhenCurrencyNotSupported() {
        // Given
        UUID customerId = UUID.randomUUID();
        given(customerRepository.findById(customerId))
                .willReturn(Optional.of(mock(Customer.class)));

        PaymentRequest paymentRequest = new PaymentRequest(
                new Payment(
                        null,
                        null,
                        new BigDecimal("10000"),
                        Currency.EUR,
                        "card123xx",
                        "Donation"
                )
        );
        // When
        assertThatThrownBy(() -> underTest.chargeCard(customerId, paymentRequest))
                .isInstanceOf(IllegalStateException.class);
        // Then
        then(cardPaymentCharger)
                .shouldHaveNoInteractions();

        then(paymentRepository)
                .should(never())
                .save(any(Payment.class));
    }

    @Test
    void itShouldThrowWhenCustomerNotFound() {
        // Given
        UUID customerId = UUID.randomUUID();
        given(customerRepository.findById(customerId))
                .willReturn(Optional.empty());
        // When
        assertThatThrownBy(() -> underTest.chargeCard(customerId, new PaymentRequest(new Payment())))
                .isInstanceOf(IllegalStateException.class);
        // Then
        then(cardPaymentCharger)
                .shouldHaveNoInteractions();

        then(paymentRepository)
                .shouldHaveNoInteractions();
    }
}








