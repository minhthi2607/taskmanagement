Luôn giao tiếp với nhau bằng tiếng Việt

Đây là file context cho dự án **Task Management** — dùng để AI hiểu về dự án khi hỗ trợ code.

## 1. Giới thiệu dự án

Ứng dụng web quản lý công việc theo mô hình kiểu Trello: người dùng tạo **Nhóm (Team)**, trong mỗi Nhóm có nhiều **Bảng (Board)** để cộng tác quản lý công việc. Trong mỗi Bảng có nhiều **Danh sách công việc (TaskList)**, trong mỗi TaskList có nhiều **Thẻ (Card)**.

- Đây là đồ án tốt nghiệp khoá học CodeGym, làm theo quy trình **Scrum**, chia thành nhiều Sprint.
- Nhóm phát triển gồm 5 thành viên, mỗi người phụ trách 1 module riêng.
- **Sprint 1** (đã hoàn thành): xây nền tảng — khung giao diện, đăng nhập/đăng ký, quản lý Nhóm và thành viên Nhóm.
- **Sprint hiện tại: Sprint 2** — xây dựng Board, BoardMember, TaskList và Card (chức năng cốt lõi kiểu Trello).

**Tiến độ Sprint 2:**
- ✅ Đã hoàn thành: 9 Entity mới + cập nhật `Invitation` (xem mục 6, đánh dấu ✅)
- ⏳ Đang làm tiếp: Repository → Service → Controller
- ⚠️ Lưu ý: validate "đúng 1 trong 2 `teamId`/`boardId` có giá trị" khi tạo Invitation sẽ xử lý ở tầng Service (#23), chưa làm ở Entity

## 2. Công nghệ sử dụng

- **Ngôn ngữ**: Java 17
- **Framework**: Spring Boot 3.5.x (Spring MVC, không dùng REST API riêng — server-side render)
- **Build tool**: Gradle
- **Database**: MySQL, truy xuất qua Spring Data JPA / Hibernate
- **View**: Thymeleaf + Thymeleaf Layout Dialect (`nz.net.ultraq.thymeleaf:thymeleaf-layout-dialect`) + `thymeleaf-extras-springsecurity6` (ẩn/hiện phần tử theo trạng thái đăng nhập)
- **CSS / UI**: Bootstrap 5.3 + Bootstrap Icons (`bootstrap-icons`)
- **Bảo mật**: Spring Security — đăng nhập session-based (không dùng JWT), mã hoá mật khẩu bằng BCrypt
- **Validate**: Spring Boot Validation (Hibernate Validator)
- **Gửi mail**: Spring Boot Starter Mail (dùng cho tính năng mời thành viên qua email — Team lẫn Board)
- **Khác**: Lombok (giảm boilerplate code)

## 3. Cấu trúc thư mục & Layout Template

```
src/main/java/com/project/taskmanagement/
 ├─ config/          → cấu hình: SecurityConfig, CustomUserDetailsService, UserPrincipal
 ├─ controller/       → xử lý request, trả về view Thymeleaf
 ├─ dto/               → object nhận dữ liệu từ form (không dùng Entity trực tiếp)
 ├─ entity/            → các bảng dữ liệu (JPA Entity)
 ├─ enums/             → enum dùng chung (Role, Visibility, InvitationStatus...)
 ├─ exception/         → xử lý lỗi tập trung (GlobalExceptionHandler)
 ├─ repository/        → Spring Data JPA repository
 └─ service/           → interface xử lý logic nghiệp vụ
     └─ impl/           → class triển khai interface service

src/main/resources/
 ├─ templates/
 │   ├─ layout/         → fragment dùng chung: header.html, sidebar.html, footer.html
 │   ├─ auth/            → login.html, register.html
 │   ├─ user/             → profile.html, change-password.html
 │   ├─ team/             → team-list.html, team-detail.html, team-form.html, team-members.html
 │   ├─ board/            → board-list.html, board-detail.html, board-form.html, board-members.html   [Sprint 2]
 │   └─ error/            → trang lỗi (403, 404...)
 └─ static/
     ├─ css/
     ├─ js/
     └─ uploads/           → nơi lưu ảnh avatar / file đính kèm (lưu local, không dùng cloud storage)
```

## 4. Quy ước đặt tên

| Đối tượng | Quy tắc | Ví dụ |
|---|---|---|
| Class Entity | PascalCase, số ít | `User`, `Team`, `Board`, `TaskList`, `Card` |
| Tên bảng DB | snake_case, số nhiều (tự map qua JPA) | `users`, `teams`, `boards`, `task_lists`, `cards` |
| Field Java | camelCase | `displayName`, `createdAt`, `taskListId` |
| Khóa chính | luôn là `id` (Long, auto increment) | `id` |
| Khóa ngoại | `<tên_entity>_id` | `team_id`, `board_id`, `task_list_id`, `card_id` |
| Enum | class PascalCase, giá trị UPPER_SNAKE_CASE | `Visibility { PRIVATE, GROUP, PUBLIC }` |
| Session lưu user đăng nhập | thống nhất tên | `SESSION_USER` |

> **Lưu ý quan trọng**: Không đặt tên entity là `Group` vì đây là từ khoá dành riêng (reserved keyword) trong MySQL. Dùng `Team` thay cho khái niệm "Nhóm".

## 5. Sơ đồ quan hệ tổng quát (Sprint 1 + Sprint 2)

```
Team ──< Board ──< TaskList ──< Card
 │          │                     │
 │          │                     ├──< CardMember (User)
 │          │                     ├──< CardLabel ──> Label
 │          │                     ├──< CardAttachment
 │          │                     └──< CardComment
 │          │
 │          ├──< BoardMember (User)   [Sprint 2]
 │          └──< Label (kho nhãn dùng chung của Board)   [Sprint 2]
 │
 └──< TeamMember (User)   [Sprint 1]
```

## 6. Entity chi tiết

> Các bảng dưới đây là đặc tả chi tiết field cho từng Entity. Khi sinh code JPA Entity, dùng đúng tên field, kiểu dữ liệu và ghi chú (not null, unique, FK, default...) như mô tả.

### 6.1 User (Sprint 1)

| Field | Kiểu | Ghi chú |
|---|---|---|
| id | Long | PK |
| email | String | unique, not null |
| displayName | String | not null |
| phone | String | |
| password | String | not null, mã hoá BCrypt |
| avatarUrl | String | |
| createdAt | LocalDateTime | |

### 6.2 Team — Nhóm (Sprint 1)

| Field | Kiểu | Ghi chú |
|---|---|---|
| id | Long | PK |
| name | String | not null |
| type | String | loại nhóm |
| visibility | Enum (`PUBLIC`, `PRIVATE`) | not null |
| description | String | |
| createdBy | Long (FK → User) | id người tạo, tự động là Quản trị |
| createdAt | LocalDateTime | |

### 6.3 TeamMember — bảng trung gian User–Team (Sprint 1)

| Field | Kiểu | Ghi chú |
|---|---|---|
| id | Long | PK |
| teamId | Long (FK → Team) | |
| userId | Long (FK → User) | |
| role | Enum (`ADMIN`, `MEMBER`) | Quản trị nhóm / Thành viên |
| joinedAt | LocalDateTime | |

### 6.4 Board — Bảng ✅ [Đã code]

Cập nhật so với bản nháp Sprint 1.

| Field | Kiểu | Ghi chú |
|---|---|---|
| id | Long | PK |
| teamId | Long (FK → Team) | |
| name | String | not null |
| visibility | Enum `BoardVisibility` (`PRIVATE`, `GROUP`, `PUBLIC`) | **[MỚI]** — xem chi tiết bên dưới |
| createdBy | Long (FK → User) | tự động là Quản trị bảng (BoardMember role ADMIN) |
| createdAt | LocalDateTime | |

Về field `visibility`:
- Quyết định ai xem/join được board (xem mục 7)
- Enum **tách riêng** thành `BoardVisibility`, không dùng chung `Visibility` (PUBLIC, PRIVATE) đang có ở `Team`
- Không có giá trị mặc định — bắt buộc `@NotNull` ở `BoardCreateDto`, Entity không tự gán default

### 6.5 BoardMember — bảng trung gian User–Board ✅ [Đã code]

| Field | Kiểu | Ghi chú |
|---|---|---|
| id | Long | PK |
| boardId | Long (FK → Board) | |
| userId | Long (FK → User) | |
| role | Enum `Role` (`ADMIN`, `MEMBER`) | Quản trị bảng / Thành viên |
| joinedAt | LocalDateTime | |

> Field `role` **dùng lại enum `Role` có sẵn từ Sprint 1** (đang dùng chung cho cả `TeamMember` và `Invitation`), không tạo enum riêng.

### 6.6 TaskList — Danh sách công việc (cột trong Board) ✅ [Đã code]

| Field | Kiểu | Ghi chú |
|---|---|---|
| id | Long | PK |
| boardId | Long (FK → Board) | |
| name | String | not null |
| position | Integer | vị trí hiển thị — dùng **position thưa** (10, 20, 30...), xem mục 8 |
| createdAt | LocalDateTime | |

### 6.7 Card — Thẻ công việc ✅ [Đã code]

| Field | Kiểu | Ghi chú |
|---|---|---|
| id | Long | PK |
| taskListId | Long (FK → TaskList) | list hiện tại đang chứa thẻ; đổi giá trị này = "di chuyển thẻ" (#34) |
| title | String | not null — **bắt buộc duy nhất lúc tạo mới** (#32) |
| description | String (TEXT) | điền/sửa sau qua popup chi tiết (#35), không bắt buộc lúc tạo |
| position | Integer | vị trí trong TaskList — dùng **position thưa** |
| createdBy | Long (FK → User) | |
| createdAt | LocalDateTime | |
| updatedAt | LocalDateTime | |

### 6.8 Label — Nhãn (thuộc Board, dùng chung cho mọi Card trong Board đó) ✅ [Đã code]

| Field | Kiểu | Ghi chú |
|---|---|---|
| id | Long | PK |
| boardId | Long (FK → Board) | kho nhãn dùng chung của board, tạo độc lập không cần Card |
| name | String | |
| color | String | mã màu hex |

### 6.9 CardLabel — bảng trung gian Card–Label ✅ [Đã code]

| Field | Kiểu | Ghi chú |
|---|---|---|
| id | Long | PK |
| cardId | Long (FK → Card) | |
| labelId | Long (FK → Label) | |

### 6.10 CardMember — thành viên được gán vào thẻ ✅ [Đã code]

| Field | Kiểu | Ghi chú |
|---|---|---|
| id | Long | PK |
| cardId | Long (FK → Card) | |
| userId | Long (FK → User) | phải là BoardMember của board chứa card mới gán được |

### 6.11 CardAttachment — file đính kèm ✅ [Đã code]

| Field | Kiểu | Ghi chú |
|---|---|---|
| id | Long | PK |
| cardId | Long (FK → Card) | |
| fileUrl | String | **not null** — lưu local giống avatar (theo quy ước Sprint 1) |
| fileName | String | **not null** — tên gốc file |
| uploadedBy | Long (FK → User) | |
| uploadedAt | LocalDateTime | |

### 6.12 CardComment — bình luận trong thẻ ✅ [Đã code]

| Field | Kiểu | Ghi chú |
|---|---|---|
| id | Long | PK |
| cardId | Long (FK → Card) | |
| userId | Long (FK → User) | |
| content | String (TEXT) | |
| createdAt | LocalDateTime | |

### 6.13 Invitation — lời mời thành viên qua email ✅ [Đã sửa xong Entity]

Cập nhật: thêm `boardId`, dùng chung cho cả Team và Board.

**Đã chốt**: dùng chung 1 entity `Invitation` cho cả 2 luồng mời (vào Team lẫn vào Board), thêm field `boardId` (nullable) song song với `teamId` (nullable) — thay vì tách riêng `BoardInvitation`.

Lý do chọn hướng này:
- Tái dùng toàn bộ logic gửi mail, sinh token, xác nhận link đã có sẵn từ Sprint 1 (`EmailService`, luồng accept invitation trong `TeamMemberServiceImpl`)
- Phù hợp quy mô đồ án, đỡ trùng lặp code

| Field | Kiểu | Ghi chú |
|---|---|---|
| id | Long | PK |
| teamId | Long (FK → Team) | **nullable** — có giá trị nếu là lời mời vào Team (#16) |
| boardId | Long (FK → Board) | **[MỚI, nullable]** — có giá trị nếu là lời mời vào Board (#23) |
| email | String | email người được mời |
| role | Enum `Role` (`ADMIN`, `MEMBER`) | quyền được gán khi tham gia — dùng lại enum `Role` có sẵn, khớp cả `TeamMember.role` lẫn `BoardMember.role` |
| token | String | mã xác nhận trong link mời |
| status | Enum (`PENDING`, `ACCEPTED`, `EXPIRED`) | |
| createdAt | LocalDateTime | |

> ⚠️ Đúng 1 trong 2 field `teamId`/`boardId` phải có giá trị, không cả 2 cùng null hoặc cùng có giá trị — validate ở Service lúc tạo Invitation, không phải ở Entity.

**Lưu ý khi code Service (#23)**:
- Luồng xử lý "accept invitation" cần rẽ nhánh theo field nào có giá trị: `teamId != null` → tạo `TeamMember`; `boardId != null` → tạo `BoardMember`
- Có thể cân nhắc tách 2 method riêng (`acceptTeamInvitation` / `acceptBoardInvitation`) dùng chung 1 phần logic sinh/xác nhận token, thay vì viết chung 1 method rẽ nhánh if/else phức tạp

### 6.14 Convention bắt buộc: dual-mapping FK

Xác nhận qua review code Sprint 1 (`TeamMember`, `Invitation`, `Team`, `Board`) và áp dụng thống nhất cho toàn bộ entity Sprint 2: mọi quan hệ khóa ngoại đều khai báo **2 field song song**:

```java
@Column(name = "task_list_id", nullable = false)
private Long taskListId;                 // dùng để GHI (insert/update)

@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "task_list_id", insertable = false, updatable = false)
private TaskList taskList;                // chỉ dùng để ĐỌC quan hệ (vd: card.getTaskList().getName())
```

> ⚠️ **Quy tắc bắt buộc khi code Service**: luôn set giá trị qua field `Long xxxId` (ví dụ `card.setTaskListId(...)`), **không bao giờ** set qua object (`card.setTaskList(...)`) — vì object có `insertable=false, updatable=false` nên Hibernate sẽ **âm thầm bỏ qua**, không lưu xuống DB, không có exception báo lỗi.

Toàn bộ Service Sprint 1 (`TeamServiceImpl`, `BoardServiceImpl`) đều tuân thủ đúng quy tắc này (dùng `.teamId(...)`, `.userId(...)` qua Builder khi tạo mới).

> 🎯 **Đặc biệt lưu ý ở #34** (di chuyển Card sang TaskList khác) — đây là thao tác *update* FK duy nhất của Sprint 2 (khác các chỗ khác chỉ *insert* lúc tạo mới), nên rủi ro gọi nhầm object setter là cao nhất, cần double-check khi code.

## 7. Quy tắc phân quyền hai lớp Team / Board

> **Quan trọng**: Quyền Quản trị nhóm/Thành viên (`TeamMember.role`) và quyền Quản trị bảng/Thành viên (`BoardMember.role`) là **hai lớp độc lập nhau**, không suy ra lẫn nhau. Một user có thể là BoardMember của 1 Board mà không cần là TeamMember của Team chứa Board đó (đặc biệt với board PUBLIC). Cả hai đều **không phải** role toàn hệ thống của Spring Security — Spring Security (`UserPrincipal.getAuthorities()`) chỉ xác nhận "đã đăng nhập" (`ROLE_USER`). Muốn biết quyền cụ thể, luôn phải query `TeamMemberRepository` / `BoardMemberRepository` trong Service, không dùng `hasRole()`.

### 7.1 Quyền XEM board (find/access), theo `Board.visibility`

| Visibility | Ai xem/tìm được |
|---|---|
| `PRIVATE` | Chỉ BoardMember |
| `GROUP` | Bất kỳ TeamMember nào của Team chứa board |
| `PUBLIC` | Bất kỳ ai (kể cả chưa đăng nhập, tìm được qua Google) |

### 7.2 Quyền SỬA board (tạo/sửa/xoá TaskList, Card...)

Áp dụng thống nhất cho mọi visibility:

```java
boolean canEditBoard(User user, Board board) {
    // Chỉ cần là BoardMember (bất kể role ADMIN hay MEMBER) là được sửa
    return boardMemberRepo.existsByBoardIdAndUserId(board.getId(), user.getId());
}
```

Đây là **cách Trello thật xử lý** (đã xác nhận qua tài liệu chính thức Atlassian): quyền sửa luôn gắn với việc **đã là BoardMember**, không có ngoại lệ "TeamMember được sửa luôn mà không cần join". Điểm khác biệt giữa các visibility chỉ nằm ở **bước JOIN** (mục 7.3), không nằm ở bước sửa.

### 7.3 Quy tắc JOIN board (trở thành BoardMember) — story #25

| Visibility | Ai join được | Cần duyệt? |
|---|---|---|
| `PRIVATE` | Chỉ người được mời trực tiếp qua #23 | Không tự join được, phải được mời |
| `GROUP` | Bất kỳ TeamMember nào của Team chứa board | Không cần duyệt — tự join thành công ngay khi bấm "Tham gia bảng" |
| `PUBLIC` | Bất kỳ ai (kể cả không phải TeamMember) | Không cần duyệt — tự join thành công ngay khi bấm "Tham gia bảng" |

```java
BoardMember joinBoard(User user, Board board) {
    switch (board.getVisibility()) {
        case GROUP:
            if (!teamMemberRepo.existsByTeamIdAndUserId(board.getTeamId(), user.getId())) {
                throw new AccessDeniedException("Không phải thành viên nhóm");
            }
            break;
        case PUBLIC:
            break; // ai cũng join được
        case PRIVATE:
            throw new AccessDeniedException("Board riêng tư — cần được mời trực tiếp");
    }
    return boardMemberRepo.save(new BoardMember(board, user, Role.MEMBER));
}
```

> Người tạo board (`Board.createdBy`) tự động có `BoardMember` với `role = ADMIN` ngay lúc tạo, không cần qua bước join.

## 8. Quy tắc position (sắp xếp thứ tự)

Áp dụng cho `TaskList.position` (#30) và `Card.position` (#33, #34): dùng **position thưa** — bước nhảy 10 (10, 20, 30...) thay vì số liền kề (1, 2, 3...).

- **Chèn giữa 2 phần tử**: `position = (position_trước + position_sau) / 2`, không cần update lại các record khác
- **Thêm vào cuối**: `position = max(position hiện có) + 10`
- **Di chuyển Card sang TaskList khác** (#34): chỉ cần đổi `taskListId` + gán `position` mới theo vị trí chèn trong TaskList đích — không cần shift hàng loạt record ở cả 2 phía
- Chỉ khi khoảng trống giữa 2 position liền kề = 0 (hết chỗ chèn) mới cần "rebalance" lại toàn bộ position trong list đó (trường hợp hiếm, xử lý sau nếu cần)

## 9. Quy tắc tạo/sửa Card (#32 vs #35)

Theo đúng hành vi Trello thật (xác nhận qua ảnh demo thực tế nhóm đã test):

| Story | Hành động | Field liên quan |
|---|---|---|
| **#32** — Tạo thẻ | Chỉ cần nhập title, bấm Enter → Card được tạo ngay | `title` (bắt buộc), `taskListId`, `position` |
| **#35** — Sửa nội dung thẻ | Mở popup chi tiết thẻ → sửa/thêm description, thành viên (CardMember), nhãn (CardLabel), đính kèm (CardAttachment), bình luận (CardComment) | tất cả các field/entity còn lại |

Endpoint tạo mới (Controller xử lý form submit) chỉ nhận `title` + `taskListId`; các phần còn lại (description, member, label, attachment, comment) đều có endpoint cập nhật riêng, gọi từ popup chi tiết sau khi Card đã tồn tại.

> Lưu ý: dự án dùng Spring MVC server-side render (Thymeleaf), **không có tầng REST API riêng**. Các thao tác cập nhật từ popup (gán member, thêm nhãn, đính kèm, comment) thực hiện qua Controller trả về fragment Thymeleaf hoặc redirect lại trang chi tiết Card, không phải gọi API JSON riêng biệt.

## 10. Luồng đăng nhập & Thymeleaf Security (Sprint 1)

`SecurityConfig` → `CustomUserDetailsService` (tra `UserRepository.findByEmail`) → trả về `UserPrincipal` (bọc entity `User` thật) → Spring Security tự so khớp password bằng BCrypt → tạo session.

Trong Controller, lấy user đang đăng nhập bằng:

```java
@AuthenticationPrincipal UserPrincipal principal
User currentUser = principal.getUser();
```

Trong Thymeleaf Header:
- Khi ĐÃ ĐĂNG NHẬP (`sec:authorize="isAuthenticated()"`): Hiện Tên người dùng (`sec:authentication="principal.user.displayName"`), Avatar, Nút Đăng xuất
- Khi CHƯA ĐĂNG NHẬP (`sec:authorize="isAnonymous()"`): Hiện nút "Đăng nhập"

## 11. Quy tắc code

- Clean code, tách rõ tầng `entity → repository → service → controller`
- Dùng DTO riêng cho các form thay vì bind trực tiếp vào Entity
- Không hardcode thông tin nhạy cảm (password DB...) — dùng biến môi trường hoặc file config riêng đã gitignore
- Đặt tên nhất quán theo bảng ở mục 4, tránh mỗi người tự đặt khác nhau gây khó merge code
- Logic phân quyền (`canEditBoard`, `joinBoard`...) đặt tập trung trong 1 Service dùng chung (ví dụ `BoardPermissionService`), tránh mỗi người tự viết lại rải rác ở từng Controller

## 12. Danh sách User Stories Sprint 2 & phân chia công việc

Team giữ nguyên phân công theo module như Sprint 1, nối tiếp mảng mỗi người đã quen từ Sprint 1.

### A. Trang chủ hiển thị Board (Hướng)

- **#4** — Danh sách bảng do user tự tạo, group theo Team, sắp xếp Alphabet
- **#5** — Danh sách bảng user được gán làm thành viên, group theo Team, sắp xếp Alphabet

> Đây là mở rộng của Story #1 (Trang chủ) mà Thi đã làm ở Sprint 1 → phần Body cần thêm data thật thay vì khung tĩnh.

### B. Board CRUD & phân quyền hiển thị (Khuyên)

- **#20** — Tạo mới board (Riêng tư/Nhóm/Công khai)
- **#21** — Đổi tên board (chỉ quản trị bảng)
- **#22** — Đổi visibility board (chỉ quản trị bảng)
- **#27** — Xóa board (chỉ quản trị viên/admin board)

### C. Board Members (Thành)

- **#23** — Mời thành viên vào board qua email
- **#24** — Loại thành viên khỏi board
- **#25** — Tham gia board (join) — áp dụng đúng quy tắc mục 7.3
- **#26** — Đổi quyền thành viên trong board (Member ↔ Admin)

### D. TaskList — Danh sách công việc (Đào)

- **#28** — Tạo mới TaskList trong board
- **#29** — Đổi tên TaskList
- **#30** — Đổi vị trí TaskList (dùng position thưa, mục 8)
- **#31** — Xóa TaskList (có popup xác nhận)

> Logic phân quyền lặp lại pattern: bảng Riêng tư → check thành viên bảng; bảng Nhóm/Công khai → check thành viên nhóm (xem mục 7).

### E. Card — Thẻ công việc (Thi — Lead, tiếp nối vai trò kiến trúc + review)

- **#32** — Tạo mới Card (chỉ cần title, xem mục 9)
- **#33** — Đổi vị trí Card trong cùng TaskList
- **#34** — Di chuyển Card sang TaskList khác (UI: dropdown chọn TaskList đích + dropdown chọn vị trí, cùng 1 board — không hỗ trợ đổi sang board khác trong scope Sprint 2)
- **#35** — Sửa nội dung Card đầy đủ: description, CardMember, CardLabel, CardAttachment, CardComment (xem mục 9)

> Đồng thời giữ vai trò: kiến trúc tổng thể, review code toàn bộ nhánh, xử lý conflict khi merge, điều phối với Antigravity.

### Lưu ý phụ thuộc thứ tự (dependency)

Board (#20-27) phải có trước thì TaskList (#28-31) mới code được, TaskList xong mới tới Card (#32-35). Để tránh Đào và Thi bị block đầu sprint, đề xuất:

- Khuyên hoàn thành entity `Board` + `BoardMember` + chức năng tạo board cơ bản **sớm nhất trong sprint**, đẩy lên nhánh chung `board-base` để Đào/Thi có thể dựa vào code trước khi các phần còn lại (#21, #22, #23-27) hoàn thiện.
- Đào và Thi có thể song song thiết kế entity `TaskList`/`Card` từ đầu sprint (không phụ thuộc logic phân quyền hoàn chỉnh), chỉ cần chờ `Board` entity tồn tại.