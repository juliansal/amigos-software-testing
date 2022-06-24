package com.amigoscode.testing.customer;

import com.amigoscode.testing.utils.PhoneNumberValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

class RegistrationServiceTest {

    private RegistrationService underTest;

    @Mock
    private PhoneNumberValidator phoneNumberValidator;

    @Mock
    private CustomerRepository customerRepository;

    @Captor
    private ArgumentCaptor<Customer> customerArgumentCaptor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        underTest = new RegistrationService(customerRepository, phoneNumberValidator);
    }

    @Test
    void itShouldSaveNewCustomer() {
        // Given
        String phoneNumber = "+447000000000";
        Customer customer = new Customer(UUID.randomUUID(), "Tara", phoneNumber);

        given(customerRepository.selectCustomerByPhoneNumber(phoneNumber))
                .willReturn(Optional.empty());

        given(phoneNumberValidator.test(phoneNumber))
                .willReturn(true);
        // When
        underTest.registerNewCustomer(new RegistrationRequest(customer));

        // Then
        then(customerRepository)
                .should()
                .save(customerArgumentCaptor.capture());

        Customer customerCapture = customerArgumentCaptor.getValue();
        assertThat(customerCapture).isEqualTo(customer);
    }

    @Test
    void itShouldSaveNewCustomerWhenIdIsNull() {
        // Given
        String phoneNumber = "+447000000000";
        Customer customer = new Customer(null, "Tara", phoneNumber);

        given(customerRepository.selectCustomerByPhoneNumber(phoneNumber))
                .willReturn(Optional.empty());

        given(phoneNumberValidator.test(phoneNumber))
                .willReturn(true);
        // When
        underTest.registerNewCustomer(new RegistrationRequest(customer));

        // Then
        then(customerRepository)
                .should()
                .save(customerArgumentCaptor.capture());

        Customer customerCapture = customerArgumentCaptor.getValue();
        assertThat(customerCapture)
                .isEqualToIgnoringGivenFields(customer, "id");
        assertThat(customerCapture.getId())
                .isNotNull();
    }

    @Test
    void itShouldNotSaveCustomerWhenTheyAlreadyExist() {
        // Given
        String phoneNumber = "+447000000000";
        Customer customer = new Customer(UUID.randomUUID(), "Tara", phoneNumber);

        given(customerRepository.selectCustomerByPhoneNumber(phoneNumber))
                .willReturn(Optional.of(customer));

        given(phoneNumberValidator.test(phoneNumber))
                .willReturn(true);
        // When
        underTest.registerNewCustomer(new RegistrationRequest(customer));
        // Then
        then(customerRepository)
                .should(never())
                .save(any());
    }

    @Test
    void itShouldNotSaveCustomerWhenPhoneNumberIsInvalid() {
        // Given
        String phoneNumber = "+447";
        Customer customer = new Customer(UUID.randomUUID(), "Tara", phoneNumber);
        RegistrationRequest request = new RegistrationRequest(customer);

        given(phoneNumberValidator.test(phoneNumber))
                .willReturn(false);
        // When
        assertThatThrownBy(() -> underTest.registerNewCustomer(request))
                .isInstanceOf(IllegalArgumentException.class);
        // Then
        then(customerRepository)
                .shouldHaveNoInteractions();
    }

    @Test
    void itShouldThrowWhenPhoneNumberProvidedIsAlreadyTaken() {
        // Given
        String phoneNumber = "+447000000000";
        Customer newCustomer = new Customer(UUID.randomUUID(), "Tara", phoneNumber);
        Customer existingCustomer = new Customer(UUID.randomUUID(), "Mika", phoneNumber);

        given(customerRepository.selectCustomerByPhoneNumber(phoneNumber))
                .willReturn(Optional.of(existingCustomer));

        given(phoneNumberValidator.test(phoneNumber))
                .willReturn(true);
        // Then
        assertThatThrownBy(() -> underTest.registerNewCustomer(new RegistrationRequest(newCustomer)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(String.format("Phone number [%s] is taken", phoneNumber));
        // And
        then(customerRepository)
                .should(never())
                .save(any());
    }
}