package com.walkmates.lab2;

import com.walkmates.model.Seeker;
import com.walkmates.repository.SeekerRepository;
import com.walkmates.service.NotificationService;
import com.walkmates.service.PaymentService;
import com.walkmates.service.SeekerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SeekerServiceTest {

    @Mock
    private SeekerRepository seekers;

    @Mock
    private PaymentService payments;

    @Mock
    private NotificationService notifications;

    private SeekerService seekerService;

    @BeforeEach
    void setUp() {
        seekerService = new SeekerService(seekers, payments, notifications);
    }

    @Test
@DisplayName("Registering a new seeker creates and saves the seeker")
void registerNewSeekerCreatesAndSavesSeeker() {
    String email = "new@example.com";
    String displayName = "New User";
    String phoneNumber = "0702223344";

    when(seekers.findByEmail(email)).thenReturn(Optional.empty());

    Seeker savedSeeker = new Seeker(email, displayName, phoneNumber);
    when(seekers.save(org.mockito.ArgumentMatchers.any(Seeker.class)))
            .thenReturn(savedSeeker);

    Seeker result = seekerService.register(email, displayName, phoneNumber);

    assertThat(result).isSameAs(savedSeeker);
    assertThat(result.getEmail()).isEqualTo(email);
    assertThat(result.getDisplayName()).isEqualTo(displayName);
    assertThat(result.getPhoneNumber()).isEqualTo(phoneNumber);

    verify(seekers).findByEmail(email);
    verify(seekers).save(org.mockito.ArgumentMatchers.any(Seeker.class));
}

@Test
@DisplayName("Registering an existing email is rejected")
void registerExistingEmailIsRejected() {
    String email = "existing@example.com";

    Seeker existingSeeker = new Seeker(
            email,
            "Existing User",
            "0703334455"
    );

    when(seekers.findByEmail(email))
            .thenReturn(Optional.of(existingSeeker));

    assertThatThrownBy(() ->
            seekerService.register(email, "Another User", "0704445566")
    )
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Email already registered");

    verify(seekers).findByEmail(email);
    org.mockito.Mockito.verify(
            seekers,
            org.mockito.Mockito.never()
    ).save(org.mockito.ArgumentMatchers.any(Seeker.class));
}

@Test
@DisplayName("Top-up for unknown seeker is rejected")
void topUpUnknownSeekerIsRejected() throws PaymentService.PaymentException {
    String seekerId = "unknown-seeker";
    String paymentMethodId = "pm-1";

    when(seekers.findById(seekerId))
            .thenReturn(Optional.empty());

    assertThatThrownBy(() ->
            seekerService.topUp(seekerId, paymentMethodId, 50.0)
    )
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Unknown seeker: " + seekerId);

    verify(seekers).findById(seekerId);

    org.mockito.Mockito.verify(
            payments,
            org.mockito.Mockito.never()
    ).charge(
            org.mockito.ArgumentMatchers.anyString(),
            org.mockito.ArgumentMatchers.anyString(),
            org.mockito.ArgumentMatchers.anyDouble()
    );

    org.mockito.Mockito.verify(
            seekers,
            org.mockito.Mockito.never()
    ).save(org.mockito.ArgumentMatchers.any(Seeker.class));
}
    
    
    @Test
@DisplayName("Successful top-up adds funds and saves seeker")
void successfulTopUpAddsFundsAndSavesSeeker() throws PaymentService.PaymentException {
    String seekerId = "seeker-1";
    String paymentMethodId = "pm-1";

    Seeker seeker = new Seeker(
            "pat@example.com",
            "Pat",
            "0701112233"
    );

    when(seekers.findById(seekerId)).thenReturn(Optional.of(seeker));
    when(payments.charge(seekerId, paymentMethodId, 50.0))
            .thenReturn("transaction-1");
    when(seekers.save(seeker)).thenReturn(seeker);

    Seeker result = seekerService.topUp(
            seekerId,
            paymentMethodId,
            50.0
    );

    assertThat(result).isSameAs(seeker);
    assertThat(seeker.getBalance()).isEqualTo(50.0);

    verify(payments).charge(seekerId, paymentMethodId, 50.0);
    verify(seekers).save(seeker);
}

@Test
@DisplayName("Payment decline does not add funds or save seeker")
void paymentDeclineDoesNotAddFundsOrSaveSeeker() throws Exception {
    String seekerId = "seeker-1";
    String paymentMethodId = "pm-1";

    Seeker seeker = new Seeker(
            "pat@example.com",
            "Pat",
            "0701112233"
    );

    when(seekers.findById(seekerId)).thenReturn(Optional.of(seeker));

    when(payments.charge(seekerId, paymentMethodId, 50.0))
            .thenThrow(new PaymentService.PaymentException("Payment declined"));

    assertThatThrownBy(() ->
            seekerService.topUp(seekerId, paymentMethodId, 50.0)
    )
            .isInstanceOf(PaymentService.PaymentException.class)
            .hasMessage("Payment declined");

    assertThat(seeker.getBalance()).isEqualTo(0.0);

    verify(payments).charge(seekerId, paymentMethodId, 50.0);
    org.mockito.Mockito.verify(seekers, org.mockito.Mockito.never()).save(seeker);
}

@Test
@DisplayName("Payment timeout does not add funds or save seeker")
void paymentTimeoutDoesNotAddFundsOrSaveSeeker()
        throws PaymentService.PaymentException {
    String seekerId = "seeker-1";
    String paymentMethodId = "pm-1";

    Seeker seeker = new Seeker(
            "pat@example.com",
            "Pat",
            "0701112233"
    );

    when(seekers.findById(seekerId)).thenReturn(Optional.of(seeker));

    when(payments.charge(seekerId, paymentMethodId, 50.0))
            .thenThrow(new PaymentService.PaymentTimeoutException("Payment timed out"));

    assertThatThrownBy(() ->
            seekerService.topUp(seekerId, paymentMethodId, 50.0)
    )
            .isInstanceOf(PaymentService.PaymentTimeoutException.class)
            .hasMessage("Payment timed out");

    assertThat(seeker.getBalance()).isEqualTo(0.0);

    verify(payments).charge(seekerId, paymentMethodId, 50.0);
    org.mockito.Mockito.verify(seekers, org.mockito.Mockito.never()).save(seeker);
}
}