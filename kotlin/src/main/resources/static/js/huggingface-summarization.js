document.querySelector('form').addEventListener('submit', function() {
    const btn = document.getElementById('summarizeBtn');
    const spinner = btn.querySelector('.spinner-border');
    btn.disabled = true;
    spinner.classList.remove('d-none');
    btn.innerHTML = '<span class="spinner-border spinner-border-sm me-2" role="status"></span>Processing...';
});