
# 🍽️ SnappFood – Multi-Role Online Food Ordering System

Welcome to **SnappFood**, a dynamic full-stack Java application built for the Advanced Programming course at AUT.  
This system replicates the real-world flow of food delivery with distinct roles: customers, restaurants, delivery agents, and admins — all tied together through a clean JavaFX interface and a robust backend.

> 🍔 Whether you're hungry, managing a restaurant, or delivering delicious meals — SnappFood has a role for you.

---

## 🧑‍🏫 Course Information
- **Course**: Advanced Programming (Spring 2025)
- **University**: Amirkabir University of Technology (AUT)
- **Instructors**: Dr. Amir Kalbasi, Dr. Hossein Zeinali

## 👨‍💻 Developers
This project was developed collaboratively by:
- **Danial Seyedi**
- **Alireza Sarabi**

Each team member actively contributed to both the frontend and backend components of the system.

---

## ✨ Key Features

- 🔐 Role-based registration & login
- 👤 User roles: Customer, Restaurant, Delivery, Admin
- 🍽️ Dynamic restaurant and menu management
- 🛒 Cart system with filtering & search
- 💸 Online payment and internal wallet
- 📦 Real-time order tracking & delivery
- 📢 Push notifications for order updates
- 🌟 Ratings, reviews, and image uploads
- 📊 Admin dashboard & analytics

---

## 🧱 Technologies Used

| Layer     | Technology                             |
|-----------|----------------------------------------|
| Frontend  | JavaFX (FXML), SceneBuilder, Java 17   |
| Backend   | Java OOP, RESTful API (HTTP/JSON)      |
| Storage   | JSON Files (Local Disk with Auto-save) |
| Utilities | Gson, JavaFX, Java HTTP Client         |

---

## 📦 Project Structure

### 🔹 Client Side (Frontend)
Located at: `Client/src/main/java/org/example/snappfrontend`

- `controllers/` – JavaFX page controllers  
- `dto/` – Data transfer objects  
- `http/` – HTTP request handlers  
- `models/` – Core frontend models  
- `utils/` – Helper functions and constants  
- `resources/pages/` – FXML files (JavaFX UI)  
- `resources/images/` – UI images and icons  

### 🔹 Server Side (Backend)
Located at: `Server/src/...`

- `controller/` – REST-style endpoints  
- `model/` – Backend data models  
- `dao/` – File-based data access layer  
- `service/` – Business logic layer  
- `utils/` – Helper functions for backend  
- `main/` – Entry point for backend execution  

---

## 🖼️ Screenshots

(Place your screenshots in a folder named `screenshots/` next to your `README.md`)

![Screenshot 1](screenshots/screenshot1.png)  
![Screenshot 2](screenshots/screenshot2.png)  
![Screenshot 3](screenshots/screenshot3.png)  
![Screenshot 4](screenshots/screenshot4.png)  
![Screenshot 5](screenshots/screenshot5.png)  
![Screenshot 6](screenshots/screenshot6.png)  
![Screenshot 7](screenshots/screenshot7.png)  

---

## ⚙️ How to Run the Project

### ✅ Prerequisites
- Java 17+ installed
- IDE like IntelliJ IDEA or Eclipse

### ▶️ Run Backend
1. Open the `Server/` folder in your IDE
2. Run the main class (entry point)
3. Backend listens on standard HTTP endpoints

### ▶️ Run Frontend
1. Open the `Client/` folder in your IDE
2. Launch the JavaFX main class
3. UI will connect to backend using RESTful HTTP

> ⚠️ Ensure the backend is running before launching the frontend.

---

## 🧠 How to Add Screenshots in GitHub

1. Create a folder named `screenshots` in the root of your GitHub project (same level as `README.md`).
2. Name your images like: `screenshot1.png`, `screenshot2.png`, ..., `screenshot7.png`
3. Push the images using Git:
```bash
mkdir screenshots
mv path/to/images/*.png screenshots/
git add screenshots/
git commit -m "Add screenshots"
git push
```
4. Keep image links in `README.md` like this:
```markdown
![Screenshot 1](screenshots/screenshot1.png)
```

---

## 📜 License

This repository is intended for educational use only as part of AUT's AP course.
