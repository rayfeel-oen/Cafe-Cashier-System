# ☕ Café POS — Point of Sale System

Aplikasi kasir desktop untuk pengelolaan transaksi dan operasional kafe, dibangun dengan **Java Swing** dan **MySQL**.

---

## ✨ Fitur

| Modul | Deskripsi |
|---|---|
| 🔐 Login | Autentikasi user sebelum masuk sistem |
| 🧾 Kasir | Input transaksi, keranjang belanja, hitung kembalian otomatis |
| 📋 Kelola Menu | Tambah, edit, hapus item menu (nama, kategori, harga, stok) |
| 📊 Laporan | Riwayat dan rekap transaksi |

---

## 🛠️ Teknologi

- **Java** (JDK 11+)
- **Java Swing** — GUI desktop
- **MySQL** — database
- **MySQL Connector/J** — JDBC driver

---

## ⚙️ Cara Menjalankan

### 1. Clone repository
```bash
git clone https://github.com/username/cafe-pos.git
cd cafe-pos
```

### 2. Siapkan database MySQL
Buat database baru bernama `cafe_pos`, lalu jalankan script berikut:

```sql
CREATE DATABASE cafe_pos;
USE cafe_pos;

CREATE TABLE users (
    id_user   INT PRIMARY KEY AUTO_INCREMENT,
    username  VARCHAR(50) NOT NULL,
    password  VARCHAR(50) NOT NULL
);

CREATE TABLE menu (
    id_menu    INT PRIMARY KEY,
    nama_menu  VARCHAR(100) NOT NULL,
    kategori   VARCHAR(50),
    harga      DOUBLE,
    stok       INT
);

CREATE TABLE transaksi (
    id_transaksi  INT PRIMARY KEY AUTO_INCREMENT,
    tanggal       DATETIME DEFAULT NOW(),
    total         DOUBLE,
    bayar         DOUBLE,
    kembalian     DOUBLE
);

-- Tambah user default
INSERT INTO users (username, password) VALUES ('admin', 'admin');
```

### 3. Sesuaikan koneksi database
Edit file `src/connection/Koneksi.java` jika perlu:

```java
private static final String URL      = "jdbc:mysql://localhost:3306/cafe_pos";
private static final String USER     = "root";
private static final String PASSWORD = "";  // sesuaikan password MySQL kamu
```

### 4. Jalankan aplikasi
Buka project di **IntelliJ IDEA**, lalu jalankan `src/main/Main.java`.

---

## 📁 Struktur Project

```
src/
├── connection/
│   └── Koneksi.java        # Manajemen koneksi database (singleton + auto-reconnect)
├── main/
│   └── Main.java           # Entry point aplikasi
├── model/
│   └── Menu.java           # Model data menu
└── view/
    ├── FormLogin.java       # Halaman login + palet warna global
    ├── FormKasir.java       # Halaman kasir + utility styling
    ├── FormMenu.java        # Halaman kelola menu
    ├── FormLaporan.java     # Halaman laporan transaksi
    └── GenshinPin.java      # Easter egg dekoratif
```

---

## 🎨 Tampilan

Menggunakan tema gelap bernuansa kopi dengan palet warna:

| Nama | Hex | Keterangan |
|---|---|---|
| Espresso | `#1A0F0A` | Background utama |
| Roast | `#2D1810` | Panel sekunder |
| Caramel | `#C8803A` | Aksen utama / tombol |
| Cream | `#F5EDD8` | Teks utama |

---

## 📌 Catatan

- Pastikan **MySQL Server** sudah berjalan sebelum membuka aplikasi
- Default login: `admin` / `admin`
- Port database default: `3306`

---

## 👤 Author

Dibuat sebagai project tugas kuliah — Surabaya, 2026.
