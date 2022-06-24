package com.amigoscode.testing.customer;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RegistrationRequest {
    private final Customer customer;

    public RegistrationRequest(@JsonProperty("customer") Customer customer) {
        this.customer = customer;
    }

    public Customer getCustomer() {
        return customer;
    }

    @Override
    public String toString() {
        return "RegistrationRequest{" +
                "customer=" + customer +
                '}';
    }
}
