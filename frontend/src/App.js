
import React from 'react';
import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import DashboardPage from './pages/DashboardPage';
import FormBuilderPage from './pages/FormBuilderPage';
import FormsPage from './pages/FormsPage';
import FormViewPage from './pages/FormViewPage';
import FormEditPage from './pages/FormEditPage';
import './App.css';

function App() {
  return (
    <Router>
      <div className="App">
        <header className="header">
          <h1>Feedback Collection System</h1>
        </header>
        <main>
          <Routes>
            <Route path="/register" element={<RegisterPage />} />
            <Route path="/login" element={<LoginPage />} />
            <Route path="/dashboard" element={<DashboardPage />} />
            <Route path="/form-builder" element={<FormBuilderPage />} />
            <Route path="/forms" element={<FormsPage />} />
            <Route path="/form/:id" element={<FormViewPage />} />
            <Route path="/form/:id/edit" element={<FormEditPage />} />
            <Route path="/" element={<RegisterPage />} />
          </Routes>
        </main>
      </div>
    </Router>
  );
}

export default App;
