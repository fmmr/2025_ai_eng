document.querySelector('form').addEventListener('submit', function() {
    const btn = document.getElementById('processBtn');
    const spinner = btn.querySelector('.spinner-border');
    btn.disabled = true;
    spinner.classList.remove('d-none');
    btn.innerHTML = '<span class="spinner-border spinner-border-sm me-2" role="status"></span>Processing...';
});

// Show/hide max words field based on operation
const operationSelect = document.getElementById('operation');
const maxWordsDiv = document.getElementById('maxWordsDiv');

function toggleMaxWords() {
    if (operationSelect.value === 'summarize') {
        maxWordsDiv.style.display = 'block';
    } else {
        maxWordsDiv.style.display = 'none';
    }
}

operationSelect.addEventListener('change', toggleMaxWords);

// Initialize on page load
document.addEventListener('DOMContentLoaded', function() {
    toggleMaxWords();
});