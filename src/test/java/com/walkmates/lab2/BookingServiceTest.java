package com.walkmates.lab2;

import com.walkmates.model.Booking;
import com.walkmates.model.Listing;
import com.walkmates.model.ListingType;
import com.walkmates.model.Seeker;
import com.walkmates.model.TrustTier;
import com.walkmates.model.Provider;
import com.walkmates.repository.BookingRepository;
import com.walkmates.repository.ListingRepository;
import com.walkmates.repository.ProviderRepository;
import com.walkmates.repository.SeekerRepository;
import com.walkmates.service.BookingService;
import com.walkmates.service.NotificationService;
import com.walkmates.service.PricingCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private SeekerRepository seekers;

    @Mock
    private ListingRepository listings;

    @Mock
    private ProviderRepository providers;

    @Mock
    private BookingRepository bookings;

    @Mock
    private PricingCalculator pricing;

    @Mock
    private NotificationService notifications;

    private BookingService bookingService;

    @BeforeEach
    void setUp() {
        bookingService = new BookingService(
                seekers,
                listings,
                providers,
                bookings,
                pricing,
                notifications
        );
    }

    @Test
    @DisplayName("Rejects booking when seeker already has maximum active bookings")
    void rejectsWhenSeekerIsAtMaximumConcurrentBookings() {
        String seekerId = "seeker-1";
        String listingId = "listing-1";

        Seeker seeker = new Seeker(
                "pat@example.com",
                "Pat",
                "0701112233"
        );
        seeker.setTrustTier(TrustTier.NEW);

        Listing listing = new Listing(
                "provider-1",
                "Dog walking",
                "A dog walking listing",
                ListingType.DOG_WALK
        );

        Booking existingBooking = new Booking(
                seekerId,
                "another-listing",
                60
        );
        existingBooking.transitionTo(com.walkmates.model.BookingStatus.CONFIRMED);

        when(seekers.findById(seekerId))
                .thenReturn(Optional.of(seeker));

        when(listings.findById(listingId))
                .thenReturn(Optional.of(listing));

        when(bookings.findBySeekerId(seekerId))
                .thenReturn(List.of(existingBooking));

        assertThatThrownBy(() ->
                bookingService.createBooking(seekerId, listingId, 60)
        )
                .isInstanceOf(BookingService.BookingRejectedException.class)
                .hasMessageContaining("booking limit");
    }

    @Test
    @DisplayName("Successfully creates and confirms a booking")
    void successfullyCreatesBooking() {
        String seekerId = "seeker-1";
        
        Seeker seeker = new Seeker(
            "pat@example.com",
            "Pat",
            "0701112233");
            
        seeker.setTrustTier(TrustTier.NEW);
        seeker.addFunds(200.00);
        
        Provider provider = new Provider(
            "Alex",
            59.3293,
            18.0686);
            
        Listing listing = new Listing(
            provider.getId(),
            "Dog walking",
            "A dog walking listing",
            ListingType.DOG_WALK);
            
        when(seekers.findById(seekerId))
            .thenReturn(Optional.of(seeker));
            
        when(listings.findById(listing.getId()))
            .thenReturn(Optional.of(listing));
            
        when(bookings.findBySeekerId(seekerId))
            .thenReturn(List.of());
            
        when(providers.findById(provider.getId()))
            .thenReturn(Optional.of(provider));
            
        when(listings.findByProviderId(provider.getId()))
            .thenReturn(List.of());
            
        when(pricing.priceFor(
            org.mockito.ArgumentMatchers.any(Booking.class),
            org.mockito.ArgumentMatchers.eq(listing),
            org.mockito.ArgumentMatchers.eq(seeker)
        
        )).thenReturn(89.60);
        
        when(seekers.save(seeker))
            .thenReturn(seeker);
            
        when(listings.save(listing))
            .thenReturn(listing);
            
        when(bookings.save(org.mockito.ArgumentMatchers.any(Booking.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
            
        Booking result = bookingService.createBooking(
            seekerId,
            listing.getId(),
            60
         );
         
        assertThat(result.getStatus())
            .isEqualTo(com.walkmates.model.BookingStatus.CONFIRMED);
            
        assertThat(result.getPrice())
            .isEqualTo(89.60);
        
        assertThat(listing.isAvailable())
            .isFalse();
            
        verify(seekers).save(seeker);
        verify(listings).save(listing);
        verify(bookings).save(result);
        verify(notifications)
            .sendBookingConfirmed(seeker, result);
        }
        
        @Test
        @DisplayName("Rejects booking when seeker has insufficient balance")
        void rejectsWhenSeekerHasInsufficientBalance() {
                String seekerId = "seeker-1";
                
                Seeker seeker = new Seeker(
                        "pat@example.com",
                        "Pat",
                        "0701112233"
                );
                
                seeker.setTrustTier(TrustTier.NEW);
                
                Provider provider = new Provider(
                        "Alex",
                        59.3293,
                        18.0686
                );
                
                Listing listing = new Listing(
                        provider.getId(),
                        "Dog walking",
                        "A dog walking listing",
                        ListingType.DOG_WALK
                );
                
                when(seekers.findById(seekerId))
                .thenReturn(Optional.of(seeker));
                
                when(listings.findById(listing.getId()))
                .thenReturn(Optional.of(listing));
                
                when(bookings.findBySeekerId(seekerId))
                .thenReturn(List.of());
                
                when(providers.findById(provider.getId()))
                .thenReturn(Optional.of(provider));
                
                when(listings.findByProviderId(provider.getId()))
                .thenReturn(List.of());
                
                when(pricing.priceFor(
                        org.mockito.ArgumentMatchers.any(Booking.class),
                        org.mockito.ArgumentMatchers.eq(listing),
                        org.mockito.ArgumentMatchers.eq(seeker)
                
                )).thenReturn(89.60);
                
                assertThatThrownBy(() ->
                 bookingService.createBooking(
                        seekerId,
                        listing.getId(),
                        60
                )
        )
        .isInstanceOf(BookingService.BookingRejectedException.class)
        .hasMessageContaining("Insufficient balance");
        
        assertThat(seeker.getBalance())
            .isEqualTo(0.00);
            
            verify(seekers, org.mockito.Mockito.never()).save(seeker);
            verify(listings, org.mockito.Mockito.never()).save(listing);
            verify(bookings, org.mockito.Mockito.never())
            .save(org.mockito.ArgumentMatchers.any(Booking.class));
            
            verify(notifications, org.mockito.Mockito.never())
            .sendBookingConfirmed(
                org.mockito.ArgumentMatchers.any(Seeker.class),
                org.mockito.ArgumentMatchers.any(Booking.class)
        );
}


@Test
@DisplayName("Rejects booking when provider is at capacity")
void rejectsWhenProviderIsAtCapacity() {
    String seekerId = "seeker-1";

    Seeker seeker = new Seeker(
            "pat@example.com",
            "Pat",
            "0701112233"
    );

    seeker.setTrustTier(TrustTier.NEW);

    Provider provider = new Provider(
            "Alex",
            59.3293,
            18.0686,
            1
    );

    Listing listing = new Listing(
            provider.getId(),
            "Dog walking",
            "A dog walking listing",
            ListingType.DOG_WALK
    );

    Booking existingBooking = new Booking(
            "another-seeker",
            listing.getId(),
            60
    );

    existingBooking.transitionTo(
            com.walkmates.model.BookingStatus.CONFIRMED
    );

    when(seekers.findById(seekerId))
            .thenReturn(Optional.of(seeker));

    when(listings.findById(listing.getId()))
            .thenReturn(Optional.of(listing));

    when(bookings.findBySeekerId(seekerId))
            .thenReturn(List.of());

    when(providers.findById(provider.getId()))
            .thenReturn(Optional.of(provider));

    when(listings.findByProviderId(provider.getId()))
            .thenReturn(List.of(listing));

    when(bookings.findByListingId(listing.getId()))
            .thenReturn(List.of(existingBooking));

    assertThatThrownBy(() ->
            bookingService.createBooking(
                    seekerId,
                    listing.getId(),
                    60
            )
    )
            .isInstanceOf(BookingService.BookingRejectedException.class)
            .hasMessageContaining("capacity");

    verify(seekers, org.mockito.Mockito.never()).save(seeker);

    verify(listings, org.mockito.Mockito.never())
            .save(listing);

    verify(bookings, org.mockito.Mockito.never())
            .save(org.mockito.ArgumentMatchers.any(Booking.class));

    verify(notifications, org.mockito.Mockito.never())
            .sendBookingConfirmed(
                    org.mockito.ArgumentMatchers.any(Seeker.class),
                    org.mockito.ArgumentMatchers.any(Booking.class)
            );
}

@Test
@DisplayName("Rejects booking when listing is unavailable")
void rejectsWhenListingIsUnavailable() {
    String seekerId = "seeker-1";

    Seeker seeker = new Seeker(
            "pat@example.com",
            "Pat",
            "0701112233"
    );

    seeker.setTrustTier(TrustTier.NEW);

    Provider provider = new Provider(
            "Alex",
            59.3293,
            18.0686
    );

    Listing listing = new Listing(
            provider.getId(),
            "Dog walking",
            "A dog walking listing",
            ListingType.DOG_WALK
    );

    listing.transitionTo(
            com.walkmates.model.ListingStatus.BOOKED
    );

    when(seekers.findById(seekerId))
            .thenReturn(Optional.of(seeker));

    when(listings.findById(listing.getId()))
            .thenReturn(Optional.of(listing));

    assertThatThrownBy(() ->
            bookingService.createBooking(
                    seekerId,
                    listing.getId(),
                    60
            )
    )
            .isInstanceOf(BookingService.BookingRejectedException.class)
            .hasMessageContaining("not available");

    // The booking must not reach any later processing.
    verify(seekers, org.mockito.Mockito.never())
            .save(seeker);

    verify(listings, org.mockito.Mockito.never())
            .save(listing);

    verify(bookings, org.mockito.Mockito.never())
            .save(org.mockito.ArgumentMatchers.any(Booking.class));

    verify(notifications, org.mockito.Mockito.never())
            .sendBookingConfirmed(
                    org.mockito.ArgumentMatchers.any(Seeker.class),
                    org.mockito.ArgumentMatchers.any(Booking.class)
            );
}

@Test
@DisplayName("Rejects booking when seeker does not exist")
void rejectsWhenSeekerDoesNotExist() {
    String seekerId = "unknown-seeker";
    String listingId = "listing-1";

    when(seekers.findById(seekerId))
            .thenReturn(Optional.empty());

    assertThatThrownBy(() ->
            bookingService.createBooking(
                    seekerId,
                    listingId,
                    60
            )
    )
            .isInstanceOf(BookingService.BookingRejectedException.class)
            .hasMessageContaining("Unknown seeker");

    // Because the seeker was not found,
    // nothing else should be touched.
    verify(listings, org.mockito.Mockito.never())
            .findById(org.mockito.ArgumentMatchers.anyString());

    verify(providers, org.mockito.Mockito.never())
            .findById(org.mockito.ArgumentMatchers.anyString());

    verify(pricing, org.mockito.Mockito.never())
            .priceFor(
                    org.mockito.ArgumentMatchers.any(Booking.class),
                    org.mockito.ArgumentMatchers.any(Listing.class),
                    org.mockito.ArgumentMatchers.any(Seeker.class)
            );

    verify(bookings, org.mockito.Mockito.never())
            .save(org.mockito.ArgumentMatchers.any(Booking.class));

    verify(notifications, org.mockito.Mockito.never())
            .sendBookingConfirmed(
                    org.mockito.ArgumentMatchers.any(Seeker.class),
                    org.mockito.ArgumentMatchers.any(Booking.class)
            );
}

@Test
@DisplayName("Rejects booking when listing does not exist")
void rejectsWhenListingDoesNotExist() {
    String seekerId = "seeker-1";
    String listingId = "unknown-listing";

    Seeker seeker = new Seeker(
            "pat@example.com",
            "Pat",
            "0701112233"
    );

    seeker.setTrustTier(TrustTier.NEW);

    when(seekers.findById(seekerId))
            .thenReturn(Optional.of(seeker));

    when(listings.findById(listingId))
            .thenReturn(Optional.empty());

    assertThatThrownBy(() ->
            bookingService.createBooking(
                    seekerId,
                    listingId,
                    60
            )
    )
            .isInstanceOf(BookingService.BookingRejectedException.class)
            .hasMessageContaining("Unknown listing");

    verify(providers, org.mockito.Mockito.never())
            .findById(org.mockito.ArgumentMatchers.anyString());

    verify(pricing, org.mockito.Mockito.never())
            .priceFor(
                    org.mockito.ArgumentMatchers.any(Booking.class),
                    org.mockito.ArgumentMatchers.any(Listing.class),
                    org.mockito.ArgumentMatchers.any(Seeker.class)
            );

    verify(seekers, org.mockito.Mockito.never())
            .save(seeker);

    verify(bookings, org.mockito.Mockito.never())
            .save(org.mockito.ArgumentMatchers.any(Booking.class));

    verify(notifications, org.mockito.Mockito.never())
            .sendBookingConfirmed(
                    org.mockito.ArgumentMatchers.any(Seeker.class),
                    org.mockito.ArgumentMatchers.any(Booking.class)
            );
}

@Test
@DisplayName("Rejects booking when provider does not exist")
void rejectsWhenProviderDoesNotExist() {
    String seekerId = "seeker-1";

    Seeker seeker = new Seeker(
            "pat@example.com",
            "Pat",
            "0701112233"
    );

    seeker.setTrustTier(TrustTier.NEW);

    Listing listing = new Listing(
            "missing-provider",
            "Dog walking",
            "A dog walking listing",
            ListingType.DOG_WALK
    );

    when(seekers.findById(seekerId))
            .thenReturn(Optional.of(seeker));

    when(listings.findById(listing.getId()))
            .thenReturn(Optional.of(listing));

    when(bookings.findBySeekerId(seekerId))
            .thenReturn(List.of());

    when(providers.findById("missing-provider"))
            .thenReturn(Optional.empty());

    assertThatThrownBy(() ->
            bookingService.createBooking(
                    seekerId,
                    listing.getId(),
                    60
            )
    )
            .isInstanceOf(BookingService.BookingRejectedException.class)
            .hasMessageContaining("Unknown provider");

    // The process must stop before pricing or saving.
    verify(pricing, org.mockito.Mockito.never())
            .priceFor(
                    org.mockito.ArgumentMatchers.any(Booking.class),
                    org.mockito.ArgumentMatchers.any(Listing.class),
                    org.mockito.ArgumentMatchers.any(Seeker.class)
            );

    verify(seekers, org.mockito.Mockito.never())
            .save(seeker);

    verify(listings, org.mockito.Mockito.never())
            .save(listing);

    verify(bookings, org.mockito.Mockito.never())
            .save(org.mockito.ArgumentMatchers.any(Booking.class));

    verify(notifications, org.mockito.Mockito.never())
            .sendBookingConfirmed(
                    org.mockito.ArgumentMatchers.any(Seeker.class),
                    org.mockito.ArgumentMatchers.any(Booking.class)
            );
}

@Test
@DisplayName("Counts active bookings toward provider capacity")
void countsActiveBookingsForProviderCapacity() {
    String seekerId = "seeker-1";

    Seeker seeker = new Seeker(
            "pat@example.com",
            "Pat",
            "0701112233"
    );

    seeker.setTrustTier(TrustTier.NEW);
    seeker.addFunds(200.00);

    Provider provider = new Provider(
            "Alex",
            59.3293,
            18.0686,
            2
    );

    Listing listing = new Listing(
            provider.getId(),
            "Dog walking",
            "A dog walking listing",
            ListingType.DOG_WALK
    );

    // One existing active booking.
    Booking activeBooking = new Booking(
            "another-seeker",
            listing.getId(),
            60
    );

    activeBooking.transitionTo(
            com.walkmates.model.BookingStatus.CONFIRMED
    );

    when(seekers.findById(seekerId))
            .thenReturn(Optional.of(seeker));

    when(listings.findById(listing.getId()))
            .thenReturn(Optional.of(listing));

    when(bookings.findBySeekerId(seekerId))
            .thenReturn(List.of());

    when(providers.findById(provider.getId()))
            .thenReturn(Optional.of(provider));

    when(listings.findByProviderId(provider.getId()))
            .thenReturn(List.of(listing));

    when(bookings.findByListingId(listing.getId()))
            .thenReturn(List.of(activeBooking));

    when(pricing.priceFor(
            org.mockito.ArgumentMatchers.any(Booking.class),
            org.mockito.ArgumentMatchers.eq(listing),
            org.mockito.ArgumentMatchers.eq(seeker)
    )).thenReturn(89.60);

    when(seekers.save(seeker))
            .thenReturn(seeker);

    when(listings.save(listing))
            .thenReturn(listing);

    when(bookings.save(org.mockito.ArgumentMatchers.any(Booking.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

    Booking result = bookingService.createBooking(
            seekerId,
            listing.getId(),
            60
    );

    assertThat(result.getStatus())
            .isEqualTo(com.walkmates.model.BookingStatus.CONFIRMED);

    assertThat(result.getPrice())
            .isEqualTo(89.60);

    verify(notifications)
            .sendBookingConfirmed(seeker, result);
}
@Test
@DisplayName("Counts only active bookings toward seeker booking limit")
void countsOnlyActiveBookingsForSeekerLimit() {
    String seekerId = "seeker-1";

    Seeker seeker = new Seeker(
            "pat@example.com",
            "Pat",
            "0701112233"
    );

    // VERIFIED allows 3 concurrent active bookings.
    seeker.setTrustTier(TrustTier.VERIFIED);
    seeker.addFunds(200.00);

    Provider provider = new Provider(
            "Alex",
            59.3293,
            18.0686
    );

    Listing listing = new Listing(
            provider.getId(),
            "Dog walking",
            "A dog walking listing",
            ListingType.DOG_WALK
    );

    // Active booking.
    Booking activeBooking = new Booking(
            seekerId,
            "another-listing",
            60
    );

    activeBooking.transitionTo(
            com.walkmates.model.BookingStatus.CONFIRMED
    );

    // Inactive booking.
    Booking completedBooking = new Booking(
            seekerId,
            "old-listing",
            60
    );

    completedBooking.transitionTo(
            com.walkmates.model.BookingStatus.CONFIRMED
    );

    completedBooking.transitionTo(
            com.walkmates.model.BookingStatus.IN_PROGRESS
    );

    completedBooking.transitionTo(
            com.walkmates.model.BookingStatus.COMPLETED
    );

    when(seekers.findById(seekerId))
            .thenReturn(Optional.of(seeker));

    when(listings.findById(listing.getId()))
            .thenReturn(Optional.of(listing));

    when(bookings.findBySeekerId(seekerId))
            .thenReturn(List.of(activeBooking, completedBooking));

    when(providers.findById(provider.getId()))
            .thenReturn(Optional.of(provider));

    when(listings.findByProviderId(provider.getId()))
            .thenReturn(List.of());

    when(pricing.priceFor(
            org.mockito.ArgumentMatchers.any(Booking.class),
            org.mockito.ArgumentMatchers.eq(listing),
            org.mockito.ArgumentMatchers.eq(seeker)
    )).thenReturn(89.60);

    when(seekers.save(seeker))
            .thenReturn(seeker);

    when(listings.save(listing))
            .thenReturn(listing);

    when(bookings.save(org.mockito.ArgumentMatchers.any(Booking.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

    Booking result = bookingService.createBooking(
            seekerId,
            listing.getId(),
            60
    );

    assertThat(result.getStatus())
            .isEqualTo(com.walkmates.model.BookingStatus.CONFIRMED);

    assertThat(result.getPrice())
            .isEqualTo(89.60);

    verify(notifications)
            .sendBookingConfirmed(seeker, result);
}

@Test
@DisplayName("Allows booking when balance exactly equals price")
void allowsBookingWhenBalanceExactlyEqualsPrice() {
    String seekerId = "seeker-1";

    Seeker seeker = new Seeker(
            "pat@example.com",
            "Pat",
            "0701112233"
    );

    seeker.setTrustTier(TrustTier.NEW);
    seeker.addFunds(89.60);

    Provider provider = new Provider(
            "Alex",
            59.3293,
            18.0686
    );

    Listing listing = new Listing(
            provider.getId(),
            "Dog walking",
            "A dog walking listing",
            ListingType.DOG_WALK
    );

    when(seekers.findById(seekerId))
            .thenReturn(Optional.of(seeker));

    when(listings.findById(listing.getId()))
            .thenReturn(Optional.of(listing));

    when(bookings.findBySeekerId(seekerId))
            .thenReturn(List.of());

    when(providers.findById(provider.getId()))
            .thenReturn(Optional.of(provider));

    when(listings.findByProviderId(provider.getId()))
            .thenReturn(List.of());

    when(pricing.priceFor(
            org.mockito.ArgumentMatchers.any(Booking.class),
            org.mockito.ArgumentMatchers.eq(listing),
            org.mockito.ArgumentMatchers.eq(seeker)
    )).thenReturn(89.60);

    when(seekers.save(seeker))
            .thenReturn(seeker);

    when(listings.save(listing))
            .thenReturn(listing);

    when(bookings.save(org.mockito.ArgumentMatchers.any(Booking.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

    Booking result = bookingService.createBooking(
            seekerId,
            listing.getId(),
            60
    );

    assertThat(result.getStatus())
            .isEqualTo(com.walkmates.model.BookingStatus.CONFIRMED);

    assertThat(result.getPrice())
            .isEqualTo(89.60);
}

@Test
@DisplayName("Charges seeker wallet when booking succeeds")
void chargesSeekerWalletWhenBookingSucceeds() {
    String seekerId = "seeker-1";

    Seeker seeker = new Seeker(
            "pat@example.com",
            "Pat",
            "0701112233"
    );

    seeker.setTrustTier(TrustTier.NEW);
    seeker.addFunds(200.00);

    Provider provider = new Provider(
            "Alex",
            59.3293,
            18.0686
    );

    Listing listing = new Listing(
            provider.getId(),
            "Dog walking",
            "A dog walking listing",
            ListingType.DOG_WALK
    );

    when(seekers.findById(seekerId))
            .thenReturn(Optional.of(seeker));

    when(listings.findById(listing.getId()))
            .thenReturn(Optional.of(listing));

    when(bookings.findBySeekerId(seekerId))
            .thenReturn(List.of());

    when(providers.findById(provider.getId()))
            .thenReturn(Optional.of(provider));

    when(listings.findByProviderId(provider.getId()))
            .thenReturn(List.of());

    when(pricing.priceFor(
            org.mockito.ArgumentMatchers.any(Booking.class),
            org.mockito.ArgumentMatchers.eq(listing),
            org.mockito.ArgumentMatchers.eq(seeker)
    )).thenReturn(89.60);

    when(seekers.save(seeker))
            .thenReturn(seeker);

    when(listings.save(listing))
            .thenReturn(listing);

    when(bookings.save(org.mockito.ArgumentMatchers.any(Booking.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

    double balanceBefore = seeker.getBalance();

    Booking result = bookingService.createBooking(
            seekerId,
            listing.getId(),
            60
    );

    double balanceAfter = seeker.getBalance();

    assertThat(result.getStatus())
            .isEqualTo(com.walkmates.model.BookingStatus.CONFIRMED);

    assertThat(result.getPrice())
            .isEqualTo(89.60);

    assertThat(balanceBefore)
            .isEqualTo(200.00);

    assertThat(balanceAfter)
            .isEqualTo(110.40);

    assertThat(balanceBefore - balanceAfter)
            .isEqualTo(89.60);

    verify(notifications)
            .sendBookingConfirmed(seeker, result);
}
}