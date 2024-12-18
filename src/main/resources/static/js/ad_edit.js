document.addEventListener('DOMContentLoaded', function () {
    // 폼 검증 및 제출
    const editForm = document.querySelector('#editForm');
    if (editForm) {
        editForm.addEventListener('submit', function (event) {
            event.preventDefault(); // 기본 폼 제출 방지

            const adStartInput = document.querySelector('#adStart');
            const adEndInput = document.querySelector('#adEnd');
            const adTitleInput = document.querySelector('#adTitle');
            const adUrlInput = document.querySelector('#adUrl');

            // 필수 입력 검증
            if (!adTitleInput.value.trim()) {
                alert('광고명을 입력해주세요.');
                return;
            }
            if (!adUrlInput.value.trim()) {
                alert('광고 URL을 입력해주세요.');
                return;
            }
            if (new Date(adStartInput.value) > new Date(adEndInput.value)) {
                alert('광고 종료일은 광고 시작일보다 이전일 수 없습니다.');
                return;
            }

            // 폼 데이터 생성
            const formData = new FormData(editForm);

            // 서버로 수정 요청
            fetch(editForm.action, {
                method: 'POST',
                body: formData,
            })
                .then(response => {
                    if (!response.ok) {
                        throw new Error('서버 요청 실패');
                    }
                    return response.json();
                })
                .then(data => {
                    alert(data.message || '수정이 완료되었습니다.');
                    window.location.href = '/ad/list'; // 수정 완료 후 목록 페이지로 이동
                })
                .catch(error => {
                    console.error('Error:', error);
                    alert('수정 중 오류가 발생했습니다. 다시 시도해주세요.');
                });
        });
    }

    // 이미지 미리보기
    const imageInput = document.querySelector('#adImageInput');
    const imagePreview = document.querySelector('#imagePreview');
    const existingImage = document.querySelector('#existingImage');

    if (imageInput) {
        imageInput.addEventListener('change', (event) => {
            const file = event.target.files[0];
            if (file) {
                const reader = new FileReader();

                reader.onload = (e) => {
                    // 기존 이미지를 숨기고 새 미리보기 이미지를 표시
                    if (existingImage) existingImage.style.display = 'none';

                    const previewImage = document.createElement('img');
                    previewImage.src = e.target.result;
                    previewImage.alt = "새로 업로드된 이미지";

                    // 기존 미리보기 영역 초기화 후 새 이미지 추가
                    imagePreview.innerHTML = '';
                    imagePreview.appendChild(previewImage);
                };

                reader.readAsDataURL(file);
            } else {
                // 파일 선택이 취소되었을 경우 기존 이미지를 다시 표시
                if (existingImage) existingImage.style.display = 'block';
                imagePreview.innerHTML = '';
                imagePreview.appendChild(existingImage);
            }
        });
    }
});
