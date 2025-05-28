// Main JavaScript for AI Engineering Course

document.addEventListener('DOMContentLoaded', function() {
    console.log('AI Engineering Course - JavaScript loaded');
    
    // Initialize Bootstrap tooltips
    initializeBootstrapTooltips();
    
    // Initialize demo functionality
    initializeDemos();
    
    
    // Add smooth scrolling for anchor links
    initializeSmoothScrolling();
    
    // Add loading states for demo buttons
    initializeLoadingStates();
});

// Demo functionality
function initializeDemos() {
    // Handle demo button clicks
    const demoButtons = document.querySelectorAll('.demo-btn');
    demoButtons.forEach(button => {
        button.addEventListener('click', function(e) {
            e.preventDefault();
            const demoType = this.dataset.demo;
            runDemo(demoType, this);
        });
    });
}

// Run a specific demo
async function runDemo(demoType, button) {
    const outputElement = document.querySelector(`#${demoType}-output`);
    if (!outputElement) return;
    
    // Show loading state
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
            // noinspection ExceptionCaughtLocallyJS
            throw new Error(`Demo failed: ${response.statusText}`);
        }
    } catch (error) {
        outputElement.innerHTML = `<div class="error">Error: ${escapeHtml(error.message)}</div>`;
    } finally {
        setLoadingState(button, false);
    }
}

// Smooth scrolling for anchor links
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

// Loading states for buttons
function initializeLoadingStates() {
    // This will be called by other functions when needed
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

// Utility functions
function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

function showNotification(message, type = 'info') {
    // Create notification element
    const notification = document.createElement('div');
    notification.className = `notification notification-${type}`;
    notification.textContent = message;
    
    // Add to page
    document.body.appendChild(notification);
    
    // Auto-remove after 3 seconds
    setTimeout(() => {
        notification.remove();
    }, 3000);
}

// Initialize Bootstrap tooltips
function initializeBootstrapTooltips() {
    // Initialize all Bootstrap tooltips
    const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    tooltipTriggerList.forEach(function (tooltipTriggerEl) {
        new bootstrap.Tooltip(tooltipTriggerEl);
    });
}


// Export functions for use in other scripts
window.AICourseFunctions = {
    runDemo,
    setLoadingState,
    showNotification,
    escapeHtml
};