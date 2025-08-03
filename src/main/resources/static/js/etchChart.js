const choicesInstances = {};
let requestData = {};
//let highlightList = [];

document.addEventListener("DOMContentLoaded", () => {
  const etchForm = document.getElementById("etchForm");
  const searchBtn = document.getElementById("searchBtn");
  const canvas = document.getElementById("etchChart");
  const canvasNormalChart = document.getElementById("etchNormalCanvas");
  const canvasScatterChart = document.getElementById("etchScatterCanvas");
  const epdChartBtn = document.getElementById("etchEpdChart");
  const loadingMessage = document.getElementById("loading-overlay");
  const searchLotBtn = document.getElementById("searchLotBtn");
  const resetLotBtn = document.getElementById("resetLotBtn");

  let chart; // 기존 차트 관리
  let series = [];
  let etchNormalCanvas;
  let etchscatterCanvas;

  loadingMessage.style.display = "none";

  initChoices("layer", "Layer 선택");
  initChoices("list_res_id", "EQ 선택");
  initChoices("chamber", "CH 선택");
  initChoices("recipe", "RECIPE 선택");
  initChoices("epdStep", "EPD STEP 선택");
  initChoices("event", "EVENT 선택");

  window.addEventListener("resize", () => chart.resize());

  epdChartBtn.addEventListener("click", function () {
    fetchEtchConditions();
  });

  document
    .getElementById("downloadPdf")
    .addEventListener("click", async function () {
      const { jsPDF } = window.jspdf;

      const pdf = new jsPDF("p", "mm", "a4");
      const pageWidth = pdf.internal.pageSize.getWidth();
      let y = 10;

      //한글 폰트 등록
      //pdf.addFont("NotoSansKR-Regular.ttf", "NotoSansKR", "normal");
      console.log(pdf.getFontList());
      pdf.setFontSize(12);
      // Etch 추이 차트 캡처
      const captureChart = async (elementId, title) => {
        const el = document.getElementById(elementId);
        if (!el) return;

        const canvas = await html2canvas(el);
        const imgData = canvas.toDataURL("image/png");
        const imgProps = pdf.getImageProperties(imgData);
        const imgHeight = (imgProps.height * pageWidth) / imgProps.width;

        if (y + imgHeight > 280) {
          pdf.addPage();
          y = 10;
        }

        pdf.text(title, 10, y);
        y += 5;
        pdf.addImage(imgData, "PNG", 10, y, pageWidth - 20, imgHeight);
        y += imgHeight + 10;
      };

      await captureChart("etchChart", "EPD Trend Chart");
      await captureChart("etchNormalCanvas", "EPD Normal Distribution");
      await captureChart("etchScatterCanvas", "EPD Scatter");

      // 요약 테이블 -> AutoTable로 추출
      const summaryTable = document.querySelector(".summary-table");
      if (summaryTable) {
        const headCells = Array.from(
          summaryTable.querySelectorAll("thead th")
        ).map((th) => th.textContent.trim());
        const bodyRows = Array.from(
          summaryTable.querySelectorAll("tbody tr")
        ).map((tr) =>
          Array.from(tr.querySelectorAll("td")).map((td) =>
            td.textContent.trim()
          )
        );

        pdf.text("EQ EPD TIME TABLE", 10, y);
        y += 5;

        pdf.autoTable({
          head: [headCells],
          body: bodyRows,
          startY: y,
          styles: {
            fontSize: 10,
          },
        });
      }

      pdf.save("epd_time_report.pdf");
    });
  // document
  //   .getElementById("downloadPdf")
  //   .addEventListener("click", async function () {
  //     const { jsPDF } = window.jspdf;
  //     const pdf = new jsPDF("p", "mm", "a4");
  //     let yPosition = 10; // PDF 상단 여백

  //     // 추이차트, 정규분포, 산포도, 요약 테이블의 각 영역 ID
  //     const targets = [
  //       { id: "etchChart", name: "Etch 추이 차트" },
  //       { id: "etchNormalCanvas", name: "정규분포" },
  //       { id: "etchScatterCanvas", name: "산포도" },
  //       { id: "summary-body", name: "장비별 EPD TIME 요약" },
  //     ];

  //     for (const target of targets) {
  //       const element = document.getElementById(target.id);
  //       if (!element) continue;

  //       // 캡처 및 PDF에 추가
  //       await html2canvas(element).then((canvas) => {
  //         const imgData = canvas.toDataURL("image/png");
  //         const pageWidth = pdf.internal.pageSize.getWidth();
  //         const imgProps = pdf.getImageProperties(imgData);
  //         const imgHeight = (imgProps.height * pageWidth) / imgProps.width;

  //         if (yPosition + imgHeight > 290) {
  //           pdf.addPage();
  //           yPosition = 10;
  //         }

  //         pdf.setFontSize(12);
  //         pdf.text(target.name, 10, yPosition);
  //         yPosition += 5;
  //         pdf.addImage(
  //           imgData,
  //           "PNG",
  //           10,
  //           yPosition,
  //           pageWidth - 20,
  //           imgHeight
  //         );
  //         yPosition += imgHeight + 10;
  //       });
  //     }

  //     pdf.save("epd_time_report.pdf");
  //   });

  searchLotBtn.addEventListener("click", () => {
    const lot = document.querySelector('[name="lot"]').value;
    const slot = document.querySelector('[name="slot"]').value;
    if (lot) {
      highlightLotSlotWithEchar(lot, slot || null);
    }
  });

  resetLotBtn.addEventListener("click", () => {
    highlightLotSlotWithEchar("", ""); // 전부 초기 스타일 복원
  });

  const InitAllContents = () => {
    // 1. ECharts 차트 초기화 (필요할 경우)
    if (chart && chart.dispose) {
      chart.dispose();
    }
    chart = null;

    if (etchNormalCanvas && etchNormalCanvas.destroy) {
      etchNormalCanvas.destroy();
    }
    etchNormalCanvas = null;

    if (etchscatterCanvas && etchscatterCanvas.destroy) {
      etchscatterCanvas.destroy();
    }
    etchscatterCanvas = null;

    // 2. 테이블 초기화
    const headRow = document.getElementById("summary-head");
    const avgRow = document.getElementById("avg-row");
    const minRow = document.getElementById("min-row");
    const maxRow = document.getElementById("max-row");

    if (headRow) headRow.innerHTML = "";
    if (avgRow) avgRow.innerHTML = "";
    if (minRow) minRow.innerHTML = "";
    if (maxRow) maxRow.innerHTML = "";
  };
  //조건 불러오는 함수
  function fetchEtchConditions() {
    fetch("/etch/epd/conditions")
      .then((response) => response.json())
      .then((data) => {
        fillSelect("layer", data.list_layer);
        fillSelect("list_res_id", data.list_res_id);
        fillSelect("chamber", data.list_chamber);
        fillSelect("recipe", data.recipe);
        fillSelect("epdStep", data.epdStep);
        fillSelect("event", data.event);
      })
      .catch((err) => console.error("조건 불러오기 실패:", err));
  }

  // function fillSelect(selectId, items) {
  //   const select = document.getElementById(selectId);
  //   if (!select) return;
  //   select.innerHTML = '';
  //   items.forEach((item) => {
  //     const option = document.createElement('option');
  //     option.value = item;
  //     option.textContent = item;
  //     select.appendChild(option);
  //   });
  // }

  // etchForm.addEventListener("submit", async (e) => {
  //   e.preventDefault(); // 기본 폼 제출 동작 차단
  //   const formData = new FormData(etchForm);

  //   // 폼 데이터를 JSON 형태로 변환
  //   const formJson = {};
  //   formData.forEach((value, key) => {
  //     if (formJson[key]) {
  //       if (!Array.isArray(formJson[key])) {
  //         formJson[key] = [formJson[key]];
  //       }
  //       formJson[key].push(value);
  //     } else {
  //       formJson[key] = value;
  //     }
  //   });

  //   const response = await fetch("/etch-condition/data", {
  //     method: "POST",
  //     headers: { "Content-Type": "application/json" },
  //     body: JSON.stringify(formJson),
  //   });

  //   const chartData = await response.json();

  //   drawChart(chartData);
  // });

  etchForm.addEventListener("submit", async (e) => {
    e.preventDefault();

    loadingMessage.style.display = "flex";

    try {
      requestData = {};
      highlightList = [];
      const formData = new FormData(etchForm);
      const periods = [];
      const eqList = [];
      const chamberList = [];
      const eventList = [];

      formData.forEach((value, key) => {
        //if (["searchLotBtn", "resetLotBtn"].includes(key)) return; // 제외 처리

        if (key === "startDate[]") {
          periods.push({ startDate: value });
        } else if (key === "endDate[]") {
          const last = periods[periods.length - 1];
          if (last) last.endDate = value;
        } else {
          // 무조건 배열로 처리할 키 목록
          const arrayKeys = [
            "layer",
            "list_res_id",
            "chamber",
            "recipe",
            "epdStep",
            "event",
          ];

          if (arrayKeys.includes(key)) {
            if (!requestData[key]) {
              requestData[key] = [];
            }
            requestData[key].push(value);

            if (key === "list_res_id") {
              eqList.push(value); // 장비 리스트 저장
            }
            if (key === "chamber") {
              chamberList.push(value); // 장비 리스트 저장
            }
            if (key == "event") {
              eventList.push(value); //
            }
          } else {
            requestData[key] = value;
          }
        }
      });

      // 기간 유효성 체크
      const isInvalidPeriod =
        periods.length === 0 || periods.some((p) => !p.startDate || !p.endDate);
      if (isInvalidPeriod) {
        alert("기간(시작일/종료일)을 모두 선택해 주세요.");
        return;
      }

      const isOverSixMonth = periods.some((p) => {
        const start = new Date(p.startDate);
        const end = new Date(p.endDate);
        const diffMonth =
          (end.getFullYear() - start.getFullYear()) * 12 +
          (end.getMonth() - start.getMonth());

        return diffMonth > 3 || start > end;
      });

      if (isOverSixMonth) {
        alert("기간은 최대 3개월까지만 가능합니다.");
        return;
      }

      // 2. Layer, EQ, Chamber 각각 필수 체크
/*      if (!requestData.layer || requestData.layer.length === 0) {
        alert("Layer를 1개 이상 선택해 주세요.");
        return;
      }*/

      if (!requestData.list_res_id || requestData.list_res_id.length === 0) {
        alert("EQ를 1개 이상 선택해 주세요.");
        return;
      }

      // if (!requestData.chamber || requestData.chamber.length === 0) {
      //   alert("Chamber를 1개 이상 선택해 주세요.");
      //   return;
      // }

      requestData.periods = periods;
      delete requestData.lot;
      delete requestData.slot;
      delete requestData.event;

      let response = await fetch("/etch/epd/etch-condition/data", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(requestData),
      });

      if (!response.ok) {
        const errorMessage = await response.text();
        throw new Error(errorMessage || "조회 실패");
      }
      const chartData = await response.json();

      //장비/구간 별 down 이벤트 조회
      const eventInfo = {};
      if (eventList.length > 0) {
        const eqDownRequestData = {};
        const eqPeriodMap = {};
        eqList.forEach((eq) => {
          eqPeriodMap[eq] = [...periods];
        });

        chamberList.forEach((chamber) => {
          eqList.forEach((eq) => {
            eqPeriodMap[eq + "-" + chamber] = [...periods];
          });
        });

        eqDownRequestData.factory = "AFB1";
        eqDownRequestData.eqPeriodMap = eqPeriodMap;
        eqDownRequestData.eventList = eventList;

        response = await fetch("/etch/epd/eq-downlist", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(eqDownRequestData),
        });

        if (!response.ok) {
          const errorMessage = await response.text();
          throw new Error(errorMessage || "조회 실패");
        }
        const eqDownDataList = await response.json();
        eqDownDataList.forEach((item) => {
          const eq = item.resId;
          const event = item.downEventId;
          const time = item.downTranTime;

          if (!eventInfo[eq]) {
            eventInfo[eq] = [];
          }

          eventInfo[eq].push({ [event]: time });
        });
      }

      //console.table(chartData);

      // const lot = document.querySelector('[name="lot"]').value;
      // const slot = document.querySelector('[name="slot"]').value;

      // if (lot) {
      //   highlightList.push({ lot, slot: slot || "" });
      // }

      // const eventInfo = {
      //   "ECGT04-A": [{ PM: "20250410102000" }, { AS_PM: "20250408102000" }],
      // };

      //drawChart(chartData, eventInfo);
      if (chartData.length === 0) {
        alert("조회 조건에 해당하는 데이터가 없습니다");
        return;
      }

      InitAllContents();
      drawChartWithEchart(chartData, eventInfo);
      drawNormalDistribution(chartData);
      drawScatterChart(chartData);
      generateSummaryTable(chartData);
    } catch (e) {
      alert("조회 중 오류가 발생했습니다." + e.message);
    } finally {
      loadingMessage.style.display = "none";
    }
  });

  function drawChartWithEchart(rawData, eventInfo) {
    chart = echarts.init(document.getElementById("etchChart"), null, {
      devicePixelRatio: window.devicePixelRatio || 1,
    });

    // 1. X축 시간 라벨 (데이터 있는 시간만)
    const allLabels = Array.from(
      new Set(
        rawData.map((d) =>
          echarts.format.formatTime("yyyy-MM-dd hh:mm", parseTime(d.time))
        )
      )
    ).sort();

    // 2. EQ-CH별 데이터 그룹화
    const grouped = {};
    rawData.forEach((d) => {
      const key = `${d.eq}-${d.chamber}`;
      if (!grouped[key]) grouped[key] = [];
      const time = parseTime(d.time);
      grouped[key].push({
        value: [time, d.epdTime],
        lot: d.lot,
        slot: d.slot,
      });
    });

    // 3. Series 구성
    series = Object.entries(grouped).map(([key, data]) => ({
      name: key,
      type: "scatter",
      data: data.map((point) => ({
        value: [
          echarts.format.formatTime("yyyy-MM-dd hh:mm", point.value[0]),
          point.value[1],
        ],
        lot: point.lot,
        slot: point.slot,
      })),
      symbolSize: 4,
      emphasis: { focus: "series" },
    }));

    // 4. 이벤트 그래픽 요소 생성
    const maxY = Math.max(...rawData.map((d) => d.epdTime));
    const yLabelTop = maxY * 1.05;
    const eventGraphics = [];

    Object.entries(eventInfo).forEach(([eqch, events]) => {
      events.forEach((eventObj, idx) => {
        const [eventType, eventTimeStr] = Object.entries(eventObj)[0];
        const eventTimeLabel = echarts.format.formatTime(
          "yyyy-MM-dd hh:mm",
          parseTime(eventTimeStr)
        );

        eventGraphics.push({
          type: "text",
          id: `label-${eqch}-${eventType}-${idx}`,
          style: {
            text: `${eqch}\n${eventType}`,
            fill: "red",
            font: "11px sans-serif",
            textAlign: "center",
            verticalAlign: "bottom",
          },
          position: [eventTimeLabel, yLabelTop],
          xAxisIndex: 0,
          yAxisIndex: 0,
          z: 10,
        });
      });
    });

    // 5. 차트 옵션 구성 및 적용
    const option = {
      tooltip: {
        trigger: "item",
        formatter: (params) => {
          const { data, seriesName } = params;
          return `
          ${seriesName}<br/>
          TIME: ${data.value[0]}<br/>
          EPD TIME: ${data.value[1]}<br/>
          LOT: ${data.lot}, SLOT: ${data.slot}`;
        },
      },
      legend: {
        type: "scroll",
        top: 10,
      },
      toolbox: {
        feature: {
          saveAsImage: {},
          restore: {},
        },
      },
      xAxis: {
        type: "category",
        name: "Time",
        data: allLabels,
        axisLabel: {
          interval: "auto",
          rotate: 45,
          margin: 10,
          hideOverlap: true,
        },
      },
      yAxis: {
        type: "value",
        name: "EPD TIME",
        scale: true,
      },
      dataZoom: [
        { type: "inside", xAxisIndex: 0 },
        { type: "slider", xAxisIndex: 0 },
      ],
      grid: {
        left: 60,
        right: 40,
        top: 60,
        bottom: 120,
      },
      series,
      graphic: {
        elements: eventGraphics,
      },
    };

    chart.setOption(option);
  }
  // function drawChartWithEchart(rawData, eventInfo) {
  //   chart = echarts.init(document.getElementById("etchChart"), null, {
  //     devicePixelRatio: window.devicePixelRatio || 1,
  //   });

  //   let minX = Infinity;
  //   let maxX = -Infinity;

  //   // 1. EQ-CH 별 데이터 그룹화
  //   const grouped = {};
  //   rawData.forEach((d) => {
  //     const key = `${d.eq}-${d.chamber}`;
  //     if (!grouped[key]) grouped[key] = [];
  //     const time = parseTime(d.time);
  //     grouped[key].push({
  //       value: [time, d.epdTime],
  //       lot: d.lot,
  //       slot: d.slot,
  //     });

  //     // 동시에 min/max 계산
  //     const tmX = time.getTime();
  //     if (tmX < minX) minX = tmX;
  //     if (tmX > maxX) maxX = tmX;
  //   });

  //   // 2. Series 배열 생성 (scatter type)
  //   series = Object.entries(grouped).map(([key, data]) => ({
  //     name: key,
  //     type: "scatter",
  //     data,
  //     symbolSize: 4,
  //     emphasis: { focus: "series" },
  //   }));

  //   // 2. graphic 요소로 이벤트 선 생성
  //   const maxY = Math.max(...rawData.map((d) => d.epdTime));
  //   const yLabelTop = maxY * 1.05; // 살짝 위쪽

  //   const eventGraphics = [];

  //   Object.entries(eventInfo).forEach(([eqch, events]) => {
  //     events.forEach((eventObj, idx) => {
  //       const [eventType, eventTimeStr] = Object.entries(eventObj)[0];
  //       const eventTime = parseTime(eventTimeStr);
  //       console.log(eventTimeStr, eventTime, typeof eventTime);

  //       eventGraphics.push({
  //         type: "line",
  //         id: `event-${eqch}-${eventType}-${idx}`,
  //         style: {
  //           stroke: "red",
  //           lineWidth: 1.5,
  //           opacity: 0.7,
  //         },
  //         xAxisIndex: 0,
  //         yAxisIndex: 0,
  //         coords: [
  //           [eventTime, 0],
  //           [eventTime, maxY],
  //         ],
  //         silent: true,
  //         z: 10,
  //       });

  //       eventGraphics.push({
  //         type: "text",
  //         id: `label-${eqch}-${eventType}-${idx}`,
  //         style: {
  //           text: `${eqch}\n${eventType}`,
  //           fill: "red",
  //           font: "11px sans-serif",
  //           textAlign: "center",
  //           verticalAlign: "bottom",
  //         },
  //         coord: [eventTime, maxY],
  //         xAxisIndex: 0,
  //         yAxisIndex: 0,
  //         z: 10,
  //       });
  //     });
  //   });

  //   console.log("eventGraphics", eventGraphics);
  //   // 4. ECharts 옵션 설정
  //   const option = {
  //     tooltip: {
  //       trigger: "item",
  //       formatter: (params) => {
  //         const { data, seriesName } = params;
  //         return `
  //       ${seriesName}<br/>
  //       TIME: ${echarts.format.formatTime(
  //         "yyyy-MM-dd hh:mm:ss",
  //         data.value[0]
  //       )}<br/>
  //       EPD TIME: ${data.value[1]}<br/>
  //       LOT: ${data.lot}, SLOT: ${data.slot}`;
  //       },
  //     },
  //     legend: {
  //       type: "scroll",
  //       top: 10,
  //     },
  //     toolbox: {
  //       feature: {
  //         saveAsImage: {},
  //         restore: {},
  //       },
  //     },
  //     xAxis: {
  //       type: "time",
  //       name: "Time",
  //       min: new Date(minX),
  //       max: new Date(maxX),
  //       axisLabel: {
  //         interval: "auto",
  //         formatter: (value) =>
  //           echarts.format.formatTime("yyyy-MM-dd hh:mm", value),
  //       },
  //     },
  //     yAxis: {
  //       type: "value",
  //       name: "EPD TIME",
  //       scale: true,
  //     },
  //     dataZoom: [
  //       { type: "inside", xAxisIndex: 0 },
  //       { type: "slider", xAxisIndex: 0 },
  //     ],
  //     series,
  //     graphic: {
  //       elements: [], // 초기화
  //     },
  //   };

  //   // 5. 차트 적용
  //   //chart.setOption(option);

  //   // 1. 먼저 series, xAxis 등 전체 차트를 초기 렌더링
  //   chart.setOption(option);

  //   // 2. 그 다음 graphic만 따로 갱신
  //   if (eventGraphics.length > 0) {
  //     chart.setOption(
  //       {
  //         graphic: {
  //           elements: eventGraphics,
  //         },
  //       },
  //       { replaceMerge: ["graphic"] }
  //     );
  //   }
  // }
  function drawChart(rawData, eventInfo) {
    const labelsSet = new Set(rawData.map((d) => d.time));
    const labels = Array.from(labelsSet).sort();

    const grouped = {};
    // 1. 데이터를 그룹화하고, 하이라이트 여부 판단
    rawData.forEach((d) => {
      const key = `${d.eq}-${d.chamber}`;
      if (!grouped[key]) grouped[key] = { points: [] };

      const xIndex = labels.indexOf(d.time); // label 위치 찾기
      //인덱스로 하면 x축 시간 문자열 어떻게 보임?const formattedTime = formatTime(d.time);

      // 해당 포인트가 하이라이트 조건과 일치하는지 체크
      // const isHighlighted = highlightList.some((hl) => {
      //   // slot 없으면 lot만 비교
      //   if (!hl.slot || hl.slot.trim() === "") {
      //     return d.lot === hl.lot;
      //   }
      //   // slot 있으면 lot + slot 둘 다 비교
      //   return d.lot === hl.lot && d.slot === hl.slot;
      // });

      grouped[key].points.push({
        x: xIndex,
        y: d.epdTime,
        lot: d.lot,
        slot: d.slot,
      });
    });

    const getRandomColor = (alpah = 1) =>
      `hsl(${Math.floor(Math.random() * 360)}, 70%, 60%, ${alpah})`;

    const datasets = Object.entries(grouped).map(([key, obj]) => {
      const baseColor = getRandomColor(0.7); // label별 랜덤 색상

      return {
        label: key,
        data: obj.points.map((p) => ({
          ...p,
          radius: 1,
          backgroundColor: baseColor,
          borderColor: baseColor,
          _baseColor: baseColor, // 나중에 reset할 때 사용
        })),
        showLine: false,
      };
    });

    // Event Line Dataset 추가
    // const maxYValue = Math.max(...rawData.map((d) => d.epdTime)) * 1.1;
    // const minYValue = Math.min(...rawData.map((d) => d.epdTime)) * 0.9;

    // const eventDatasets = [];
    // const customLabels = [];

    // Object.entries(eventInfo).forEach(([eqCh, eventList]) => {
    //   eventList.forEach((eventObj) => {
    //     const [eventType, eventTime] = Object.entries(eventObj)[0]; // key, value 꺼내기

    //     const xIndex = findNearestXIndex(labels, eventTime); // 가까운 위치 찾기
    //     const labelText = `${eqCh}\n${eventType}\n${eventTime}`; // 장비+이벤트+날짜

    //     eventDatasets.push({
    //       label: labelText,
    //       data: [
    //         { x: xIndex, y: minYValue },
    //         { x: xIndex, y: maxYValue },
    //       ],
    //       borderColor: "red",
    //       borderWidth: 2,
    //       pointRadius: 0,
    //       showLine: true,
    //     });
    //   });
    // });

    //const finalDatasets = [...datasets, ...eventDatasets];

    const eventLabels = [];

    Object.entries(eventInfo).forEach(([eqCh, eventList]) => {
      eventList.forEach((eventObj) => {
        const [eventType, eventTime] = Object.entries(eventObj)[0];
        const xIndex = findNearestXIndex(labels, eventTime);

        eventLabels.push({
          xIndex,
          text: `${eqCh}\n${eventType}\n${eventTime}`,
        });
      });
    });

    if (chart) chart.destroy();

    chart = new Chart(canvas.getContext("2d"), {
      type: "scatter",
      data: { labels, datasets },
      options: {
        animation: false,
        normalized: true,
        spanGaps: true,
        parsing: false,
        responsive: true,
        maintainAspectRatio: false,
        interaction: { mode: "index", intersect: false },
        elements: {
          point: {
            radius: (ctx) => ctx.raw.radius,
            backgroundColor: (ctx) => ctx.raw.backgroundColor,
            borderColor: (ctx) => ctx.raw.borderColor,
          },
        },
        scales: {
          y: { beginAtZero: true, title: { display: true, text: "EPD TIME" } },
          x: {
            type: "category",
            ticks: {
              autoSkip: true,
              callback: function (value, index, ticks) {
                const rawLabel = this.getLabelForValue(value); // yyyyMMddHHmmss
                return formatTime(rawLabel);
              },
            },
            // time: {
            //   tooltipFormat: "yyyy-MM-dd HH:mm:ss",
            //   displayFormats: {
            //     hour: "yyyy-MM-dd HH:mm:ss",
            //     day: "yyyy-MM-dd HH:mm:ss",
            //   },
            // },
            title: { display: true, text: "Time" },
          },
        },
        plugins: {
          legend: {
            labels: {
              filter: (legendItem, chartData) => {
                return !legendItem.text.includes("\n");
                // label에 \n 들어간거(이벤트)는 legend 안보이게
              },
            },
          },
          tooltip: {
            filter: (tooltipItem) => !tooltipItem.dataset.label.includes("\n"),
            callbacks: {
              title: () => "",
              label: (ctx) => {
                const point = ctx.raw;
                const label = ctx.dataset.label;
                const labels = ctx.chart.data.labels;
                return `(${label} (${labels[point.x]}, ${point.y}) | LOT: ${
                  point.lot
                }, SLOT: ${point.slot}`;
              },
            },
          },
          zoom: {
            wheel: { enabled: true },
            pinch: { enabled: true },
            mode: "xy",
            onZoomComplete: ({ chart }) => {
              reloadData(chart);
            },
            onPanComplete: ({ chart }) => {
              reloadData(chart);
            },
          },
        },
      },
      plugins: [
        {
          id: "eventLineAndLabel",
          afterDatasetsDraw(chart) {
            const {
              ctx,
              chartArea: { top, bottom },
              scales: { x, y },
            } = chart;
            const baseY = y.getPixelForValue(y.min) - 40;

            eventLabels.forEach((labelInfo, idx) => {
              const xPos = x.getPixelForValue(labelInfo.xIndex);

              // 세로선
              ctx.save();
              ctx.beginPath();
              ctx.strokeStyle = "red";
              ctx.lineWidth = 2;
              ctx.moveTo(xPos, top);
              ctx.lineTo(xPos, bottom);
              ctx.stroke();
              ctx.restore();

              // 텍스트
              ctx.save();
              ctx.font = "10px Arial";
              ctx.fillStyle = "red";
              const isLeft = idx % 2 === 0; // 좌/우 번갈아
              const step = Math.floor(idx / 2) + 1;

              const baseOffsetX = 6; // 기본 선으로부터 거리
              const extraOffsetX = 5; // 대각선 느낌용 추가 거리
              const offsetY = 20; // 위아래 거리 (겹침 방지)

              const labelX = isLeft
                ? xPos - baseOffsetX - step * extraOffsetX // 왼쪽 점점 더 멀리
                : xPos + baseOffsetX + step * extraOffsetX; // 오른쪽 점점 더 멀리

              const labelY = isLeft
                ? baseY - step * offsetY // 위로
                : baseY + step * offsetY; // 아래로

              ctx.textAlign = isLeft ? "right" : "left";

              const lines = labelInfo.text.split("\n");
              lines.forEach((line, i) => {
                ctx.fillText(line, labelX, labelY + i * 12);
              });

              ctx.restore();
            });
          },
        },
      ],
    });
  }
  //현재 주석 처리
  // function drawChart(rawData) {
  //   const labelsSet = new Set(rawData.map((d) => d.time));
  //   const labels = Array.from(labelsSet).sort();

  //   const grouped = {};
  //   // 1. 데이터를 그룹화하고, 하이라이트 여부 판단
  //   rawData.forEach((d) => {
  //     const key = `${d.eq}-${d.chamber}`;
  //     if (!grouped[key]) grouped[key] = { points: [] };

  //     const xIndex = labels.indexOf(d.time); // label 위치 찾기
  //     //인덱스로 하면 x축 시간 문자열 어떻게 보임?const formattedTime = formatTime(d.time);

  //     // 해당 포인트가 하이라이트 조건과 일치하는지 체크
  //     // const isHighlighted = highlightList.some((hl) => {
  //     //   // slot 없으면 lot만 비교
  //     //   if (!hl.slot || hl.slot.trim() === "") {
  //     //     return d.lot === hl.lot;
  //     //   }
  //     //   // slot 있으면 lot + slot 둘 다 비교
  //     //   return d.lot === hl.lot && d.slot === hl.slot;
  //     // });

  //     grouped[key].points.push({
  //       x: xIndex,
  //       y: d.epdTime,
  //       lot: d.lot,
  //       slot: d.slot,
  //     });
  //   });

  //   // rawData.forEach((d) => {
  //   //   const key = `${d.eq}-${d.chamber}`;
  //   //   if (!grouped[key]) grouped[key] = { points: [] };

  //   //   const formattedTime = formatTime(d.time);

  //   //   // 해당 포인트가 하이라이트 조건과 일치하는지 체크
  //   //   const isHighlighted = highlightList.some((hl) => {
  //   //     // slot 없으면 lot만 비교
  //   //     if (!hl.slot || hl.slot.trim() === "") {
  //   //       return d.lot === hl.lot;
  //   //     }
  //   //     // slot 있으면 lot + slot 둘 다 비교
  //   //     return d.lot === hl.lot && d.slot === hl.slot;
  //   //   });

  //   //   grouped[key].points.push({
  //   //     x: formattedTime,
  //   //     y: d.epdTime,
  //   //     lot: d.lot,
  //   //     slot: d.slot,
  //   //     isHighlight: isHighlighted,
  //   //   });
  //   // });

  //   const getRandomColor = (alpah = 1) =>
  //     `hsl(${Math.floor(Math.random() * 360)}, 70%, 60%, ${alpah})`;

  //   //const normalPoints = [];
  //   //const highlightPoints = [];

  //   // Object.entries(grouped).forEach(([key, obj]) => {
  //   //   obj.points.forEach((p) => {
  //   //     const point = { x: p.x, y: p.y, lot: p.lot, slot: p.slot };
  //   //     if (p.isHighlight) {
  //   //       highlightPoints.push(point);
  //   //     } else {
  //   //       normalPoints.push(point);
  //   //     }
  //   //   });
  //   // });

  //   // const datasets = [
  //   //   {
  //   //     label: "Normal Data",
  //   //     data: normalPoints,
  //   //     borderColor: getRandomColor(0.3),
  //   //     backgroundColor: getRandomColor(0.3),
  //   //     borderWidth: 1,
  //   //     pointRadius: 2,
  //   //     pointHoverRadius: 4,
  //   //     showLine: false,
  //   //   },
  //   //   {
  //   //     label: "Highlight",
  //   //     data: highlightPoints,
  //   //     borderColor: "red",
  //   //     backgroundColor: "red",
  //   //     borderWidth: 2,
  //   //     pointRadius: 6,
  //   //     pointHoverRadius: 8,
  //   //     showLine: false,
  //   //   },
  //   // ];

  //   // const datasets = Object.entries(grouped).map(([key, obj]) => {
  //   //   const isHighlight = obj.points.some((p) => p.isHighlight);
  //   //   return {
  //   //     label: key,
  //   //     data: obj.points,
  //   //     borderColor: getRandomColor(0.3),
  //   //     backgroundColor: isHighlight ? "red" : "rgba(0,0,255,0.5)",
  //   //     borderWidth: isHighlight ? 2 : 0,
  //   //     pointRadius: isHighlight ? 6 : 1,
  //   //     pointHoverRadius: isHighlight ? 6 : 3,
  //   //     showLine: false,
  //   //   };
  //   // });

  //   // const datasets = Object.entries(grouped).map(([key, obj]) => {
  //   //   return {
  //   //     label: key,
  //   //     data: obj.points.map((p) => ({
  //   //       ...p,
  //   //       radius: p.isHighlight ? 6 : 1,
  //   //       backgroundColor: p.isHighlight ? "red" : "rgba(0,0,255,0.5)",
  //   //       borderColor: p.isHighlight ? "red" : "rgba(0,0,255,0.5)",
  //   //     })),
  //   //     showLine: false,
  //   //   };
  //   // });

  //   // const datasets = Object.entries(grouped).map(([key, obj]) => {
  //   //   return {
  //   //     label: key,
  //   //     data: obj.points.map((p) => ({
  //   //       ...p,
  //   //       radius: 1,
  //   //       backgroundColor: "rgba(0,0,255,0.5)",
  //   //       borderColor: "rgba(0,0,255,0.5)",
  //   //     })),
  //   //     showLine: false,
  //   //   };
  //   // });

  //   // const datasets = Object.entries(grouped).map(([key, obj]) => {
  //   //   return {
  //   //     label: key,
  //   //     data: obj.points,
  //   //     borderColor: "rgba(0,0,255,0.5)",
  //   //     backgroundColor: "rgba(0,0,255,0.5)",
  //   //     radius: 1,
  //   //     showLine: false,
  //   //   };
  //   // });

  //   const datasets = Object.entries(grouped).map(([key, obj]) => {
  //     const baseColor = getRandomColor(0.7); // label별 랜덤 색상

  //     return {
  //       label: key,
  //       data: obj.points.map((p) => ({
  //         ...p,
  //         radius: 1,
  //         backgroundColor: baseColor,
  //         borderColor: baseColor,
  //         _baseColor: baseColor, // 나중에 reset할 때 사용
  //       })),
  //       showLine: false,
  //     };
  //   });

  //   if (chart) chart.destroy();

  //   chart = new Chart(canvas.getContext("2d"), {
  //     type: "scatter",
  //     data: { labels, datasets },
  //     options: {
  //       animation: false,
  //       normalized: true,
  //       spanGaps: true,
  //       parsing: false,
  //       responsive: true,
  //       maintainAspectRatio: false,
  //       interaction: { mode: "index", intersect: false },
  //       elements: {
  //         point: {
  //           radius: (ctx) => ctx.raw.radius,
  //           backgroundColor: (ctx) => ctx.raw.backgroundColor,
  //           borderColor: (ctx) => ctx.raw.borderColor,
  //         },
  //       },
  //       scales: {
  //         y: { beginAtZero: true, title: { display: true, text: "EPD TIME" } },
  //         x: {
  //           type: "category",
  //           ticks: {
  //             autoSkip: true,
  //             callback: function (value, index, ticks) {
  //               const rawLabel = this.getLabelForValue(value); // yyyyMMddHHmmss
  //               return formatTime(rawLabel);
  //             },
  //           },
  //           // time: {
  //           //   tooltipFormat: "yyyy-MM-dd HH:mm:ss",
  //           //   displayFormats: {
  //           //     hour: "yyyy-MM-dd HH:mm:ss",
  //           //     day: "yyyy-MM-dd HH:mm:ss",
  //           //   },
  //           // },
  //           title: { display: true, text: "Time" },
  //         },
  //       },
  //       plugins: {
  //         tooltip: {
  //           callbacks: {
  //             title: () => "",
  //             label: (ctx) => {
  //               const point = ctx.raw;
  //               const label = ctx.dataset.label;
  //               const labels = ctx.chart.data.labels;
  //               return `(${label} (${labels[point.x]}, ${point.y}) | LOT: ${
  //                 point.lot
  //               }, SLOT: ${point.slot}`;
  //             },
  //           },
  //         },
  //         zoom: {
  //           wheel: { enabled: true },
  //           pinch: { enabled: true },
  //           mode: "xy",
  //           onZoomComplete: ({ chart }) => {
  //             reloadData(chart);
  //           },
  //           onPanComplete: ({ chart }) => {
  //             reloadData(chart);
  //           },
  //         },
  //       },
  //     },
  //   });
  // }
  // function drawChart(rawData, highlightList) {
  //   const labelsSet = new Set(rawData.map((d) => d.time));
  //   const labels = Array.from(labelsSet).sort();

  //   const grouped = {};
  //   const highlightKeys = new Set();

  //   // 1. 데이터를 그룹화하고, 하이라이트 여부 판단
  //   rawData.forEach((d) => {
  //     const key = `${d.eq}-${d.chamber}`;
  //     if (!grouped[key]) grouped[key] = { data: {}, points: [] };

  //     grouped[key].data[d.time] = d.epdTime;

  //     // 해당 포인트가 하이라이트 조건과 일치하는지 체크
  //     const isHighlighted = highlightList.some(
  //       (hl) => d.lot === hl.lot && d.slot === hl.slot
  //     );

  //     if (isHighlighted) {
  //       grouped[key].points.push(d.time);
  //       highlightKeys.add(key);
  //     }
  //   });

  //   const getRandomColor = (alpah = 1) =>
  //     `hsl(${Math.floor(Math.random() * 360)}, 70%, 60%, ${alpah})`;

  //   const datasets = Object.entries(grouped).map(([key, obj]) => {
  //     const isHighlight = highlightKeys.has(key);

  //     return {
  //       label: key,
  //       data: labels.map((time) => obj.data[time] || null),
  //       borderColor: isHighlight ? "red" : getRandomColor(0.5),
  //       borderWidth: 2,
  //       backgroundColor: "transparent",
  //       fill: false,
  //       spanGaps: true,
  //       pointRadius: labels.map((time) =>
  //         isHighlight && obj.points.includes(time) ? 4 : 0
  //       ), // 마커 표시
  //       pointHoverRadius: labels.map((time) =>
  //         isHighlight && obj.points.includes(time) ? 6 : 0
  //       ),
  //       showLine: false,
  //     };
  //   });

  //   if (chart) chart.destroy();

  //   chart = new Chart(canvas.getContext("2d"), {
  //     type: "line",
  //     data: { labels, datasets },
  //     options: {
  //       responsive: true,
  //       maintainAspectRatio: false,
  //       interaction: { mode: "index", intersect: false },
  //       scales: {
  //         y: { beginAtZero: true, title: { display: true, text: "EPD TIME" } },
  //         x: { title: { display: true, text: "Time" } },
  //       },
  //       plugins: {
  //         tooltip: {
  //           callbacks: {
  //             label: function (context) {
  //               return ` ${context.dataset.label}: ${context.formattedValue}`;
  //             },
  //           },
  //         },
  //         zoom: {
  //           wheel: { enabled: true },
  //           pinch: { enabled: true },
  //           mode: x,
  //           s,
  //         },
  //       },
  //     },
  //   });
  // }

  function drawNormalDistribution(rawData) {
    const eqchList = [...new Set(rawData.map((d) => `${d.eq}-${d.chamber}`))];
    const epdAll = rawData.map((d) => d.epdTime);
    const minX = Math.min(...epdAll) - 2;
    const maxX = Math.max(...epdAll) + 2;
    const step = (maxX - minX) / 300;

    const globalMean = (minX + maxX) / 2; // 무조건 중앙
    const fixedStdDev = 3; // 자연스러운 폭 (정규분포 특징)
    // 색상 고정 (반복 사용 가능)
    const colorMap = [
      "#FF6384",
      "#36A2EB",
      "#FFCE56",
      "#4BC0C0",
      "#9966FF",
      "#FF9F40",
      "#C9CBCF",
      "#8AFF33",
      "#33FFA5",
      "#FF33EC",
      "#A0522D",
      "#00CED1",
      "#DC143C",
      "#7FFF00",
      "#FFD700",
      "#40E0D0",
      "#8A2BE2",
      "#DEB887",
      "#5F9EA0",
      "#7CFC00",
      "#6495ED",
      "#FF4500",
      "#DA70D6",
      "#228B22",
      "#B22222",
      "#FF1493",
      "#1E90FF",
      "#CD5C5C",
      "#ADFF2F",
      "#D2691E",
      "#E9967A",
      "#BA55D3",
      "#9370DB",
      "#3CB371",
      "#4169E1",
      "#FF6347",
      "#708090",
      "#2E8B57",
      "#F08080",
      "#9932CC",
    ];

    const datasets = eqchList.map((eqch, idx) => {
      const filtered = rawData.filter((d) => `${d.eq}-${d.chamber}` === eqch);
      const epdList = filtered.map((d) => d.epdTime);
      const mean = epdList.reduce((a, b) => a + b, 0) / epdList.length;
      let stdDev = Math.sqrt(
        epdList.reduce((a, b) => a + Math.pow(b - mean, 2), 0) /
          (epdList.length - 1)
      );

      // 표준편차 최소값 보정 (너무 작으면 퍼짐 방지)
      stdDev = isNaN(stdDev) || stdDev < 1 ? 1.5 : stdDev;
      const scaleFactor = epdList.length < 10 ? 50 : 20;
      const normalCurve = [];
      for (let x = minX; x <= maxX; x += step) {
        const y =
          (1 / (stdDev * Math.sqrt(2 * Math.PI))) *
          Math.exp(-0.5 * Math.pow((x - mean) / stdDev, 2)) *
          scaleFactor;
        normalCurve.push({ x, y });
      }

      console.log(eqch, mean, stdDev);

      return {
        label: eqch,
        data: normalCurve,
        //borderColor: `hsl(${Math.floor(Math.random() * 360)}, 70%, 60%)`,
        borderColor: colorMap[idx % colorMap.length],
        borderWidth: 2,
        fill: false,
        pointRadius: 0,
      };
    });

    if (etchNormalCanvas) etchNormalCanvas.destroy();

    etchNormalCanvas = new Chart(canvasNormalChart.getContext("2d"), {
      type: "line",
      data: { datasets },
      options: {
        responsive: true,
        plugins: {
          title: {
            display: true,
            text: "EQ-CH 별 EPD TIME 정규분포",
            font: { size: 16 },
          },
          legend: {
            labels: {
              boxWidth: 20,
              font: { size: 12 },
            },
          },
        },
        scales: {
          x: {
            type: "linear",
            title: { display: true, text: "EPD TIME" },
            min: minX,
            max: maxX,
            ticks: {
              stepSize: 2,
              callback: function (value) {
                return value.toFixed(1);
              },
            },
          },
          y: {
            beginAtZero: true,
            suggestedMax: 1, // 너무 높게 안 가도록 제한
            title: { display: true, text: "Density (Scaled)" },
          },
        },
      },
    });
  }
  function drawScatterChart(rawData) {
    const eqchList = [...new Set(rawData.map((d) => `${d.eq}-${d.chamber}`))];

    const colorMap = [
      "#FF6384",
      "#36A2EB",
      "#FFCE56",
      "#4BC0C0",
      "#9966FF",
      "#FF9F40",
      "#C9CBCF",
      "#8AFF33",
      "#33FFA5",
      "#FF33EC",
      "#A0522D",
      "#00CED1",
      "#DC143C",
      "#7FFF00",
      "#FFD700",
      "#40E0D0",
      "#8A2BE2",
      "#DEB887",
      "#5F9EA0",
      "#7CFC00",
    ];

    const datasets = eqchList.map((eqch, idx) => {
      const filtered = rawData.filter((d) => `${d.eq}-${d.chamber}` === eqch);
      const points = filtered.map((d) => ({
        x: d.epdTime,
        y: d.reticleDensity,
      }));

      return {
        label: eqch,
        data: points,
        borderColor: colorMap[idx % colorMap.length],
        backgroundColor: colorMap[idx % colorMap.length],
        pointRadius: 4,
        showLine: false, // 선 없음
      };
    });

    if (etchscatterCanvas) etchscatterCanvas.destroy();

    etchscatterCanvas = new Chart(canvasScatterChart.getContext("2d"), {
      type: "scatter",
      data: { datasets },
      options: {
        responsive: true,
        plugins: {
          title: {
            display: true,
            text: "EQ-CH 별 EPD TIME vs RETICLE 산포도",
            font: { size: 16 },
          },
          legend: {
            labels: {
              boxWidth: 20,
              font: { size: 12 },
            },
          },
        },
        scales: {
          x: {
            title: { display: true, text: "EPD TIME" },
            ticks: {
              stepSize: 2,
              callback: function (value) {
                return value.toFixed(1);
              },
            },
          },
          y: {
            title: { display: true, text: "RETICLE DENSITY" },
          },
        },
      },
    });
  }
  function initChoices(selectId, placeholder) {
    const select = document.getElementById(selectId);
    if (!select) return;

    const instance = new Choices(select, {
      removeItemButton: true,
      placeholderValue: placeholder,
      searchEnabled: true,
    });

    choicesInstances[selectId] = instance;
  }

  function fillSelect(selectId, items) {
    const instance = choicesInstances[selectId];
    if (!instance) return;

    // 기존 선택값 유지하고 싶지 않으면 clear 먼저
    instance.clearChoices();

    // Choices에 값 설정
    instance.setChoices(
      items.map((item) => ({ value: item, label: item })),
      "value",
      "label",
      false
    );
  }

  function removePeriod(button) {
    button.closest(".period-row").remove();
  }

  function highlightLotSlot(targetLot, targetSlot) {
    chart.data.datasets.forEach((dataset) => {
      dataset.data.forEach((point) => {
        const isLotMatch = point.lot === targetLot;
        const isSlotMatch = targetSlot ? point.slot === targetSlot : true;
        if (isLotMatch && isSlotMatch) {
          point.radius = 6;
          point.backgroundColor = "red";
          point.borderColor = "red";
        } else {
          point.radius = 1;
          point.backgroundColor = point._baseColor;
          point.borderColor = point._baseColor;
        }
      });
    });
    chart.update();
  }

  // function highlightLotSlotWithEchar(targetLot, targetSlot) {
  //   const isReset = !targetLot || targetLot.trim() === "";

  //   const highlightedSeries = series.map((s) => {
  //     const newData = s.data.map((point) => {
  //       const isMatch =
  //         !isReset &&
  //         point.lot === targetLot &&
  //         (!targetSlot || point.slot === targetSlot);

  //       return {
  //         ...point,
  //         symbolSize: isMatch ? 10 : 4,
  //         itemStyle: isMatch
  //           ? { color: "red", borderColor: "red" }
  //           : point.itemStyle ?? {},
  //       };
  //     });

  //     return {
  //       ...s,
  //       data: newData,
  //     };
  //   });

  //   chart.setOption(
  //     { series: highlightedSeries },
  //     { replaceMerge: ["series"] }
  //   );
  // }

  function highlightLotSlotWithEchar(targetLot, targetSlot) {
    const isReset = !targetLot || targetLot.trim() === "";

    const highlightedSeries = series.map((s) => {
      const newData = s.data.map((point) => {
        const isMatch =
          !isReset &&
          point.lot === targetLot &&
          (!targetSlot || point.slot === targetSlot);

        return {
          ...point,
          symbolSize: isMatch ? 10 : 4,
          itemStyle: {
            ...(point.itemStyle || {}),
            color: isMatch ? "red" : point.itemStyle?.color ?? undefined,
            borderColor: isMatch
              ? "red"
              : point.itemStyle?.borderColor ?? undefined,
          },
        };
      });

      return {
        ...s,
        data: newData,
      };
    });

    try {
      chart.setOption(
        { series: highlightedSeries },
        { replaceMerge: ["series"] }
      );
    } catch (e) {
      console.error("하이라이트 적용 중 에러 발생:", e);
    }
  }
  // function highlightLotSlotWithEchar(targetLot, targetSlot) {
  //   const isReset = !targetLot || targetLot.trim() === "";

  //   const highlightedSeries = series.map((s) => {
  //     const newData = s.data.map((point) => {
  //       const isMatch =
  //         !isReset &&
  //         point.lot === targetLot &&
  //         (!targetSlot || point.slot === targetSlot);

  //       return {
  //         ...point,
  //         symbolSize: isMatch ? 10 : 4,
  //         itemStyle: isMatch ? { color: "red", borderColor: "red" } : undefined,
  //       };
  //     });
  //     return { ...s, data: newData };
  //   });

  //   chart.setOption({
  //     series: highlightedSeries,
  //   });
  // }

  // function highlightLotSlotWithEchar(targetLot, targetSlot) {
  //   const highlightedSeries = series.map((s) => {
  //     const newData = s.data.map((point) => {
  //       const isMatch =
  //         point.lot === targetLot && (!targetSlot || point.slot === targetSlot);
  //       return {
  //         ...point,
  //         symbolSize: isMatch ? 10 : 4,
  //         itemStyle: {
  //           color: isMatch ? "red" : undefined,
  //           borderColor: isMatch ? "red" : undefined,
  //         },
  //       };
  //     });
  //     return { ...s, data: newData };
  //   });

  //   chart.setOption({
  //     series: highlightedSeries,
  //   });
  // }
});

function generateSummaryTable(rawData) {
  const eqchMap = {};
  rawData.forEach((d) => {
    const eqch = `${d.eq}-${d.chamber}`;
    if (!eqchMap[eqch]) eqchMap[eqch] = [];
    eqchMap[eqch].push(d.epdTime);
  });

  const summary = Object.entries(eqchMap).map(([eqch, list]) => ({
    eqch,
    avg: (list.reduce((a, b) => a + b, 0) / list.length).toFixed(2),
    min: Math.min(...list),
    max: Math.max(...list),
  }));

  const maxAvg = Math.max(...summary.map((s) => Number(s.avg)));
  const maxMax = Math.max(...summary.map((s) => s.max));
  const minMin = Math.min(...summary.map((s) => s.min));

  const headRow = document.getElementById("summary-head");
  const avgRow = document.getElementById("avg-row");
  const minRow = document.getElementById("min-row");
  const maxRow = document.getElementById("max-row");

  headRow.innerHTML = "<th>TYPE</th>";
  avgRow.innerHTML = "<td>AVG</td>";
  minRow.innerHTML = "<td>MIN</td>";
  maxRow.innerHTML = "<td>MAX</td>";

  const renderedEqch = new Set();

  summary.forEach((row) => {
    //headRow.innerHTML += `<th>${row.eqch}</th>`;
    if (!renderedEqch.has(row.eqch)) {
      headRow.innerHTML += `<th>${row.eqch}</th>`;
      renderedEqch.add(row.eqch);
    }
    avgRow.innerHTML += `<td style="${
      row.avg == maxAvg ? "color:red;font-weight:bold;" : ""
    }">${row.avg}</td>`;
    minRow.innerHTML += `<td style="${
      row.min == minMin ? "color:red;font-weight:bold;" : ""
    }">${row.min}</td>`;
    maxRow.innerHTML += `<td style="${
      row.max == maxMax ? "color:red;font-weight:bold;" : ""
    }">${row.max}</td>`;
  });
}

function addPeriod() {
  const html = `
    <div class="form-row period-row d-flex align-items-center mb-1">
      <input type="date" name="startDate[]" class="form-control form-control-sm w-auto me-1" placeholder="시작일">
      ~
      <input type="date" name="endDate[]" class="form-control form-control-sm w-auto ms-1 me-2" placeholder="종료일">
      <button type="button" class="btn btn-danger btn-sm" onclick="removePeriod(this)">– 제거</button>
    </div>
  `;
  document
    .getElementById("periodContainer")
    .insertAdjacentHTML("beforeend", html);
}

function removePeriod(button) {
  const periodContainer = document.getElementById("periodContainer");
  const rows = periodContainer.getElementsByClassName("period-row");

  if (rows.length <= 1) {
    alert("구간은 최소 1개 이상 있어야 합니다.");
    return;
  }

  button.parentElement.remove();
}

async function reloadData(chart) {
  const min = chart.scales.x.min;
  const max = chart.scales.x.max;

  const response = await fetch("/etch-condition/data", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      ...requestData, // 기존 조건 그대로 유지
      periods: [
        {
          startDate: formatTimeToString(min),
          endDate: formatTimeToString(max),
        },
      ],
    }),
  });

  const newData = await response.json();
  drawChart(newData, highlightList);
}

const findNearestXIndex = (labels, targetTime) => {
  let nearestIndex = 0;
  let minDiff = Infinity;

  labels.forEach((label, idx) => {
    const diff = Math.abs(Number(label) - Number(targetTime));
    if (diff < minDiff) {
      minDiff = diff;
      nearestIndex = idx;
    }
  });

  return nearestIndex;
};
