// SKY+PTY 코드 → 아이콘 이름 매핑
const ICON_MAP = {
  10: 'ksun', 11: 'ksun',
  41: 'cloudyRain2', 42: 'cloudyRain2', 45: 'cloudyRain2',
  31: 'krain', 32: 'krain', 35: 'krain',
  30: 'kcloudy', 40: 'kcloudy',
  33: 'ksnow', 36: 'ksnow', 37: 'ksnow',
  43: 'cloudySnow2', 46: 'cloudySnow2', 47: 'cloudySnow2',
};

function WeatherCard({ weatherData }) {
  if (!weatherData) {
    return (
      <>
        <p className="location">
          <span
            className="material-icons"
            style={{ fontSize: 'xx-large', display: 'inline-flex', flexDirection: 'row', paddingRight: '0.2em' }}
          >
            my_location
          </span>
          성공회대학교 현재 날씨
        </p>
        <p style={{ fontSize: 'large', padding: '1em' }}>날씨 데이터를 불러오는 중...</p>
      </>
    );
  }

  const { T1H, PTY, SKY, RN1, REH } = weatherData;
  const code = Number(`${SKY}${PTY}`);
  const iconName = ICON_MAP[code] || 'ksun';

  return (
    <>
      <p className="location">
        <span
          className="material-icons"
          style={{ fontSize: 'xx-large', display: 'inline-flex', flexDirection: 'row', paddingRight: '0.2em' }}
        >
          my_location
        </span>
        성공회대학교 현재 날씨
      </p>

      <p className="temp-box">
        <span className="temp">{T1H}°C</span>
        <span className="weather-icon">
          <img className="weather-kicon" src={`/img/icon/${iconName}.png`} alt="날씨 아이콘" />
        </span>
      </p>

      <p className="cloudy">강수량(mm) :&nbsp;<span>{RN1}</span></p>
      <p className="water">습도(%) :&nbsp;<span>{REH}</span></p>
    </>
  );
}

export default WeatherCard;
