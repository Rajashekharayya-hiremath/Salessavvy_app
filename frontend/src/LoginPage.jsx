import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import "./assets/styles.css";
import logo from './assets/logo.png';

export default function LoginPage() {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState(null);
  const navigate = useNavigate();

  const handleSignIn = async (e) => {
    e.preventDefault();
    setError(null);

    if (!username.trim() || !password.trim()) {
      setError("Username and password are required");
      return;
    }

    try {
      const response = await fetch("http://localhost:9090/api/auth/login", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        credentials: "include",
        body: JSON.stringify({ username, password }),
      });

      const data = await response.json();
      console.log("Login Response:", data);

      if (response.ok) {
        if (data.role === "CUSTOMER") {
          navigate("/customerhome");
        } else if (data.role === "ADMIN") {
          navigate("/admindashboard");
        } else {
          navigate("/"); // Redirect to a default page if role is unknown
        }
      } else {
        const errorMessage =
          data.error || "Something went wrong. Please try again.";
        throw new Error(errorMessage);
      }
    } catch (err) {
      setError(err.message || "Unexpected error occurred");
    }
  };

  return (
  <div className="auth-page login-page">
    <div className="auth-overlay"></div>

    <div className="auth-wrapper">

      <div className="auth-left">
        <div className="brand-logo">
          <div className="logo-container">
             <img src={logo} alt="Sales Savvy Logo" className="login-logo" />
          </div>
        </div>

        <h1 className="brand-title">SalesSavvy</h1>

        <h2 className="brand-tagline">
          Shop Smarter. Sell Better.
        </h2>

        <p className="brand-description">
          Discover products, manage orders and enjoy a seamless shopping experience.
        </p>
      </div>

      <div className="auth-card">
        <h2 className="auth-title">Welcome Back</h2>

        {error && <p className="error-message">{error}</p>}

        <form onSubmit={handleSignIn} className="form-content">

          <div className="form-group">
            <label htmlFor="username" className="form-label">
              Username
            </label>

            <input
              id="username"
              type="text"
              placeholder="Enter your username"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              required
              className="form-input"
            />
          </div>

          <div className="form-group">
            <label htmlFor="password" className="form-label">
              Password
            </label>

            <input
              id="password"
              type="password"
              placeholder="Enter your password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              className="form-input"
            />
          </div>

          <button type="submit" className="auth-button">
            Sign In
          </button>

        </form>

        <div className="form-footer">
          <a href="/register" className="form-link">
            New User? Sign up here
          </a>
        </div>

      </div>
    </div>
  </div>

);
}