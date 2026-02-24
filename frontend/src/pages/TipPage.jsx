import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Header from '../components/Header.jsx';
import WeatherReportButtons from '../components/WeatherReportButtons.jsx';

function TipPage() {
  const [counts, setCounts] = useState(null);
  const navigate = useNavigate();

  function loadCounts() {
    fetch('/api/reports')
      .then((r) => r.json())
      .then((data) => setCounts(data))
      .catch(() => console.error('제보 데이터 로드 실패'));
  }

  useEffect(() => {
    loadCounts();
  }, []);

  return (
    <>
      <div id="header">
        <Header />
      </div>

      <WeatherReportButtons counts={counts} onReportSuccess={loadCounts} />

      {/* 동아리 모집 링크 */}
      <a href="https://everytime.kr/367581" target="_blank" rel="noreferrer">
        <img
          src="/img/dongari.png"
          width="92%"
          style={{ textAlign: 'center', margin: 'auto', display: 'block', paddingBottom: '50px' }}
          alt="동아리 모집"
        />
      </a>

      <div className="padding2" />
      <div className="foot2">
        <button id="weather-tip2" type="button" onClick={() => navigate('/')}>
          <span className="material-icons-back">fast_rewind</span>
          <span className="text-tip">성공회대 날씨 정보로 돌아가기</span>
        </button>
      </div>
    </>
  );
}

export default TipPage;
