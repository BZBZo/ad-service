document.addEventListener('DOMContentLoaded', () => {
    const statusButtons = document.querySelectorAll('.status-btn');

    statusButtons.forEach(button => {
        button.addEventListener('click', function () {
            const adId = button.getAttribute('data-id'); // 광고 ID 가져오기
            const status = button.getAttribute('data-status'); // 클릭한 버튼의 상태

            if (!adId || !status) {
                alert('광고 ID 또는 상태 값이 누락되었습니다.');
                return;
            }

            // 상태 변경 요청
            fetch(`/api/ad/updateStatus/${adId}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ status: status }), // 상태를 서버로 전송
            })
                .then(response => {
                    if (!response.ok) {
                        throw new Error('서버 응답 오류');
                    }
                    return response.json();
                })
                .then(data => {
                    alert(data.message || '상태가 성공적으로 변경되었습니다.');
                    window.location.href = '/ad/list?updated=true';
                })
                .catch(error => {
                    console.error('Error:', error);
                    alert('상태 변경 중 오류 발생');
                });
        });
    });
});
