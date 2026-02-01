package com.erp.smb.search.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Security tests for search endpoints.
 * Validates authentication and authorization rules.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
public class SearchControllerSecurityTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void testSearchRequiresAuthentication() throws Exception {
        // When: Calling search without JWT
        // Then: Should be rejected (401 or 403 depending on Spring Security entry-point configuration)
        mockMvc.perform(get("/api/search")
                .param("q", "test")
                .header("X-Tenant-Id", "demo"))
                .andExpect(status().is4xxClientError());
    }
    
    @Test
    void testIndexingEndpointRequiresADMIN() throws Exception {
        // When: Calling indexing endpoint without auth
        // Then: Should be rejected
        String testBody = """
            {
                "entityId": "1",
                "tenantId": "demo",
                "entityType": "PRODUCT",
                "searchableFields": {"name": "Test"}
            }
            """;
        
        mockMvc.perform(post("/api/search/index/products")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Tenant-Id", "demo")
                .content(testBody))
                .andExpect(status().is4xxClientError()); // No auth at all
    }
    
    @Test
    void testReindexEndpointRequiresAuth() throws Exception {
        // When: Calling reindex endpoint without auth
        // Then: Should be rejected
        mockMvc.perform(post("/api/search/reindex/PRODUCT")
                .header("X-Tenant-Id", "demo"))
                .andExpect(status().is4xxClientError());
    }
}
