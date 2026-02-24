import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import LoadingScreen from '../components/LoadingScreen.jsx';
import Header from '../components/Header.jsx';
import RollingBanner from '../components/RollingBanner.jsx';
import WeatherCard from '../components/WeatherCard.jsx';
import KurumiCharacter from '../components/KurumiCharacter.jsx';
import WeatherForecast from '../components/WeatherForecast.jsx';
import ReportSummaryChips from '../components/ReportSummaryChips.jsx';
import { fetchWeatherData } from '../services/weatherService.js';

// 현재 시간대 키 생성
function getCurrentKey() {
  const now = new Date();
  const year = now.getFullYear();
  const month = String(now.getMonth() + 1).padStart(2, '0');
  const day = String(now.getDate()).padStart(2, '0');
  const hours = String(now.getHours()).padStart(2, '0');
  return `${year}${month}${day}.${hours}00`;
}

function MainPage() {
  const [allWeatherData, setAllWeatherData] = useState(null);
  const [reports, setReports] = useState(null);
  const [notices, setNotices] = useState([]);
  const navigate = useNavigate();

  useEffect(() => {
    // 날씨 데이터 (Open-Meteo 오픈데이터)
    fetchWeatherData()
      .then((data) => setAllWeatherData(data))
      .catch(() => console.error('날씨 API 연결 실패'));

    // 제보 데이터
    fetch('/api/reports')
      .then((r) => r.json())
      .then((data) => setReports(data))
      .catch(() => console.error('제보 API 연결 실패'));

    // 공지사항
    fetch('/api/notices')
      .then((r) => r.json())
      .then((data) => setNotices(data))
      .catch(() => console.error('공지사항 API 연결 실패'));

    // PC 접속 알림 (원본 동작 유지)
    const isMobile = /Android|iPhone|iPad|iPod|BlackBerry|Windows Phone/i.test(navigator.userAgent);
    if (!isMobile) {
      alert('쿠름이는 모바일 접속을 권장합니다!');
    }
  }, []);

  const currentKey = getCurrentKey();
  const currentWeather = allWeatherData ? allWeatherData[currentKey] : null;

  return (
    <>
      <LoadingScreen />

      <div id="back">
        <Header />
        <RollingBanner notices={notices} />

        {/* 현재 날씨 카드 (온도, 강수량, 습도, 쿠름이) */}
        <div className="text-box">
          <WeatherCard weatherData={currentWeather} />
          <KurumiCharacter reports={reports} />
        </div>
      </div>

      {/* 시간별 예보 카드 */}
      <WeatherForecast allWeatherData={allWeatherData} />
      <ReportSummaryChips reports={reports} />

      {/* 라이브리 댓글 */}
      <div id="lv-container" data-id="city" data-uid="MTAyMC81NzM4OC8zMzg1Mg==">
        <noscript>라이브리 댓글 작성을 위해 JavaScript를 활성화 해주세요</noscript>
      </div>

      <div className="padding" />
      <div className="foot">
        <button id="weather-tip" type="button" onClick={() => navigate('/tip')}>
          <span className="material-icons-tip">notifications</span>
          <span className="text-tip">실시간 회대 날씨 제보하러가기</span>
        </button>
      </div>
    </>
  );
}

export default MainPage;
