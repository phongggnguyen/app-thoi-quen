 Clone project
 1. Tổng quan & Mục tiêu Dự án

  "My Habits" là một ứng dụng di động trên nền tảng Android, đóng vai trò như một người trợ lý cá nhân giúp người dùng thiết lập,
  theo dõi và duy trì các thói quen hàng ngày. Mục tiêu chính của ứng dụng là cung cấp một công cụ mạnh mẽ, trực quan để người dùng
  xây dựng lối sống tích cực thông qua việc theo dõi tiến trình, nhận lời nhắc và xem báo cáo chi tiết về sự kiên trì của bản thân.

2. Phân tích Giao diện (UI) và Luồng hoạt động (User Flow)

  Giao diện của ứng dụng được xây dựng chủ yếu bằng XML Layout kết hợp với các thành phần của Material Design, tạo cảm giác quen
  thuộc và hiện đại.

  Luồng hoạt động chính:                                                                                                            ▄
                                                                                                                                    ▀
    1. Màn hình khởi động & Khóa (Splash & Lock): Khi mở app, một màn hình splash (SplashActivity) sẽ xuất hiện. Nếu người dùng đã
      cài đặt mật khẩu, ứng dụng sẽ chuyển đến màn hình khóa (activity_lock_screen.xml) yêu cầu nhập mật khẩu.
    2. Thiết lập lần đầu (`activity_user_setup.xml`): Nếu là lần đầu sử dụng, người dùng sẽ được dẫn đến màn hình thiết lập thông tin
      cá nhân (tên, ảnh đại diện).
    3. Màn hình chính (`activity_main.xml`): Đây là trung tâm của ứng dụng.
       * Hiển thị danh sách các thói quen cho ngày hôm nay (item_habit.xml).
       * Có một menu điều hướng (Navigation Drawer - nav_header_main.xml) để truy cập các chức năng khác như: Báo cáo, Ghi chú, Quản
         lý Danh mục, Cài đặt.
       * Có một nút "Thêm" (ic_add_habit.xml) để tạo thói quen mới.
    4. Thêm/Sửa Thói quen (`activity_add_habit.xml`):
       * Người dùng nhập tên thói quen, mô tả, chọn màu sắc, biểu tượng, và danh mục.
       * Thiết lập lịch trình (các ngày trong tuần) và lời nhắc (giờ thông báo).
   5. Chi tiết Thói quen (`activity_habit_detail.xml`):
       * Hiển thị thông tin chi tiết về một thói quen.
       * Trình bày một lịch (view_monthly_calendar.xml) hiển thị các ngày đã hoàn thành thói quen.
       * Cung cấp các biểu đồ tiến trình (sử dụng MPAndroidChart).
   6. Báo cáo (`activity_report.xml`):
       * Cung cấp cái nhìn tổng quan về tiến độ của tất cả các thói quen.
       * Hiển thị các biểu đồ như tỷ lệ hoàn thành, chuỗi ngày dài nhất, v.v.
   7. Các chức năng khác:
       * Ghi chú (`activity_list_note.xml`): Quản lý các ghi chú cá nhân.
       * Sao lưu/Phục hồi (`activity_backup.xml`, `activity_restore.xml`): Cho phép người dùng bảo vệ dữ liệu. 
 3. Kiến trúc Phần mềm (Software Architecture)

  Dự án này có khả năng cao sử dụng kiến trúc MVVM (Model-View-ViewModel), một kiến trúc phổ biến và được Google khuyến khích trong
  phát triển Android.

   * Model (Mô hình):
       * Entities: Các lớp dữ liệu trong package com.example.myhabits.models (ví dụ: Habit.java, Note.java, User.java). Chúng định
         nghĩa cấu trúc của các bảng trong cơ sở dữ liệu.
       * DAO (Data Access Object): Các interface trong package database/dao. Chúng định nghĩa các phương thức để truy vấn CSDL
         (thêm, sửa, xóa, đọc).
       * Room Database: Lớp trừu tượng kế thừa từ RoomDatabase để khởi tạo và quản lý CSDL.
       * Repository (Kho chứa): (Có thể có hoặc không) Một lớp trung gian giữa ViewModel và nguồn dữ liệu (DAO). Nó giúp tách biệt
         logic lấy dữ liệu.
                                                                                                                                    ▄
   * View (Giao diện):                                                                                                              ▀
       * Bao gồm các Activities (activities package) và các file XML Layout (layout directory).
       * Nhiệm vụ chính của View là hiển thị dữ liệu lên màn hình và nhận tương tác từ người dùng (như click, vuốt). View không chứa
         logic xử lý dữ liệu.
    * ViewModel (Tầng xử lý logic cho UI):
       * Mỗi Activity (hoặc Fragment) sẽ có một ViewModel tương ứng.
       * ViewModel chịu trách nhiệm gọi Repository/DAO để lấy dữ liệu, xử lý logic và cung cấp dữ liệu cho View thông qua LiveData
         hoặc StateFlow.
       * ViewModel không biết gì về View, giúp nó tồn tại qua các thay đổi cấu hình (như xoay màn hình) mà không làm mất dữ liệu.

  Luồng dữ liệu:
  Tương tác người dùng (View) -> Gọi phương thức trong ViewModel -> ViewModel yêu cầu dữ liệu từ Repository -> Repository gọi DAO để
  truy vấn Room Database -> Dữ liệu trả về qua LiveData -> View cập nhật lại giao diện.

  4. Phân tích Chức năng Chi tiết

    * Quản lý Thói quen (CRUD):
       * Create/Update: AddHabit.java thu thập thông tin từ các trường input, tạo một đối tượng Habit và gọi phương thức insert hoặc
         update của HabitDao (thông qua ViewModel).                                                                                 █
       * Read: MainActivity.java và ListHabit.java quan sát một LiveData<List<Habit>> từ ViewModel. Khi dữ liệu thay đổi,
         HabitAdapter sẽ được thông báo để cập nhật lại RecyclerView.
       * Delete: Trong HabitDetail.java hoặc menu ngữ cảnh, một lệnh xóa sẽ được gửi đến ViewModel, sau đó gọi phương thức delete
         của HabitDao.
    * Thông báo (Notifications):
       * Khi người dùng tạo lời nhắc, ứng dụng sử dụng AlarmManager của Android để lên lịch một tác vụ trong tương lai.
       * Khi đến giờ, AlarmManager sẽ kích hoạt một BroadcastReceiver (trong package receivers).
       * BroadcastReceiver này sau đó sẽ khởi chạy một Service hoặc trực tiếp tạo và hiển thị một thông báo (Notification) cho người
         dùng.

   * Sao lưu & Phục hồi:
       * Sao lưu: BackupActivity.java sẽ đọc toàn bộ dữ liệu từ Room Database (có thể bằng cách truy vấn tất cả các bảng) và chuyển
         đổi chúng thành một định dạng có thể lưu trữ (như JSON). File này sau đó được lưu vào bộ nhớ ngoài của thiết bị.
       * Phục hồi: RestoreActivity.java cho phép người dùng chọn một file sao lưu. Ứng dụng sẽ đọc file, phân tích cú pháp dữ liệu
         và ghi đè/chèn lại vào Room Database.

  5. Cấu trúc Cơ sở dữ liệu (Database)
                                                                                                                                    █
  Dựa trên các file model, CSDL Room có thể bao gồm các bảng chính sau:

   * `User`: Lưu thông tin người dùng (ID, tên, đường dẫn ảnh đại diện).
   * `Habit`: Bảng trung tâm, lưu thông tin về một thói quen (ID, tên, mô tả, màu sắc, ID danh mục, lịch trình).
   * `Category`: Lưu các danh mục do người dùng tạo (ID, tên danh mục).
   * `DailyStatus`: Ghi lại trạng thái của một thói quen vào một ngày cụ thể (ID, habit_id, date, status - e.g., completed, missed).
     Đây là bảng quan trọng nhất để tạo báo cáo.
   * `Note`: Lưu các ghi chú của người dùng (ID, tiêu đề, nội dung, ngày tạo).

  Mối quan hệ:
   * User 1-nhiều Habit (Một người dùng có nhiều thói quen).                                                                        █
   * Category 1-nhiều Habit (Một danh mục có nhiều thói quen).
   * Habit 1-nhiều DailyStatus (Một thói quen có nhiều bản ghi trạng thái theo ngày).

  6. Tổng kết Công nghệ và Vai trò

   * Kotlin: Ngôn ngữ lập trình chính, giúp code ngắn gọn, an toàn và hiện đại.
   * XML & Material Design: Xây dựng giao diện người dùng tĩnh và tuân thủ nguyên tắc thiết kế của Google.
   * Jetpack Compose: Được sử dụng cho một số thành phần UI, cho thấy sự chuyển đổi dần sang UI khai báo hiện đại.
   * Room: Cung cấp một lớp trừu tượng mạnh mẽ trên SQLite, giúp việc quản lý CSDL trở nên dễ dàng và an toàn kiểu.
   * ViewModel & LiveData: Nền tảng của kiến trúc MVVM, giúp quản lý trạng thái UI một cách hiệu quả và không bị mất dữ liệu.
   * Retrofit & Gson: Dù có trong dự án, chúng có thể chưa được sử dụng hoặc dùng cho một tính năng ẩn (như đồng bộ hóa đám mây
     trong tương lai).                                                                                                              ▄
   * MPAndroidChart: Một thư viện mạnh mẽ để vẽ các loại biểu đồ khác nhau, cốt lõi của tính năng báo cáo.                          ▀
   * Google Generative AI: Một phát hiện thú vị, cho thấy dự án có thể đang thử nghiệm hoặc lên kế hoạch cho các tính năng thông
     minh như gợi ý thói quen, phân tích sâu hơn, v.v.
