import React from 'react';
import { CartIcon } from './CartIcon';
import { ProfileDropdown } from './ProfileDropdown';
import './assets/styles.css';
import headerLogo from './assets/header-logo.png';

export function Header({ cartCount, username }) {
  return (
    <header className="header">
      <div className="header-content">
        <img src={headerLogo} alt="Sales Savvy" className="header-logo"/>
        <div className="header-actions">
          <CartIcon count={cartCount} />
          <ProfileDropdown username={username} />
        </div>
      </div>
    </header>
  );
}