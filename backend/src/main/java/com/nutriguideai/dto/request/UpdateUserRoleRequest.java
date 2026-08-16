package com.nutriguideai.dto.request;

import com.nutriguideai.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "New role for a user account (ADMIN only).")
public class UpdateUserRoleRequest {

    @Schema(
            description = "Target role",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "Role is required")
    private Role role;
}