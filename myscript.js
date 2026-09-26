        const startNowBtn = document.getElementById('startNowBtn');
        const popupOverlay = document.getElementById('popupOverlay');
        const closePopupBtn = document.getElementById('closePopupBtn');

        function openPopup() {
            popupOverlay.classList.remove('hidden');
            document.body.classList.add('modal-open');
        }

        function closePopup() {
            popupOverlay.classList.add('hidden');
            document.body.classList.remove('modal-open');
        }

        startNowBtn.addEventListener('click', openPopup);
        closePopupBtn.addEventListener('click', closePopup);

        popupOverlay.addEventListener('click', function (event) {
            if (event.target === popupOverlay) {
                closePopup();
            }
        });

        document.addEventListener('keydown', function (event) {
            if (event.key === 'Escape' && !popupOverlay.classList.contains('hidden')) {
                closePopup();
            }
        });