# XV ADDON (Minecraft 1.21.11)

Addon สำหรับ Meteor Client — โมดูล: death-coords, hunger-warning, spawner-notifier / คำสั่ง .xv / HUD xv-watermark

## วิธี build (ต้องใช้ JDK 21 + Gradle 9.2+)
    gradle wrapper --gradle-version 9.2.1   # ครั้งแรกครั้งเดียว
    ./gradlew build
jar อยู่ใน build/libs/ (เอาไฟล์ที่ไม่มี -sources)

## หรือ build บน GitHub (ไม่ต้องลงอะไร)
อัปโหลดโฟลเดอร์นี้ขึ้น GitHub repo ใหม่ -> แท็บ Actions -> Build -> Run workflow -> โหลด artifact

## ติดตั้ง
ใส่ใน .minecraft/mods คู่กับ Fabric Loader, Fabric API, Meteor Client (1.21.11)
