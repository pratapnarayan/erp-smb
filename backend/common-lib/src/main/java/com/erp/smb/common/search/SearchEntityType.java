package com.erp.smb.common.search;

/**
 * Enum representing searchable entity types in the system.
 * 
 * V1 SCOPE (ACTIVE): PRODUCT, CUSTOMER, ORDER
 * FUTURE SCOPE (DISABLED): ENQUIRY, EMPLOYEE, INVOICE
 * 
 * Only v1 entities should be indexed, searched, or returned in APIs.
 */
public enum SearchEntityType {
    // V1 - ACTIVE
    PRODUCT,
    CUSTOMER,
    ORDER,
    
    // FUTURE - NOT IMPLEMENTED IN V1
    ENQUIRY,
    EMPLOYEE,
    INVOICE;
    
    public String getTableName() {
        return "search_" + this.name().toLowerCase() + "s";
    }
    
    /**
     * Check if this entity type is supported in v1
     */
    public boolean isV1Supported() {
        return this == PRODUCT || this == CUSTOMER || this == ORDER;
    }
    
    /**
     * Get all v1 supported entity types
     */
    public static SearchEntityType[] getV1Supported() {
        return new SearchEntityType[]{PRODUCT, CUSTOMER, ORDER};
    }
}
