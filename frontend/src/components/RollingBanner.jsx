import { useEffect, useRef } from 'react';

const LMS_BASE = 'https://lms.skhu.ac.kr/ilos/community/notice_view_form.acl?ARTL_NUM=';

function RollingBanner({ notices }) {
  const listRef = useRef(null);

  useEffect(() => {
    if (!notices || notices.length < 3) return;

    // 초기 클래스 세팅
    const items = listRef.current?.querySelectorAll('li');
    if (!items || items.length === 0) return;
    items[0].className = 'prev';
    items[1].className = 'current';
    items[2].className = 'next';

    const interval = setInterval(() => {
      const ul = listRef.current;
      if (!ul) return;

      const prev    = ul.querySelector('.prev');
      const current = ul.querySelector('.current');
      const next    = ul.querySelector('.next');

      if (!current || !next) return;

      if (prev) prev.className = '';

      current.className = 'prev';

      const afterNext = next.nextElementSibling;
      if (afterNext) {
        afterNext.className = 'next';
      } else {
        ul.querySelector('li:first-child').className = 'next';
      }

      next.className = 'current';
    }, 3000);

    return () => clearInterval(interval);
  }, [notices]);

  return (
    <div className="rollingbanner">
      <span className="banner-icon material-icons">campaign</span>
      <div className="wrap">
        <ul ref={listRef}>
          {notices.map((notice, i) => (
            <li key={notice.id || i}>
              <a
                href={LMS_BASE + notice.noticeNum}
                target="_blank"
                rel="noreferrer"
              >
                {notice.title}
              </a>
            </li>
          ))}
        </ul>
      </div>
    </div>
  );
}

export default RollingBanner;
