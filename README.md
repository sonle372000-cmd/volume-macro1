# Volume Macro

App Android: giữ nút **Giảm âm lượng** để tự động chạm (tap) vào một tọa độ trên màn hình.

## Vì sao cần Accessibility Service?

Android không cho ứng dụng thường bắt phím cứng khi chạy nền, và cũng không cho ứng dụng thường
tự giả lập thao tác chạm màn hình. Cả hai việc này chỉ được phép thông qua **Accessibility Service**
(Dịch vụ Trợ năng), nên app dùng cách này thay vì chạy "ngầm" kiểu thông thường.

## Cách lấy file APK

### Cách 1 — Dùng Android Studio (khuyên dùng nếu bạn có máy tính)

1. Mở thư mục `VolumeMacro` này bằng **Android Studio** (File > Open).
2. Để Android Studio tự tải Gradle/SDK (cần mạng).
3. Bấm Run để cài thẳng vào điện thoại, hoặc **Build > Build Bundle(s)/APK(s) > Build APK(s)**
   để lấy file `.apk` trong `app/build/outputs/apk/debug/`.

### Cách 2 — Build tự động trên GitHub (không cần cài gì trên máy)

Project đã kèm sẵn file `.github/workflows/build.yml` để **GitHub Actions tự build APK giúp bạn**:

1. Tạo một repo mới (public hoặc private) trên GitHub, ví dụ `volume-macro`.
2. Đẩy (push) toàn bộ nội dung thư mục `VolumeMacro` này lên repo đó (dùng git, hoặc trên web GitHub
   chọn "Add file > Upload files" rồi kéo thả cả thư mục vào).
3. Vào tab **Actions** của repo, workflow "Build APK" sẽ tự chạy (mất khoảng 2-3 phút).
4. Khi chạy xong, vào lần chạy đó, kéo xuống mục **Artifacts**, tải file
   **VolumeMacro-debug-apk** về máy/điện thoại — bên trong là file `app-debug.apk`.
5. Copy file `.apk` vào điện thoại và cài đặt (cần bật "Cài từ nguồn không xác định").

## Cách dùng

1. Mở app **Volume Macro**.
2. Bấm **"Chọn điểm trên màn hình"**:
   - Lần đầu sẽ xin quyền "Hiển thị trên các ứng dụng khác" (overlay) → cấp quyền → quay lại app bấm lại nút này.
   - Một chấm xanh nổi sẽ hiện lên, **kéo chấm** tới đúng vị trí bạn muốn app tự động chạm vào
     (ví dụ nút bắn trong game, nút "Tiếp theo"...).
   - Bấm nút **"Xác nhận điểm"** để lưu tọa độ.
   - (Bạn cũng có thể tự gõ tay tọa độ X, Y nếu đã biết, rồi bấm **Lưu**.)
3. Bấm **"Mở Cài đặt Trợ năng (Accessibility)"**, tìm **Volume Macro** trong danh sách và **bật lên**.
   - Với Android 13+: nếu không thấy nút bật hoặc bị mờ ("Cài đặt bị hạn chế"), vào
     **Cài đặt > Ứng dụng > Volume Macro > (menu 3 chấm) > Cho phép cài đặt bị hạn chế**, rồi quay lại bật Accessibility.
4. Quay về app, dòng trạng thái sẽ hiện **"ĐANG BẬT"**.
5. Bây giờ, ở bất kỳ màn hình/app nào, **giữ nút Giảm âm lượng** đủ lâu (mặc định 500ms, chỉnh được
   trong ô "Thời gian giữ") → app sẽ tự động chạm vào tọa độ đã lưu.

## Tuỳ chỉnh

- **Thời gian giữ (ms)**: thời gian phải giữ phím trước khi macro kích hoạt, tránh việc chỉ bấm
  giảm âm lượng bình thường cũng bị coi là kích hoạt.
- Mặc định khi giữ phím, app **chặn luôn việc giảm âm lượng thật** (để không vừa tap vừa tụt volume).
  Nếu muốn âm lượng vẫn giảm bình thường, mở file
  `MacroAccessibilityService.kt`, tìm dòng cuối hàm `onKeyEvent` và đổi `return true` thành
  `return super.onKeyEvent(event)`.
- Muốn nhiều điểm chạm liên tiếp (macro nhiều bước) thay vì 1 điểm: có thể mở rộng hàm `performTap()`
  để dispatch nhiều `GestureDescription` nối tiếp nhau bằng `postDelayed`.

## Lưu ý

- Tính năng giả lập chạm màn hình có thể vi phạm điều khoản dịch vụ của một số ứng dụng
  (đặc biệt là game có chống gian lận / anti-cheat). Bạn tự chịu trách nhiệm khi sử dụng.
- Toạ độ X, Y là toạ độ pixel tuyệt đối của màn hình (không phải theo app cụ thể), nên nếu đổi
  hướng màn hình hoặc dùng ở app khác có bố cục khác, có thể cần chọn lại điểm.
