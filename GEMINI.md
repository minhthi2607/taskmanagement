Luôn giao tiếp với nhau bằng tiếng Việt

Đây là file context cho dự án **Task Management** — dùng để AI hiểu về dự án khi hỗ trợ code.

## 1. Giới thiệu dự án

Ứng dụng web quản lý công việc theo mô hình kiểu Trello: người dùng tạo **Nhóm (Team)**, trong mỗi Nhóm có nhiều **Bảng (Board)** để cộng tác quản lý công việc. Trong mỗi Bảng có nhiều **Danh sách công việc (TaskList)**, trong mỗi TaskList có nhiều **Thẻ (Card)**.

- Đây là đồ án tốt nghiệp khoá học CodeGym, làm theo quy trình **Scrum**, chia thành nhiều Sprint.
- Nhóm phát triển gồm 5 thành viên, mỗi người phụ trách 1 module riêng.
- **Sprint 1** (đã hoàn thành): xây nền tảng — khung giao diện, đăng nhập/đăng ký, quản lý Nhóm và thành viên Nhóm.
- **Sprint hiện tại: Sprint 2** — xây dựng Board, BoardMember, TaskList và Card (chức năng cốt lõi kiểu Trello).

**Tiến độ Sprint 2:**
- ✅ Đã merge vào `dev`: Board + BoardMember (Khuyên, Thành — PR #12, #11), TaskList (Đào — PR đã merge)
- ⏳ Đang làm: Card (#32-35, Thi) — chưa bắt đầu, nhánh riêng cần `merge dev` mới nhất trước khi code (đã có sẵn đầy đủ Board/BoardMember/TaskList)
- Mỗi PR trên đều đã qua đủ quy trình: review code tĩnh → build thật → test end-to-end qua giao diện thật trước khi merge — giữ nguyên quy trình này cho PR của Card

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
- **Kéo-thả (từ phản hồi giảng viên, xem mục 13)**: jQuery UI Draggable/Sortable (`https://jqueryui.com/draggable/`) — dùng cho #30 (TaskList) và #33/#34 (Card). ⚠️ jQuery UI Sortable/Draggable **không hỗ trợ bàn phím** (bug chính thức #9633 của jQuery UI, chưa fix nhiều năm) — bắt buộc giữ song song phương án nhập số thủ công làm fallback, xem mục 13.2.

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

> ⚠️ **CẬP NHẬT (phản hồi giảng viên, xem mục 13.1)**: dòng `GROUP` bên dưới đã lỗi thời — board GROUP giờ **tự động thêm mọi TeamMember**, không cần bấm "Tham gia bảng" nữa. Bảng dưới đây giữ nguyên để tham khảo lịch sử thiết kế ban đầu; chi tiết thiết kế mới xem mục 13.1.

| Visibility | Ai join được | Cần duyệt? |
|---|---|---|
| `PRIVATE` | Chỉ người được mời trực tiếp qua #23 | Không tự join được, phải được mời |
| `GROUP` | ~~Bất kỳ TeamMember nào của Team chứa board~~ — **nay tự động, xem mục 13.1** | ~~Không cần duyệt — tự join thành công ngay khi bấm "Tham gia bảng"~~ |
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
>
> Code mẫu trên vẫn đúng cho **PUBLIC** và **PRIVATE**. Với **GROUP**, method `joinBoard()` (nút "Tham gia bảng") gần như không còn cần thiết vì thành viên đã tự động có mặt — xem mục 13.1 để biết có nên giữ lại nút này hay không.

## 8. Quy tắc position (sắp xếp thứ tự)

Áp dụng cho `TaskList.position` (#30) và `Card.position` (#33, #34): dùng **position thưa** — bước nhảy 10 (10, 20, 30...) thay vì số liền kề (1, 2, 3...).

- **Chèn giữa 2 phần tử**: `position = (position_trước + position_sau) / 2`, không cần update lại các record khác
- **Thêm vào cuối**: `position = max(position hiện có) + 10`
- **Di chuyển Card sang TaskList khác** (#34): chỉ cần đổi `taskListId` + gán `position` mới theo vị trí chèn trong TaskList đích — không cần shift hàng loạt record ở cả 2 phía
- Chỉ khi khoảng trống giữa 2 position liền kề = 0 (hết chỗ chèn) mới cần "rebalance" lại toàn bộ position trong list đó (trường hợp hiếm, xử lý sau nếu cần)

> ⚠️ **CẬP NHẬT (phản hồi giảng viên, xem mục 13.2)**: UI "nhập số thủ công" mô tả bên dưới **không còn là phương án chính** — đã chuyển sang kéo-thả (jQuery UI) cho cả #30 và #33/#34. Đoạn dưới đây giữ để tham khảo lịch sử; chi tiết thiết kế mới xem mục 13.2.
>
> **Về UI nhập vị trí — quyết định BAN ĐẦU cho TaskList (#30)**: `sprint2.txt` (yêu cầu gốc) không yêu cầu kéo-thả, chỉ ghi "đổi vị trí". Đào triển khai #30 bằng **ô nhập số thủ công** (người dùng tự gõ giá trị position, ví dụ "15" để chèn giữa 10 và 20) thay vì kéo-thả tự động tính `(trước+sau)/2` — đã merge vào `dev` theo quyết định này.
>
> **Card (#33, #34) do Thi phụ trách — quyết định BAN ĐẦU**: áp dụng cùng cách "nhập số thủ công" như #30, dùng dropdown chọn TaskList đích cho #34 — đã merge vào `dev` theo quyết định này.

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

## 13. Cải tiến sau phản hồi giảng viên (Sprint 2.1)

Sprint 2 đã merge và test xong (xem lịch sử ở các mục trên). Sau khi giáo viên chấm/xem demo, có 3 phản hồi cần chỉnh sửa lại — không phải bug, mà là cải tiến thiết kế. Áp dụng sau khi Sprint 2 gốc đã ổn định trên `dev`.

### 13.1 Tự động thêm thành viên nhóm vào board GROUP

**Thay đổi so với thiết kế ban đầu (mục 7.3)**: bỏ cơ chế "tự bấm Tham gia bảng" cho board GROUP, thay bằng **tự động đồng bộ hai chiều, liên tục** giữa `TeamMember` và `BoardMember` của mọi board GROUP thuộc Team đó.

**Các điểm kích hoạt đồng bộ (đã xác nhận với người dùng)**:

| Sự kiện | Hành động cần làm |
|---|---|
| Tạo board mới với `visibility = GROUP` | Tự động tạo `BoardMember` (role `MEMBER`) cho **mọi** `TeamMember` hiện có của Team đó, trừ người tạo board (đã có `ADMIN` sẵn) |
| Đổi visibility board **sang** GROUP (#22) | Giống hệt trên — tự thêm mọi TeamMember hiện có làm BoardMember (không đụng tới người đã là BoardMember từ trước, không hạ role của họ) |
| Có người **mới vào Team** (#16, chấp nhận lời mời) | Tự động thêm người đó làm `BoardMember` (role `MEMBER`) vào **mọi board GROUP** hiện có của Team |
| Có người **bị loại khỏi Team** (#17) | Tự động xóa `BoardMember` của người đó khỏi **mọi board GROUP** của Team (đề xuất, đã thống nhất để nhất quán logic — nếu rời nhóm mà vẫn giữ quyền trên board của nhóm thì vô lý) |
| Đổi visibility board **khỏi** GROUP (sang PRIVATE/PUBLIC) | **Không** tự động xóa BoardMember đã có — giữ nguyên, tránh trải nghiệm bị tước quyền đột ngột |

**Về nút "Tham gia bảng" (#25) cho GROUP**: không còn cần thiết nữa vì đã tự động — cân nhắc **ẩn nút này** khi `visibility = GROUP` (vì bấm vào cũng không còn tác dụng gì, người dùng đã có sẵn trong BoardMember rồi). Nút "Tham gia bảng" **vẫn giữ nguyên** cho `PUBLIC` (đúng thiết kế cũ, không đổi).

**Việc cần làm khi code**:
- Thêm method `syncBoardMembersOnGroupVisibility(Board board)` trong `BoardService`/`BoardPermissionService` — gọi khi tạo board hoặc đổi visibility sang GROUP
- Sửa `TeamMemberServiceImpl` (nơi xử lý accept invitation #16 và xóa thành viên #17) — gọi thêm logic đồng bộ board GROUP tương ứng
- Cân nhắc tách riêng 1 method dùng chung, ví dụ `BoardMemberSyncService`, để tránh lặp code giữa nhiều nơi gọi tới (tạo board, đổi visibility, join team, leave team)

### 13.2 Kéo-thả (Drag & Drop) thay cho nhập số thủ công — #30, #33, #34

**Thư viện bắt buộc dùng**: jQuery UI Draggable/Sortable (`https://jqueryui.com/draggable/`, giáo viên chỉ định cụ thể link này).

**Phạm vi áp dụng (đã xác nhận với người dùng)**:
- **#30** (TaskList trong Board): kéo-thả để đổi thứ tự các cột
- **#33** (Card trong cùng TaskList): kéo-thả để đổi thứ tự thẻ
- **#34** (Card sang TaskList khác): **gộp chung vào cùng 1 thao tác kéo-thả với #33** — kéo thẻ từ cột này thả sang cột khác, đúng kiểu Trello thật. **Bỏ UI dropdown chọn TaskList đích + dropdown vị trí đã làm trước đó** (không dùng nữa).

**⚠️ Vấn đề accessibility cần xử lý (liên quan trực tiếp tới phản hồi mục 13.3 — AA)**: jQuery UI Sortable/Draggable **không hỗ trợ bàn phím** (xác nhận qua bug ticket chính thức #9633 của jQuery UI, mở từ nhiều năm, chưa fix). Nếu chỉ dùng kéo-thả thuần, người dùng chỉ dùng bàn phím hoặc trình đọc màn hình sẽ **không thể** đổi vị trí TaskList/Card — vi phạm ngược lại chính yêu cầu accessibility.

**Giải pháp đã chốt**: **giữ song song cả 2 phương án**, không xóa hẳn ô nhập số thủ công đã làm:
- Kéo-thả (jQuery UI) — phương án chính, trải nghiệm tốt cho chuột/cảm ứng
- Ô nhập số thủ công đã có sẵn (từ thiết kế gốc #30, #33, #34) — **giữ lại làm fallback cho bàn phím**, đặt trong modal chi tiết Card / menu 3 chấm của TaskList như hiện tại, không xóa code cũ

**Về mặt kỹ thuật**:
- Backend **không cần đổi gì** — endpoint hiện có (`/task-list/{id}/reorder` tương tự nếu có, `/card/{cardId}/reorder`, `/card/{cardId}/move`) vẫn dùng chung công thức `(position_trước + position_sau) / 2` ở mục 8, chỉ khác nguồn gốc giá trị `newPosition` đến từ đâu
- **Bắt buộc chuyển sang gọi AJAX (`fetch`)** thay vì submit form thường khi kéo-thả — nếu vẫn submit theo kiểu POST-redirect-reload cũ, mỗi lần thả sẽ load lại toàn trang, trải nghiệm kéo-thả bị giật/không mượt. Sau khi kéo-thả xong, chỉ cần cập nhật lại DOM cục bộ (di chuyển phần tử HTML) mà không cần chờ reload — có thể gọi lại API để đồng bộ `position` thật, nhưng UI nên phản hồi ngay lập tức (optimistic update) rồi rollback nếu API lỗi

### 13.3 Tương phản màu Badge đạt chuẩn WCAG AA

**Yêu cầu**: mọi badge màu trong app phải đạt tỉ lệ tương phản tối thiểu theo WCAG AA — **4.5:1** cho chữ thường, **3:1** cho chữ lớn (≥18pt hoặc ≥14pt in đậm) hoặc thành phần UI (border, icon).

**Phạm vi áp dụng — đã chốt áp dụng đầy đủ (không thu hẹp)**:
1. **Badge cố định** (màu do code định sẵn, dễ kiểm soát): vai trò (`Quản trị viên`/`Thành viên`/`Quản trị nhóm`), visibility (`PRIVATE`/`GROUP`/`PUBLIC`)
2. **Badge màu Label tự chọn** (khó hơn — người dùng tự chọn màu qua color picker khi tạo Label, xem mục 6.8): cần thêm ràng buộc để không thể chọn màu vi phạm AA

**Việc cần làm khi code**:
- **Badge cố định**: rà lại toàn bộ CSS class Bootstrap đang dùng (`bg-success-subtle`, `bg-danger-subtle`...) hoặc màu tùy chỉnh, kiểm tra tỉ lệ tương phản bằng công cụ (ví dụ WebAIM Contrast Checker), chỉnh lại mã màu nếu không đạt — ưu tiên dùng chữ đậm màu tối trên nền màu nhạt (subtle) thay vì chữ trắng trên nền màu tươi, vì kiểu subtle thường dễ đạt AA hơn
- **Label tự chọn màu**: khi người dùng chọn màu nền qua color picker (mục 6.8 `Label.color`), JS cần **tự tính độ sáng (luminance)** và **tự chọn màu chữ tương phản** (đen hoặc trắng) hiển thị trên badge đó, thay vì cố định 1 màu chữ — công thức tính độ sáng tương đối chuẩn W3C: `L = 0.2126*R + 0.7152*G + 0.0722*B` (R,G,B đã chuẩn hóa 0-1, có gamma correction theo WCAG), nếu `L` cao (nền sáng) → chữ đen, nếu `L` thấp (nền tối) → chữ trắng
- Không cần chặn người dùng chọn màu nào — chỉ cần đảm bảo màu chữ luôn tương phản đủ với màu nền họ chọn, xử lý tự động phía client (JS) khi render badge Label

## 14. Sprint 3 — Đính kèm, Nhãn, Tìm kiếm, Thông báo, Bình luận đầy đủ, Nhật ký thời gian

Sprint 3 build tiếp trên nền Card đã hoàn thiện ở Sprint 2. Nguồn story: `sprint3.txt` (#36-55). Team lần này còn **4 người**: Thi (lead), Khuyên, Thành, Đào — Hướng không tham gia sprint này.

### 14.1 Entity mới cần tạo

#### Notification — thông báo trong ứng dụng [MỚI]

| Field | Kiểu | Ghi chú |
|---|---|---|
| id | Long | PK |
| userId | Long (FK → User) | người **nhận** thông báo |
| type | Enum `NotificationType` | xem danh sách giá trị bên dưới |
| content | String | nội dung đã dựng sẵn (server tự build câu hoàn chỉnh lúc tạo, KHÔNG dựng lại ở client — đúng convention không có REST API/client-templating của dự án, xem mục 2) |
| link | String (nullable) | URL điều hướng khi bấm vào thông báo (VD: `/board/12` hoặc `/card/45`) |
| isRead | Boolean | mặc định `false` |
| createdAt | LocalDateTime | |

`NotificationType` (enum mới, đặt trong `enums/`): `CARD_ADDED` (#44), `CARD_MOVED` (#45), `CARD_MEMBER_ASSIGNED` (#46), `BOARD_MEMBER_ADDED` (#47), `TEAM_MEMBER_ADDED` (#48), `CARD_DUE_REMINDER` (#53), `CARD_WATCH_ACTIVITY` (#54).

> ⚠️ Lưu ý quan trọng khi code: `content` build **1 lần lúc tạo Notification**, lưu thẳng vào DB dạng chuỗi hoàn chỉnh (ví dụ: `"minh thi đã thêm 'Sửa lỗi login' vào 'To Do'"`). Không lưu các mảnh dữ liệu rời rồi ráp câu lúc hiển thị — vì nếu sau này Card/Board bị đổi tên hoặc xóa, thông báo cũ vẫn phải giữ nguyên nội dung lịch sử đúng như lúc phát sinh.

#### CardWatcher — theo dõi thẻ (#54) [MỚI]

| Field | Kiểu | Ghi chú |
|---|---|---|
| id | Long | PK |
| cardId | Long (FK → Card) | |
| userId | Long (FK → User) | |
| createdAt | LocalDateTime | |

Ràng buộc unique `(cardId, userId)` — 1 người chỉ theo dõi 1 thẻ 1 lần (giống pattern `uk_board_user` đã dùng ở `BoardMember`).

#### CardTimeLog — nhật ký thời gian đã dùng (#55) [MỚI]

| Field | Kiểu | Ghi chú |
|---|---|---|
| id | Long | PK |
| cardId | Long (FK → Card) | |
| userId | Long (FK → User) | người log |
| hours | BigDecimal | số giờ đã dùng (cho phép số lẻ, ví dụ 1.5 giờ) |
| note | String (nullable) | ghi chú ngắn về công việc đã làm |
| loggedAt | LocalDateTime | ngày/giờ công việc diễn ra (khác `createdAt` là lúc **tạo bản ghi**) |
| createdAt | LocalDateTime | |

**Quyết định thiết kế**: #55 là *time logging* (nhiều lần ghi nhận thời gian thực tế), không phải 1 field ước lượng duy nhất trên `Card` — nên tách bảng riêng `CardTimeLog`, có thể có nhiều bản ghi cho 1 Card. Tổng giờ hiển thị trên Card = `SUM(hours)` tính động qua query, không lưu field tổng trên `Card` (tránh dữ liệu trùng lặp/lệch pha).

### 14.2 Cập nhật Entity `Card` (thêm field cho #53)

| Field mới | Kiểu | Ghi chú |
|---|---|---|
| dueDate | LocalDateTime (nullable) | hạn chót của thẻ |
| reminderMinutes | Integer (nullable) | số phút nhắc **trước** `dueDate`. `null` = mặc định nhắc đúng lúc `dueDate` (theo đúng spec #53: *"nếu không chọn nhắc trước thì mặc định đến due date sẽ gửi"*) |
| reminderSentAt | LocalDateTime (nullable) | thời điểm đã gửi thông báo nhắc, dùng để **chống gửi trùng** khi Scheduler quét lại nhiều lần. Khi người dùng sửa `dueDate`/`reminderMinutes`, **phải reset field này về `null`** để nhắc lại đúng theo mốc mới |

### 14.3 Cơ chế quét & gửi nhắc việc (#53)

Dùng Spring `@Scheduled` (đã có sẵn `spring-boot-starter` support, không cần thêm dependency) — 1 job chạy định kỳ (đề xuất mỗi 1 phút) quét các Card có:
```
dueDate IS NOT NULL
AND reminderSentAt IS NULL
AND NOW() >= (dueDate - reminderMinutes phút, hoặc dueDate nếu reminderMinutes NULL)
```
Với mỗi Card khớp điều kiện: tạo `Notification` (type `CARD_DUE_REMINDER`) cho **người tạo thẻ** và **mọi CardMember** của thẻ đó (đúng yêu cầu #53: *"gửi thông báo tới người tham gia thẻ và người tạo ra thẻ đó"*), rồi set `reminderSentAt = NOW()` để không gửi lại lần quét sau.

### 14.4 Cơ chế "theo dõi thẻ" (#54)

Khi có bất kỳ hành động nào xảy ra trên 1 Card (di chuyển #34, thêm thành viên #37, tới due date #53, xóa #35-adjacent, sửa nội dung #35, được tag tên trong comment #49), Service tương ứng cần:
1. Query danh sách `CardWatcher` của Card đó
2. Tạo `Notification` (type `CARD_WATCH_ACTIVITY`) cho từng người theo dõi (trừ chính người vừa thực hiện hành động, tránh tự thông báo cho mình)

> Vì đây là hành vi "móc" (hook) vào **nhiều Service khác nhau** đã tồn tại (CardServiceImpl, BoardMemberServiceImpl...), đề xuất tạo 1 method dùng chung, ví dụ `NotificationService.notifyCardWatchers(Long cardId, String activityDescription, Long actorUserId)`, để mọi nơi cần bắn thông báo watch chỉ cần gọi 1 dòng, tránh lặp code.

### 14.5 Phân nhóm & phân công Sprint 3

Nhóm theo tính liên kết chức năng, tiếp nối đúng domain quen thuộc từ Sprint 2.

#### A. Đính kèm file & Bình luận đầy đủ (Đào)
- **#36** — Đính kèm file vào thẻ (Entity `CardAttachment` đã có từ Sprint 2, mục 6.11 — chỉ cần Service/Controller/UI). Lưu file local theo đúng quy ước avatar Sprint 1 (`static/uploads/`). **Ghi chú**: `sprint3.txt` chỉ mô tả việc THÊM file + hiển thị tên tệp, không hề nhắc đến việc XÓA file đính kèm. Đào tự bổ sung `deleteAttachment()` (không có trong yêu cầu gốc), cho phép cả người upload lẫn Board ADMIN xóa — **đã xác nhận đây là quyết định hợp lý, KHÁC với #51** vì #36 không có ràng buộc "của mình" như câu chữ #51, nên không có gì sai khi để Admin xóa được (khác #51 vốn có ràng buộc rõ ràng, phải thu hẹp lại chỉ cho chủ bình luận).
- **#49** — Bình luận (đã có "thêm" từ Sprint 2 #35, story này bổ sung validate không được để trống)
- **#50** — Sửa bình luận của chính mình
- **#51** — Xóa bình luận — **ĐÃ XÁC NHẬN LẠI VỚI GIẢNG VIÊN**: chỉ CHỦ bình luận được xóa, Board ADMIN **KHÔNG** được xóa bình luận của người khác. Giữ đúng sát nghĩa spec gốc `sprint3.txt`, có popup xác nhận trước khi xóa.
- **#52** — Xem danh sách bình luận, sắp xếp thời gian **tăng dần** (cũ → mới) — đã fix bằng `@OrderBy("createdAt ASC")` trên `Card.comments`

> Làm sau khi Đào xong việc tồn đọng: #30 (kéo-thả TaskList) và phần Team ở mục 13.1. **[Đã xong — xem lịch sử review PR trong quá trình làm việc]**

> ⚠️ **Cấu hình bắt buộc cho #36 (đính kèm file)**: mỗi người cần tự thêm vào `application.yaml` local của mình (không commit, theo đúng quy ước bảo mật đã có):
> ```yaml
> spring:
>   servlet:
>     multipart:
>       max-file-size: 10MB
>       max-request-size: 10MB
> ```
> Lý do: Spring Boot mặc định giới hạn upload chỉ 1MB — validate 10MB trong `CardServiceImpl.addAttachment()` sẽ không bao giờ chạy tới với file 1-10MB nếu thiếu cấu hình này, gây lỗi `MaxUploadSizeExceededException` khó hiểu thay vì thông báo đã viết sẵn. Cần cập nhật `application.yaml.example` (file mẫu, có commit) để người mới clone biết cần thêm dòng này — **hiện tại file mẫu chưa có, cần bổ sung**.

#### B. Tìm kiếm/lọc trong Board + Time log (Khuyên)
- **#38** — Tìm kiếm theo tiêu đề thẻ (gần đúng, kết quả hiện trên board hiện tại)
- **#39** — Tìm kiếm theo nhãn (chọn nhiều nhãn cùng lúc)
- **#40** — Tìm kiếm theo thành viên (chọn nhiều thành viên cùng lúc)
- **#55** — Time log (Entity `CardTimeLog` mới, mục 14.1)

> #38-40 nên gộp chung **1 popup Tìm kiếm** với 3 tab/tiêu chí, theo đúng mô tả gốc ("Menu góc trên tay phải > Tìm kiếm > popup nhiều tiêu chí"), không làm 3 popup riêng biệt.

#### C. Quản lý Nhãn + gán thành viên có tìm kiếm (Thành)
- **#41** — Gán 1 hoặc nhiều nhãn cho thẻ (Entity `Label`/`CardLabel` đã có từ Sprint 2, mục 6.8-6.9 — chỉ cần Service/Controller/UI)
- **#42** — Tạo nhãn mới (~7 màu có sẵn)
- **#43** — Sửa/xóa nhãn (xóa thì gỡ khỏi mọi thẻ đang gán, có popup xác nhận)
- **#37** — Gán thành viên vào thẻ có ô tìm kiếm (mở rộng CardMember đã có từ #35)

> Lưu ý: mô tả gốc #37 trong `sprint3.txt` có đoạn *"Nhãn sau khi gán hiển thị..."* — đây là lỗi copy-paste từ #41, không áp dụng cho #37, bỏ qua khi code.

> ⚠️ **Quy tắc phân quyền RIÊNG cho đúng 4 story này (#37, #41, #42, #43) — KHÁC với mục 7.2 chung**: Cả 4 story đều có nguyên văn câu chữ phân quyền cụ thể trong `sprint3.txt` (PRIVATE → chỉ BoardMember/Admin; GROUP/PUBLIC → chỉ TeamMember thật, không chỉ BoardMember). Đây **chặt hơn** `checkEditPermission` (mục 7.2) — vì board PUBLIC cho phép bất kỳ ai tự join thành BoardMember (mục 7.3), nếu dùng chung `checkEditPermission` sẽ để lọt người ngoài Team can thiệp vào gán thành viên/quản lý nhãn trên board PUBLIC của Team đó.
>
> **Quyết định phạm vi (đã xác nhận)**: quy tắc chặt hơn này **CHỈ áp dụng cho đúng 6 method thuộc 4 story trên** (`addCardMember`/`removeCardMember`, `addCardLabel`/`removeCardLabel`, `createLabel`, `updateLabel`/`deleteLabel`) — **KHÔNG** thay đổi `checkEditPermission` chung, không ảnh hưởng các tính năng khác đã merge (Card #32-35, Attachment #36, Comment #49-52, Search #38-40, TimeLog #55) vì các story đó không có câu chữ phân quyền riêng này trong đề bài.
>
> **Thiết kế**: thêm method mới `checkCardMemberOrLabelPermission(Long boardId, User user)` trong `BoardPermissionService` (giữ nguyên `checkEditPermission`/`checkAdminPermission` cũ, không sửa) — PRIVATE thì check `existsByBoardIdAndUserId` (như cũ), GROUP/PUBLIC thì check `existsByTeamIdAndUserId` (chặt hơn, bắt buộc là TeamMember thật).
>
> **Ghi chú thêm (từ review PR #22)**: badge màu Nhãn hiện đang hardcode chữ trắng (`color: #fff`), vi phạm tương phản AA với các màu nền sáng (Vàng `#ffc107`, Cyan `#0dcaf0`) — đây chính là phạm vi mục 13.3 (đã tạm gác từ Sprint 2.1), giờ cần sửa cụ thể cho đúng chỗ này khi Thành fix PR.

#### D. Hệ thống Thông báo — nền tảng (Thi, lead)
- **#44** — Thông báo khi thêm thẻ mới
- **#45** — Thông báo khi di chuyển thẻ
- **#46** — Thông báo khi được gán vào thẻ
- **#47** — Thông báo khi được thêm vào Board
- **#48** — Thông báo khi được thêm vào Team
- Tạo `NotificationService` dùng chung (method `createNotification(...)`) để Khuyên/Thành/Đào gọi khi cần bắn thông báo từ Service của họ (VD: `TaskListServiceImpl`/`CardServiceImpl` gọi khi tạo thẻ; `BoardMemberServiceImpl` gọi khi thêm thành viên...)
- UI: dropdown chuông thông báo ở header (khung sẵn từ Sprint 1 story #2), sắp xếp mới nhất lên đầu, đánh dấu đã đọc khi bấm vào

### 14.6 Giai đoạn 2 — chỉ làm sau khi mục D (Thông báo) xong

| Story | Phụ thuộc | Người làm |
|---|---|---|
| **#53** — Due date + nhắc việc | Cần `NotificationService` | Khuyên (sau khi xong nhóm B) |
| **#54** — Theo dõi thẻ | Cần `NotificationService` | Thành (sau khi xong nhóm C) |

### 14.7 Lưu ý phụ thuộc & thứ tự triển khai

- Nhóm A, B, C, D **có thể làm song song ngay từ đầu sprint** — không phụ thuộc lẫn nhau
- Thi nên hoàn thành `NotificationService` (chỉ cần interface + implementation cơ bản, chưa cần đủ UI) **sớm nhất có thể**, đẩy lên nhánh chung để Khuyên/Thành biết trước chữ ký method mà gọi, tránh phải sửa lại nhiều nơi khi API đổi
- Đào có việc tồn đọng từ Sprint 2 (#30, mục 13.1 phần Team) — ưu tiên xong việc đó trước khi bắt đầu nhóm A. **[Đã xong]**

> **Cập nhật tiến độ (sau khi hoàn thành mục 13)**: Toàn bộ việc tồn đọng của mục 13.1/13.2 (Board, Team, TaskList, Card kéo-thả) đã merge xong vào `dev`, bao gồm cả 1 vòng phát hiện và sửa bug quan trọng (mất quyền ADMIN Board khi rời Team, do 2 người code trùng lặp logic + lỗi thiếu `final` gây NullPointerException — đã sửa, có unit test bảo vệ + xác nhận bằng test tay). Sprint 3 (mục 14 ở trên) **có thể bắt đầu ngay**, chưa ai bắt tay code tại thời điểm cập nhật tài liệu này.