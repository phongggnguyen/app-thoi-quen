# My Habits - Ứng dụng Theo dõi Thói quen trên Android

"My Habits" là một ứng dụng Android toàn diện được thiết kế để giúp người dùng xây dựng, theo dõi và duy trì các thói quen tích cực. Ứng dụng cung cấp một bộ tính năng phong phú để theo dõi tiến độ, duy trì động lực và có được thông tin chi tiết về các kiểu thói quen cá nhân.

## 🌟 Tính năng Chính

*   **Quản lý Thói quen:** Dễ dàng thêm, sửa và xóa thói quen.
*   **Phân loại:** Sắp xếp các thói quen vào các danh mục tùy chỉnh để quản lý tốt hơn.
*   **Theo dõi Tiến độ:** Đánh dấu các thói quen là đã hoàn thành, đã bỏ lỡ hoặc đang chờ xử lý hàng ngày.
*   **Báo cáo Chi tiết:** Trực quan hóa tiến trình với các biểu đồ và lịch biểu sâu sắc. Xem báo cáo cho các thói quen riêng lẻ hoặc tóm tắt hàng ngày/hàng tháng.
*   **Ghi chú:** Ghi lại các ghi chú liên quan đến thói quen và tiến trình của bạn.
*   **Lời nhắc Tùy chỉnh:** Thiết lập thông báo để không bao giờ quên một thói quen.
*   **Hồ sơ Người dùng:** Cá nhân hóa trải nghiệm của bạn với hồ sơ người dùng và ảnh đại diện.
*   **Bảo mật Dữ liệu:** Bảo vệ dữ liệu của bạn bằng mật khẩu cục bộ.
*   **Sao lưu & Phục hồi:** Bảo vệ dữ liệu thói quen của bạn bằng cách sao lưu và phục hồi khi cần.
*   **Tìm kiếm:** Nhanh chóng tìm kiếm thói quen, ghi chú hoặc danh mục.
*   **Hỗ trợ bởi AI (Suy đoán):** Bao gồm thư viện Google Generative AI, cho thấy các tính năng AI tiềm năng sắp ra mắt hoặc đã được tích hợp.

## 🛠️ Công nghệ & Thư viện sử dụng

*   **Ngôn ngữ:** [Kotlin](https://kotlinlang.org/)
*   **Kiến trúc:** Android tiêu chuẩn (Activities, Services, Broadcast Receivers)
*   **Core Android Jetpack:**
    *   [AndroidX](https://developer.android.com/jetpack)
    *   [Lifecycle (ViewModel & LiveData)](https://developer.android.com/topic/libraries/architecture/lifecycle)
    *   [Thư viện Room Persistence](https://developer.android.com/training/data-storage/room) để lưu trữ cơ sở dữ liệu cục bộ.
*   **UI (Giao diện người dùng):**
    *   XML Layouts với các thành phần Material Design.
    *   [Jetpack Compose](https://developer.android.com/jetpack/compose) (được sử dụng cho một số thành phần UI).
    *   [MPAndroidChart](https://github.com/PhilJay/MPAndroidChart) để hiển thị biểu đồ trong báo cáo.
    *   [Material-CalendarView](https://github.com/prolificinteractive/material-calendarview) cho các chế độ xem lịch tương tác.
*   **Mạng (Networking):**
    *   [Retrofit](https://square.github.io/retrofit/) & [OkHttp](https://square.github.io/okhttp/) để xử lý các yêu cầu mạng.
    *   [Gson](https://github.com/google/gson) để tuần tự hóa/giải tuần tự hóa JSON.
*   **Ngày & Giờ:**
    *   [ThreeTenABP](https://github.com/JakeWharton/ThreeTenABP) để xử lý ngày và giờ hiệu quả.
*   **AI (Trí tuệ nhân tạo):**
    *   [Google Generative AI SDK](https://ai.google.dev/docs/android_quickstart)

## 🚀 Cách để Build (Xây dựng ứng dụng)

1.  Sao chép repository:
    ```bash
    git clone <repository-url>
    ```
2.  Mở dự án trong Android Studio.
3.  Để Gradle đồng bộ và tải xuống các dependency cần thiết.
4.  Build và chạy ứng dụng trên thiết bị Android hoặc máy giả lập (yêu cầu SDK tối thiểu 24).

## 📂 Cấu trúc Dự án

Dự án tuân theo cấu trúc ứng dụng Android tiêu chuẩn:

```
app-thoi-quen/
├── app/
│   ├── src/main/
│   │   ├── java/com/example/myhabits/
│   │   │   ├── activities/   # Các màn hình ứng dụng (Activities)
│   │   │   ├── adapters/     # Các adapter cho RecyclerView
│   │   │   ├── database/     # Cài đặt Room DB, DAOs, và entities
│   │   │   ├── models/       # Các lớp mô hình dữ liệu
│   │   │   ├── receivers/    # Broadcast receivers (ví dụ: cho thông báo)
│   │   │   ├── services/     # Các dịch vụ chạy nền
│   │   │   └── utils/        # Các lớp tiện ích
│   │   ├── res/              # Tài nguyên (layouts, drawables, strings, etc.)
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts      # Script build cấp ứng dụng
└── build.gradle.kts          # Script build cấp dự án
```