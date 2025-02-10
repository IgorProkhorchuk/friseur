document.addEventListener("DOMContentLoaded", function () {
    class SwipeUpNavigation {
        constructor(options = {}) {
            this.threshold = options.threshold || 100;
            this.sensitivity = options.sensitivity || 10;
            this.bookPageUrl = options.bookPageUrl || '/home';
            this.startY = 0;
            this.isSwiping = false;

            this.handleTouchStart = this.handleTouchStart.bind(this);
            this.handleTouchMove = this.handleTouchMove.bind(this);
            this.handleTouchEnd = this.handleTouchEnd.bind(this);

            this.createTransitionOverlay();
            this.init();
        }

        createTransitionOverlay() {
            this.overlay = document.createElement('div');
            this.overlay.style.position = 'fixed';
            this.overlay.style.top = '0';
            this.overlay.style.left = '0';
            this.overlay.style.width = '100%';
            this.overlay.style.height = '100%';
            this.overlay.style.backgroundColor = 'white';
            this.overlay.style.transform = 'translateY(100%)';
            this.overlay.style.transition = 'transform 0.3s ease-out';
            this.overlay.style.zIndex = '9999';
            this.overlay.style.display = 'none';
            document.body.appendChild(this.overlay);
        }

        init() {
            document.addEventListener('touchstart', this.handleTouchStart, { passive: true });
            document.addEventListener('touchmove', this.handleTouchMove, { passive: false });
            document.addEventListener('touchend', this.handleTouchEnd, { passive: true });
        }

        handleTouchStart(event) {
            this.startY = event.touches[0].clientY;
            this.isSwiping = false;
        }

        handleTouchMove(event) {
            const currentY = event.touches[0].clientY;
            const deltaY = this.startY - currentY;

            if (deltaY > this.sensitivity) {
                this.isSwiping = true;
                event.preventDefault();
            }
        }

        handleTouchEnd(event) {
            if (!this.isSwiping) return;

            const currentY = event.changedTouches[0].clientY;
            const deltaY = this.startY - currentY;

            if (deltaY > this.threshold) {
                this.navigateToBookPage();
            }

            this.isSwiping = false;
        }

        navigateToBookPage() {
            this.overlay.style.display = 'block';
            this.overlay.offsetHeight; // Force reflow
            this.overlay.style.transform = 'translateY(0)';

            setTimeout(() => {
                window.location.href = this.bookPageUrl;
            }, 300);
        }

        destroy() {
            document.removeEventListener('touchstart', this.handleTouchStart);
            document.removeEventListener('touchmove', this.handleTouchMove);
            document.removeEventListener('touchend', this.handleTouchEnd);

            if (this.overlay && this.overlay.parentNode) {
                this.overlay.parentNode.removeChild(this.overlay);
            }
        }
    }

    const swipeUpNav = new SwipeUpNavigation({
        threshold: 100,
        sensitivity: 50,
        bookPageUrl: '/home'
    });
});
