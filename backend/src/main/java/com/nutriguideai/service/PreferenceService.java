
package com.nutriguideai.service;

import com.nutriguideai.dto.request.PreferenceRequest;
import com.nutriguideai.dto.response.PreferenceResponse;

/**
 * Service contract for food preferences.
 */
public interface PreferenceService {

    PreferenceResponse getPreferences();

    PreferenceResponse upsertPreferences(PreferenceRequest request);
}