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
      const res = await fetch('/api/reports', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
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
