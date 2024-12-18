document.addEventListener('DOMContentLoaded', function () {
    // "select-all" 체크박스
    const selectAllCheckbox = document.querySelector('.select-all');
    if (selectAllCheckbox) {
        selectAllCheckbox.addEventListener('change', function () {
            const rowCheckboxes = document.querySelectorAll('.row-checkbox');
            rowCheckboxes.forEach(checkbox => (checkbox.checked = selectAllCheckbox.checked));
        });
    }

    // 광고 삭제 버튼
    const deleteButton = document.querySelector('.register-btn-delete');
    if (deleteButton) {
        deleteButton.addEventListener('click', function () {
            const selectedIds = [];
            const checkedBoxes = document.querySelectorAll('.row-checkbox:checked');

            checkedBoxes.forEach(checkbox => {
                const id = checkbox.getAttribute('data-id');
                if (id) selectedIds.push(parseInt(id, 10));
            });

            if (selectedIds.length > 0) {
                if (confirm(`선택한 ${selectedIds.length}개의 광고를 삭제하시겠습니까?`)) {
                    fetch('/api/ad/delete', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({ ids: selectedIds }),
                    })
                        .then(response => {
                            if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
                            return response.json();
                        })
                        .then(result => {
                            alert(result.message || '광고가 삭제되었습니다.');
                            location.reload();
                        })
                        .catch(error => {
                            console.error('Error:', error);
                            alert('광고 삭제 중 문제가 발생했습니다.');
                        });
                }
            } else {
                alert('삭제할 광고를 선택하세요.');
            }
        });
    }

    // 각 행 클릭 이벤트 처리
    const adRows = document.querySelectorAll('.ad-item .row');
    adRows.forEach(row => {
        row.addEventListener('click', function (event) {
            if (event.target.closest('input[type="checkbox"]')) return; // 체크박스 클릭 방지
            const adId = this.getAttribute('data-id');
            if (adId) {
                window.location.href = `/ad/detail/${adId}`;
            }
        });
    });

    // 필터링 로직
    const positionFilters = document.querySelectorAll('#position-filters .filter-btn');
    const statusFilters = document.querySelectorAll('#status-filters .filter-btn');
    const adItems = document.querySelectorAll('.ad-item');

    function applyFilters() {
        const selectedPosition = document.querySelector('#position-filters .selected')?.dataset.filter || 'all';
        const selectedStatus = document.querySelector('#status-filters .selected')?.dataset.filter || 'all';

        adItems.forEach(item => {
            const position = item.dataset.position;
            const status = item.dataset.status;

            const positionMatch = selectedPosition === 'all' || position === selectedPosition;
            const statusMatch = selectedStatus === 'all' || status === selectedStatus;

            item.style.display = positionMatch && statusMatch ? '' : 'none';
        });
    }

    function handleFilterClick(filterGroup, button) {
        filterGroup.forEach(btn => btn.classList.remove('selected'));
        button.classList.add('selected');
        applyFilters();
    }

    positionFilters.forEach(button => {
        button.addEventListener('click', () => handleFilterClick(positionFilters, button));
    });

    statusFilters.forEach(button => {
        button.addEventListener('click', () => handleFilterClick(statusFilters, button));
    });
});
