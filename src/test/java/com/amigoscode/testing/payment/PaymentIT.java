package com.amigoscode.testing.payment;

import com.amigoscode.testing.customer.Customer;
import com.amigoscode.testing.customer.RegistrationRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PaymentIT {
    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void itShouldCreatePaymentSuccessfully() throws Exception {
        // Given
        UUID custId = UUID.randomUUID();
        Customer customer = new Customer(custId, "Jaime", "+447000000000");
        RegistrationRequest registrationRequest = new RegistrationRequest(customer);

        ResultActions customerActions = mockMvc.perform(
                put("/api/v1/customer-registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(Objects.requireNonNull(objectToJson(registrationRequest)))
        );

        Long paymentId = 1L;
        Payment payment = new Payment(
                paymentId,
                custId,
                new BigDecimal("100.00"),
                Currency.GBP,
                "x0x0x0x0x0",
                "Food");
        PaymentRequest paymentRequest = new PaymentRequest(payment);
        // When
        ResultActions paymentActions = mockMvc.perform(
                post("/api/v1/payment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(Objects.requireNonNull(objectToJson(paymentRequest))));

        // Then
        customerActions.andExpect(status().isOk());
        paymentActions.andExpect(status().isOk());

        ResultActions retrievePaymentAction = mockMvc.perform(
                get("/api/v1/payment/{id}", paymentId));
        retrievePaymentAction.andExpect(status().isOk());

        MockHttpServletResponse content = retrievePaymentAction.andReturn().getResponse();
        Payment actualPayment = new ObjectMapper().readValue(content.getContentAsString(), Payment.class);

        assertThat(actualPayment)
                .isNotNull();
        assertThat(actualPayment)
                .isEqualToComparingFieldByField(payment);

        // TODO: Ensure sms is delivered
    }

    private String objectToJson(Object customer) {
        try {
            return new ObjectMapper().writeValueAsString(customer);
        } catch (JsonProcessingException e) {
            fail("Failed to convert object to Json");
            return null;
        }
    }
}
