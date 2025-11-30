package com.erp.smb.enquiry.web;

import com.erp.smb.common.dto.PageResponse;
import com.erp.smb.enquiry.domain.Enquiry;
import com.erp.smb.enquiry.repo.EnquiryRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/enquiry")
public class EnquiryController {
  private final EnquiryRepository repo;
  public EnquiryController(EnquiryRepository repo){this.repo=repo;}

  @GetMapping
  public ResponseEntity<PageResponse<Enquiry>> list(@RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "20") int size){
    var p = repo.findAll(PageRequest.of(page, size));
    return ResponseEntity.ok(new PageResponse<>(p.getContent(), p.getNumber(), p.getSize(), p.getTotalElements(), p.getTotalPages()));
  }

  @PostMapping
  public Enquiry create(@RequestBody Enquiry e){ return repo.save(e);} 
}
