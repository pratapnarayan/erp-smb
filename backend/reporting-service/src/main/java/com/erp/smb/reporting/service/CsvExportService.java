package com.erp.smb.reporting.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Service
public class CsvExportService {
    private final JdbcTemplate jdbcTemplate;
    public CsvExportService(JdbcTemplate jdbcTemplate) { this.jdbcTemplate = jdbcTemplate; }

    public long exportQueryToCsv(String sql, Object[] params, Path file, long maxRows) throws IOException {
        Files.createDirectories(file.getParent());
        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            SqlRowSet rs = jdbcTemplate.queryForRowSet(sql, params);
            String[] columns = rs.getMetaData().getColumnNames();
            // header
            writer.write(String.join(",", escape(columns)));
            writer.newLine();
            long count = 0;
            while (rs.next()) {
                if (count >= maxRows) break;
                List<String> row = new ArrayList<>(columns.length);
                for (String c : columns) {
                    Object val = rs.getObject(c);
                    row.add(escape(val == null ? "" : String.valueOf(val)));
                }
                writer.write(String.join(",", row));
                writer.newLine();
                count++;
            }
            writer.flush();
            return count;
        }
    }

    private String[] escape(String[] in) {
        String[] out = new String[in.length];
        for (int i=0;i<in.length;i++) out[i] = escape(in[i]);
        return out;
    }

    private String escape(String v) {
        if (v.contains(",") || v.contains("\"") || v.contains("\n") || v.contains("\r")) {
            return '"' + v.replace("\"","\"\"") + '"';
        }
        return v;
    }
}
