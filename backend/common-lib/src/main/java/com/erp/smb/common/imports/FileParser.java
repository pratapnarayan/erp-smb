package com.erp.smb.common.imports;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * Interface for parsing different file formats
 */
public interface FileParser {
  
  /**
   * Parse a file and return rows of data
   * @param file The uploaded file
   * @return List of string arrays, each representing a row
   * @throws IOException if file cannot be parsed
   */
  List<String[]> parse(MultipartFile file) throws IOException;
  
  /**
   * Check if this parser supports the given file
   * @param filename The name of the file
   * @return true if this parser can handle the file
   */
  boolean supports(String filename);
}
