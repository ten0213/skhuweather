// reports: { rainy, cloudy, sunny, dusty, windy, snowy }
const TYPES = ['rainy', 'cloudy', 'sunny', 'dusty', 'windy', 'snowy'];

const KURUMI_MAP = {
  rainy:  { img: '/img/kurumi/rainy_kurumi.png',  text: '쿠름이는 꽃친구의<br/>목을 축여주고 있대요!' },
  cloudy: { img: '/img/kurumi/cloudy_kurumi.png', text: '쿠름이는 구름이 걷힌<br/>맑은 하늘을 어서 보고 싶대요!' },
  sunny:  { img: '/img/kurumi/sunny_kurumi.png',  text: '쿠름이는 맑은 하늘이<br/>너무 좋대요!' },
  dusty:  { img: '/img/kurumi/dust_kurumi.png',   text: '쿠름이는 나쁜<br/>공기가 싫대요!' },
  windy:  { img: '/img/kurumi/windy_kurumi.png',  text: '쿠름이는 빨리<br/>바람이 잔잔해졌으면 좋겠대요!' },
  snowy:  { img: '/img/kurumi/snow_kurumi.png',   text: '쿠름이는 지금<br/>눈과 노는 중이래요!' },
};

const DEFAULT = {
  img: '/img/default.png',
  text: '쿠름이가 제보한<br/>날씨를 궁금해한대요!',
};

function getWinner(reports) {
  if (!reports) return null;

  const counts = TYPES.map((t) => Number(reports[t] || 0));
  const max = Math.max(...counts);

  if (max === 0) return null;

  const winners = counts.filter((c) => c === max);
  if (winners.length > 1) return null; // 동점이면 기본 이미지

  const idx = counts.indexOf(max);
  return TYPES[idx];
}

function KurumiCharacter({ reports }) {
  const winner = getWinner(reports);
  const info = winner ? KURUMI_MAP[winner] : DEFAULT;

  return (
    <div className="turtle-box">
      <span
        id="skhu-turtle"
        dangerouslySetInnerHTML={{ __html: `<p>${info.text}</p>` }}
      />
      <span className="turtle-icon">
        <img className="turtle-image" src={info.img} alt="쿠름이" />
      </span>
    </div>
  );
}

export default KurumiCharacter;
