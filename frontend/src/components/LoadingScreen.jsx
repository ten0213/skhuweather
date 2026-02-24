import { useEffect, useState } from 'react';

function LoadingScreen() {
  const [fadeOut, setFadeOut] = useState(false);
  const [hidden, setHidden] = useState(false);

  useEffect(() => {
    const fadeTimer = setTimeout(() => setFadeOut(true), 1800);
    const hideTimer = setTimeout(() => setHidden(true), 2300);
    return () => {
      clearTimeout(fadeTimer);
      clearTimeout(hideTimer);
    };
  }, []);

  if (hidden) return null;

  return (
    <div id="load" className={fadeOut ? 'fade-out' : ''}>
      <img src="/img/splash screen.png" alt="loading" />
    </div>
  );
}

export default LoadingScreen;
