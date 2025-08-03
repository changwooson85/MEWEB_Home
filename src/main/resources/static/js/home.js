const jobListContainer = document.getElementById("jobListContainer");
const jobResListBtn = document.getElementById("loadJobResList");
const jobListBtn = document.getElementById("viewEquipmentBtn");
const epdChartBtn = document.getElementById("etchEpdChart");

jobListContainer.addEventListener('click', function(e) {
    if (e.target && e.target.classList.contains('ajax-link')) {
        e.preventDefault();
        const url = e.target.getAttribute('href');
        fetch(url)
            .then(response => response.text())
            .then(html => {
                jobListContainer.innerHTML = html;
            })
            .catch(error => console.error('Error loading page:', error));
    }
});

document.addEventListener("DOMContentLoaded", function () {
    //const link = document.querySelectorAll('.ajax-link');
    bindSearchFormEvent();
      const switchButtons = document.querySelectorAll(".tab-switch-btn");
        switchButtons.forEach(btn => {
                    btn.addEventListener("click", function () {
                        const targetTabId = this.getAttribute("data-target-tab");
                        const tabToClick = document.getElementById(targetTabId);
                        if (tabToClick) {
                            tabToClick.click();
                        }
                    });
                });

    if (jobListBtn) {
        jobListBtn.addEventListener("click", function (event) {
            event.preventDefault(); // 기본 동작 방지
            var selectedResId = document.getElementById('equipmentSelect').value;
                                if (!selectedResId) {
                                    alert('Res_ID를 선택하세요.');
                                    return;
                                }
            document.getElementById("resIdInput").value = selectedResId;
            document.getElementById("deviceSearchForm").style.display = "block";
            loadJobList(0);
        });
    }

    if (epdChartBtn) {
            epdChartBtn.addEventListener("click", function (event) {
                event.preventDefault(); // 기본 동작 방지
                document.getElementById('epdChartSection').style.display = 'block';
                epdChartIntial();
            });
        }

    if (jobResListBtn) {
            jobResListBtn.addEventListener("click", function (event) {
                event.preventDefault(); // 기본 동작 방지
                loadJobResList();
            });
        }

    document.querySelectorAll("btn").forEach(function(button){
                 if (button.classList.contains('tabButton')){
                 button.addEventListener("click", function(){
                                     const tabId = this.getAttribute('data-tab')
                                     moveToTab(tabId);
                                 });
                 }
            });

    function moveToTab(tabId){
        const trigger = document.querySelector(`#tab-${tabId}`);
        if(trigger){
            const tab = new bootstrap.Tab(trigger);
            tab.show();
        }
    }
});
function bindSearchFormEvent() {
    const form = document.getElementById("deviceSearchForm");
    if (form) {
        console.log("submit 이벤트 바인딩됨");
        form.addEventListener("submit", function(event) {
            event.preventDefault();
            handleDeviceSearch(event);
        });
    } else {
        console.log("form 못 찾음");
    }
}
function handleDeviceSearch(event) {
    event.preventDefault();

    const form = event.target;
    const formData = new FormData(form);

    const device = formData.get("device");
    const selectedResId = formData.get("res_id");
    const size = 20;

    if (!selectedResId) {
        alert("Res_ID를 선택하세요.");
        return false;
    }

    // POST 방식 fetch
    fetch("/crasjoblst/search", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            device: device,
            res_id: selectedResId,
            size: size
        })
    })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                loadJobList(data.page, data.device);
            } else {
                alert(data.message || "검색 결과가 없습니다.");
            }
        })
        .catch(error => {
            console.error("Search error:", error);
        });

    return false;
}

function loadJobResList() {
            fetch("/crasjoblst/res_id") // Spring Boot의 CrasJobLst 데이터 요청
                 .then(function(response) {
                                return response.json();  // 서버에서 JSON 형식으로 res_id 정보를 반환한다고 가정
                 })
                 .then(function(data){
                    var equipmentSelect = document.getElementById('equipmentSelect');
                    equipmentSelect.innerHTML = '<option value="">-- Res_ID 선택 --</option>';
                    // data가 ["RES_ID01", "RES_ID02", ...]와 같은 배열이라고 가정
                    data.forEach(function(item) {
                        var option = document.createElement('option');
                        option.value = item;
                        option.text = item;
                        equipmentSelect.appendChild(option);
                         });
                    document.getElementById('equipmentSection').style.display = 'block';
                 })
                 .catch(function(error){
                    console.error("res_id 정보 로드 실패:", error);
                 });
}

function epdChartIntial() {

}
function loadJobList(page,  device ='') {

                var selectedResId = document.getElementById('equipmentSelect').value;
                if (!selectedResId) {
                    alert('Res_ID를 선택하세요.');
                    return;
                }
                const url =`/crasjoblst/view?page=${page}&size=20&res_id=${encodeURIComponent(selectedResId)}`;


                //"/crasjoblst/view?page=0&size=20&res_id=" + selectedResId
                fetch(url) // Spring Boot의 CrasJobLst 데이터 요청
                    .then(response => response.text())
                    .then(html => {
                        jobListContainer.innerHTML = html;
                        bindSearchFormEvent();
                        if (device) highlightRowByDevice(device);
                    })
                   .catch(error => console.error("Error loading Job List:", error));
}
function highlightRowByDevice(targetDevice) {
    const rows = document.querySelectorAll("#jobListContainer table tbody tr");
    rows.forEach(row => row.classList.remove("highlighted-row"));
    let found = false;
    rows.forEach(row => {
            const deviceCell = row.cells[1]; // DEVICE 컬럼 (두 번째 열)
            if (deviceCell && deviceCell.textContent.trim() === targetDevice.trim()) {
                row.classList.add("highlighted-row"); // ✅ CSS 클래스 적용
                row.scrollIntoView({ behavior: "smooth", block: "center" });
                found = true;
            }
        });
}
