package com.walkmates.lab1;

import com.walkmates.model.Seeker;
import com.walkmates.model.TrustTier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SeekerSpecBasedTest {

    // Activity 2.1 - Equivalence Partitioning
   
    // Email: valid and invalid equivalence classes
    @Test
    @DisplayName("An email without @ is rejected at registration")
    void invalidEmailIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new Seeker(
                        "not-an-email",
                        "Sam",
                        "0707654321"
                ));
    }

    @Test
    @DisplayName("A valid email is accepted at registration")
    void validEmailIsAccepted() {
        Seeker seeker = new Seeker(
                "sam@example.com",
                "Sam",
                "0707654321"
        );

        assertThat(seeker.getEmail()).isEqualTo("sam@example.com");
    }

    // Display name: valid and invalid equivalence classes
    @Test
    @DisplayName("A display name with less than 2 characters is rejected at registration")
    void invalidNameIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new Seeker(
                        "sam@example.com",
                        "S",
                        "0707654321"
                ));
    }

    @Test
    @DisplayName("A valid display name is accepted at registration")
    void validNameIsAccepted() {
        Seeker seeker = new Seeker(
                "sam@example.com",
                "Sam",
                "0707654321"
        );

        assertThat(seeker.getDisplayName()).isEqualTo("Sam");
    }

    // Phone number: valid and invalid equivalence classes
    @Test
    @DisplayName("A phone number with less than 10 digits is rejected at registration")
    void invalidPhoneIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new Seeker(
                        "sam@example.com",
                        "Sam",
                        "070765432"
                ));
    }

    @Test
    @DisplayName("A valid Swedish phone number is accepted at registration")
    void validPhoneIsAccepted() {
        Seeker seeker = new Seeker(
                "sam@example.com",
                "Sam",
                "0707654321"
        );

        assertThat(seeker.getPhoneNumber()).isEqualTo("0707654321");
    }

    // Wallet top-up: valid and invalid equivalence classes

    @Test
    @DisplayName("A top-up below the minimum amount is rejected")
    void topUpBelowMinimumIsRejected() {
        Seeker seeker = new Seeker(
                "sam@example.com",
                "Sam",
                "0707654321"
        );

        assertThrows(IllegalArgumentException.class,
                () -> seeker.addFunds(9.99));
    }

    @Test
    @DisplayName("A top-up within the allowed range is accepted")
    void validTopUpAmountIsAccepted() {
        Seeker seeker = new Seeker(
                "sam@example.com",
                "Sam",
                "0707654321"
        );

        seeker.addFunds(100.00);

        assertThat(seeker.getBalance()).isEqualTo(100.00);
    }

    @Test
    @DisplayName("A top-up above the single-transaction maximum is rejected")
    void topUpAboveSingleTransactionMaximumIsRejected() {
        Seeker seeker = new Seeker(
                "sam@example.com",
                "Sam",
                "0707654321"
        );

        assertThrows(IllegalArgumentException.class,
                () -> seeker.addFunds(5000.01));
    }

    // Activity 2.2 - Boundary Value Analysis
    // Minimum top-up: 10.00 SEK

    @Test
    @DisplayName("A top-up of 9.99 SEK, just below the minimum, is rejected")
    void topUpJustBelowMinimumIsRejected() {
        Seeker seeker = new Seeker(
                "sam@example.com",
                "Sam",
                "0707654321"
        );

        assertThrows(IllegalArgumentException.class,
                () -> seeker.addFunds(9.99));
    }

    @Test
    @DisplayName("A top-up of exactly 10.00 SEK, the minimum, is accepted")
    void topUpAtMinimumIsAccepted() {
        Seeker seeker = new Seeker(
                "sam@example.com",
                "Sam",
                "0707654321"
        );

        seeker.addFunds(10.00);

        assertThat(seeker.getBalance()).isEqualTo(10.00);
    }

    @Test
    @DisplayName("A top-up of 10.01 SEK, just above the minimum, is accepted")
    void topUpJustAboveMinimumIsAccepted() {
        Seeker seeker = new Seeker(
                "sam@example.com",
                "Sam",
                "0707654321"
        );

        seeker.addFunds(10.01);

        assertThat(seeker.getBalance()).isEqualTo(10.01);
    }

    // Maximum single top-up: 5,000.00 SEK
    @Test
    @DisplayName("A top-up of 4999.99 SEK, just below the single-transaction maximum, is accepted")
    void topUpJustBelowSingleTransactionMaximumIsAccepted() {
        Seeker seeker = new Seeker(
                "sam@example.com",
                "Sam",
                "0707654321"
        );

        seeker.addFunds(4999.99);

        assertThat(seeker.getBalance()).isEqualTo(4999.99);
    }

    @Test
    @DisplayName("A top-up of exactly 5000.00 SEK, the single-transaction maximum, is accepted")
    void topUpAtSingleTransactionMaximumIsAccepted() {
        Seeker seeker = new Seeker(
                "sam@example.com",
                "Sam",
                "0707654321"
        );

        seeker.addFunds(5000.00);

        assertThat(seeker.getBalance()).isEqualTo(5000.00);
    }

    @Test
    @DisplayName("A top-up of 5000.01 SEK, just above the single-transaction maximum, is rejected")
    void topUpJustAboveSingleTransactionMaximumIsRejected() {
        Seeker seeker = new Seeker(
                "sam@example.com",
                "Sam",
                "0707654321"
        );

        assertThrows(IllegalArgumentException.class,
                () -> seeker.addFunds(5000.01));
    }

    // Maximum balance: 20,000.00 SEK
    @Test
    @DisplayName("A balance of 19999.99 SEK, just below the maximum, is accepted")
    void balanceJustBelowMaximumIsAccepted() {
        Seeker seeker = new Seeker(
                "sam@example.com",
                "Sam",
                "0707654321"
        );

        seeker.addFunds(5000.00);
        seeker.addFunds(5000.00);
        seeker.addFunds(5000.00);
        seeker.addFunds(4999.99);

        assertThat(seeker.getBalance()).isEqualTo(19999.99);
    }

    @Test
    @DisplayName("A balance of exactly 20000.00 SEK, the maximum, is accepted")
    void balanceAtMaximumIsAccepted() {
        Seeker seeker = new Seeker(
                "sam@example.com",
                "Sam",
                "0707654321"
        );

        seeker.addFunds(5000.00);
        seeker.addFunds(5000.00);
        seeker.addFunds(5000.00);
        seeker.addFunds(4990.00);
        seeker.addFunds(10.00);

        assertThat(seeker.getBalance()).isEqualTo(20000.00);
    }

    @Test
    @DisplayName("A top-up that would bring the balance above 20000.00 SEK is rejected")
    void balanceJustAboveMaximumIsRejected() {
        Seeker seeker = new Seeker(
                "sam@example.com",
                "Sam",
                "0707654321"
        );

        seeker.addFunds(5000.00);
        seeker.addFunds(5000.00);
        seeker.addFunds(5000.00);
        seeker.addFunds(5000.00);

        assertThrows(IllegalArgumentException.class,
                () -> seeker.addFunds(10.00));

        assertThat(seeker.getBalance()).isEqualTo(20000.00);
    }

    // Activity 2.3 - Decision Table

    // New registration starts with new tier
    @Test
    @DisplayName("A newly registered Seeker starts with the NEW trust tier")
    void newSeekerStartsWithNewTrustTier() {
        Seeker seeker = new Seeker(
                "sam@example.com",
                "Sam",
                "0707654321"
        );

        assertThat(seeker.getTrustTier()).isEqualTo(TrustTier.NEW);
    }

    // NEW: 1 booking, 15% fee
    @Test
    @DisplayName("NEW tier allows 1 concurrent booking and has a 15% platform fee")
    void newTierHasCorrectFeeAndMaxBookings() {
        Seeker seeker = new Seeker(
                "sam@example.com",
                "Sam",
                "0707654321"
        );

        seeker.setTrustTier(TrustTier.NEW);

        assertThat(seeker.getMaxConcurrentBookings()).isEqualTo(1);
        assertThat(seeker.getTrustTier().getPlatformFee()).isEqualTo(0.15);
    }

    // VERIFIED: 3 bookings, 12% fee
    @Test
    @DisplayName("VERIFIED tier allows 3 concurrent bookings and has a 12% platform fee")
    void verifiedTierHasCorrectFeeAndMaxBookings() {
        Seeker seeker = new Seeker(
                "sam@example.com",
                "Sam",
                "0707654321"
        );

        seeker.setTrustTier(TrustTier.VERIFIED);

        assertThat(seeker.getMaxConcurrentBookings()).isEqualTo(3);
        assertThat(seeker.getTrustTier().getPlatformFee()).isEqualTo(0.12);
    }

    // TRUSTED: 5 bookings, 8% fee
    @Test
    @DisplayName("TRUSTED tier allows 5 concurrent bookings and has an 8% platform fee")
    void trustedTierHasCorrectFeeAndMaxBookings() {
        Seeker seeker = new Seeker(
                "sam@example.com",
                "Sam",
                "0707654321"
        );

        seeker.setTrustTier(TrustTier.TRUSTED);

        assertThat(seeker.getMaxConcurrentBookings()).isEqualTo(5);
        assertThat(seeker.getTrustTier().getPlatformFee()).isEqualTo(0.08);
    }

    // PRO_SITTER: 10 bookings, 5% fee
    @Test
    @DisplayName("PRO_SITTER tier allows 10 concurrent bookings and has a 5% platform fee")
    void proSitterTierHasCorrectFeeAndMaxBookings() {
        Seeker seeker = new Seeker(
                "sam@example.com",
                "Sam",
                "0707654321"
        );

        seeker.setTrustTier(TrustTier.PRO_SITTER);

        assertThat(seeker.getMaxConcurrentBookings()).isEqualTo(10);
        assertThat(seeker.getTrustTier().getPlatformFee()).isEqualTo(0.05);
    }
}