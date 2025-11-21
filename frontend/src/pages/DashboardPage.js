import React, { useEffect, useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { logoutUser } from '../services/api';

const DashboardPage = () => {
  const [user, setUser] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    const userInfo = localStorage.getItem('userInfo');
    if (!userInfo) {
      navigate('/login');
      return;
    }
    setUser(JSON.parse(userInfo).user);
  }, [navigate]);

  const handleLogout = () => {
    logoutUser();
    navigate('/login');
  };

  if (!user) {
    return null;
  }

  return (
    <div className="container">
      <div className="dashboard-container">
        <div className="dashboard-header">
          <h1>Welcome, {user.name}!</h1>
          <button onClick={handleLogout} className="btn btn-logout">Logout</button>
        </div>

        <div className="dashboard-content">
          <div className="dashboard-actions">
            <Link to="/form-builder" className="btn btn-primary btn-large">
              Create New Form
            </Link>
            <Link to="/forms" className="btn btn-secondary btn-large">
              View My Forms
            </Link>
          </div>

          <div className="system-info-card">
            <h2>About Our Feedback Collection System</h2>
            <div className="info-section">
              <h3>What We Offer</h3>
              <p>
                Our Feedback Collection System is a comprehensive platform designed to help organizations
                gather, analyze, and act on valuable user feedback. We provide powerful tools to create
                meaningful connections with your users and make data-driven decisions.
              </p>
            </div>

            <div className="features-grid">
              <div className="feature-card">
                <h4>Customizable Forms</h4>
                <p>Create tailored feedback forms with various question types to gather specific insights.</p>
              </div>
              <div className="feature-card">
                <h4>Real-time Analytics</h4>
                <p>Get instant insights with our powerful analytics dashboard and reporting tools.</p>
              </div>
              <div className="feature-card">
                <h4>Data Visualization</h4>
                <p>Transform feedback data into clear, actionable insights with interactive charts.</p>
              </div>
              <div className="feature-card">
                <h4>User Management</h4>
                <p>Easily manage user access and permissions for your feedback collection team.</p>
              </div>
            </div>

            <div className="info-section">
              <h3>Getting Started</h3>
              <ol className="getting-started-list">
                <li>Create your first feedback form using our intuitive form builder</li>
                <li>Share the form with your target audience</li>
                <li>Collect and analyze responses in real-time</li>
                <li>Generate reports and take action based on insights</li>
              </ol>
            </div>

            <div className="info-section">
              <h3>Why Choose Us?</h3>
              <ul className="benefits-list">
                <li>User-friendly interface for easy navigation</li>
                <li>Secure data storage and privacy protection</li>
                <li>Customizable reporting and analytics</li>
                <li>24/7 customer support</li>
                <li>Regular updates and new features</li>
              </ul>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default DashboardPage; 