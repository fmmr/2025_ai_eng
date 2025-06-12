// Note: detectedObjects is injected from the template via a global variable

// Color palette for different objects
const colors = [
    '#FF0000', '#00FF00', '#0000FF', '#FFFF00', '#FF00FF', '#00FFFF',
    '#FFA500', '#800080', '#008000', '#FFC0CB', '#A0522D', '#808080'
];

function drawBoundingBoxes() {
    const img = document.getElementById('detectionImage');
    const canvas = document.getElementById('boundingBoxCanvas');
    
    if (!img || !canvas || !window.detectedObjects || window.detectedObjects.length === 0) {
        return;
    }
    
    // Wait for image to load
    if (!img.complete) {
        img.onload = drawBoundingBoxes;
        return;
    }
    
    const ctx = canvas.getContext('2d');
    
    // Set canvas size to match image
    const rect = img.getBoundingClientRect();
    canvas.width = img.offsetWidth;
    canvas.height = img.offsetHeight;
    canvas.style.width = img.offsetWidth + 'px';
    canvas.style.height = img.offsetHeight + 'px';
    
    // Calculate scale factors (image might be resized by CSS)
    const scaleX = img.offsetWidth / img.naturalWidth;
    const scaleY = img.offsetHeight / img.naturalHeight;
    
    // Clear canvas
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    
    // Draw bounding boxes
    window.detectedObjects.forEach((obj, index) => {
        const box = obj.box;
        const color = colors[index % colors.length];
        
        // Scale coordinates
        const x = box.xmin * scaleX;
        const y = box.ymin * scaleY;
        const width = (box.xmax - box.xmin) * scaleX;
        const height = (box.ymax - box.ymin) * scaleY;
        
        // Draw bounding box
        ctx.strokeStyle = color;
        ctx.lineWidth = 3;
        ctx.strokeRect(x, y, width, height);
        
        // Draw label background
        const label = `${obj.label} (${obj.percentage}%)`;
        ctx.font = '14px Arial';
        const textWidth = ctx.measureText(label).width;
        const textHeight = 16;
        
        ctx.fillStyle = color;
        ctx.fillRect(x, y - textHeight - 4, textWidth + 8, textHeight + 4);
        
        // Draw label text
        ctx.fillStyle = 'white';
        ctx.fillText(label, x + 4, y - 6);
    });
}

// Form submission handling
document.querySelector('form').addEventListener('submit', function() {
    const btn = document.getElementById('detectBtn');
    const spinner = btn.querySelector('.spinner-border');
    btn.disabled = true;
    spinner.classList.remove('d-none');
    btn.innerHTML = '<span class="spinner-border spinner-border-sm me-2" role="status"></span>Processing...';
});

// Handle checkbox behavior
const useDefaultCheckbox = document.getElementById('useDefault');
const imageInput = document.getElementById('image');

useDefaultCheckbox.addEventListener('change', function() {
    if (this.checked) {
        imageInput.disabled = true;
        imageInput.value = '';
        // Reset to default kitchen image
        updateBeforeImage('/images/kitchen.png', 'kitchen.png');
    } else {
        imageInput.disabled = false;
    }
});

// Handle image upload preview
imageInput.addEventListener('change', function(e) {
    const file = e.target.files[0];
    if (file) {
        const reader = new FileReader();
        reader.onload = function(e) {
            updateBeforeImage(e.target.result, file.name);
        };
        reader.readAsDataURL(file);
        // Uncheck default checkbox when image is uploaded
        useDefaultCheckbox.checked = false;
    }
});

function updateBeforeImage(src, name) {
    const beforeSection = document.querySelector('[data-before-section]');
    if (beforeSection) {
        const img = beforeSection.querySelector('img');
        const nameSpan = beforeSection.querySelector('strong');
        if (img) img.src = src;
        if (nameSpan) nameSpan.textContent = name;
    }
}

// Initialize state
if (useDefaultCheckbox.checked) {
    imageInput.disabled = true;
}

// Draw bounding boxes when page loads (if objects exist)
document.addEventListener('DOMContentLoaded', function() {
    setTimeout(drawBoundingBoxes, 100); // Small delay to ensure image is loaded
});

// Redraw on window resize
window.addEventListener('resize', drawBoundingBoxes);