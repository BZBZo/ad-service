document.addEventListener('DOMContentLoaded', () => {
    const adImageInput = document.getElementById('adImageInput');
    const previewImage = document.getElementById('previewImage');
    const guidelinesText = document.getElementById('guidelinesText');
    const deleteImageButton = document.getElementById('deleteImageButton');
    const adAreaInputs = document.querySelectorAll('input[name="adArea"]'); // 광고 위치 라디오 버튼
    const adForm = document.querySelector('.ad-form'); // 광고 신청 폼

    if (!adImageInput || !previewImage || !guidelinesText || !deleteImageButton || !adForm) {
        console.error("Required elements not found in the DOM.");
        return;
    }

    // 광고 위치에 따른 가이드라인 변경
    if (adAreaInputs) {
        adAreaInputs.forEach((input) => {
            input.addEventListener('change', function () {
                updateGuidelines(input.value);
            });
        });

        // 페이지 로드 시 기본 가이드라인 설정
        const selectedArea = document.querySelector('input[name="adArea"]:checked');
        if (selectedArea) {
            updateGuidelines(selectedArea.value);
        }
    }

    // 이미지 선택 시 미리보기 표시
    adImageInput.addEventListener('change', function (event) {
        const file = event.target.files[0];

        if (file) {
            const reader = new FileReader();
            reader.onload = function (e) {
                previewImage.src = e.target.result;
                previewImage.style.display = 'block';
                guidelinesText.style.display = 'none';
                deleteImageButton.style.display = 'block'; // 삭제 버튼 표시
            };
            reader.readAsDataURL(file);
        } else {
            resetImagePreview();
        }
    });

    // X 버튼 클릭 시 이미지 초기화
    deleteImageButton.addEventListener('click', function () {
        resetImagePreview();
        adImageInput.value = ""; // 파일 입력 값 초기화
    });

    // 폼 제출 시 광고 등록 요청
    adForm.addEventListener('submit', function (event) {
        event.preventDefault(); // 기본 폼 제출 방지

        const formData = new FormData(adForm);

        fetch('/api/ad/write', {
            method: 'POST',
            body: formData,
        })
            .then(response => {
                if (response.ok) {
                    alert('광고가 성공적으로 등록되었습니다.');
                    window.location.href = '/ad/list'; // 등록 후 이동할 페이지
                } else {
                    return response.text().then(text => { throw new Error(text); });
                }
            })
            .catch(error => {
                console.error('Error:', error);
                alert('광고 등록 중 오류가 발생했습니다.\n' + error.message);
            });
    });

    // 이미지 및 버튼 초기화 함수
    function resetImagePreview() {
        previewImage.src = '';
        previewImage.style.display = 'none';
        guidelinesText.style.display = 'block';
        deleteImageButton.style.display = 'none'; // 삭제 버튼 숨기기
    }

    // 광고 위치에 따른 가이드라인 업데이트 함수
    function updateGuidelines(position) {
        switch (position) {
            case 'side':
                guidelinesText.innerHTML = '<p>사이드: 가로 500px 세로 800px</p>';
                break;
            case 'top':
                guidelinesText.innerHTML = '<p>메인 상단: 가로 1600px 세로 840px</p>';
                break;
            case 'mid':
                guidelinesText.innerHTML = '<p>메인 중앙: 가로 1600px 세로 150px</p>';
                break;
            default:
                guidelinesText.innerHTML = '<p>위치를 선택하세요</p>';
                break;
        }
    }
});
