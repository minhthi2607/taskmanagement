function showAddCardForm(listId) {
    document.getElementById('addCardTrigger-' + listId).classList.add('d-none');
    const form = document.getElementById('addCardForm-' + listId);
    form.classList.remove('d-none');
    form.querySelector('input[name="title"]').focus();
}

function hideAddCardForm(listId) {
    document.getElementById('addCardTrigger-' + listId).classList.remove('d-none');
    document.getElementById('addCardForm-' + listId).classList.add('d-none');
}

// Story #50: Bật/Tắt form chỉnh sửa bình luận tại chỗ
function toggleEditComment(commentId) {
    const textEl = document.getElementById('commentText-' + commentId);
    const formEl = document.getElementById('commentEditForm-' + commentId);
    if (textEl && formEl) {
        textEl.classList.toggle('d-none');
        formEl.classList.toggle('d-none');
    }
}

// Story #35: Khôi phục lại mô tả gốc khi đóng modal chi tiết Card
// mà không bấm Lưu, tránh hiển thị sai nội dung mô tả ở lần mở kế tiếp.
document.querySelectorAll('[id^="cardDetailModal-"]').forEach(function (modalEl) {
    let originalDescription = '';

    modalEl.addEventListener('show.bs.modal', function () {
        const textarea = modalEl.querySelector('textarea[name="description"]');
        if (textarea) {
            originalDescription = textarea.value;
        }
    });

    modalEl.addEventListener('hidden.bs.modal', function () {
        const textarea = modalEl.querySelector('textarea[name="description"]');
        if (textarea) {
            textarea.value = originalDescription;
        }
    });
});

// Story #33/#34: Dropdown "Di chuyển sang danh sách khác" được Thymeleaf render 1 lần
// lúc tải trang nên bị stale sau khi kéo-thả Card bằng AJAX (không F5). Mỗi lần mở modal,
// đọc lại danh sách TaskList thật từ DOM (.card-list) và TaskList hiện tại của Card
// (dựa trên card đang nằm trong .card-list nào tại thời điểm này) để sinh lại option mới.
document.querySelectorAll('[id^="cardDetailModal-"]').forEach(function (modalEl) {
    modalEl.addEventListener('show.bs.modal', function () {
        var cardId = modalEl.id.replace('cardDetailModal-', '');
        var $currentCardEl = $('[data-card-id="' + cardId + '"]');
        var $select = $(modalEl).find('select[name="targetTaskListId"]');
        if (!$currentCardEl.length || !$select.length) {
            return;
        }

        var currentTaskListId = String($currentCardEl.closest('.card-list').data('tasklist-id'));

        // Giữ lại option rỗng/placeholder đầu tiên (nếu có), xóa hết option còn lại
        $select.find('option').filter(function () {
            return this.value !== '';
        }).remove();

        $(".card-list").each(function () {
            var taskListId = String($(this).data('tasklist-id'));
            if (taskListId === currentTaskListId) {
                return;
            }
            $select.append(
                $('<option></option>').attr('value', taskListId).text($(this).data('tasklist-name'))
            );
        });
    });
});

// Story #33/#34: Kéo-thả Card (đổi vị trí trong cùng TaskList / chuyển sang TaskList khác)
// gộp chung 1 thao tác duy nhất (jQuery UI Sortable) - xem GEMINI.md mục 13.2.
// Ô nhập số thủ công (mục "e"/"f" trong modal chi tiết Card) vẫn giữ nguyên làm fallback bàn phím.
var cardDragOriginalTaskListId = null;

$(".card-list").sortable({
    connectWith: ".card-list",
    items: "> [data-card-id]",
    placeholder: "card-placeholder",
    tolerance: "pointer",
    start: function (event, ui) {
        cardDragOriginalTaskListId = String(ui.item.closest(".card-list").data("tasklist-id"));
    },
    stop: function (event, ui) {
        handleCardDrop(ui.item);
    }
}).disableSelection();

// CSRF: dự án dùng CSRF mặc định của Spring Security, Thymeleaf tự chèn
// input ẩn name="_csrf" vào mọi <form method="post"> đã có sẵn (reorder/move...).
// Lấy lại đúng giá trị đó cho fetch() thay vì tự nghĩ cơ chế khác.
function getCsrfToken() {
    var input = document.querySelector('input[name="_csrf"]');
    return input ? input.value : null;
}

// Công thức position thưa (mục 8): (position_trước + position_sau) / 2
function calculateCardPosition($cardEl) {
    var $prev = $cardEl.prevAll('[data-card-id]').first();
    var $next = $cardEl.nextAll('[data-card-id]').first();
    var prevPos = $prev.length ? parseInt($prev.attr('data-position'), 10) : null;
    var nextPos = $next.length ? parseInt($next.attr('data-position'), 10) : null;

    if (prevPos !== null && nextPos !== null) {
        return Math.floor((prevPos + nextPos) / 2);
    }
    if (prevPos === null && nextPos !== null) {
        return nextPos > 10 ? (nextPos - 10) : Math.floor(nextPos / 2);
    }
    if (prevPos !== null && nextPos === null) {
        return prevPos + 10;
    }
    return 10; // thẻ duy nhất trong danh sách
}

function handleCardDrop($cardEl) {
    var cardId = $cardEl.attr('data-card-id');
    var newTaskListId = String($cardEl.closest('.card-list').data('tasklist-id'));
    var isMove = newTaskListId !== cardDragOriginalTaskListId;
    var newPosition = calculateCardPosition($cardEl);

    var params = new URLSearchParams();
    params.set('_csrf', getCsrfToken() || '');
    params.set('position', newPosition);

    var url;
    if (isMove) {
        url = '/card/' + cardId + '/move';
        params.set('targetTaskListId', newTaskListId);
    } else {
        url = '/card/' + cardId + '/reorder';
    }

    fetch(url, {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: params.toString()
    }).then(function (response) {
        if (!response.ok) {
            throw new Error('Request thất bại với status ' + response.status);
        }
        // Thành công: chỉ cập nhật lại position trong DOM, không reload toàn trang.
        $cardEl.attr('data-position', newPosition);
    }).catch(function () {
        alert('Có lỗi xảy ra khi cập nhật vị trí thẻ. Trang sẽ được tải lại để đồng bộ dữ liệu.');
        window.location.reload();
    });
}

// Story #30 (Mục 13.2): Kéo-thả TaskList bằng jQuery UI Sortable + AJAX
$(document).ready(function () {
    const canEdit = window.boardConfig ? window.boardConfig.canEditBoard : false;
    const $container = $('#task-list-sortable-container');

    // Đọc CSRF token và header name từ meta tag (phục vụ Spring Security CSRF)
    const csrfToken = $('meta[name="_csrf"]').attr('content');
    const csrfHeader = $('meta[name="_csrf_header"]').attr('content');

    if ($container.length && canEdit) {
        $container.sortable({
            items: '> .task-list-column',
            handle: '.card-header',
            placeholder: 'task-list-placeholder',
            cursor: 'grabbing',
            opacity: 0.85,
            tolerance: 'pointer',
            start: function (event, ui) {
                ui.placeholder.css({
                    'width': ui.item.outerWidth() + 'px',
                    'height': ui.item.outerHeight() + 'px',
                    'background-color': '#e2e8f0',
                    'border': '2px dashed #94a3b8',
                    'border-radius': '0.5rem',
                    'flex-shrink': '0'
                });
            },
            update: function (event, ui) {
                const $columns = $container.children('.task-list-column');
                $columns.each(function (index) {
                    const listId = $(this).data('list-id');
                    const newPosition = (index + 1) * 10;

                    $(this).data('list-position', newPosition);
                    $(this).find('.badge-position-display').text('#' + newPosition);

                    // Đóng gói headers truyền CSRF + XMLHttpRequest
                    const ajaxHeaders = { 'X-Requested-With': 'XMLHttpRequest' };
                    if (csrfHeader && csrfToken) {
                        ajaxHeaders[csrfHeader] = csrfToken;
                    }

                    // Gửi AJAX POST cập nhật vị trí ngầm (optimistic update)
                    $.ajax({
                        url: '/task-list/' + listId + '/update-position',
                        type: 'POST',
                        headers: ajaxHeaders,
                        data: { position: newPosition },
                        error: function (xhr, status, error) {
                            console.error('Lỗi khi cập nhật vị trí danh sách ID ' + listId + ':', error);
                        }
                    });
                });
            }
        });
    }

    // Story #37: Tìm kiếm thành viên trong modal Card
    document.querySelectorAll('.member-search-input').forEach(function(input) {
        input.addEventListener('input', function() {
            const cardId = this.getAttribute('data-card-id');
            const query = this.value.toLowerCase().trim();
            const resultsContainer = document.getElementById('memberSearchResults-' + cardId);
            const items = resultsContainer.querySelectorAll('.member-search-item');
            const noResultsMsg = resultsContainer.querySelector('.no-results-msg');
            let hasResults = false;

            if (query.length > 0) {
                resultsContainer.style.display = 'block';
                items.forEach(function(item) {
                    const name = item.querySelector('.member-name').textContent.toLowerCase();
                    const email = item.querySelector('.member-email').textContent.toLowerCase();
                    if (name.includes(query) || email.includes(query)) {
                        item.style.setProperty('display', 'block', 'important');
                        hasResults = true;
                    } else {
                        item.style.setProperty('display', 'none', 'important');
                    }
                });
                if (hasResults) {
                    noResultsMsg.style.setProperty('display', 'none', 'important');
                } else {
                    noResultsMsg.style.setProperty('display', 'block', 'important');
                }
            } else {
                resultsContainer.style.display = 'none';
            }
        });
    });
});
