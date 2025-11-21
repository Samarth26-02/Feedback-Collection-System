import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';

const FormEditPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [form, setForm] = useState(null);
  const [formTitle, setFormTitle] = useState('');
  const [formDescription, setFormDescription] = useState('');
  const [fields, setFields] = useState([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

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
        setFormTitle(formData.title || '');
        setFormDescription(formData.description || '');
        setFields(formData.fields || []);
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

  const addField = (type) => {
    const newField = {
      id: Date.now().toString(),
      type: type,
      label: '',
      required: false,
      placeholder: '',
      options: type === 'select' || type === 'radio' || type === 'checkbox' ? ['Option 1', 'Option 2'] : null,
      order: fields.length
    };
    setFields([...fields, newField]);
  };

  const updateField = (fieldId, updates) => {
    setFields(fields.map(field => 
      field.id === fieldId ? { ...field, ...updates } : field
    ));
  };

  const removeField = (fieldId) => {
    setFields(fields.filter(field => field.id !== fieldId));
  };

  const moveField = (fieldId, direction) => {
    const index = fields.findIndex(field => field.id === fieldId);
    if ((direction === 'up' && index > 0) || (direction === 'down' && index < fields.length - 1)) {
      const newFields = [...fields];
      const targetIndex = direction === 'up' ? index - 1 : index + 1;
      [newFields[index], newFields[targetIndex]] = [newFields[targetIndex], newFields[index]];
      setFields(newFields);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSaving(true);

    if (!formTitle.trim()) {
      setError('Form title is required');
      setSaving(false);
      return;
    }

    try {
      const token = localStorage.getItem('token');
      
      const formData = {
        title: formTitle,
        description: formDescription,
        fields: fields
      };

      const response = await fetch(`http://localhost:8080/api/forms/${id}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`,
        },
        body: JSON.stringify(formData),
      });

      if (response.ok) {
        const result = await response.json();
        console.log('Form updated successfully:', result);
        navigate('/forms');
      } else {
        if (response.status === 401) {
          setError('Session expired. Please login again.');
        } else if (response.status === 403) {
          setError('You do not have permission to edit this form.');
        } else {
          const errorData = await response.json();
          setError(errorData.message || 'Error updating form');
        }
      }
    } catch (err) {
      setError('Error updating form. Please try again.');
      console.error('Error:', err);
    } finally {
      setSaving(false);
    }
  };

  const renderFieldEditor = (field) => {
    return (
      <div key={field.id} className="field-editor">
        <div className="field-header">
          <select 
            value={field.type} 
            onChange={(e) => updateField(field.id, { type: e.target.value })}
            className="field-type-select"
          >
            <option value="text">Text Input</option>
            <option value="textarea">Text Area</option>
            <option value="email">Email</option>
            <option value="number">Number</option>
            <option value="select">Dropdown</option>
            <option value="radio">Radio Buttons</option>
            <option value="checkbox">Checkboxes</option>
            <option value="date">Date</option>
          </select>
          <div className="field-actions">
            <button type="button" onClick={() => moveField(field.id, 'up')} className="btn-small">↑</button>
            <button type="button" onClick={() => moveField(field.id, 'down')} className="btn-small">↓</button>
            <button type="button" onClick={() => removeField(field.id)} className="btn-small btn-danger">×</button>
          </div>
        </div>
        
        <div className="field-settings">
          <input
            type="text"
            placeholder="Field label"
            value={field.label}
            onChange={(e) => updateField(field.id, { label: e.target.value })}
            className="field-input"
          />
          <input
            type="text"
            placeholder="Placeholder text"
            value={field.placeholder}
            onChange={(e) => updateField(field.id, { placeholder: e.target.value })}
            className="field-input"
          />
          <label className="checkbox-label">
            <input
              type="checkbox"
              checked={field.required}
              onChange={(e) => updateField(field.id, { required: e.target.checked })}
            />
            Required
          </label>
        </div>

        {(field.type === 'select' || field.type === 'radio' || field.type === 'checkbox') && (
          <div className="options-editor">
            <label>Options:</label>
            {field.options.map((option, index) => (
              <div key={index} className="option-input">
                <input
                  type="text"
                  value={option}
                  onChange={(e) => {
                    const newOptions = [...field.options];
                    newOptions[index] = e.target.value;
                    updateField(field.id, { options: newOptions });
                  }}
                  className="field-input"
                />
                <button 
                  type="button" 
                  onClick={() => {
                    const newOptions = field.options.filter((_, i) => i !== index);
                    updateField(field.id, { options: newOptions });
                  }}
                  className="btn-small btn-danger"
                >
                  ×
                </button>
              </div>
            ))}
            <button 
              type="button" 
              onClick={() => {
                const newOptions = [...field.options, 'New Option'];
                updateField(field.id, { options: newOptions });
              }}
              className="btn-small"
            >
              Add Option
            </button>
          </div>
        )}
      </div>
    );
  };

  if (loading) {
    return (
      <div className="container">
        <div className="form-builder-container">
          <div className="form-builder-header">
            <h1>Loading Form...</h1>
            <button onClick={() => navigate('/forms')} className="btn btn-secondary">
              Back to Forms
            </button>
          </div>
          <div className="loading-message">
            <p>Loading form for editing...</p>
          </div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="container">
        <div className="form-builder-container">
          <div className="form-builder-header">
            <h1>Error</h1>
            <button onClick={() => navigate('/forms')} className="btn btn-secondary">
              Back to Forms
            </button>
          </div>
          <div className="error-message">
            {error}
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="container">
      <div className="form-builder-container">
        <div className="form-builder-header">
          <h1>Edit Form: {form?.title}</h1>
          <button onClick={() => navigate('/forms')} className="btn btn-secondary">
            Back to Forms
          </button>
        </div>

        <form onSubmit={handleSubmit} className="form-builder-form">
          <div className="form-basic-info">
            <h2>Form Details</h2>
            <div className="form-group">
              <label htmlFor="title">Form Title *</label>
              <input
                type="text"
                id="title"
                value={formTitle}
                onChange={(e) => setFormTitle(e.target.value)}
                placeholder="Enter form title"
                required
                className="form-input"
              />
            </div>
            <div className="form-group">
              <label htmlFor="description">Description</label>
              <textarea
                id="description"
                value={formDescription}
                onChange={(e) => setFormDescription(e.target.value)}
                placeholder="Enter form description"
                className="form-input"
                rows="3"
              />
            </div>
          </div>

          <div className="form-fields-section">
            <h2>Form Fields</h2>
            <div className="add-field-buttons">
              <button type="button" onClick={() => addField('text')} className="btn btn-primary">
                Add Text Input
              </button>
              <button type="button" onClick={() => addField('textarea')} className="btn btn-primary">
                Add Text Area
              </button>
              <button type="button" onClick={() => addField('email')} className="btn btn-primary">
                Add Email
              </button>
              <button type="button" onClick={() => addField('number')} className="btn btn-primary">
                Add Number
              </button>
              <button type="button" onClick={() => addField('select')} className="btn btn-primary">
                Add Dropdown
              </button>
              <button type="button" onClick={() => addField('radio')} className="btn btn-primary">
                Add Radio
              </button>
              <button type="button" onClick={() => addField('checkbox')} className="btn btn-primary">
                Add Checkbox
              </button>
              <button type="button" onClick={() => addField('date')} className="btn btn-primary">
                Add Date
              </button>
            </div>

            <div className="fields-list">
              {fields.map(renderFieldEditor)}
            </div>
          </div>

          {error && <div className="error-message">{error}</div>}

          <div className="form-actions">
            <button 
              type="submit" 
              className="btn btn-success"
              disabled={saving}
            >
              {saving ? 'Updating Form...' : 'Update Form'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default FormEditPage;
