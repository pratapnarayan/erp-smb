import React from 'react';

export default function FrostedCard({ title, subtitle, actions, children, footer, tight = false }) {
  return (
    <section className={`card frosted ${tight ? 'card--tight' : ''}`}>
      {(title || actions || subtitle) && (
        <header className="card-header">
          <div className="card-titles">
            {title && <h3 className="card-title">{title}</h3>}
            {subtitle && <p className="card-subtitle">{subtitle}</p>}
          </div>
          <div className="card-actions">{actions}</div>
        </header>
      )}
      <div className="card-body">{children}</div>
      {footer && <footer className="card-footer">{footer}</footer>}
    </section>
  );
}
