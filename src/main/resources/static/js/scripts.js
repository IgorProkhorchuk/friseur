// JavaScript to display the correct language text
document.addEventListener("DOMContentLoaded", function () {
    const userLang = navigator.language || navigator.userLanguage; // Detect browser language
    const langCode = userLang.substring(0, 2); // Get the first two characters, e.g., "en"

    // Select all spans within the .coming-soon-text paragraph
    const texts = document.querySelectorAll(".coming-soon-text span");

    if (!texts.length) {
        return;
    }

    texts.forEach(span => {
        // Display the span if its lang attribute matches the detected language
        if (span.getAttribute("lang") === langCode) {
            span.style.display = "inline";
        }
    });

    // Fallback: if no matching language found, show English as default
    if (![...texts].some(span => span.style.display === "inline")) {
        const fallback = document.querySelector(".coming-soon-text span[lang='en']");
        if (fallback) {
            fallback.style.display = "inline";
        }
    }
});
