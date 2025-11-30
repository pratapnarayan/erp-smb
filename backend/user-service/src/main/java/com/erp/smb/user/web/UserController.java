package com.erp.smb.user.web;

import com.erp.smb.common.dto.PageResponse;
import com.erp.smb.user.domain.UserProfile;
import com.erp.smb.user.repo.UserProfileRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserProfileRepository repo;
    public UserController(UserProfileRepository repo){this.repo = repo;}

    @GetMapping
    public ResponseEntity<PageResponse<UserProfile>> list(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size){
        var p = repo.findAll(PageRequest.of(page, size));
        return ResponseEntity.ok(new PageResponse<>(p.getContent(), p.getNumber(), p.getSize(), p.getTotalElements(), p.getTotalPages()));
    }
}
