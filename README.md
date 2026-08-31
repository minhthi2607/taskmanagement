# Task Management

Ứng dụng web quản lý công việc theo mô hình kiểu **Trello**: người dùng tạo **Nhóm (Team)**, mỗi Nhóm có nhiều **Bảng (Board)**, mỗi Board có nhiều **Danh sách công việc (TaskList)**, mỗi TaskList có nhiều **Thẻ (Card)** — hỗ trợ kéo-thả, gán thành viên, nhãn (Label), bình luận, đính kèm file, chấm công thời gian (time log) và thông báo (Notification).

> Đây là đồ án tốt nghiệp khoá học CodeGym, phát triển theo mô hình **Scrum**, chia nhiều Sprint, nhóm 5 thành viên mỗi người phụ trách 1 module.

## Mục lục

- [Tính năng chính](#tính-năng-chính)
- [Công nghệ sử dụng](#công-nghệ-sử-dụng)
- [Cấu trúc thư mục](#cấu-trúc-thư-mục)
- [Sơ đồ quan hệ dữ liệu](#sơ-đồ-quan-hệ-dữ-liệu)
- [Yêu cầu môi trường](#yêu-cầu-môi-trường)
- [Cài đặt & Chạy dự án](#cài-đặt--chạy-dự-án)
- [Cấu hình (application.yaml)](#cấu-hình-applicationyaml)
- [Chạy kiểm thử](#chạy-kiểm-thử)
- [Tài liệu tham khảo](#tài-liệu-tham-khảo)

## Tính năng chính

- **Xác thực & phân quyền**: đăng ký, đăng nhập bằng email (Spring Security, mã hoá mật khẩu BCrypt), quản lý phiên đăng nhập, đổi mật khẩu, cập nhật hồ sơ cá nhân.
- **Quản lý Nhóm (Team)**: tạo/sửa/xoá nhóm, mời thành viên qua email, phân quyền `ADMIN`/`MEMBER`, xoá thành viên.
- **Quản lý Bảng (Board)**: tạo/sửa/xoá board trong nhóm, 3 mức hiển thị (`PRIVATE`, `GROUP`, `PUBLIC`), mời & quản lý thành viên board, tìm kiếm/lọc thẻ trong board.
- **Danh sách công việc (TaskList)**: tạo/sửa/xoá, sắp xếp thứ tự bằng kéo-thả (kèm phương án nhập số thủ công dự phòng cho bàn phím).
- **Thẻ công việc (Card)**: tạo/sửa/xoá, kéo-thả để đổi vị trí/di chuyển giữa các TaskList, gán/gỡ thành viên, gắn/gỡ nhãn (Label), bình luận, đính kèm file, theo dõi (watch), ghi nhận thời gian làm việc (time log), đặt hạn chót kèm nhắc việc tự động.
- **Nhãn (Label)**: tạo/sửa/xoá nhãn dùng chung cho từng Board.
- **Thông báo (Notification)**: nhận thông báo khi được mời, gán việc, bình luận…, đánh dấu đã đọc từng cái hoặc tất cả.
- **Gửi email**: thông báo lời mời tham gia Team/Board qua SMTP (Gmail).

## Công nghệ sử dụng

| Thành phần | Công nghệ |
|---|---|
| Ngôn ngữ | Java 17 |
| Framework | Spring Boot 3.5.x (Spring MVC – server-side render, không tách REST API riêng) |
| Build tool | Gradle (kèm Gradle Wrapper) |
| Database | MySQL, truy xuất qua Spring Data JPA / Hibernate |
| View engine | Thymeleaf + Thymeleaf Layout Dialect + `thymeleaf-extras-springsecurity6` |
| UI | Bootstrap 5.3 + Bootstrap Icons |
| Bảo mật | Spring Security (session-based, không dùng JWT), mã hoá mật khẩu BCrypt |
| Validate | Spring Boot Validation (Hibernate Validator) |
| Gửi mail | Spring Boot Starter Mail |
| Kéo-thả | jQuery UI Draggable/Sortable |
| Tác vụ định kỳ | Spring Scheduler (`@EnableScheduling`) — nhắc hạn chót thẻ |
| Khác | Lombok, Spring Boot DevTools |
| Test | JUnit 5, Spring Boot Test, Mockito (unit test Service, không cần DB), Spring Security Test |

## Cấu trúc thư mục

```
src/main/java/com/project/taskmanagement/
 ├─ config/          → cấu hình: SecurityConfig, SchedulerConfig, LoginInputValidationFilter,
 │                      CustomUserDetailsService, UserPrincipal, WebMvcConfig...
 ├─ controller/       → xử lý request, trả về view Thymeleaf
 ├─ dto/               → object nhận dữ liệu từ form (không dùng Entity trực tiếp)
 ├─ entity/            → các bảng dữ liệu (JPA Entity)
 ├─ enums/             → enum dùng chung (Role, Visibility, BoardVisibility, InvitationStatus, NotificationType)
 ├─ exception/         → xử lý lỗi tập trung (GlobalExceptionHandler, SafeRedirectHelper)
 ├─ repository/        → Spring Data JPA repository
 ├─ scheduler/         → tác vụ chạy định kỳ (CardDueReminderScheduler — nhắc hạn chót thẻ, Story #53)
 ├─ specification/     → JPA Specification (tìm kiếm/lọc Card)
 └─ service/           → interface xử lý logic nghiệp vụ
     └─ impl/           → class triển khai interface service

src/main/resources/
 ├─ database/schema.sql → script khởi tạo schema MySQL
 ├─ templates/
 │   ├─ layout/         → fragment dùng chung: header.html, sidebar.html, footer.html, main.html, auth.html
 │   ├─ auth/            → login.html, register.html
 │   ├─ user/             → profile.html, change-password.html
 │   ├─ team/             → team-list.html, team-detail.html, team-form.html, team-edit.html
 │   ├─ board/            → board-detail.html
 │   └─ error/            → trang lỗi (403, 404, 500...)
 └─ static/
     ├─ css/, js/         → custom.css, board-detail.js
     └─ uploads/           → nơi lưu ảnh avatar / file đính kèm (lưu local, không dùng cloud storage)
```

## Sơ đồ quan hệ dữ liệu

```
Team ──< Board ──< TaskList ──< Card
 │          │                     │
 │          │                     ├──< CardMember (User)
 │          │                     ├──< CardLabel ──> Label
 │          │                     ├──< CardAttachment
 │          │                     ├──< CardComment
 │          │                     ├──< CardWatcher (User)
 │          │                     └──< CardTimeLog
 │          │
 │          ├──< BoardMember (User)
 │          └──< Label (kho nhãn dùng chung của Board)
 │
 └──< TeamMember (User)

User ──< Notification
Team / Board ──< Invitation (mời thành viên qua email)
```

## Yêu cầu môi trường

- **JDK 17** trở lên
- **MySQL 8.x** (đang chạy, đã tạo sẵn database rỗng)
- Tài khoản **SMTP** (ví dụ Gmail) để gửi mail mời thành viên — có thể bỏ trống nếu chỉ chạy thử tính năng khác
- Không cần cài Gradle — dự án dùng Gradle Wrapper (`gradlew` / `gradlew.bat`)

## Cài đặt & Chạy dự án

1. **Clone dự án**

   ```bash
   git clone <repository-url>
   cd taskmanagement
   ```

2. **Tạo database MySQL**

   ```sql
   CREATE DATABASE taskmanagement_db CHARACTER SET utf8mb4;
   ```

3. **Tạo file cấu hình** từ file mẫu:

   ```bash
   cp src/main/resources/application.yaml.example src/main/resources/application.yaml
   ```

   Mở `application.yaml` vừa tạo và điền thông tin thật (DB, mail) — xem chi tiết ở [phần cấu hình](#cấu-hình-applicationyaml) bên dưới. **Không commit file `application.yaml` chứa thông tin thật lên git.**

4. **Chạy ứng dụng**

    - Windows:
      ```bash
      gradlew.bat bootRun
      ```
    - Linux/macOS:
      ```bash
      ./gradlew bootRun
      ```

   Ứng dụng mặc định chạy tại: **http://localhost:8080**

   Hibernate được cấu hình `ddl-auto: update`, tự động tạo/cập nhật bảng theo Entity khi khởi động lần đầu (đồng thời có sẵn `src/main/resources/database/schema.sql` để tham khảo/khởi tạo thủ công nếu cần).

## Cấu hình (application.yaml)

File `application.yaml.example` là mẫu cấu hình, các biến quan trọng:

| Biến | Ý nghĩa | Mặc định |
|---|---|---|
| `server.port` | Cổng chạy ứng dụng | `8080` |
| `spring.datasource.url` | Chuỗi kết nối MySQL | `jdbc:mysql://localhost:3306/taskmanagement_db` |
| `DB_USERNAME` / `DB_PASSWORD` | Tài khoản MySQL (đọc qua biến môi trường) | `root` / `CHANGE_ME` |
| `spring.jpa.hibernate.ddl-auto` | Chiến lược đồng bộ schema | `update` |
| `MAIL_USERNAME` / `MAIL_PASSWORD` | Tài khoản SMTP gửi mail mời thành viên | `CHANGE_ME` |
| `app.upload-dir` | Thư mục lưu file upload (avatar, đính kèm) | `uploads/` |
| `spring.servlet.multipart.max-file-size` | Giới hạn dung lượng file đính kèm thẻ | `10MB` |

Có thể truyền các giá trị nhạy cảm qua biến môi trường thay vì sửa trực tiếp file yaml, ví dụ:

```bash
export DB_USERNAME=root
export DB_PASSWORD=your_password
export MAIL_USERNAME=your_email@gmail.com
export MAIL_PASSWORD=your_app_password
```

> Với Gmail, cần dùng **App Password** (mật khẩu ứng dụng), không dùng mật khẩu tài khoản thông thường.

## Chạy kiểm thử

Đa số test (Service, Controller) dùng JUnit 5 + Mockito, mock toàn bộ Repository nên **không cần** kết nối MySQL thật. Riêng `TaskmanagementApplicationTests.contextLoads()` khởi động toàn bộ Spring Context nên **cần MySQL thật đang chạy** — set biến môi trường như hướng dẫn ở [phần cấu hình](#cấu-hình-applicationyaml) trước khi chạy:

```bash
export DB_USERNAME=root
export DB_PASSWORD=your_password
./gradlew test
```

(Windows PowerShell: `$env:DB_PASSWORD="your_password"` trước khi chạy `gradlew.bat test`.)

## Tài liệu tham khảo

- [Spring Boot Reference Documentation](https://docs.spring.io/spring-boot/reference/index.html)
- [Spring Data JPA](https://docs.spring.io/spring-boot/reference/data/sql.html#data.sql.jpa-and-spring-data)
- [Thymeleaf](https://www.thymeleaf.org/documentation.html)
- [Spring Security](https://docs.spring.io/spring-security/reference/index.html)
- Xem thêm [HELP.md](HELP.md) cho các liên kết hướng dẫn chi tiết theo từng thành phần (Gradle, Web, Validation, Mail...).