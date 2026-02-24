const LABELS = {
  rainy: '비 와요',
  cloudy: '흐려요',
  sunny: '맑아요',
  dusty: '공기 나빠요',
  windy: '바람 불어요',
  snowy: '눈 와요',
};

function ReportSummaryChips({ reports }) {
  const items = Object.entries(LABELS)
    .map(([key, label]) => ({
      key,
      label,
      count: Number(reports?.[key] || 0),
    }))
    .filter(({ count }) => count > 0)
    .sort((a, b) => b.count - a.count)
    .slice(0, 3);

  if (items.length === 0) return null;

  return (
    <ul className="report-chip-list">
      {items.map(({ key, label, count }) => (
        <li key={key} className="report-chip">
          <span className="report-chip-label">{label}</span>
          <span className="report-chip-heart">♥</span>
          <span className="report-chip-count">{count}</span>
        </li>
      ))}
    </ul>
  );
}

export default ReportSummaryChips;
