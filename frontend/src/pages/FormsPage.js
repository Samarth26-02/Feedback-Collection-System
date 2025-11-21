import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';

const FormsPage = () => {
  const [forms, setForms] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchForms();
  }, []);

  const fetchForms = async () => {
    try {
      setLoading(true);
      const token = localStorage.getItem('token');
      
      const response = await fetch('http://localhost:8080/api/forms', {
        headers: {
          'Authorization': `Bearer ${token}`,
        },
      });
      
      if (response.ok) {
        const formsData = await response.json();
        setForms(formsData);
      } else {
        if (response.status === 401) {
          setError('Session expired. Please login again.');
        } else {
          setError('Failed to fetch forms');
        }
      }
    } catch (err) {
      setError('Error loading forms. Please try again.');
      console.error('Error fetching forms:', err);
    } finally {
      setLoading(false);
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

  const handleDeleteForm = async (formId) => {
    if (!window.confirm('Are you sure you want to delete this form?')) {
      return;
    }

    try {
      const token = localStorage.getItem('token');
      
      const response = await fetch(`http://localhost:8080/api/forms/${formId}`, {
        method: 'DELETE',
        headers: {
          'Authorization': `Bearer ${token}`,
        },
      });

      if (response.ok) {
        // Remove the deleted form from the list
        setForms(forms.filter(form => form.id !== formId));
      } else {
        if (response.status === 401) {
          setError('Session expired. Please login again.');
        } else if (response.status === 403) {
          setError('You do not have permission to delete this form.');
        } else {
          setError('Failed to delete form');
        }
      }
    } catch (err) {
      setError('Error deleting form. Please try again.');
      console.error('Error deleting form:', err);
    }
  };

  if (loading) {
    return (
      <div className="container">
        <div className="forms-container">
          <div className="forms-header">
            <h1>My Forms</h1>
            <Link to="/dashboard" className="btn btn-secondary">
              Back to Dashboard
            </Link>
          </div>
          <div className="loading-message">
            <p>Loading forms...</p>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="container">
      <div className="forms-container">
        <div className="forms-header">
          <h1>My Forms</h1>
          <div className="header-actions">
            <Link to="/form-builder" className="btn btn-primary">
              Create New Form
            </Link>
            <Link to="/dashboard" className="btn btn-secondary">
              Back to Dashboard
            </Link>
          </div>
        </div>

        {error && <div className="error-message">{error}</div>}

        {forms.length === 0 ? (
          <div className="empty-state">
            <h2>No forms found</h2>
            <p>You haven't created any forms yet. Create your first form to get started!</p>
            <Link to="/form-builder" className="btn btn-primary btn-large">
              Create Your First Form
            </Link>
          </div>
        ) : (
          <div className="forms-grid">
            {forms.map((form) => (
              <div key={form.id} className="form-card">
                <div className="form-card-header">
                  <h3>{form.title}</h3>
                  <div className="form-status">
                    <span className={`status-badge ${form.active ? 'active' : 'inactive'}`}>
                      {form.active ? 'Active' : 'Inactive'}
                    </span>
                  </div>
                </div>
                
                <div className="form-card-body">
                  <p className="form-description">
                    {form.description || 'No description provided'}
                  </p>
                  
                  <div className="form-meta">
                    <div className="meta-item">
                      <strong>Created:</strong> {formatDate(form.createdAt)}
                    </div>
                    <div className="meta-item">
                      <strong>Last Updated:</strong> {formatDate(form.updatedAt)}
                    </div>
                    <div className="meta-item">
                      <strong>Form ID:</strong> #{form.id}
                    </div>
                  </div>
                </div>

                <div className="form-card-actions">
                  <Link 
                    to={`/form/${form.id}`}
                    className="btn btn-primary btn-small"
                  >
                    View Form
                  </Link>
                  <Link 
                    to={`/form/${form.id}/edit`}
                    className="btn btn-secondary btn-small"
                  >
                    Edit
                  </Link>
                  <button 
                    className="btn btn-danger btn-small"
                    onClick={() => handleDeleteForm(form.id)}
                  >
                    Delete
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default FormsPage;
