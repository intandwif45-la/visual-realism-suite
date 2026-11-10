# 3D Visual Realism Suite — Grafika Komputer Minggu 11

**Mata Kuliah:** Grafika Komputer (CGO-301), Semester V, 2 SKS
**Mahasiswa:** Intan Dwi Febrianti — NIM 25050974126
**Dosen Pengampu:** Drs. Bambang Sujatmiko, M.T.
**Topik:** Rendering Faces pada Visual Realism (Berbasis Java OOP & Modern OpenGL/GLSL)

> ⚠️ **PENTING — BELUM DIJALANKAN DI SINI.**
> Proyek ini dibuat di lingkungan tanpa Java, Gradle, GPU, dan OpenGL.
> Kode Java/GLSL di sini ditulis lengkap dan konsisten, **tetapi belum pernah
> di-compile atau di-run di sini**. Build dan run **HARUS** dilakukan di laptop
> dengan JDK 17 + Gradle + GPU/OpenGL. Angka FPS di laporan sengaja
> dikosongkan untuk diisi setelah pengujian di laptop.

## 1. Yang dibuat

Aplikasi Java 3D Viewer standalone (lanjutan konsep proyek Minggu 10
`ThreeDViewing`) yang diperluas menjadi **3D Visual Realism Suite**:

| # | Fitur (Modul Bagian III) | Implementasi |
|---|---|---|
| a | Mesh 3D Bermateri | Sphere UV, House (kubus + atap prisma), Ground Plane — semua punya normal vertex + UV (`MeshFactory.java`) |
| b | Shading Model Switcher | Tombol **B**: Phong ↔ Blinn-Phong, real-time (`phong.frag`) |
| c | Flat vs Smooth Toggle | Tombol **F**: Flat (normal per-face) ↔ Smooth (rata-rata normal vertex) |
| d | HSR Controls | Tombol **Z**: Z-Buffer Depth Test on/off; **C**: Back-Face Culling on/off (`HSRManager.java`) |
| e | Texture Mapping | Tekstur PNG dimuat via STBImage (`Texture2D.java`), filter Nearest / Bilinear-Mipmap (tombol **M**) |
| f | Real-Time Shadow Mapping | 2-pass shadow mapping, Depth FBO 2048×2048 (`ShadowMapFBO.java`, `Renderer.java`), toggle **S** |

## 2. Cara install (di laptop)

1. Install **JDK 17** (misalnya Temurin 17 dari adoptium.net). Cek:
   ```powershell
   java -version
   ```
   Harus muncul `17.x.x`.
2. Install **Gradle** (atau pakai wrapper — lihat bawah). Cek:
   ```powershell
   gradle -v
   ```
3. Clone / salin folder proyek ini ke laptop, atau unzip
   `visual-realism-suite.zip`.

## 3. Cara run

```bash
# dari root folder proyek
./gradlew run
```

Di Windows (PowerShell / CMD):

```powershell
.\gradlew.bat run
```

Kalau `gradlew` belum bisa jalan (Gradle belum terpasang), jalankan
langsung dengan Gradle yang sudah terinstall:

```bash
gradle run
```

Build JAR:

```bash
./gradlew build
```

File hasil build ada di `build/libs/`.

### Catatan native LWJGL

`build.gradle` sekarang memilih native LWJGL otomatis sesuai OS
(Windows → `natives-windows`, macOS → `natives-macos`,
Linux → `natives-linux`), jadi **tidak perlu edit manual lagi**.
Kalau proyekmu masih memakai `build.gradle` lama yang tertulis
`natives-linux`, ganti keempat baris `natives-linux` menjadi
`natives-windows` (atau pakai file `build.gradle` baru dari ZIP
terbaru), lalu klik Sync / Refresh Gradle di IntelliJ sebelum Run.

## 4. Kontrol keyboard

| Tombol | Fungsi |
|---|---|
| **B** | Toggle Phong / Blinn-Phong |
| **F** | Toggle Flat / Smooth Shading |
| **Z** | Toggle Z-Buffer Depth Test |
| **C** | Toggle Back-Face Culling |
| **T** | Toggle Texture on/off |
| **M** | Toggle filter Nearest / Bilinear-Mipmap |
| **S** | Toggle Shadow Mapping on/off |
| Drag mouse kiri | Orbit kamera |
| Scroll mouse | Zoom |
| **ESC** | Keluar |

Setiap toggle juga tampil di **title window** dan dicetak ke **console**.

## 5. Struktur file

```
visual-realism-suite/
├── build.gradle
├── settings.gradle
├── gradlew / gradlew.bat
├── README.md
├── Laporan_Praktikum_Grafika_Komputer_Minggu11.docx
├── Laporan_Praktikum_Grafika_Komputer_Minggu11.pdf
└── src/main/
    ├── java/com/grafkom/viewing3d/
    │   ├── Main.java            # window GLFW, loop, input keyboard, kamera
    │   ├── Shader.java          # compile/link GLSL, setter uniform
    │   ├── Mesh.java            # VAO/VBO/EBO (pos 3 + normal 3 + UV 2)
    │   ├── MeshFactory.java     # Sphere UV, Cube/House, Ground, flat/smooth
    │   ├── HSRManager.java      # Z-Buffer + Back-Face Culling (persis modul)
    │   ├── Texture2D.java       # STBImage, filter Nearest/Linear (persis modul)
    │   ├── TextureGenerator.java# checker prosedural (fallback kalau file hilang)
    │   ├── ShadowMapFBO.java    # Depth FBO 2048x2048 (persis modul)
    │   ├── Camera.java          # kamera orbit JOML
    │   └── Renderer.java        # 2-pass shadow mapping
    └── resources/
        ├── shaders/
        │   ├── phong.vert       # persis modul + FragPosLightSpace
        │   ├── phong.frag       # persis modul + shadow factor + bias
        │   ├── shadow.vert / shadow.frag
        │   └── depth.vert / depth.frag   # alias shadow (dipakai Main)
        └── textures/
            ├── brick.png        # tekstur bata prosedural 512x512
            └── checker.png      # tekstur checker 512x512
```

## 6. Cara ambil screenshot untuk laporan

1. Jalankan `./gradlew run`.
2. Untuk tiap indikator, tekan tombol yang sesuai, lalu screenshot
   (Windows: `Win + Shift + S`, atau tombol `Print Screen`):
   - (a) Phong vs Blinn-Phong — tekan **B**, screenshot sebelum dan sesudah
   - (b) Flat vs Smooth — tekan **F**, screenshot sebelum dan sesudah
   - (c) HSR On/Off — tekan **Z** lalu **C**, screenshot tiap kondisi
   - (d) Texture Filtered — tekan **M**, screenshot Nearest vs Bilinear
   - (e) Real-time Shadow — tekan **S**, screenshot bayangan di lantai
3. Simpan screenshot, masukkan ke Bab 4 laporan (ganti placeholder gambar),
   isi tabel FPS di Bab 5 dengan hasil pengukuran di laptopmu.
4. Isi **link repository GitHub** di cover laporan (placeholder di laporan).

## 7. Kalau error saat build

- **Gradle Java home invalid** (pernah terjadi di proyek BINA):
  cek `org.gradle.java.home` di `gradle.properties` / Android Studio —
  arahkan ke JDK 17 yang benar-benar ada di laptop, lalu
  `gradle clean` dan build ulang. Jangan ubah kode aplikasi untuk
  error setup build.
- **Shader gagal compile:** baca pesan error di console — nama file
  dan baris shader ditampilkan oleh `Shader.java`.
- **Tekstur tidak muncul:** pastikan run dari root proyek supaya
  path `src/main/resources/textures/...` ketemu; kalau file hilang,
  program otomatis memakai checker prosedural dan mencetak pesan
  di console.

## 8. Yang masih harus Feby lakukan sendiri

1. Run program di laptop (JDK 17 + Gradle) dan pastikan window muncul.
2. Ambil 5+ screenshot pengujian (lihat Bab 6 di atas).
3. Isi tabel FPS dan link GitHub di laporan, ganti placeholder gambar
   dengan screenshot asli.
4. Kumpulkan laporan (DOCX/PDF) dalam ZIP/RAR paling lambat H+3
   setelah jadwal laboratorium, sesuai modul.
