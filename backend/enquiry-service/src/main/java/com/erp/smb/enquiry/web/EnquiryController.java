package com.erp.smb.enquiry.web;

import com.erp.smb.common.dto.PageResponse;
import com.erp.smb.enquiry.domain.Enquiry;
import com.erp.smb.enquiry.repo.EnquiryRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

 import java.util.Map;

@RestController
@RequestMapping("/api/enquiry")
public class EnquiryController {
  private final EnquiryRepository repo;
  public EnquiryController(EnquiryRepository repo){this.repo=repo;}

  @GetMapping
  public ResponseEntity<PageResponse<Enquiry>> list(@RequestParam(name = "page", defaultValue = "0") int page,
                                                    @RequestParam(name = "size", defaultValue = "20") int size){
    var p = repo.findAll(PageRequest.of(page, size));
    return ResponseEntity.ok(new PageResponse<>(p.getContent(), p.getNumber(), p.getSize(), p.getTotalElements(), p.getTotalPages()));
  }

  @PostMapping
  public Enquiry create(@RequestBody Enquiry e){ return repo.save(e);} 

  @PutMapping("/{id}/status")
  public ResponseEntity<?> updateStatus(@PathVariable(name = "id") long id, @RequestBody Map<String, String> body) {
    var e = repo.findById(id).orElse(null);
    if (e == null) {
      return ResponseEntity.notFound().build();
    }
    String status = body == null ? null : body.get("status");
    if (status == null || status.isBlank()) {
      return ResponseEntity.badRequest().body(Map.of("error", "status_required"));
    }
    e.setStatus(status);
    return ResponseEntity.ok(repo.save(e));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<?> delete(@PathVariable(name = "id") long id) {
    var e = repo.findById(id).orElse(null);
    if (e == null) {
      return ResponseEntity.notFound().build();
    }
    if (!isClosed(e.getStatus())) {
      return ResponseEntity.status(409).body(Map.of("error", "only_closed_enquiries_can_be_deleted"));
    }
    repo.delete(e);
    return ResponseEntity.noContent().build();
  }

  private boolean isClosed(String status) {
    if (status == null) return false;
    String s = status.trim().toUpperCase();
    return s.equals("CLOSED") || s.equals("RESOLVED");
  }
}
