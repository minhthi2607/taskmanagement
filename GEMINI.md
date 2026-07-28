Luôn giao tiếp với nhau bằng tiếng Việt

Đây là file context cho dự án **Task Management** — dùng để AI hiểu về dự án khi hỗ trợ code.

## 1. Giới thiệu dự án

Ứng dụng web quản lý công việc theo mô hình kiểu Trello: người dùng tạo **Nhóm (Team)**, trong mỗi Nhóm có nhiều **Bảng (Board)** để cộng tác quản lý công việc.

- Đây là đồ án tốt nghiệp khoá học CodeGym, làm theo quy trình **Scrum**, chia thành nhiều Sprint.
- Nhóm phát triển gồm 5 thành viên, mỗi người phụ trách 1 module riêng.
- Sprint hiện tại: **Sprint 1** — xây nền tảng: khung giao diện, đăng nhập/đăng ký, quản lý Nhóm và thành viên Nhóm. Board/Task thật sự sẽ làm ở Sprint 2.

## 2. Công nghệ sử dụng

- **Ngôn ngữ**: Java 17
- **Framework**: Spring Boot 3.5.x (Spring MVC, không dùng REST API riêng — server-side render)
- **Build tool**: Gradle
- **Database**: MySQL, truy xuất qua Spring Data JPA / Hibernate
- **View**: Thymeleaf + Thymeleaf Layout Dialect (`nz.net.ultraq.thymeleaf:thymeleaf-layout-dialect`) + `thymeleaf-extras-springsecurity6` (ẩn/hiện phần tử theo trạng thái đăng nhập)
- **CSS / UI**: Bootstrap 5.3 + Bootstrap Icons (`bootstrap-icons`)
- **Bảo mật**: Spring Security — đăng nhập session-based (không dùng JWT), mã hoá mật khẩu bằng BCrypt
- **Validate**: Spring Boot Validation (Hibernate Validator)
- **Gửi mail**: Spring Boot Starter Mail (dùng cho tính năng mời thành viên qua email)
- **Khác**: Lombok (giảm boilerplate code)

## 3. Cấu trúc thư mục & Layout Template
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
 │   └─ error/            → trang lỗi (403, 404...)
 └─ static/
     ├─ css/
     ├─ js/
     └─ uploads/           → nơi lưu ảnh avatar người dùng upload (lưu local, không dùng cloud storage)

## 4. Quy ước đặt tên
| Đối tượng | Quy tắc | Ví dụ |
|---|---|---|
| Class Entity | PascalCase, số ít | `User`, `Team`, `TeamMember` |
| Tên bảng DB | snake_case, số nhiều (tự map qua JPA) | `users`, `teams`, `team_members` |
| Field Java | camelCase | `displayName`, `createdAt` |
| Khóa chính | luôn là `id` (Long, auto increment) | `id` |
| Khóa ngoại | `<tên_entity>_id` | `team_id`, `user_id` |
| Enum | class PascalCase, giá trị UPPER_SNAKE_CASE | `Visibility { PUBLIC, PRIVATE }` |
| Session lưu user đăng nhập | thống nhất tên | `SESSION_USER` |
**Lưu ý quan trọng**: Không đặt tên entity là `Group` vì đây là từ khoá dành riêng (reserved keyword) trong MySQL. Dùng `Team` thay cho khái niệm "Nhóm".

## 5. Entity chính (Sprint 1)
- **User**: id, email (unique), displayName, phone, password (mã hoá BCrypt), avatarUrl, createdAt
- **Team**: id, name, type, visibility (`PUBLIC`/`PRIVATE`), description, createdBy, createdAt
- **TeamMember** (bảng trung gian User–Team, chứa quyền theo từng Team): id, teamId, userId, role (`ADMIN`/`MEMBER`), joinedAt
- **Board** (chuẩn bị cho Sprint 2): id, teamId, name, createdBy, createdAt
- **Invitation** (mời thành viên qua email): id, teamId, email, role, token, status (`PENDING`/`ACCEPTED`/`EXPIRED`), createdAt
**Quan trọng về phân quyền**: Quyền Quản trị nhóm / Thành viên là quyền **theo từng Team cụ thể**, lưu trong `TeamMember.role`, KHÔNG phải role toàn hệ thống của Spring Security. Spring Security (`UserPrincipal.getAuthorities()`) chỉ xác nhận "đã đăng nhập" (`ROLE_USER`). Muốn biết 1 user có phải Quản trị của 1 Team cụ thể hay không, phải query `TeamMemberRepository` trong Service, không dùng `hasRole()`.

## 6. Luồng đăng nhập & Thymeleaf Security
`SecurityConfig` → `CustomUserDetailsService` (tra `UserRepository.findByEmail`) → trả về `UserPrincipal` (bọc entity `User` thật) → Spring Security tự so khớp password bằng BCrypt → tạo session.
- Trong Controller, lấy user đang đăng nhập bằng:
```java
@AuthenticationPrincipal UserPrincipal principal
User currentUser = principal.getUser();
Trong Thymeleaf Header:
Ẩn/hiện theo trạng thái đăng nhập:
Khi ĐÃ ĐĂNG NHẬP (sec:authorize="isAuthenticated()"): Hiện Tên người dùng (sec:authentication="principal.user.displayName"), Avatar, Nút Đăng xuất.
Khi CHƯA ĐĂNG NHẬP (sec:authorize="isAnonymous()"): Hiện nút "Đăng nhập".

## 7. Quy tắc code

- Clean code, tách rõ tầng `entity → repository → service → controller`
- Dùng DTO riêng cho các form thay vì bind trực tiếp vào Entity
- Không hardcode thông tin nhạy cảm (password DB...) — dùng biến môi trường hoặc file config riêng đã gitignore
- Đặt tên nhất quán theo bảng ở mục 4, tránh mỗi người tự đặt khác nhau gây khó merge code


## 8. Chi tiết danh sách User Stories (Sprint 1)
### A. Khung giao diện (Thi - Lead phụ trách)
- **#1 Trang chủ**:
  - Giao diện gồm 3 phần: Menu top, Menu trái, và Phần Body trang.
  - Phần Body trang gồm 2 khung hiển thị:
    - Danh sách bảng tự tạo (phân loại theo nhóm).
    - Danh sách bảng được gán làm thành viên (phân loại theo nhóm).
- **#2 Menu top**:
  - Logo ứng dụng (góc trái).
  - Lối tắt hiển thị Danh sách các nhóm của người dùng.
  - Nút Thông báo (icon quả chuông 🔔).
  - Tên hiển thị người dùng | Nút "Đăng nhập" (trường hợp chưa đăng nhập).
- **#3 Menu trái**:
  - Tùy chọn "Bảng".
  - Danh sách các nhóm của người dùng.
  - Nút "Tạo nhóm".
---
### B. Tài khoản & Đăng nhập (Khuyên phụ trách)
- **#6 Đăng ký tài khoản**:
  - Trường thông tin: Email (*), Tên hiển thị (*), Phone, Password (*), Re-password (*).
  - Yêu cầu: Validate bắt buộc các trường (*). Password từ 6–32 ký tự. Email là duy nhất (nếu trùng hiển thị thông báo dùng email khác). Đăng ký thành công hiển thị thông báo, tự động điều hướng sang trang Đăng nhập và điền sẵn thông tin.
- **#7 Đăng nhập**:
  - Trường thông tin: Email (*), Password (*).
  - Yêu cầu: Validate bắt buộc (*), không cho phép nhập ký tự đặc biệt (`*`, `%`, `'`,...). Password có nút bật/tắt xem lại mật khẩu đã nhập. Thành công điều hướng sang Trang chủ, thất bại hiển thị thông báo nhập sai.
- **#8 Đổi mật khẩu**:
  - Trường thông tin: Mật khẩu hiện tại (*), Mật khẩu mới (*), Nhập lại mật khẩu (*).
  - Yêu cầu: Mật khẩu từ 6–32 ký tự. Mật khẩu mới không trùng mật khẩu cũ. Hiển thị thông báo thành công/thất bại. Đổi thành công bắt buộc đăng nhập lại.
- **#9 Thay đổi thông tin cá nhân**:
  - Vị trí: Từ Tên hiển thị ở menu top -> Chọn "Chỉnh sửa thông tin" -> Điều hướng sang trang Thông tin cá nhân.
  - Trường thông tin: Avatar (*), Tên hiển thị (*), Email (*), Số điện thoại, Nút "Lưu".
  - Yêu cầu: Tự động điền dữ liệu cũ. Tên hiển thị không có ký tự đặc biệt. **Không cho đổi Email**.
---
### C. Quản lý Nhóm (Thành phụ trách)
- **#10 Tạo mới 1 nhóm**:
  - Vị trí: Từ Trang chủ > mục Nhóm > nhấn nút "+".
  - Trường thông tin: Tên nhóm (*), Loại nhóm (*), Quyền (*) (Công khai / Riêng tư), Mô tả.
  - Định nghĩa Quyền:
    - *Công khai*: Hiển thị cho bất kỳ ai có liên kết, tìm kiếm Google được, chỉ người được mời vào Không gian làm việc mới có thể thêm/chỉnh sửa bảng trong Nhóm.
    - *Riêng tư*: Chỉ thành viên trong nhóm mới có thể truy cập.
  - Yêu cầu: Validate trống trường (*). Thành công hiển thị thông báo. Người tạo tự động gán là **Quản trị của nhóm**.
- **#11 Xem thông tin của 1 nhóm**:
  - Thông tin hiển thị: Tên nhóm, Loại nhóm, Quyền, Mô tả.
- **#14 Xem danh sách nhóm đã tạo**:
  - Vị trí: Trong giao diện Trang chủ, ở menu trái phía dưới mục Nhóm.
  - Yêu cầu: Hiển thị danh sách nhóm mà người dùng đã tạo hoặc đã tham dự, sắp xếp theo thứ tự **Alphabet**.
---
### D. Sửa / Xóa / Quyền riêng tư Nhóm (Đào phụ trách)
- **#12 Thay đổi thông tin nhóm**:
  - Nút "Chỉnh sửa" nằm cạnh tên nhóm -> Hiển thị khung chỉnh sửa gồm: Tên nhóm, Loại nhóm, Quyền, Mô tả, Nút "Lưu".
- **#13 Thay đổi quyền riêng tư của nhóm**:
  - Nút "Chỉnh sửa" nằm cạnh tên nhóm -> Cập nhật Quyền của nhóm: Tên nhóm (*), Loại nhóm (*), Quyền (*) (Công khai / Riêng tư), Mô tả, Nút "Lưu".
- **#15 Xóa nhóm**:
  - Nút "Chỉnh sửa" nằm cạnh tên nhóm -> Nút Xóa -> Hiển thị Popup xác nhận.
  - Yêu cầu: Sau khi xóa, tất cả dữ liệu nhóm không thể hiển thị/tìm kiếm. Các thành viên trong nhóm nhận được thông báo.
---
### E. Quản lý thành viên & Danh sách bảng (Hướng phụ trách)
- **#16 Thêm thành viên vào nhóm**:
  - Vị trí: Trang chủ > chọn Nhóm > Thành viên > Nút "Mời thành viên" -> Bật Popup gồm:
    - Email (địa chỉ email người muốn mời).
    - Quyền: *Thành viên* (xem bảng & tạo bảng mới) hoặc *Quản trị nhóm* (quản lý thành viên, sửa thông tin nhóm, xem & tạo bảng).
    - Nút "Thêm".
  - Yêu cầu: Chỉ người có quyền **Quản trị nhóm** mới được dùng. Thêm thành công gửi Email mời chứa link tham gia nhóm.
- **#17 Loại thành viên ra khỏi nhóm**:
  - Vị trí: Danh sách thành viên > cột Hành động > Nút "Loại bỏ" -> Hiển thị Popup xác nhận:
    - *Đồng ý*: Loại khỏi nhóm -> Ẩn nhóm & các bảng trong nhóm khỏi giao diện người bị loại (các bảng đã mời riêng không ảnh hưởng). Nếu đang ở trong nhóm thì bị kích ra kèm thông báo.
    - *Quay lại*: Trở về danh sách thành viên.
  - Yêu cầu: Chỉ quyền **Quản trị nhóm** được dùng.
- **#18 Đổi quyền thành viên nhóm**:
  - Vị trí: Danh sách thành viên > cột Quyền > Chọn dropdown (*Thành viên* / *Quản trị nhóm*).
  - Yêu cầu: Chỉ người có quyền **Quản trị nhóm** được dùng.
- **#19 Xem danh sách bảng trong nhóm**:
  - Cách 1: Từ Trang chủ > Menu trái > Chọn nhóm > Chọn "Bảng".
  - Cách 2: Từ Body trang > Tìm nhóm > Chọn "Bảng".
  - Điều hướng tới danh sách bảng của nhóm.
  - Yêu cầu: Với nhóm *Riêng tư*, chỉ Quản trị nhóm và Thành viên mới nhìn thấy danh sách bảng. Vai trò Khách chỉ nhìn thấy các bảng mà mình đã là thành viên.