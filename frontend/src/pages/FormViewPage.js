import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';

const FormViewPage = () => {
  const { id } = useParams();
  const [form, setForm] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [formData, setFormData] = useState({});
  const [submitting, setSubmitting] = useState(false);
  const [submitMessage, setSubmitMessage] = useState('');

  useEffect(() => {
    fetchForm();
  }, [id]);

  const fetchForm = async () => {
    try {
      setLoading(true);
      const token = localStorage.getItem('token');
      
      const response = await fetch(`http://localhost:8080/api/forms/${id}`, {
        headers: {
          'Authorization': `Bearer ${token}`,
        },
      });
      
      if (response.ok) {
        const formData = await response.json();
        setForm(formData);
      } else if (response.status === 401) {
        setError('Session expired. Please login again.');
      } else if (response.status === 403) {
        setError('You do not have permission to access this form.');
      } else if (response.status === 404) {
        setError('Form not found');
      } else {
        setError('Failed to load form');
      }
    } catch (err) {
      setError('Error loading form. Please try again.');
      console.error('Error fetching form:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleInputChange = (fieldId, value) => {
    setFormData(prev => ({
      ...prev,
      [fieldId]: value
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    setSubmitMessage('');

    try {
      const response = await fetch(`http://localhost:8080/api/forms/${id}/submit`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          responses: formData
        })
      });

      const result = await response.json();

      if (response.ok) {
        setSubmitMessage('Form submitted successfully! Thank you for your feedback.');
        setFormData({}); // Clear form data
      } else {
        setSubmitMessage(`Error: ${result.message}`);
      }
    } catch (err) {
      console.error('Form submission error:', err);
      setSubmitMessage('Error submitting form. Please try again.');
    } finally {
      setSubmitting(false);
    }
  };

  const formatDate = (dateString) => {
    if (!dateString) return 'N/A';
    try {
      let date;
      
      // Handle different date formats from backend
      if (typeof dateString === 'string') {
        // Handle ISO string format
        if (dateString.includes('T')) {
          date = new Date(dateString);
        } else {
          // Handle other string formats
          date = new Date(dateString);
        }
      } else if (typeof dateString === 'object' && dateString !== null) {
        // Handle object format (might be LocalDateTime from Java)
        if (dateString.year && dateString.month && dateString.dayOfMonth) {
          // Java LocalDateTime format
          date = new Date(dateString.year, dateString.month - 1, dateString.dayOfMonth, 
                         dateString.hour || 0, dateString.minute || 0, dateString.second || 0);
        } else if (dateString.timestamp) {
          date = new Date(dateString.timestamp);
        } else {
          date = new Date(dateString);
        }
      } else {
        date = new Date(dateString);
      }
      
      // Check if date is valid
      if (isNaN(date.getTime())) {
        console.warn('Invalid date:', dateString);
        return 'N/A';
      }
      
      return date.toLocaleDateString() + ' ' + date.toLocaleTimeString();
    } catch (e) {
      console.error('Date formatting error:', e, 'Input:', dateString);
      return 'N/A';
    }
  };

  if (loading) {
    return (
      <div className="container">
        <div className="form-view-container">
          <div className="form-view-header">
            <h1>Loading Form...</h1>
            <Link to="/forms" className="btn btn-secondary">
              Back to Forms
            </Link>
          </div>
          <div className="loading-message">
            <p>Loading form details...</p>
          </div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="container">
        <div className="form-view-container">
          <div className="form-view-header">
            <h1>Error</h1>
            <Link to="/forms" className="btn btn-secondary">
              Back to Forms
            </Link>
          </div>
          <div className="error-message">
            {error}
          </div>
        </div>
      </div>
    );
  }

  if (!form) {
    return (
      <div className="container">
        <div className="form-view-container">
          <div className="form-view-header">
            <h1>Form Not Found</h1>
            <Link to="/forms" className="btn btn-secondary">
              Back to Forms
            </Link>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="container">
      <div className="form-view-container">
        <div className="form-view-header">
          <h1>{form.title}</h1>
          <div className="header-actions">
            <Link to={`/form/${id}/edit`} className="btn btn-primary">
              Edit Form
            </Link>
            <Link to="/forms" className="btn btn-secondary">
              Back to Forms
            </Link>
          </div>
        </div>

        <div className="form-details">
          <div className="form-info-card">
            <h2>Form Information</h2>
            <div className="form-info-grid">
              <div className="info-item">
                <strong>Title:</strong> {form.title}
              </div>
              <div className="info-item">
                <strong>Description:</strong> {form.description || 'No description provided'}
              </div>
              <div className="info-item">
                <strong>Status:</strong> 
                <span className={`status-badge ${form.active ? 'active' : 'inactive'}`}>
                  {form.active ? 'Active' : 'Inactive'}
                </span>
              </div>
              <div className="info-item">
                <strong>Form ID:</strong> #{form.id}
              </div>
              <div className="info-item">
                <strong>Created:</strong> {formatDate(form.createdAt)}
              </div>
              <div className="info-item">
                <strong>Last Updated:</strong> {formatDate(form.updatedAt)}
              </div>
            </div>
          </div>

          <div className="form-preview-card">
            <h2>Form Preview</h2>
            <div className="form-preview">
              <div className="preview-header">
                <h3>{form.title}</h3>
                {form.description && <p className="preview-description">{form.description}</p>}
              </div>
              
              {submitMessage && (
                <div className={`submit-message ${submitMessage.includes('Error') ? 'error' : 'success'}`}>
                  {submitMessage}
                </div>
              )}
              
              <form onSubmit={handleSubmit}>
                <div className="preview-fields">
                  {form.fields && form.fields.length > 0 ? (
                    form.fields.map((field, index) => (
                      <div key={index} className="preview-field">
                        <label className="preview-label">
                          {field.label || `Field ${index + 1}`}
                          {field.required && <span className="required">*</span>}
                        </label>
                        {renderPreviewField(field, formData[field.id] || '', handleInputChange)}
                      </div>
                    ))
                  ) : (
                    <p className="no-fields">No fields configured for this form.</p>
                  )}
                </div>
                
                <div className="preview-actions">
                  <button 
                    type="submit" 
                    className="btn btn-primary"
                    disabled={submitting}
                  >
                    {submitting ? 'Submitting...' : 'Submit Form'}
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

const renderPreviewField = (field, value, onChange) => {
  switch (field.type) {
    case 'text':
    case 'email':
    case 'number':
    case 'date':
      return (
        <input
          type={field.type}
          placeholder={field.placeholder || `Enter ${field.type}...`}
          className="preview-input"
          value={value}
          onChange={(e) => onChange(field.id, e.target.value)}
        />
      );
    case 'textarea':
      return (
        <textarea
          placeholder={field.placeholder || 'Enter text...'}
          className="preview-textarea"
          rows="3"
          value={value}
          onChange={(e) => onChange(field.id, e.target.value)}
        />
      );
    case 'select':
      return (
        <select 
          className="preview-select"
          value={value}
          onChange={(e) => onChange(field.id, e.target.value)}
        >
          <option value="">{field.placeholder || 'Select an option...'}</option>
          {field.options && field.options.map((option, index) => (
            <option key={index} value={option}>{option}</option>
          ))}
        </select>
      );
    case 'radio':
      return (
        <div className="preview-radio-group">
          {field.options && field.options.map((option, index) => (
            <label key={index} className="preview-radio-label">
              <input 
                type="radio" 
                name={`field_${field.id}`} 
                value={option}
                checked={value === option}
                onChange={(e) => onChange(field.id, e.target.value)}
              />
              {option}
            </label>
          ))}
        </div>
      );
    case 'checkbox':
      return (
        <div className="preview-checkbox-group">
          {field.options && field.options.map((option, index) => (
            <label key={index} className="preview-checkbox-label">
              <input 
                type="checkbox" 
                value={option}
                checked={Array.isArray(value) && value.includes(option)}
                onChange={(e) => {
                  const currentValues = Array.isArray(value) ? value : [];
                  if (e.target.checked) {
                    onChange(field.id, [...currentValues, option]);
                  } else {
                    onChange(field.id, currentValues.filter(v => v !== option));
                  }
                }}
              />
              {option}
            </label>
          ))}
        </div>
      );
    default:
      return (
        <input
          type="text"
          placeholder="Unknown field type"
          className="preview-input"
          value={value}
          onChange={(e) => onChange(field.id, e.target.value)}
        />
      );
  }
};

export default FormViewPage;
