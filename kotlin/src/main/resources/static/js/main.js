// Main JavaScript for AI Engineering Course

document.addEventListener('DOMContentLoaded', function() {
    console.log('AI Engineering Course - JavaScript loaded');
    
    // Initialize Bootstrap tooltips
    initializeBootstrapTooltips();
    
    // Initialize demo functionality
    initializeDemos();
    
    
    // Add smooth scrolling for anchor links
    initializeSmoothScrolling();
    
    // Add loading states for demo buttons and forms
    initializeLoadingStates();
    initializeFormSubmissions();
});

function initializeDemos() {
    const demoButtons = document.querySelectorAll('.demo-btn');
    demoButtons.forEach(button => {
        button.addEventListener('click', function(e) {
            e.preventDefault();
            const demoType = this.dataset.demo;
            runDemo(demoType, this);
        });
    });
}

async function runDemo(demoType, button) {
    const outputElement = document.querySelector(`#${demoType}-output`);
    if (!outputElement) return;
    
    setLoadingState(button, true);
    outputElement.innerHTML = '<div class="loading">Running demo...</div>';
    
    try {
        const response = await fetch(`/api/demo/${demoType}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            }
        });
        
        if (response.ok) {
            const result = await response.text();
            outputElement.innerHTML = `<pre>${escapeHtml(result)}</pre>`;
        } else {
            throw new Error(`Demo failed: ${response.statusText}`);
        }
    } catch (error) {
        outputElement.innerHTML = `<div class="error">Error: ${escapeHtml(error.message)}</div>`;
    } finally {
        setLoadingState(button, false);
    }
}

function initializeSmoothScrolling() {
    const anchorLinks = document.querySelectorAll('a[href^="#"]');
    anchorLinks.forEach(link => {
        link.addEventListener('click', function(e) {
            e.preventDefault();
            const targetId = this.getAttribute('href').substring(1);
            const targetElement = document.getElementById(targetId);
            
            if (targetElement) {
                targetElement.scrollIntoView({
                    behavior: 'smooth',
                    block: 'start'
                });
            }
        });
    });
}

function initializeLoadingStates() {
}

function initializeFormSubmissions() {
    const demoForms = document.querySelectorAll('form[action*="/demo/"]');
    demoForms.forEach(form => {
        form.addEventListener('submit', function() {
            const submitButton = form.querySelector('button[type="submit"]');
            if (submitButton) {
                setFormLoadingState(submitButton, true);
            }
        });
    });
}

function setFormLoadingState(button, isLoading) {
    if (isLoading) {
        button.disabled = true;
        
        const buttonText = button.querySelector('.button-text');
        const spinner = button.querySelector('.spinner-border');
        
        if (buttonText && spinner) {
            buttonText.classList.add('d-none');
            spinner.classList.remove('d-none');
        } else {
            button.dataset.originalText = button.textContent;
            button.innerHTML = '<span class="spinner-border spinner-border-sm me-2" role="status"></span>Processing...';
        }
        
        button.classList.add('loading');
    } else {
        button.disabled = false;
        
        const buttonText = button.querySelector('.button-text');
        const spinner = button.querySelector('.spinner-border');
        
        if (buttonText && spinner) {
            buttonText.classList.remove('d-none');
            spinner.classList.add('d-none');
        } else {
            button.innerHTML = button.dataset.originalText || button.innerHTML;
        }
        
        button.classList.remove('loading');
    }
}

function setLoadingState(button, isLoading) {
    if (isLoading) {
        button.disabled = true;
        button.dataset.originalText = button.textContent;
        button.textContent = 'Running...';
        button.classList.add('loading');
    } else {
        button.disabled = false;
        button.textContent = button.dataset.originalText || button.textContent;
        button.classList.remove('loading');
    }
}

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

function showNotification(message, type = 'info') {
    const notification = document.createElement('div');
    notification.className = `notification notification-${type}`;
    notification.textContent = message;
    
    document.body.appendChild(notification);
    
    setTimeout(() => {
        notification.remove();
    }, 3000);
}

function initializeBootstrapTooltips() {
    const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    tooltipTriggerList.forEach(function (tooltipTriggerEl) {
        new bootstrap.Tooltip(tooltipTriggerEl);
    });
}

window.AICourseFunctions = {
    runDemo,
    setLoadingState,
    showNotification,
    escapeHtml
};