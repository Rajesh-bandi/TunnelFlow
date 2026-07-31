import { useState, useEffect } from "react";

export function LightboxModal({ media, onClose }) {
  useEffect(() => {
    const handleKeyDown = (e) => {
      if (e.key === "Escape") onClose();
    };
    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, [onClose]);

  if (!media) return null;

  return (
    <div className="lightbox-overlay" onClick={onClose}>
      <div className="lightbox-content" onClick={(e) => e.stopPropagation()}>
        <div className="lightbox-header">
          <span className="lightbox-title">{media.alt || media.caption || "Expanded Media View"}</span>
          <button className="lightbox-close-btn" onClick={onClose} title="Close (Esc)">
            ✕
          </button>
        </div>

        <div className="lightbox-media-container">
          {media.isVideo ? (
            <video
              src={media.src}
              autoPlay
              loop
              controls
              playsInline
              className="lightbox-media-element"
            />
          ) : (
            <img src={media.src} alt={media.alt || "Expanded View"} className="lightbox-media-element" />
          )}
        </div>

        {media.caption && <div className="lightbox-caption">{media.caption}</div>}
      </div>
    </div>
  );
}

export function MediaViewer({ src, alt, caption, isVideo = false }) {
  const [isOpen, setIsOpen] = useState(false);

  return (
    <>
      <div
        className="media-frame clickable-media"
        onClick={() => setIsOpen(true)}
        title="Click to view full screen"
      >
        <div className="expand-hover-badge">🔍 Click for Big View</div>
        {isVideo ? (
          <video
            src={src}
            autoPlay
            loop
            muted
            playsInline
            controls
            className="media-video-element"
          />
        ) : (
          <img src={src} alt={alt || caption} className="media-image-element" loading="lazy" />
        )}
        {caption && <div className="media-caption">{caption}</div>}
      </div>

      {isOpen && (
        <LightboxModal
          media={{ src, alt, caption, isVideo }}
          onClose={() => setIsOpen(false)}
        />
      )}
    </>
  );
}

export function CarouselViewer({ items, caption }) {
  const [currentIndex, setCurrentIndex] = useState(0);
  const [lightboxMedia, setLightboxMedia] = useState(null);

  useEffect(() => {
    const timer = setInterval(() => {
      setCurrentIndex((prev) => (prev + 1) % items.length);
    }, 4500);
    return () => clearInterval(timer);
  }, [items.length]);

  const prevSlide = (e) => {
    e.stopPropagation();
    setCurrentIndex((prev) => (prev - 1 + items.length) % items.length);
  };

  const nextSlide = (e) => {
    e.stopPropagation();
    setCurrentIndex((prev) => (prev + 1) % items.length);
  };

  return (
    <>
      <div className="carousel-container">
        <div className="carousel-viewport clickable-media" title="Click to view full screen">
          <div className="expand-hover-badge">🔍 Click for Big View</div>
          {items.map((item, idx) => (
            <div
              key={idx}
              className={`carousel-slide ${idx === currentIndex ? "active" : ""}`}
              style={{ opacity: idx === currentIndex ? 1 : 0, transition: "opacity 0.4s ease-in-out" }}
              onClick={() => setLightboxMedia(item)}
            >
              {item.isVideo ? (
                <video
                  src={item.src}
                  autoPlay
                  loop
                  muted
                  playsInline
                  controls
                  className="media-video-element"
                />
              ) : (
                <img src={item.src} alt={item.alt || `Slide ${idx + 1}`} className="media-image-element" />
              )}
            </div>
          ))}

          {/* Carousel Navigation Arrows */}
          {items.length > 1 && (
            <>
              <button className="carousel-arrow carousel-arrow-left" onClick={prevSlide} aria-label="Previous Slide">
                ❮
              </button>
              <button className="carousel-arrow carousel-arrow-right" onClick={nextSlide} aria-label="Next Slide">
                ❯
              </button>
            </>
          )}

          {/* Slide Counter Badge */}
          <div className="carousel-counter-chip">
            {currentIndex + 1} / {items.length}
          </div>
        </div>

        {/* Carousel Dot Indicators */}
        {items.length > 1 && (
          <div className="carousel-dots">
            {items.map((_, idx) => (
              <button
                key={idx}
                className={`carousel-dot ${idx === currentIndex ? "active" : ""}`}
                onClick={(e) => {
                  e.stopPropagation();
                  setCurrentIndex(idx);
                }}
                aria-label={`Go to slide ${idx + 1}`}
              />
            ))}
          </div>
        )}

        {caption && <div className="media-caption">{caption}</div>}
      </div>

      {lightboxMedia && (
        <LightboxModal
          media={lightboxMedia}
          onClose={() => setLightboxMedia(null)}
        />
      )}
    </>
  );
}
