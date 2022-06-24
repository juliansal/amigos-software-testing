package com.amigoscode.testing.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class PhoneNumberValidatorTest {

    private PhoneNumberValidator underTest;

    @BeforeEach
    void setUp() {
        underTest = new PhoneNumberValidator();
    }

    @ParameterizedTest
    @CsvSource({
            "+447000000000,true,The valid phone number is failing validation",
            "+437000000000,false,Only UK area code should be allow",
            "+4470,false,Phone number should have 12 digits",
            "4470000000000,false,Phone number should include +"
    })
    @DisplayName("Should allow valid phone numbers")
    void itShouldAllowValidatedPhoneNumber(String phoneNumber, String expected, String reason) {
        // When
        boolean isValid = underTest.test(phoneNumber);
        // Then
        assertThat(isValid)
                .as(reason)
                .isEqualTo(Boolean.valueOf(expected));
    }

}
