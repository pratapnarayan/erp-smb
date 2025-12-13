-- Align ID column with JPA @Id Long type
ALTER TABLE enquiry.enquiries
    ALTER COLUMN id TYPE BIGINT;
