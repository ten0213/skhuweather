const CLIENT_ID_STORAGE_KEY = 'skhuweather_report_client_id';
const CLIENT_ID_REGEX = /^[A-Za-z0-9._-]+$/;
const MAX_CLIENT_ID_LENGTH = 128;

function isValidClientId(value) {
  return (
    typeof value === 'string' &&
    value.length > 0 &&
    value.length <= MAX_CLIENT_ID_LENGTH &&
    CLIENT_ID_REGEX.test(value)
  );
}

function createClientId() {
  if (typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function') {
    return crypto.randomUUID();
  }
  return `${Date.now()}-${Math.random().toString(16).slice(2)}`;
}

function getClientId() {
  try {
    const saved = localStorage.getItem(CLIENT_ID_STORAGE_KEY);
    if (isValidClientId(saved)) {
      return saved;
    }

    const next = createClientId();
    if (!isValidClientId(next)) {
      return null;
    }

    localStorage.setItem(CLIENT_ID_STORAGE_KEY, next);
    return next;
  } catch {
    return null;
  }
}

const WEATHER_TYPES = [
  { key: 'rainy',  label: '비가 와요',     img: '/img/report/report_rainy.png',  type: 0 },
  { key: 'cloudy', label: '흐려요',         img: '/img/report/report_cloudy.png', type: 1 },
  { key: 'sunny',  label: '화창해요',       img: '/img/report/report_sunny.png',  type: 2 },
  { key: 'dusty',  label: '공기가 나빠요',  img: '/img/report/report_dust.png',   type: 3 },
  { key: 'windy',  label: '바람이 불어요',  img: '/img/report/report_windy.png',  type: 4 },
  { key: 'snowy',  label: '눈이 와요',      img: '/img/report/report_snow.png',   type: 5 },
];

function WeatherReportButtons({ counts, onReportSuccess }) {
  async function handleReport(weatherType) {
    try {
      const clientId = getClientId();
      const headers = { 'Content-Type': 'application/json' };
      if (clientId) {
        headers['X-Client-Id'] = clientId;
      }

      const res = await fetch('/api/reports', {
        method: 'POST',
        headers,
        credentials: 'include',
        body: JSON.stringify({ weatherType }),
      });

      const data = await res.json();

      if (res.ok) {
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
