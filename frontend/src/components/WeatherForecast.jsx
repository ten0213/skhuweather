// SKY+PTY 코드 → 아이콘 이름 매핑
const ICON_MAP = {
  10: 'ksun', 11: 'ksun',
  41: 'cloudyRain2', 42: 'cloudyRain2', 45: 'cloudyRain2',
  31: 'krain', 32: 'krain', 35: 'krain',
  30: 'kcloudy', 40: 'cloudy2',
  33: 'ksnow', 36: 'ksnow', 37: 'ksnow',
  43: 'cloudySnow2', 46: 'cloudySnow2', 47: 'cloudySnow2',
};

// 시간대 키 계산 (자정 경계 처리 포함)
function makeTimeKeys() {
  const now = new Date();
  const year = now.getFullYear();
  const month = String(now.getMonth() + 1).padStart(2, '0');
  const baseDay = now.getDate();
  const baseHour = now.getHours();

  function makeKey(offsetHour) {
    let h = baseHour + offsetHour;
    let d = baseDay;
    if (h >= 24) {
      h -= 24;
      d += 1;
    }
    return `${year}${month}${String(d).padStart(2, '0')}.${String(h).padStart(2, '0')}00`;
  }

  return [makeKey(1), makeKey(2), makeKey(3)];
}

function displayHour(offset) {
  const h = new Date().getHours() + offset;
  if (h === 24) return '00시';
  if (h === 25) return '01시';
  if (h === 26) return '02시';
  return `${h}시`;
}

function ForecastItem({ allData, timeKey, label }) {
  if (!allData || !allData[timeKey]) {
    return (
      <div className="tmwt">
        <span className="time">{label}</span>
        <span className="weatherimg weatherimg-empty" />
        <span className="tem">-</span>
        <span className="precipitation">강수량(mm): -</span>
      </div>
    );
  }

  const { T1H, PTY, SKY, RN1 } = allData[timeKey];
  const code = Number(`${SKY}${PTY}`);
  const iconName = ICON_MAP[code] || 'ksun';

  return (
    <div className="tmwt">
      <span className="time">{label}</span>
      <span className="weatherimg">
        <img className="weather-kicon forecast-kicon" src={`/img/icon/${iconName}.png`} alt="날씨" />
      </span>
      <span className="tem">{T1H}도</span>
      <span className="precipitation">강수량(mm): {RN1}</span>
    </div>
  );
}

function WeatherForecast({ allWeatherData }) {
  const [key1, key2, key3] = makeTimeKeys();

  return (
    <div id="wea">
      <ForecastItem allData={allWeatherData} timeKey={key1} label={displayHour(1)} />
      <ForecastItem allData={allWeatherData} timeKey={key2} label={displayHour(2)} />
      <ForecastItem allData={allWeatherData} timeKey={key3} label={displayHour(3)} />
    </div>
  );
}

export default WeatherForecast;
