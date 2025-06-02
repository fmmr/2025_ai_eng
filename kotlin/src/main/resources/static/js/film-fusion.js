// Film Fusion Demo JavaScript
document.addEventListener('DOMContentLoaded', function() {
    const artStyles = [
        ["Renaissance painting", "Classical Renaissance art style with rich colors and detailed composition"],
        ["Art Nouveau poster", "Flowing organic lines and decorative elements in vintage poster style"], 
        ["Film noir style", "High contrast black and white with dramatic shadows and lighting"],
        ["Minimalist design", "Clean, simple geometric shapes with limited color palette"],
        ["Impressionist painting", "Soft brushstrokes and light-focused artistic interpretation"],
        ["Comic book style", "Bold colors, dynamic action lines and comic book aesthetics"],
        ["Cyberpunk aesthetic", "Neon colors, futuristic technology and dystopian atmosphere"],
        ["Art Deco poster", "Geometric patterns, bold typography and luxurious 1920s style"],
        ["Surrealist artwork", "Dreamlike, fantastical imagery with unexpected combinations"],
        ["Pop art style", "Bright colors, bold graphics inspired by popular culture"],
        ["Gothic cathedral art", "Dark, ornate medieval styling with religious symbolism"],
        ["Japanese woodblock print", "Traditional ukiyo-e style with flat colors and flowing lines"],
        ["Abstract expressionism", "Bold, emotional brushstrokes and non-representational forms"],
        ["Steampunk design", "Victorian-era industrial machinery and brass aesthetic"],
        ["Vintage travel poster", "Classic 1950s travel advertisement styling"],
        ["Graffiti street art", "Urban spray paint style with bold colors and street culture"],
        ["Watercolor painting", "Soft, translucent colors with organic paint flow"],
        ["Soviet propaganda poster", "Bold red colors, strong worker imagery and socialist realism"]
    ];

    const dalle2Sizes = [
        ['256x256', '256×256'],
        ['512x512', '512×512'], 
        ['1024x1024', '1024×1024']
    ];
    
    const dalle3Sizes = [
        ['1024x1024', 'Square (1024×1024)'],
        ['1024x1792', 'Portrait (1024×1792)'],
        ['1792x1024', 'Landscape (1792×1024)']
    ];

    const formDataElement = document.getElementById('formData');
    const formData = formDataElement ? {
        movie: formDataElement.dataset.movie || '',
        artStyle: formDataElement.dataset.artStyle || '',
        model: formDataElement.dataset.model || 'dall-e-3',
        size: formDataElement.dataset.size || '1024x1024',
        style: formDataElement.dataset.style || 'vivid',
        quality: formDataElement.dataset.quality || 'standard'
    } : {};

    const artStyleSelect = document.getElementById('artStyle');
    if (artStyleSelect) {
        artStyleSelect.addEventListener('change', function() {
            const selectedStyle = this.value;
            const styleData = artStyles.find(style => style[0] === selectedStyle);
            const descriptionDiv = document.getElementById('artStyleDescription');
            
            if (styleData && descriptionDiv) {
                descriptionDiv.textContent = styleData[1];
                descriptionDiv.className = 'form-text small text-primary mt-1';
            } else if (descriptionDiv) {
                descriptionDiv.textContent = '';
            }
        });
    }

    function updateSizeOptions() {
        const modelSelect = document.getElementById('model');
        const sizeSelect = document.getElementById('size');
        const dalle3Options = document.getElementById('dalle3Options');
        
        if (!modelSelect || !sizeSelect || !dalle3Options) return;
        
        const selectedModel = modelSelect.value;
        const currentSelectedSize = sizeSelect.value;
        
        sizeSelect.innerHTML = '';
        
        if (selectedModel === 'dall-e-2') {
            dalle3Options.style.display = 'none';
            dalle2Sizes.forEach(size => {
                const option = document.createElement('option');
                option.value = size[0];
                option.textContent = size[1];
                if (currentSelectedSize === size[0] || 
                    (!currentSelectedSize && size[0] === '1024x1024') ||
                    (formData && formData.size === size[0])) {
                    option.selected = true;
                }
                sizeSelect.appendChild(option);
            });
        } else if (selectedModel === 'dall-e-3') {
            dalle3Options.style.display = '';
            dalle3Sizes.forEach(size => {
                const option = document.createElement('option');
                option.value = size[0];
                option.textContent = size[1];
                if (currentSelectedSize === size[0] || 
                    (!currentSelectedSize && size[0] === '1024x1024') ||
                    (formData && formData.size === size[0])) {
                    option.selected = true;
                }
                sizeSelect.appendChild(option);
            });
        }
    }

    updateSizeOptions();
    
    if (artStyleSelect && artStyleSelect.value) {
        artStyleSelect.dispatchEvent(new Event('change'));
    }

    const modelSelect = document.getElementById('model');
    if (modelSelect) {
        modelSelect.addEventListener('change', updateSizeOptions);
    }

    const filmFusionForm = document.getElementById('filmFusionForm');
    if (filmFusionForm) {
        filmFusionForm.addEventListener('submit', function() {
            const button = document.getElementById('generateBtn');
            const spinner = document.getElementById('loadingSpinner');
            const buttonText = document.getElementById('buttonText');
            
            if (button && spinner && buttonText) {
                button.disabled = true;
                spinner.classList.remove('d-none');
                buttonText.textContent = 'Generating...';
                
                setTimeout(() => {
                    if (button.disabled) {
                        buttonText.textContent = 'Creating...';
                    }
                }, 5000);
                
                setTimeout(() => {
                    if (button.disabled) {
                        buttonText.textContent = 'Almost ready...';
                    }
                }, 15000);
            }
        });
    }
});