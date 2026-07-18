import React, { useState } from 'react';
import FrostedCard from './FrostedCard.jsx';
import Badge from './Badge.jsx';
import { API_BASE_URL } from '../api/config/baseUrl.js';

export default function DataImport() {
  const [activeTab, setActiveTab] = useState('customers');
  const [uploading, setUploading] = useState(false);
  const [result, setResult] = useState(null);

  const tabs = [
    { key: 'customers', label: 'Customers', icon: '👥' },
    { key: 'products', label: 'Products', icon: '📦' },
    { key: 'opening-stock', label: 'Opening Stock', icon: '📊' },
  ];

  const handleFileUpload = async (e, type) => {
    const file = e.target.files[0];
    if (!file) return;

    // Validate file size (5MB)
    if (file.size > 5 * 1024 * 1024) {
      setResult({
        type,
        success: false,
        message: 'File size exceeds 5MB limit',
        errors: []
      });
      return;
    }

    // Validate file type
    const validTypes = ['.csv', '.xlsx', '.xls'];
    const fileExt = file.name.substring(file.name.lastIndexOf('.')).toLowerCase();
    if (!validTypes.includes(fileExt)) {
      setResult({
        type,
        success: false,
        message: 'Invalid file format. Please upload CSV or Excel file',
        errors: []
      });
      return;
    }

    setUploading(true);
    setResult(null);

    const formData = new FormData();
    formData.append('file', file);

    try {
      let endpoint = '';
      if (type === 'customers') {
        endpoint = '/customers/import';
      } else if (type === 'products') {
        endpoint = '/products/import';
      } else if (type === 'opening-stock') {
        endpoint = '/products/import/opening-stock';
      }

      // Route through the gateway like every other request; auth is the
      // HttpOnly accessToken cookie (sent automatically via credentials:'include'),
      // not a Bearer token — tokens are no longer stored in localStorage.
      const response = await fetch(`${API_BASE_URL}${endpoint}`, {
        method: 'POST',
        credentials: 'include',
        body: formData
      });

      const data = await response.json();

      setResult({
        type,
        success: response.ok && data.failedCount === 0,
        data: data,
        errors: data.errors || []
      });
    } catch (error) {
      setResult({
        type,
        success: false,
        message: 'Error uploading file: ' + error.message,
        errors: []
      });
    } finally {
      setUploading(false);
      // Reset file input
      e.target.value = '';
    }
  };

  const downloadTemplate = async (type) => {
    try {
      let endpoint = '';
      let filename = '';

      if (type === 'customers') {
        endpoint = '/customers/import/template';
        filename = 'customers_import_template.csv';
      } else if (type === 'products') {
        endpoint = '/products/import/template';
        filename = 'products_import_template.csv';
      } else if (type === 'opening-stock') {
        endpoint = '/products/import/opening-stock/template';
        filename = 'opening_stock_import_template.csv';
      }

      const response = await fetch(`${API_BASE_URL}${endpoint}`, {
        credentials: 'include',
      });

      const blob = await response.blob();
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = filename;
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);
    } catch (error) {
      console.error('Error downloading template:', error);
      alert('Error downloading template');
    }
  };

  return (
    <div>
      <div style={{ marginBottom: '1.5rem', display: 'flex', gap: '0.5rem', borderBottom: '1px solid var(--border)' }}>
        {tabs.map(tab => (
          <button
            key={tab.key}
            onClick={() => { setActiveTab(tab.key); setResult(null); }}
            style={{
              padding: '0.75rem 1.5rem',
              background: activeTab === tab.key ? 'var(--card-bg)' : 'transparent',
              border: 'none',
              borderBottom: activeTab === tab.key ? '2px solid var(--primary)' : '2px solid transparent',
              cursor: 'pointer',
              fontSize: '0.95rem',
              fontWeight: activeTab === tab.key ? '600' : '400',
              color: activeTab === tab.key ? 'var(--text-primary)' : 'var(--text-secondary)',
              transition: 'all 0.2s ease'
            }}
          >
            {tab.icon} {tab.label}
          </button>
        ))}
      </div>

      {activeTab === 'customers' && (
        <ImportCard
          title="Import Customers"
          subtitle="Upload CSV or Excel file to import customer data"
          type="customers"
          fields={[
            { name: 'customer_name', required: true },
            { name: 'phone', required: false },
            { name: 'email', required: false },
            { name: 'address', required: false },
            { name: 'gst_number', required: false },
            { name: 'opening_balance', required: false },
            { name: 'balance_type', required: false, note: 'Credit/Debit' }
          ]}
          validations={[
            'Customer name is required',
            'Email must be valid format',
            'Phone must be numeric',
            'Balance must be numeric',
            'Balance type must be Credit or Debit'
          ]}
          onUpload={handleFileUpload}
          onDownloadTemplate={downloadTemplate}
          uploading={uploading}
          result={result}
        />
      )}

      {activeTab === 'products' && (
        <ImportCard
          title="Import Products"
          subtitle="Upload CSV or Excel file to import product data"
          type="products"
          fields={[
            { name: 'product_name', required: true },
            { name: 'sku', required: false },
            { name: 'category', required: false },
            { name: 'unit', required: false },
            { name: 'cost_price', required: false },
            { name: 'selling_price', required: false },
            { name: 'gst_rate', required: false, note: '0-28%' }
          ]}
          validations={[
            'Product name is required',
            'Prices must be numeric',
            'GST rate must be between 0 and 28'
          ]}
          onUpload={handleFileUpload}
          onDownloadTemplate={downloadTemplate}
          uploading={uploading}
          result={result}
        />
      )}

      {activeTab === 'opening-stock' && (
        <ImportCard
          title="Import Opening Stock"
          subtitle="Upload CSV or Excel file to import opening stock data"
          type="opening-stock"
          fields={[
            { name: 'product_name', required: true, note: 'Must exist in products' },
            { name: 'quantity', required: true },
            { name: 'warehouse', required: false }
          ]}
          validations={[
            'Product name is required and must exist',
            'Quantity must be greater than 0',
            'Quantity must be numeric'
          ]}
          onUpload={handleFileUpload}
          onDownloadTemplate={downloadTemplate}
          uploading={uploading}
          result={result}
        />
      )}
    </div>
  );
}

function ImportCard({ title, subtitle, type, fields, validations, onUpload, onDownloadTemplate, uploading, result }) {
  const fileInputId = `file-input-${type}`;

  return (
    <div className="grid cols-1">
      <FrostedCard title={title} subtitle={subtitle}>
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>

          {/* File Upload Section */}
          <div>
            <label htmlFor={fileInputId} style={{ display: 'block', marginBottom: '0.5rem', fontWeight: '600' }}>
              Upload File
            </label>
            <div style={{ display: 'flex', gap: '1rem', alignItems: 'center' }}>
              <input
                id={fileInputId}
                type="file"
                accept=".csv,.xlsx,.xls"
                onChange={(e) => onUpload(e, type)}
                disabled={uploading}
                style={{ flex: 1 }}
              />
              <button
                className="btn btn-secondary"
                onClick={() => onDownloadTemplate(type)}
                disabled={uploading}
              >
                📥 Download Template
              </button>
            </div>
            <p style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', marginTop: '0.5rem' }}>
              Max file size: 5MB | Max rows: 5,000 | Formats: CSV, Excel (.xlsx, .xls)
            </p>
          </div>

          {/* Fields Reference */}
          <div>
            <h4 style={{ marginBottom: '0.75rem', fontSize: '0.95rem' }}>Required Fields</h4>
            <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.5rem' }}>
              {fields.map(field => (
                <Badge key={field.name} color={field.required ? 'blue' : 'gray'}>
                  {field.name} {field.required && '*'} {field.note && `(${field.note})`}
                </Badge>
              ))}
            </div>
          </div>

          {/* Validations */}
          <div>
            <h4 style={{ marginBottom: '0.75rem', fontSize: '0.95rem' }}>Validation Rules</h4>
            <ul style={{ fontSize: '0.9rem', color: 'var(--text-secondary)', paddingLeft: '1.25rem', margin: 0 }}>
              {validations.map((validation, idx) => (
                <li key={idx}>{validation}</li>
              ))}
            </ul>
          </div>

          {/* Upload Status */}
          {uploading && (
            <div style={{ padding: '1rem', background: 'var(--primary-light)', borderRadius: '8px', textAlign: 'center' }}>
              <div>⏳ Uploading and processing file...</div>
            </div>
          )}

          {/* Result Display */}
          {result && result.type === type && (
            <div style={{ padding: '1rem', background: result.success ? '#d4edda' : '#f8d7da', borderRadius: '8px', border: `1px solid ${result.success ? '#c3e6cb' : '#f5c6cb'}` }}>
              {result.data ? (
                <>
                  <div style={{ marginBottom: '1rem', fontWeight: '600' }}>
                    {result.success ? '✅ Import Successful' : '⚠️ Import Completed with Errors'}
                  </div>
                  <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '1rem', marginBottom: '1rem' }}>
                    <div>
                      <div style={{ fontSize: '0.85rem', color: '#666' }}>Total Rows</div>
                      <div style={{ fontSize: '1.5rem', fontWeight: '600' }}>{result.data.totalRows}</div>
                    </div>
                    <div>
                      <div style={{ fontSize: '0.85rem', color: '#28a745' }}>Success</div>
                      <div style={{ fontSize: '1.5rem', fontWeight: '600', color: '#28a745' }}>{result.data.successCount}</div>
                    </div>
                    <div>
                      <div style={{ fontSize: '0.85rem', color: '#dc3545' }}>Failed</div>
                      <div style={{ fontSize: '1.5rem', fontWeight: '600', color: '#dc3545' }}>{result.data.failedCount}</div>
                    </div>
                  </div>

                  {result.errors.length > 0 && (
                    <div>
                      <div style={{ fontWeight: '600', marginBottom: '0.5rem' }}>Errors:</div>
                      <div style={{ maxHeight: '200px', overflowY: 'auto', fontSize: '0.9rem' }}>
                        {result.errors.map((error, idx) => (
                          <div key={idx} style={{ padding: '0.5rem', background: 'rgba(220, 53, 69, 0.1)', marginBottom: '0.25rem', borderRadius: '4px' }}>
                            <strong>Row {error.rowNumber}:</strong> {error.field} - {error.reason}
                          </div>
                        ))}
                      </div>
                    </div>
                  )}
                </>
              ) : (
                <div style={{ color: '#721c24' }}>❌ {result.message}</div>
              )}
            </div>
          )}
        </div>
      </FrostedCard>
    </div>
  );
}
