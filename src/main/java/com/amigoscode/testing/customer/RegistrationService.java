package com.amigoscode.testing.customer;

import com.amigoscode.testing.utils.PhoneNumberValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class RegistrationService {

    private final CustomerRepository customerRepository;
    private final PhoneNumberValidator phoneNumberValidator;

    @Autowired
    public RegistrationService(CustomerRepository customerRepository, PhoneNumberValidator phoneNumberValidator) {
        this.customerRepository = customerRepository;
        this.phoneNumberValidator = phoneNumberValidator;
    }

    public void registerNewCustomer(RegistrationRequest request) {
        String phoneNumber = request.getCustomer().getPhoneNumber();
        String name = request.getCustomer().getName();
        // TODO: Validate phone number
        if (!phoneNumberValidator.test(phoneNumber)) {
            throw new IllegalArgumentException(String.format("Phone number "+ phoneNumber +" is not valid"));
        }

        Optional<Customer> optionalCustomer = customerRepository
                .selectCustomerByPhoneNumber(phoneNumber);

        if (optionalCustomer.isPresent()) {
            boolean hasSameName = optionalCustomer.get().getName().equals(name);
            if (hasSameName) {
                // client might have sent request twice
                return;
            }
            throw new IllegalStateException(String.format("Phone number [%s] is taken", phoneNumber));
        }

        if (request.getCustomer().getId() == null) {
            request.getCustomer().setId(UUID.randomUUID());
        }

        customerRepository.save(request.getCustomer());
    }
}
