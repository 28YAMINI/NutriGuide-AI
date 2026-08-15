package com.nutriguideai.service;

import com.nutriguideai.dto.request.PreferenceRequest;
import com.nutriguideai.dto.response.PreferenceResponse;
import com.nutriguideai.entity.FoodPreference;
import com.nutriguideai.entity.User;
import com.nutriguideai.enums.BudgetLevel;
import com.nutriguideai.enums.DietType;
import com.nutriguideai.exception.ResourceNotFoundException;
import com.nutriguideai.exception.UnauthorizedException;
import com.nutriguideai.repository.FoodPreferenceRepository;
import com.nutriguideai.repository.UserRepository;
import com.nutriguideai.service.impl.PreferenceServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PreferenceServiceImplTest {

    private static final String EMAIL = "alice@nutriguide.com";

    @Mock
    private FoodPreferenceRepository foodPreferenceRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PreferenceServiceImpl preferenceService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void upsert_createsNewRecord_whenNoneExists() {
        authenticateAs(EMAIL);
        User user = User.builder().id(1L).email(EMAIL).build();
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(foodPreferenceRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(foodPreferenceRepository.save(any(FoodPreference.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PreferenceRequest request = PreferenceRequest.builder()
                .dietType(DietType.VEGETARIAN)
                .budgetLevel(BudgetLevel.MEDIUM)
                .region("North India")
                .build();

        preferenceService.upsertPreferences(request);

        ArgumentCaptor<FoodPreference> captor = ArgumentCaptor.forClass(FoodPreference.class);
        verify(foodPreferenceRepository).save(captor.capture());

        FoodPreference saved = captor.getValue();
        assertEquals(DietType.VEGETARIAN, saved.getDietType());
        assertEquals(BudgetLevel.MEDIUM, saved.getBudgetLevel());
        assertEquals("North India", saved.getRegion());
        assertEquals(1L, saved.getUser().getId());
    }

    @Test
    void upsert_partialUpdate_keepsExistingValuesForNullFields() {
        authenticateAs(EMAIL);
        User user = User.builder().id(1L).email(EMAIL).build();
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

        FoodPreference existing = FoodPreference.builder()
                .user(user)
                .dietType(DietType.VEGAN)
                .budgetLevel(BudgetLevel.LOW)
                .region("South India")
                .build();
        when(foodPreferenceRepository.findByUserId(1L)).thenReturn(Optional.of(existing));
        when(foodPreferenceRepository.save(any(FoodPreference.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Only dietType is "sent" — everything else stays null on the request
        PreferenceRequest request = PreferenceRequest.builder()
                .dietType(DietType.FLEXITARIAN)
                .build();

        preferenceService.upsertPreferences(request);

        verify(foodPreferenceRepository).save(existing);
        assertEquals(DietType.FLEXITARIAN, existing.getDietType()); // updated
        assertEquals(BudgetLevel.LOW, existing.getBudgetLevel());    // untouched
        assertEquals("South India", existing.getRegion());           // untouched
    }

    @Test
    void upsert_savesAllergies_whenProvided() {
        authenticateAs(EMAIL);
        User user = User.builder().id(1L).email(EMAIL).build();
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(foodPreferenceRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(foodPreferenceRepository.save(any(FoodPreference.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PreferenceRequest request = PreferenceRequest.builder()
                .allergies(List.of("PEANUTS", "MILK"))
                .build();

        preferenceService.upsertPreferences(request);

        ArgumentCaptor<FoodPreference> captor = ArgumentCaptor.forClass(FoodPreference.class);
        verify(foodPreferenceRepository).save(captor.capture());
        assertEquals(List.of("PEANUTS", "MILK"), captor.getValue().getAllergies());
    }

    @Test
    void upsert_throwsUnauthorized_whenNoAuthentication() {
        // No SecurityContext set on purpose
        assertThrows(UnauthorizedException.class,
                () -> preferenceService.upsertPreferences(PreferenceRequest.builder().build()));
    }

    @Test
    void upsert_throwsResourceNotFound_whenUserDoesNotExist() {
        authenticateAs("ghost@nutriguide.com");
        when(userRepository.findByEmail("ghost@nutriguide.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> preferenceService.upsertPreferences(PreferenceRequest.builder().build()));
    }

    @Test
    void get_returnsResponse_whenRecordExists() {
        authenticateAs(EMAIL);
        User user = User.builder().id(1L).email(EMAIL).build();
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

        FoodPreference existing = FoodPreference.builder()
                .user(user)                       // ← needed by PreferenceResponse.fromEntity
                .dietType(DietType.VEGETARIAN)
                .budgetLevel(BudgetLevel.HIGH)
                .build();
        when(foodPreferenceRepository.findByUserId(1L)).thenReturn(Optional.of(existing));

        PreferenceResponse response = preferenceService.getPreferences();

        assertNotNull(response);
        assertEquals(DietType.VEGETARIAN, response.getDietType());
        assertEquals(BudgetLevel.HIGH, response.getBudgetLevel());
    }

    @Test
    void get_throwsResourceNotFound_whenNoRecord() {
        authenticateAs(EMAIL);
        User user = User.builder().id(1L).email(EMAIL).build();
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(foodPreferenceRepository.findByUserId(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> preferenceService.getPreferences());
    }

    private void authenticateAs(String email) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(email, null));
    }
}