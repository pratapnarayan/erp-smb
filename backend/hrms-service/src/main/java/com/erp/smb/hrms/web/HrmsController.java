package com.erp.smb.hrms.web;

import com.erp.smb.common.dto.PageResponse;
import com.erp.smb.hrms.domain.Employee;
import com.erp.smb.hrms.repo.EmployeeRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/hrms")
public class HrmsController {
  private final EmployeeRepository repo; public HrmsController(EmployeeRepository repo){this.repo=repo;}
  @GetMapping
  public ResponseEntity<PageResponse<Employee>> list(@RequestParam(name = "page", defaultValue = "0") int page, @RequestParam(name = "size", defaultValue = "20") int size){ var p=repo.findAll(PageRequest.of(page,size)); return ResponseEntity.ok(new PageResponse<>(p.getContent(), p.getNumber(), p.getSize(), p.getTotalElements(), p.getTotalPages())); }
  @PostMapping
  public Employee create(@RequestBody Employee e){ return repo.save(e);} 
}
