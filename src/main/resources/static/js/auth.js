(function () {
  const forms = document.querySelectorAll('[data-auth-form]');

  const patterns = {
    email: /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
    mobile: /^\d{10}$/,
    uppercase: /[A-Z]/,
    number: /\d/,
    special: /[^A-Za-z0-9]/
  };

  function setError(field, message) {
    const feedback = field.closest('.mb-3, .mb-4')?.querySelector('[data-error-for="' + field.name + '"]');
    if (feedback) {
      feedback.textContent = message || '';
    }
  }

  function validateField(field) {
    const value = field.value.trim();
    let message = '';

    if (field.name === 'fullName') {
      if (value.length < 3) message = 'Full name must be at least 3 characters';
    }

    if (field.name === 'email') {
      if (!patterns.email.test(value)) message = 'Invalid email format';
    }

    if (field.name === 'mobile') {
      if (!patterns.mobile.test(value)) message = 'Enter valid 10-digit mobile number';
    }

    if (field.name === 'password') {
      if (value.length < 8) message = 'Password must be at least 8 characters';
      else if (!patterns.uppercase.test(value)) message = 'Password must include at least 1 uppercase letter';
      else if (!patterns.number.test(value)) message = 'Password must include at least 1 number';
      else if (!patterns.special.test(value)) message = 'Password must include at least 1 special character';
    }

    if (field.name === 'confirmPassword') {
      const password = document.querySelector('input[name="password"]');
      if (password && value !== password.value) message = 'Passwords do not match';
    }

    setError(field, message);
    return !message;
  }

  forms.forEach((form) => {
    const fields = form.querySelectorAll('input');

    fields.forEach((field) => {
      field.addEventListener('input', function () {
        validateField(field);
      });
    });

    form.querySelectorAll('[data-toggle-password]').forEach((toggleBtn) => {
      toggleBtn.addEventListener('click', function () {
        const target = form.querySelector('#' + this.dataset.togglePassword);
        if (!target) return;
        const isPassword = target.type === 'password';
        target.type = isPassword ? 'text' : 'password';
        this.textContent = isPassword ? 'Hide' : 'Show';
      });
    });

    form.addEventListener('submit', function (event) {
      let valid = true;
      fields.forEach((field) => {
        if (!validateField(field)) valid = false;
      });

      if (!valid) {
        event.preventDefault();
        return;
      }

      const submitBtn = form.querySelector('button[type="submit"]');
      if (submitBtn) {
        submitBtn.disabled = true;
        const spinner = submitBtn.querySelector('.spinner-border');
        if (spinner) spinner.classList.remove('d-none');
      }
    });
  });
})();
