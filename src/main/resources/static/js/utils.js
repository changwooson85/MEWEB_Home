// utils.js

function formatTime(str) {
  if (!str || str.length < 14) return str; // 예외 처리

  return (
    str.slice(0, 4) +
    "-" +
    str.slice(4, 6) +
    "-" +
    str.slice(6, 8) +
    " " +
    str.slice(8, 10) +
    ":" +
    str.slice(10, 12) +
    ":" +
    str.slice(12, 14)
  );
}
function formatTimeToString(time) {
  const date = new Date(time);

  const yyyy = date.getFullYear();
  const mm = String(date.getMonth() + 1).padStart(2, "0");
  const dd = String(date.getDate()).padStart(2, "0");

  return `${yyyy}${mm}${dd}`; // yyyyMMdd 형태
}

function parseTime(timeStr) {
  // 예: "20250420133000" → Date 객체
  const year = timeStr.substring(0, 4);
  const month = timeStr.substring(4, 6) - 1; // JS의 month는 0-based
  const day = timeStr.substring(6, 8);
  const hour = timeStr.substring(8, 10);
  const minute = timeStr.substring(10, 12);
  const second = timeStr.substring(12, 14);
  return new Date(year, month, day, hour, minute, second);
}
