function refreshPage() {
  if (AndroidApp && AndroidApp.reloadPage) {
    AndroidApp.reloadPage(); // Android에서 reload 수행
  }
}