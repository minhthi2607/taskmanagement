# Fix Review — Nhánh `thanh` (khắc phục 2 lỗi blocking + 1 test regression phát hiện ở `docs/review.md`)

**Ngày sửa:** 2026-08-27
**Bối cảnh:** Tiếp theo `docs/review.md` (QA test PR #31 gộp fix #24–#28 + Story #54) — phát hiện 2 lỗi blocking (issue #27 chưa hoàn thành do bug template, và lỗ hổng Open Redirect #26 chưa vá hết ở `NotificationController`) cùng 1 unit test bị regression. Tài liệu này ghi lại việc khắc phục và kết quả xác nhận lại bằng test thật (không giả lập).

---

## VIỆC 1 — Sửa lỗi `board-detail.html` (issue #27)

**Rà soát toàn bộ file** tìm mọi biểu thức so sánh trực tiếp với `currentUser.id`/`currentUser.xxx` chưa có null-guard (không chỉ đúng 3 dòng đã báo cáo ban đầu). Kết quả rà soát — 10 vị trí dùng `currentUser`, phân loại theo mức độ rủi ro thực tế:

| Dòng (trước khi sửa) | Có crash thật khi `currentUser == null`? | Xử lý |
|---|---|---|
| 152 — `member.user.id == currentUser.id` | ✅ Crash (đã xác nhận qua log gốc) | **Sửa** — thêm `currentUser != null and` |
| 158 — `!isBoardAdmin or member.user.id == currentUser.id` | Không (OR short-circuit tại `!isBoardAdmin` khi ẩn danh), nhưng phụ thuộc thứ tự operand mong manh | **Sửa** theo đúng yêu cầu — thêm `currentUser == null or` ở đầu |
| 163 — `isBoardAdmin and member.user.id != currentUser.id` | Không (AND short-circuit tại `isBoardAdmin=false`), tương tự | **Sửa** theo đúng yêu cầu — thêm `currentUser != null and` ở đầu |
| **698 — `card.watchers.![userId].contains(currentUser.id)`** | ✅ **Crash thật, PHÁT HIỆN MỚI qua rà soát** — không nằm trong 3 dòng báo cáo gốc, không có bất kỳ guard nào (kể cả gián tiếp), nằm trong modal chi tiết Card luôn được Thymeleaf render (kể cả khi modal chưa mở) | **Sửa** — thêm `currentUser != null and` |
| 173, 433 | Không — cả 2 nằm lồng trong `th:if="${isBoardAdmin}"` (dòng 172) / `th:if="${isBoardAdmin}"` (dòng 432, dạng `th:block`), `isBoardAdmin=false` với khách ẩn danh nên toàn bộ khối bị bỏ qua, `currentUser` không bao giờ được evaluate | Không sửa — đã an toàn theo cấu trúc |
| 815, 868 | Không — cả 2 mở đầu bằng `canEditBoard and (...)`, `canEditBoard(boardId, null)` luôn trả `false` (đã đọc `BoardPermissionServiceImpl.canEditBoard` dòng 111-116 xác nhận) nên short-circuit trước khi chạm `currentUser` | Không sửa — đã an toàn |
| 882, 888 | Không — cả 2 lồng trong `th:if="${canEditBoard}"` (dòng 881) | Không sửa — đã an toàn |
| 933 | — | Đã có sẵn `currentUser != null and ...` từ trước, dùng làm mẫu tham chiếu |

**Xác nhận không sửa thừa**: các vị trí không đổi đều đã kiểm chứng logic short-circuit + đọc trực tiếp code Java (`BoardPermissionServiceImpl.isBoardAdmin`/`canEditBoard` trả `false` khi `userId == null`, không throw), không phải suy đoán.

### Xác nhận bằng test thật (không giả lập)

```
GET http://localhost:8080/board/7  (không cookie, ẩn danh)
→ HTTP 200, <title>QA Public Board - Bảng công việc</title> (đúng 1 title, không lẫn error/500)
→ nội dung TaskList "To Do"/"Doing" và Card "QA Test Card" hiển thị đầy đủ
→ 0 dấu hiệu trang lỗi 500
```
Test chéo thêm trên `board/8` ("Test Board", PUBLIC — phát sinh độc lập trong lúc để môi trường mở cho kiểm tra tay) — trước khi sửa từng lỗi lúc 17:46:37 (log `ERROR ... Exception processing template "board/board-detail"`), sau khi sửa và deploy lại (19:12) truy cập lại `/board/8` → **HTTP 200, render đúng, 0 lỗi**. Xác nhận fix không chỉ đúng cho 1 board cụ thể.

---

## VIỆC 2 — Vá Open Redirect ở `NotificationController` (issue #26)

- Tạo `src/main/java/com/project/taskmanagement/exception/SafeRedirectHelper.java` (`@Component`) — copy nguyên logic từ `GlobalExceptionHandler.getSafeRedirectUrl()` cũ (chấp nhận path tương đối; với URL tuyệt đối kiểm tra host/port khớp server; log cảnh báo khi chặn; fallback `defaultUrl`).
- `GlobalExceptionHandler.java`: inject `SafeRedirectHelper` qua constructor (`@RequiredArgsConstructor`), xóa method `getSafeRedirectUrl()` cũ, 6 chỗ gọi đổi thành `safeRedirectHelper.getSafeRedirectUrl(...)` — **không đổi hành vi** (đã xác nhận qua `detect_changes()` + build/test PASS).
- `NotificationController.java`: inject `SafeRedirectHelper`, thay cả 2 chỗ dùng `request.getHeader("Referer")` thô (trong `markAsRead` và `markAllAsRead`) bằng gọi qua helper.

### Xác nhận bằng test thật (request thật, Referer giả, không giả lập)

```
POST /notifications/read-all          Header: Referer: http://evil.com/phishing
→ TRƯỚC FIX: HTTP 302, Location: http://evil.com/phishing   (khai thác được)
→ SAU FIX:   HTTP 302, Location: http://localhost:8080/      ✅ an toàn

POST /notifications/999999/read       Header: Referer: http://evil.com/phishing2
→ TRƯỚC FIX: HTTP 302, Location: http://evil.com/phishing2  (khai thác được)
→ SAU FIX:   HTTP 302, Location: http://localhost:8080/      ✅ an toàn
```
Log xác nhận đúng cảnh báo mong đợi phát ra từ class mới:
```
WARN c.p.t.exception.SafeRedirectHelper : Chặn redirect không an toàn, referer: http://evil.com/phishing
WARN c.p.t.exception.SafeRedirectHelper : Chặn redirect không an toàn, referer: http://evil.com/phishing2
```

---

## VIỆC 3 — Sửa test regression

`TeamMemberServiceImplTest.acceptInvitation_DelegatesToSyncServiceOnly`: đổi `invitation.email` từ hard-code `"newmember@example.com"` (không khớp `currentUser.getEmail()` = `"admin@example.com"` từ `@BeforeEach`) thành `currentUser.getEmail()` — test giờ đi qua đúng guard #28 rồi mới tới đoạn logic thực sự cần kiểm tra (delegate qua `boardMemberSyncService`). Chỉ sửa trong đúng 1 test method, không đụng `setUp()` dùng chung (tránh ảnh hưởng `removeMember_DelegatesToSyncServiceOnly`).

---

## BƯỚC CUỐI — Build & Test

| Task | Kết quả |
|---|---|
| `./gradlew compileJava` | ✅ BUILD SUCCESSFUL |
| `./gradlew test` | ✅ **75/75 PASS — 0 FAIL** |

### Chi tiết theo từng class (không còn fail)

| Class | Tests | Failures |
|---|---:|---:|
| TaskListControllerTest | 2 | 0 |
| BoardMemberServiceImplTest | 2 | 0 |
| BoardMemberSyncServiceImplTest | 2 | 0 |
| BoardPermissionServiceImplTest | 23 | 0 |
| BoardServiceImplTest | 7 | 0 |
| CardServiceImplTest | 11 | 0 |
| CardTimeLogServiceImplTest | 7 | 0 |
| LabelServiceImplTest | 10 | 0 |
| TaskListServiceImplTest | 5 | 0 |
| **TeamMemberServiceImplTest** | 2 | **0** ✅ (trước đó 1 fail) |
| TeamServiceImplTest | 3 | 0 |
| TaskmanagementApplicationTests | 1 | 0 |
| **Tổng** | **75** | **0** |

Không có test nào khác bị ảnh hưởng bởi việc thêm `SafeRedirectHelper` (đúng như `detect_changes()` dự đoán trước khi sửa: risk LOW, chỉ 3 class liên quan — `NotificationController`, `GlobalExceptionHandler`, `TeamMemberServiceImplTest`).

### Kiểm thử HTTP thật lại đúng 2 kịch bản đã FAIL trong `docs/review.md`

1. **Xem board PUBLIC ẩn danh** (`GET /board/7`, không cookie) → ✅ PASS — xem được nội dung bình thường, không còn lỗi Thymeleaf/HTML vỡ.
2. **Open Redirect qua `NotificationController`** (Referer giả `http://evil.com/phishing`) → ✅ PASS — redirect về trang mặc định an toàn, không sang evil.com.

### Log sau khi deploy fix (19:12 trở đi)

Không phát hiện `ERROR` nào mới. Chỉ có `WARN` mong đợi từ `SafeRedirectHelper` khi chặn 2 lần test Referer giả ở trên.

---

## Kết luận

Cả 2 lỗi blocking và 1 test regression trong `docs/review.md` đã được khắc phục và xác nhận lại bằng test thật (HTTP request thật, không giả lập), build/test suite sạch 75/75. Đủ điều kiện merge vào `dev` xét theo phạm vi đã test trong tài liệu này.

**Còn lại (đã ghi nhận ở `docs/review.md`, không thuộc phạm vi bắt buộc của lần sửa này):**
- Story #54 (nút "Theo dõi") và popup xác nhận xóa file đính kèm (#24) đã xác minh đúng qua đọc code JS/HTML, khuyến khích người dùng tự kiểm tra nhanh qua UI thật 1 lần trước khi release cho chắc chắn tuyệt đối.
