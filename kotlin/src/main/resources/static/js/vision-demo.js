// Vision Demo JavaScript
document.addEventListener('DOMContentLoaded', function() {
    // Image data with suggested prompts
    const imageData = {
        'magnus.png': [
            "Analyze the chess position on the board",
            "Describe the tournament setting and atmosphere", 
            "What can you tell me about the players and their setup?",
            "Identify any sponsors or tournament details visible"
        ],
        'sushi.png': [
            "Identify each type of fish and seafood in this platter",
            "Assess the freshness and quality of the sashimi",
            "Describe the presentation and traditional elements",
            "What would you recommend trying first?"
        ],
        'briller.png': [
            "Extract all the prescription details from this form",
            "Read and transcribe all visible text",
            "What type of vision correction is prescribed?",
            "Identify the optometrist or clinic information"
        ],
        'kompis.png': [
            "What breed is this dog and describe its characteristics?",
            "Describe the winter scene and weather conditions",
            "What is the dog's mood and behavior in this photo?",
            "Analyze the photographic composition and lighting"
        ]
    };

    // Get form data from data attributes
    const formDataElement = document.getElementById('formData');
    const formData = formDataElement ? {
        selectedImage: formDataElement.dataset.selectedImage || '',
        prompt: formDataElement.dataset.prompt || '',
        model: formDataElement.dataset.model || 'gpt-4o', // Models.Defaults.VISION_ANALYSIS
        detail: formDataElement.dataset.detail || 'auto'
    } : {};

    const selectedImageInput = document.getElementById('selectedImage');
    const suggestedPromptsDiv = document.getElementById('suggestedPrompts');
    const promptSuggestionsDiv = document.getElementById('promptSuggestions');
    const promptTextarea = document.getElementById('prompt');

    // Handle image selection
    document.querySelectorAll('.image-selection-card').forEach(card => {
        card.addEventListener('click', function() {
            const filename = this.dataset.filename;
            
            // Remove previous selection
            document.querySelectorAll('.image-selection-card .card').forEach(c => {
                c.classList.remove('border-primary', 'border-3');
                c.classList.add('border-2');
            });
            
            // Add selection to clicked card
            const cardElement = this.querySelector('.card');
            cardElement.classList.add('border-primary', 'border-3');
            cardElement.classList.remove('border-2');
            
            // Update hidden input
            selectedImageInput.value = filename;
            
            // Show suggested prompts
            showSuggestedPrompts(filename);
        });
    });

    function showSuggestedPrompts(filename) {
        const prompts = imageData[filename] || [];
        
        if (prompts.length > 0) {
            // Clear previous suggestions
            promptSuggestionsDiv.innerHTML = '';
            
            // Add suggestion buttons
            prompts.forEach(promptText => {
                const button = document.createElement('button');
                button.type = 'button';
                button.className = 'btn btn-outline-primary btn-sm';
                button.textContent = promptText;
                button.addEventListener('click', function() {
                    promptTextarea.value = promptText;
                    promptTextarea.focus();
                });
                promptSuggestionsDiv.appendChild(button);
            });
            
            suggestedPromptsDiv.style.display = 'block';
        } else {
            suggestedPromptsDiv.style.display = 'none';
        }
    }

    // Restore form state if available
    if (formData.selectedImage) {
        const card = document.querySelector(`[data-filename="${formData.selectedImage}"]`);
        if (card) {
            card.click();
        }
    }

    // Form submission with loading states
    const visionForm = document.getElementById('visionForm');
    if (visionForm) {
        visionForm.addEventListener('submit', function(e) {
            if (!selectedImageInput.value) {
                e.preventDefault();
                alert('Please select an image to analyze first!');
                return;
            }
            
            const button = document.getElementById('analyzeBtn');
            const spinner = document.getElementById('loadingSpinner');
            const buttonText = document.getElementById('buttonText');
            
            if (button && spinner && buttonText) {
                button.disabled = true;
                spinner.classList.remove('d-none');
                buttonText.textContent = 'Analyzing...';
                
                // Show additional loading messages
                setTimeout(() => {
                    if (button.disabled) {
                        buttonText.textContent = 'Processing image...';
                    }
                }, 3000);
                
                setTimeout(() => {
                    if (button.disabled) {
                        buttonText.textContent = 'Almost ready...';
                    }
                }, 10000);
            }
        });
    }
});