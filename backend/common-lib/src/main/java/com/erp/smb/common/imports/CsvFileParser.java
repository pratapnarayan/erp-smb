package com.erp.smb.common.imports;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 * CSV file parser implementation
 */
@Component
public class CsvFileParser implements FileParser {
  
  @Override
  public List<String[]> parse(MultipartFile file) throws IOException {
    try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
      return reader.readAll();
    } catch (CsvException e) {
      throw new IOException("Error parsing CSV file", e);
    }
  }
  
  @Override
  public boolean supports(String filename) {
    return filename != null && filename.toLowerCase().endsWith(".csv");
  }
}
