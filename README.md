
# SnappFood 🍽️

Welcome to **SnappFood**, a full-featured, multi-role food ordering system developed as a final project for the *Advanced Programming* course at Amirkabir University of Technology (AUT).  
This Java-based application emulates real-world online food delivery operations through an intuitive JavaFX interface and a well-structured backend. It supports various user roles — Buyer , Seller , Courier and Admin — each with a tailored experience.

>  Hungry to code? Whether you're placing an order, managing a kitchen, or delivering meals, **SnappFood** is built to serve.

---

## 🧑‍🏫 Course Details

- **Course Title**: Advanced Programming – Spring 2025  
- **Institution**: Amirkabir University of Technology (AUT)  
- **Instructors**: Dr. Amir Kalbasi, Dr. Hossein Zeinali  

---

## Development Team

Project developed collaboratively by:

-👨‍💻 **Danial Seyedi**  
-👨‍💻 **Alireza Sarabi**

Both developers contributed to the design and implementation of both frontend and backend components.

---

## ✨ Features at a Glance

-  Secure, role-based authentication and authorization  
-  Multi-role support: Buyer, Seller, Courier, Admin  
-  Restaurant and menu management (with live updates)  
-  Smart cart system with search and category filters  
-  Profile management for all user types  
-  Advanced search and filtering by food, restaurant, or price  
-  Online payments and internal wallet integration  
-  Order creation, confirmation, and delivery flow  
-  Real-time order tracking for customers and couriers   
-  Ratings, reviews, and image uploads by customers  
-  Discount codes and promotional offers  
-  Admin dashboard with reports and analytics  
-  Transaction history for payments and earnings  
---

## 🖼️ Screenshots
<p align="center">
  <img src="screenshots/screenshot1.jpg" width="700"><br><br>
  <img src="screenshots/screenshot2.jpg" width="700"><br><br>
  <img src="screenshots/screenshot4.jpg" width="700"><br><br>
  <img src="screenshots/screenshot3.jpg" width="700"><br><br>
  <img src="screenshots/screenshot5.jpg" width="700">
</p>
---


## 📁 Project Structure

### 🔹 Frontend (`Client/src/main/java/org/example/snappfrontend/`)

- `controllers/` – JavaFX controllers (UI logic)  
- `dto/` – Data transfer objects for API communication  
- `http/` – HTTP request handling utilities  
- `models/` – Core frontend data models  
- `utils/` – Constants and utility functions  
- `resources/pages/` – FXML layout files  
- `resources/images/` – Icons and UI assets  

### 🔹 Backend (`Server/src/...`)

- `controller/` – RESTful endpoints for handling requests  
- `model/` – Core backend data models
- `dao/` – Data access layer using Hibernate ORM
- `dto/` – Backend data transfer objects  
- `utils/` – Backend utility classes  
- `main/` – Application entry point 

---

  ## 🧱 Tech Stack

| Layer        | Technologies Used                                |
|--------------|--------------------------------------------------|
| Frontend     | JavaFX (FXML), SceneBuilder, Java 17             |
| Backend      | Java OOP, RESTful APIs over HTTP/JSON            |
| Storage      | Relational Database (via Hibernate ORM)          |
| Libraries    | Gson, JavaFX, Java HTTP Client                   |

---

## ⚙️ Getting Started

### ✅ Requirements

- Java 17 or higher  
- IDE (e.g., IntelliJ IDEA, Eclipse)
- DataBase

### 🚀 Running the Backend

1. Open the `Server/` directory in your IDE  
2. Locate and run the `main` class  
3. The backend will start and listen for HTTP requests  

### 🖥️ Running the Frontend

1. Open the `Client/` directory in your IDE  
2. Run the main JavaFX application class  
3. The UI will automatically communicate with the backend via REST  

> ⚠️ Make sure the backend is up and running before launching the frontend.

---

