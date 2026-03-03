import { useState, useEffect } from 'react';

// React 리렌더링·StrictMode 더블마운트와 무관하게 동작하는 모듈 단위 플래그
let globalIsSubmitting = false;
const SESSION_KEY = 'lastReportAt';
const BLOCK_MS = 10_000; // 백엔드 IP 단기 제한(10초)과 동일

const WEATHER_TYPES = [
  { key: 'rainy',  label: '비가 와요',     img: '/img/report/report_rainy.png',  type: 0 },
  { key: 'cloudy', label: '흐려요',         img: '/img/report/report_cloudy.png', type: 1 },
  { key: 'sunny',  label: '화창해요',       img: '/img/report/report_sunny.png',  type: 2 },
  { key: 'dusty',  label: '공기가 나빠요',  img: '/img/report/report_dust.png',   type: 3 },
  { key: 'windy',  label: '바람이 불어요',  img: '/img/report/report_windy.png',  type: 4 },
  { key: 'snowy',  label: '눈이 와요',      img: '/img/report/report_snow.png',   type: 5 },
];

<<<<<<< HEAD
function setCookie(name, value, days) {
  const d = new Date();
  d.setTime(d.getTime() + days * 24 * 60 * 60 * 1000);
  document.cookie = `${name}=${encodeURIComponent(value)};expires=${d.toUTCString()};path=/;SameSite=Strict`;
}

function getCookie(name) {
  const match = document.cookie.match(new RegExp('(?:^|; )' + name + '=([^;]*)'));
  return match ? decodeURIComponent(match[1]) : null;
}

// 기기별 고정 sessionId (localStorage + cookie 이중 저장)
function getSessionId() {
  let id = localStorage.getItem('weatherSessionId');
  if (!id) {
    id = getCookie('weatherSessionId');
    if (id) {
      localStorage.setItem('weatherSessionId', id);
    }
  }
  if (!id) {
    id = crypto.randomUUID();
    localStorage.setItem('weatherSessionId', id);
  }
  setCookie('weatherSessionId', id, 365);
  return id;
}

// 마지막 제보 시각 저장/조회
function getLastReportTime() {
  let saved = localStorage.getItem('lastReportTime');
  if (!saved) {
    saved = getCookie('lastReportTime');
    if (saved) {
      localStorage.setItem('lastReportTime', saved);
    }
  }
  return saved ? parseInt(saved, 10) : null;
}

function setLastReportTime() {
  const now = Date.now().toString();
  localStorage.setItem('lastReportTime', now);
  setCookie('lastReportTime', now, 1);
}

// 남은 제한 시간을 "X시간 Y분" 형태로 반환, null이면 제한 없음
function getRemainingTime() {
  const last = getLastReportTime();
  if (!last) return null;
  const elapsed = Date.now() - last;
  const THREE_HOURS = 3 * 60 * 60 * 1000;
  if (elapsed >= THREE_HOURS) return null;
  const remaining = THREE_HOURS - elapsed;
  const minutes = Math.ceil(remaining / 60000);
  if (minutes >= 60) return `${Math.floor(minutes / 60)}시간 ${minutes % 60}분`;
  return `${minutes}분`;
}

=======
>>>>>>> 8a9cd08a9d3b6ccb6869711738dc61ea117d9c03
function WeatherReportButtons({ counts, onReportSuccess }) {
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [statusMsg, setStatusMsg] = useState('');

  // 새로고침 후에도 30초 차단 윈도우 복원
  useEffect(() => {
    const lastAt = Number(sessionStorage.getItem(SESSION_KEY) || 0);
    const remaining = BLOCK_MS - (Date.now() - lastAt);
    if (remaining > 0) {
      globalIsSubmitting = true;
      setIsSubmitting(true);
      const timer = setTimeout(() => {
        globalIsSubmitting = false;
        setIsSubmitting(false);
      }, remaining);
      return () => clearTimeout(timer);
    }
  }, []);

  async function handleReport(weatherType) {
    if (globalIsSubmitting) return;
    globalIsSubmitting = true;
    setIsSubmitting(true);
    setStatusMsg('');
    try {
      const res = await fetch('/api/reports', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify({ weatherType }),
      });

      const data = await res.json();

      if (res.ok) {
        sessionStorage.setItem(SESSION_KEY, Date.now().toString());
        setStatusMsg('제보 완료!');
        onReportSuccess?.();
      } else if (res.status !== 429) {
        setStatusMsg(data.message || '제보에 실패했습니다.');
      }
    } catch (e) {
      setStatusMsg('서버에 연결할 수 없습니다.');
    } finally {
      const lastAt = Number(sessionStorage.getItem(SESSION_KEY) || 0);
      const remaining = BLOCK_MS - (Date.now() - lastAt);
      if (remaining > 0) {
        // 제보 성공: 30초 윈도우가 끝날 때 재활성화, 2초 후 메시지 제거
        setTimeout(() => { globalIsSubmitting = false; setIsSubmitting(false); }, remaining);
        setTimeout(() => setStatusMsg(''), 2000);
      } else {
        // 제보 실패: 2초 후 재활성화
        setTimeout(() => { globalIsSubmitting = false; setIsSubmitting(false); setStatusMsg(''); }, 2000);
      }
    }
  }

  return (
    <div className="icon-box">
      <h2 id="give_tip">현재 회대의 날씨를 제보해주세요!</h2>
      {statusMsg && (
        <p style={{ textAlign: 'center', fontWeight: 'bold', margin: '8px 0' }}>
          {statusMsg}
        </p>
      )}
      <div className="turtle-sit">
        {WEATHER_TYPES.map(({ key, label, img, type }) => (
          <li key={key} className="turtle-feel">
            <button className="report-btn" onClick={() => handleReport(type)} disabled={isSubmitting}>
              <img src={img} style={{ width: '150px' }} alt={label} />
              <span>{counts?.[key] ?? 0}</span>
            </button>
            <br />
            {label}
          </li>
        ))}
      </div>
    </div>
  );
}

export default WeatherReportButtons;
