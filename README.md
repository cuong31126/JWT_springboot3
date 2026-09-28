# Hướng Dẫn Sử Dụng & Kiểm Thử JWT Spring Boot 3 (Nimbus JOSE + JWT)

## 1. Yêu cầu môi trường & Cơ sở dữ liệu

- **Java:** 17 hoặc 21+
- **Maven:** 3.8+
- **MySQL:** Tạo database bằng lệnh SQL:
  ```sql
  CREATE DATABASE IF NOT EXISTS jwt_springboot3 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
  ```
- Kiểm tra tài khoản và mật khẩu MySQL trong file `src/main/resources/application.properties`:
  ```properties
  spring.datasource.url=jdbc:mysql://localhost:3306/jwt_springboot3?serverTimezone=UTC&allowPublicKeyRetrieval=true&useSSL=false
  spring.datasource.username=root
  spring.datasource.password=your_password
  ```

---

## 2. Khởi chạy ứng dụng

Chạy lệnh trong terminal tại thư mục gốc của dự án:
```bash
mvn spring-boot:run
```
Ứng dụng khởi chạy tại: **`http://localhost:8005`**

*(Tùy chọn chuyển đổi thư viện JWT trong `application.properties`: `security.jwt.provider=nimbus` hoặc `security.jwt.provider=jjwt`)*

---

## 3. Tài khoản kiểm thử có sẵn

| Email | Mật khẩu | Họ và tên |
| :--- | :--- | :--- |
| `trungnh@hcmute.edu.vn` | `123456` | Nguyen Huu Trung |

---

## 4. Kiểm thử trên Giao diện Web (AJAX)

1. Truy cập trang đăng nhập: **`http://localhost:8005/login`**
2. Nhập Email và Password mẫu ở trên -> Bấm **Login**.
3. Hệ thống tự động nhận JWT token lưu vào `localStorage.token` và chuyển sang trang **`http://localhost:8005/user/profile`**.
4. Trang Profile tự động đính kèm Token gửi tới API `/users/me` và hiển thị thông tin người dùng.
5. Bấm nút **Logout** để xóa Token và quay về trang đăng nhập.

### Kiểm tra bằng DevTools (F12):
- **Tab Application > Local storage > http://localhost:8005**: Xem key `token`.
- **Tab Network**:
  - Request `POST /auth/login`: Nhận chuỗi token trả về.
  - Request `GET /users/me`: Xem mục *Request Headers* có dòng `Authorization: Bearer <token>`.

---

## 5. Kiểm thử qua REST API (Postman / cURL)

### 1. Đăng ký tài khoản mới (`POST /auth/signup`)
- **URL:** `http://localhost:8005/auth/signup`
- **Method:** `POST`
- **Header:** `Content-Type: application/json`
- **Body:**
  ```json
  {
    "email": "user01@hcmute.edu.vn",
    "password": "password123",
    "fullName": "Nguyen Van A"
  }
  ```

### 2. Đăng nhập lấy Token (`POST /auth/login`)
- **URL:** `http://localhost:8005/auth/login`
- **Method:** `POST`
- **Header:** `Content-Type: application/json`
- **Body:**
  ```json
  {
    "email": "user01@hcmute.edu.vn",
    "password": "password123"
  }
  ```
- **Response nhận về:**
  ```json
  {
    "token": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...",
    "expiresIn": 3600000
  }
  ```

### 3. Lấy thông tin tài khoản hiện tại (`GET /users/me`)
- **URL:** `http://localhost:8005/users/me`
- **Method:** `GET`
- **Header:** `Authorization: Bearer <dán_token_vào_đây>`
- **Response:** Thông tin JSON của User đã đăng nhập.

### 4. Lấy danh sách toàn bộ User (`GET /users/`)
- **URL:** `http://localhost:8005/users/`
- **Method:** `GET`
- **Header:** `Authorization: Bearer <dán_token_vào_đây>`
- **Response:** Danh sách mảng JSON chứa các User trong cơ sở dữ liệu.
