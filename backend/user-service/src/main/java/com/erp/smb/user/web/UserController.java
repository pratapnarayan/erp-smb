package com.erp.smb.user.web;

import com.erp.smb.common.dto.PageResponse;
import com.erp.smb.user.domain.UserProfile;
import com.erp.smb.user.repo.UserProfileRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Set<String> ALLOWED_ROLES = Set.of(
            "ADMIN", "OWNER", "MANAGER", "USER", "VIEWER", "HR", "FINANCE", "OPERATIONS"
    );

    /** Only an ADMIN caller may provision another ADMIN or OWNER — HR is not privileged enough. */
    private static final Set<String> PRIVILEGED_ROLES = Set.of("ADMIN", "OWNER");

    private final UserProfileRepository repo;

    public UserController(UserProfileRepository repo) {
        this.repo = repo;
    }

    // ── GET /api/users  (any authenticated user) ─────────────────────────────

    @GetMapping
    public ResponseEntity<PageResponse<UserProfile>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var p = repo.findAll(PageRequest.of(page, size));
        return ResponseEntity.ok(new PageResponse<>(
                p.getContent(), p.getNumber(), p.getSize(),
                p.getTotalElements(), p.getTotalPages()));
    }

    // ── POST /api/users  (ADMIN or HR only) ──────────────────────────────────

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<?> create(
            @Valid @RequestBody UserProfile profile,
            @AuthenticationPrincipal UserDetails caller) {

        // Normalise role to upper-case for consistent storage
        if (profile.getRole() != null) {
            profile.setRole(profile.getRole().toUpperCase());
        }

        // Guard: role must be one of the recognised values
        if (!ALLOWED_ROLES.contains(profile.getRole())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "invalid_role",
                                 "allowed", ALLOWED_ROLES));
        }

        // Guard: only ADMIN may provision another ADMIN/OWNER — otherwise an HR
        // caller (also allowed to hit this endpoint) could escalate privileges
        // by creating themselves or a colleague a full ADMIN/OWNER account.
        boolean callerIsAdmin = caller != null && caller.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        if (!callerIsAdmin && PRIVILEGED_ROLES.contains(profile.getRole())) {
            return ResponseEntity.status(403)
                    .body(Map.of("error", "forbidden",
                                 "message", "Only ADMIN can assign ADMIN or OWNER roles"));
        }

        // Guard: username must be unique across the organisation
        if (repo.existsByUsername(profile.getUsername())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "username_already_exists",
                                 "username", profile.getUsername()));
        }

        // Clear any client-supplied id to force DB-assigned identity
        profile.setId(null);

        return ResponseEntity.ok(repo.save(profile));
    }

    // ── DELETE /api/users/{username}  (ADMIN only) ───────────────────────────
    // Removes the user profile from the directory. The caller must be ADMIN;
    // self-deletion is blocked as a safety guard.

    @DeleteMapping("/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<?> delete(
            @PathVariable String username,
            @AuthenticationPrincipal UserDetails caller) {

        // Self-deletion guard
        if (caller != null && caller.getUsername().equalsIgnoreCase(username)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "self_delete_forbidden",
                                 "message", "You cannot delete your own profile"));
        }

        if (!repo.existsByUsername(username)) {
            return ResponseEntity.status(404)
                    .body(Map.of("error", "user_not_found", "username", username));
        }

        repo.deleteByUsername(username);
        return ResponseEntity.noContent().build();
    }
}
