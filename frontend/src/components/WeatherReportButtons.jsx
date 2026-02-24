
const WEATHER_TYPES = [
  { key: 'rainy',  label: '비가 와요',     img: '/img/report/report_rainy.png',  type: 0 },
  { key: 'cloudy', label: '흐려요',         img: '/img/report/report_cloudy.png', type: 1 },
  { key: 'sunny',  label: '화창해요',       img: '/img/report/report_sunny.png',  type: 2 },
  { key: 'dusty',  label: '공기가 나빠요',  img: '/img/report/report_dust.png',   type: 3 },
  { key: 'windy',  label: '바람이 불어요',  img: '/img/report/report_windy.png',  type: 4 },
  { key: 'snowy',  label: '눈이 와요',      img: '/img/report/report_snow.png',   type: 5 },
];

// 기기별 고정 sessionId (localStorage에 저장)
function getSessionId() {
  let id = localStorage.getItem('weatherSessionId');
  if (!id) {
    id = crypto.randomUUID();
    localStorage.setItem('weatherSessionId', id);
  }
  return id;
}

// 마지막 제보 시각 저장/조회
function getLastReportTime() {
  const saved = localStorage.getItem('lastReportTime');
  return saved ? parseInt(saved, 10) : null;
}

function setLastReportTime() {
  localStorage.setItem('lastReportTime', Date.now().toString());
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

function WeatherReportButtons({ counts, onReportSuccess }) {
  async function handleReport(weatherType) {
    const remaining = getRemainingTime();
    if (remaining) {
      alert(`제보는 3시간에 1번만 가능합니다.\n${remaining} 후에 다시 제보해주세요.`);
      return;
    }

    const sessionId = getSessionId();

    try {
      const res = await fetch('/api/reports', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ sessionId, weatherType }),
      });

      const data = await res.json();

      if (res.ok) {
        setLastReportTime();
        alert('제보 완료!');
        onReportSuccess?.();
      } else {
        alert(data.message || '제보에 실패했습니다.');
      }
    } catch (e) {
      alert('서버에 연결할 수 없습니다.');
    }
  }

  return (
    <div className="icon-box">
      <h2 id="give_tip">현재 회대의 날씨를 제보해주세요!</h2>
      <div className="turtle-sit">
        {WEATHER_TYPES.map(({ key, label, img, type }) => (
          <li key={key} className="turtle-feel">
            <button className="report-btn" onClick={() => handleReport(type)}>
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
