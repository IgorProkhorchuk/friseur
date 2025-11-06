    function toggleAccordion(button) {
        const allContents = document.querySelectorAll('.accordion-content');
        const content = button.nextElementSibling;

        allContents.forEach(c => {
            if (c !== content) {
                c.style.display = 'none';
                c.previousElementSibling.classList.remove('active');
            }
        });

        const isVisible = content.style.display === 'block';
        content.style.display = isVisible ? 'none' : 'block';
        button.classList.toggle('active', !isVisible);
    }

    document.addEventListener('DOMContentLoaded', () => {
        document.querySelectorAll('.accordion-content').forEach(c => c.style.display = 'none');
    });