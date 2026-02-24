// 성공회대학교 위치 좌표
const SKHU_LAT = 37.4892;
const SKHU_LON = 126.8553;

// WMO 날씨 코드 → 기상청 SKY/PTY 코드 변환
function wmoToSkyPty(weathercode, cloudcover) {
  // PTY: 0=없음, 1=비, 2=비/눈, 3=눈
  let PTY = 0;
  if ([51, 53, 55, 61, 63, 65, 80, 81, 82, 95, 96, 99].includes(weathercode)) PTY = 1;
  else if ([71, 73, 75, 77, 85, 86].includes(weathercode)) PTY = 3;

  // SKY: 1=맑음, 3=구름많음, 4=흐림
  let SKY = 1;
  if (cloudcover > 60) SKY = 4;
  else if (cloudcover > 20) SKY = 3;

  return { SKY, PTY };
}

// Open-Meteo 시간 문자열 → 앱 키 형식 변환
// "2026-02-24T14:00" → "20260224.1400"
function timeToKey(t) {
  return t.replace(/-/g, '').replace('T', '.').replace(':', '');
}

export async function fetchWeatherData() {
  const url =
    `https://api.open-meteo.com/v1/forecast` +
    `?latitude=${SKHU_LAT}&longitude=${SKHU_LON}` +
    `&hourly=temperature_2m,relativehumidity_2m,precipitation,weathercode,cloudcover` +
    `&timezone=Asia%2FSeoul&forecast_days=2`;

  const res = await fetch(url);
  if (!res.ok) throw new Error('Open-Meteo API 오류');
  const data = await res.json();

  const { time, temperature_2m, relativehumidity_2m, precipitation, weathercode, cloudcover } =
    data.hourly;

  const result = {};
  time.forEach((t, i) => {
    const key = timeToKey(t);
    const { SKY, PTY } = wmoToSkyPty(weathercode[i], cloudcover[i]);
    result[key] = {
      T1H: temperature_2m[i].toFixed(1),
      REH: relativehumidity_2m[i],
      RN1: precipitation[i] === 0 ? '강수없음' : String(precipitation[i]),
      SKY,
      PTY,
    };
  });

  return result;
}
