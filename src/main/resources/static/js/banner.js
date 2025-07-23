document.addEventListener("DOMContentLoaded", function () {
    const track = document.querySelector('.slider-track');
    const prevBtn = document.querySelector('.prev');
    const nextBtn = document.querySelector('.next');
    let currentSlide = 0;
    const totalSlides = document.querySelectorAll('.slide').length;

    nextBtn.addEventListener('click', () => {
        if (currentSlide < totalSlides - 1) {
            currentSlide++;
            track.style.transform = `translateX(-${currentSlide * 100}%)`;
        }
    });

    prevBtn.addEventListener('click', () => {
        if (currentSlide > 0) {
            currentSlide--;
            track.style.transform = `translateX(-${currentSlide * 100}%)`;
        }
    });
});
