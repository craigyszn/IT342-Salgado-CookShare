# 🍳 CookShare

A cross-platform recipe sharing application where users can create, discover, and share food recipes. Built with Spring Boot, React, and Kotlin Android.

---

## 📱 Platforms

- **Web** — React + TypeScript
- **Mobile** — Kotlin (Android)
- **Backend** — Spring Boot (Java)

---

## ✨ Features

- User registration and login (JWT + Google OAuth)
- Create, browse, and delete recipes
- Upload recipe images and profile photos (Supabase Storage)
- Rate and comment on recipes
- Search and filter recipes by category
- Nutrition facts per recipe (Spoonacular API)
- Favorites/bookmarks
- Admin dashboard for moderation

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 21, Spring Boot, Spring Security, JPA |
| Database | MySQL |
| Web Frontend | React 18, TypeScript, CSS |
| Mobile | Kotlin, Retrofit, Glide |
| Storage | Supabase (image uploads) |
| External API | Spoonacular (recipes + nutrition) |
| Auth | JWT, Google OAuth2 |

---

## 🚀 Getting Started

### Prerequisites
- Java 21+
- Node.js 18+
- Android Studio
- MySQL
- Maven

### Backend Setup
```bash
cd backend/cookshare
# Configure application.properties with your DB credentials and API keys
./mvnw spring-boot:run
```

### Web Setup
```bash
cd web/CookShare
npm install
npm run dev
```

### Mobile Setup
1. Open `mobile/` folder in Android Studio
2. Sync Gradle
3. Run on emulator or physical device

---

## ⚙️ Environment Variables

Create a `.env` file in `web/CookShare/`:
```
VITE_SUPABASE_URL=your_supabase_url
VITE_SUPABASE_ANON_KEY=your_supabase_anon_key
VITE_SPOONACULAR_API_KEY=your_spoonacular_key
```

Add to `backend/cookshare/src/main/resources/application.properties`:
```
supabase.url=your_supabase_url
supabase.anon-key=your_supabase_anon_key
spoonacular.api-key=your_spoonacular_key
```

---

## 👨‍💻 Author

**Craig Zachary L. Salgado**
IT342 — System Integration and Architecture
Cebu Institute of Technology - University