# TripExpense – Budget Manager with Payment Proof

**TripExpense** is an Android application designed to track money spent on trip expenses while storing and displaying payment screenshot proofs for each expense.

---

## 📱 Features

1. **Add Expense Screen**
   - Enter **Title**, **Amount (₹)**, and select a **Category** (*Food*, *Travel*, *Stay*, *Shopping*).
   - Attach a payment proof screenshot directly from device storage or use a 1-tap sample payment proof (e.g. BMTC ₹6 ticket).
   - Live **ImageView preview** shown before saving.

2. **Upload & Persist Payment Screenshot**
   - Selected images are copied to internal app storage (`context.filesDir`).
   - SQLite stores the image file path to guarantee fast, offline, persistent access across device reboots.

3. **Expense List Screen (`ListView` + `ImageView`)**
   - Displays all expenses in a custom `ListView`.
   - Displays a memory-efficient downscaled **screenshot thumbnail** using `BitmapFactory.Options.inSampleSize`.
   - **Tap Item**: Opens a popup dialog showing the full payment screenshot.
   - **Long-Press Item**: Prompts confirmation to delete the expense, removes the entry from SQLite, and auto-refreshes the list.

4. **Summary Screen (SQL `SUM` + `GROUP BY`)**
   - Displays the **Total Spent** overall using `SELECT SUM(amount) FROM expenses`.
   - Displays **Category Breakdown** using `SELECT category, SUM(amount) AS total FROM expenses GROUP BY category`.

5. **Budget Alert System**
   - Set a custom **Trip Budget (₹)** stored in `SharedPreferences`.
   - Triggers a **Toast warning** whenever total expenses cross the set budget limit.

---

## 🛠️ Architecture & Concepts Used

- **UI Framework**: Android XML Layouts, `MaterialCardView`, `TabLayout`, `ViewPager2`
- **List & Image Rendering**: `ListView`, `ExpenseAdapter` (`ArrayAdapter`), `ImageView`
- **Database**: SQLite (`SQLiteOpenHelper`)
- **Storage**: Internal App Storage (`filesDir`), `SharedPreferences` for trip budget
- **Permissions**: `READ_MEDIA_IMAGES` (Android 13+) and `READ_EXTERNAL_STORAGE` (Android 12 & below)

---

## 📂 Project Structure

```
com.example.lab_test2/
├── MainActivity.kt               # Navigation host, TabLayout, ViewPager2 & Budget Alert Toast check
├── ViewPagerAdapter.kt           # ViewPager2 FragmentStateAdapter
├── AddExpenseFragment.kt         # Form, image picker, sample BMTC proof & insertion logic
├── ExpenseListFragment.kt        # ListView, full screenshot preview dialog & long-press delete
├── SummaryFragment.kt            # Trip budget setup, Total spent & SQL SUM GROUP BY breakdown
├── ExpenseAdapter.kt             # Custom ListView ArrayAdapter with bitmap thumbnail scaling
├── ExpenseDatabaseHelper.kt      # SQLite OpenHelper (Table: expenses, SQL SUM & GROUP BY queries)
└── Expense.kt                    # Data classes for Expense & CategoryTotal
```

---

## 🚀 How to Build & Run

1. Open the project in **Android Studio**.
2. Sync Project with Gradle Files.
3. Run on an Android Emulator or physical device (Target SDK 37 / Min SDK 24).
4. Run Unit Tests:
   ```bash
   ./gradlew :app:testDebugUnitTest
   ```
5. Build Debug APK:
   ```bash
   ./gradlew app:assembleDebug
   ```
