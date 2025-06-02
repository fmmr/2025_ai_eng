// Image Editing Demo JavaScript
document.addEventListener('DOMContentLoaded', function() {
    const formDataElement = document.getElementById('formData');
    const formData = formDataElement ? {
        prompt: formDataElement.dataset.prompt || '',
        size: formDataElement.dataset.size || '1024x1024'
    } : {};

    if (formData.prompt) {
        const promptTextarea = document.getElementById('prompt');
        if (promptTextarea && !promptTextarea.value) {
            promptTextarea.value = formData.prompt;
        }
    }

    if (formData.size) {
        const sizeSelect = document.getElementById('size');
        if (sizeSelect) {
            sizeSelect.value = formData.size;
        }
    }

    const imageEditForm = document.getElementById('imageEditForm');
    if (imageEditForm) {
        imageEditForm.addEventListener('submit', function(e) {
            const promptTextarea = document.getElementById('prompt');
            
            if (!promptTextarea.value.trim()) {
                e.preventDefault();
                alert('Please enter a description of what you want to add to the image!');
                promptTextarea.focus();
                return;
            }
            
            const button = document.getElementById('editBtn');
            const spinner = document.getElementById('loadingSpinner');
            const buttonText = document.getElementById('buttonText');
            const progressContainer = document.getElementById('progressContainer');
            const progressBar = document.getElementById('progressBar');
            const progressText = document.getElementById('progressText');
            
            if (button && spinner && buttonText) {
                button.disabled = true;
                spinner.classList.remove('d-none');
                buttonText.textContent = 'Processing...';
                
                if (progressContainer) {
                    progressContainer.classList.remove('d-none');
                }
                
                const steps = [
                    { progress: 20, text: 'Uploading images to DALL-E 2...', delay: 1000 },
                    { progress: 40, text: 'Analyzing original image...', delay: 3000 },
                    { progress: 60, text: 'Processing mask area...', delay: 6000 },
                    { progress: 80, text: 'Applying your prompt...', delay: 12000 },
                    { progress: 95, text: 'Finalizing edited image...', delay: 20000 }
                ];
                
                steps.forEach((step, index) => {
                    setTimeout(() => {
                        if (button.disabled) {
                            if (progressBar) progressBar.style.width = step.progress + '%';
                            if (progressText) progressText.textContent = step.text;
                            buttonText.textContent = step.text;
                        }
                    }, step.delay);
                });
                
                setTimeout(() => {
                    if (button.disabled) {
                        if (progressBar) progressBar.style.width = '99%';
                        if (progressText) progressText.textContent = 'Almost done... DALL-E 2 is finishing up!';
                        buttonText.textContent = 'Almost done...';
                    }
                }, 30000);
            }
        });
    }

    const promptTextarea = document.getElementById('prompt');
    if (promptTextarea) {
        const examplePrompts = [
            "A hot air balloon floating in the sky",
            "A flock of birds flying south for winter",
            "A ski jumper soaring through the air",
            "A small airplane with winter landing skis",
            "Northern lights dancing across the sky",
            "A paraglider with bright colorful wings",
            "A vintage biplane doing aerial stunts",
            "A group of migrating geese in V formation",
            "A weather balloon with scientific instruments",
            "A drone capturing aerial footage of the landscape"
        ];

        promptTextarea.addEventListener('focus', function() {
            if (!this.value.trim() || this.value === formData.prompt) {
                showPromptSuggestions(this, examplePrompts);
            }
        });
    }

    function showPromptSuggestions(textarea, suggestions) {
        const existingSuggestions = document.getElementById('promptSuggestions');
        if (existingSuggestions) {
            existingSuggestions.remove();
        }

        const suggestionsDiv = document.createElement('div');
        suggestionsDiv.id = 'promptSuggestions';
        suggestionsDiv.className = 'position-absolute bg-white border rounded shadow-sm p-2 mt-1';
        suggestionsDiv.style.cssText = 'z-index: 1000; max-height: 200px; overflow-y: auto; width: 100%;';

        const title = document.createElement('small');
        title.className = 'text-muted fw-bold';
        title.textContent = '💡 Example prompts (click to use):';
        suggestionsDiv.appendChild(title);

        suggestions.slice(0, 5).forEach(prompt => {
            const promptBtn = document.createElement('div');
            promptBtn.className = 'small text-primary cursor-pointer p-1 hover-bg-light';
            promptBtn.style.cursor = 'pointer';
            promptBtn.textContent = prompt;
            
            promptBtn.addEventListener('click', function() {
                textarea.value = prompt;
                suggestionsDiv.remove();
                textarea.focus();
            });
            
            promptBtn.addEventListener('mouseenter', function() {
                this.style.backgroundColor = '#f8f9fa';
            });
            
            promptBtn.addEventListener('mouseleave', function() {
                this.style.backgroundColor = '';
            });
            
            suggestionsDiv.appendChild(promptBtn);
        });

        textarea.parentNode.style.position = 'relative';
        textarea.parentNode.appendChild(suggestionsDiv);

        setTimeout(() => {
            document.addEventListener('click', function(e) {
                if (!suggestionsDiv.contains(e.target) && e.target !== textarea) {
                    suggestionsDiv.remove();
                }
            }, { once: true });
        }, 100);
    }
});