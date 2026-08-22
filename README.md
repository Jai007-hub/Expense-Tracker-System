# AJAVA Expense Tracker System

A multi-user personal finance management desktop application built with Java and Java Swing as part of the Object Oriented Programming (OOPs) project

🧾 About the Project
The Java Expense Tracker System is a fully offline desktop application that allows multiple users to track, manage, and analyse their daily expenses through a clean graphical interface. No internet connection or database required — all data is stored locally in CSV files.
The project demonstrates all three required OOP concepts:


✅ Interface — ExpenseService.java defines the service contract

✅ Multi-threading — BackgroundSaver.java auto saves every 30 seconds

✅ GUI — Complete Swing interface with tables, dialogs and buttons



✨ Features:-

👤 Multi-user support — each user has isolated expense records

➕ Add, ✏️ Edit, 🗑️ Delete expenses easily

↩️ Undo last deleted expense

🔍 Search and filter by name or category

📊 Monthly and category wise summary report

💱 10 currency support — INR, USD, EUR, GBP, JPY, CAD, AUD, CHF, AED, SGD

💾 Auto save every 30 seconds in background

🔄 Switch between users without restarting

📅 Data persists across sessions via CSV storage

🔃 Column sorting by clicking table headers


🗂️ Project Structure:-

ExpenseTracker/

├── src/

│   ├── Expense.java          → Data model

│   ├── ExpenseService.java   → Interface (contract)

│   ├── ExpenseManager.java   → Business logic

│   ├── BackgroundSaver.java  → Multi-threading

│   ├── UserManager.java      → User management

│   └── ExpenseTrackerGUI.java → GUI + main entry point

├── out/                      → Compiled class files

├── data/

│   ├── expenses.csv          → All expense records

│   └── users.txt             → Registered usernames

└── run.bat                   → One click compile and run


🛠️ Technologies Used
TechnologyPurposeJava JDK 17+Core programming languageJava SwingGUI frameworkJava StreamsFiltering and grouping dataScheduledExecutorServiceBackground auto-save threadJava I/OCSV and TXT file handlingVS CodeDevelopment environment

🚀 How to Run
Prerequisites:

Java JDK 17 or higher installed
Verify: java -version and javac -version

Option 1 — One click:
Double click run.bat

Option 2 — Manual:
bashjavac -d out src\Expense.java src\ExpenseService.java src\ExpenseManager.java src\BackgroundSaver.java src\UserManager.java src\ExpenseTrackerGUI.java
java -cp out ExpenseTrackerGUI


<img width="1180" height="761" alt="image" src="https://github.com/user-attachments/assets/7ec3ba2c-9e09-4fbd-af11-aec9ffc20741" />



💡 OOP Concepts Demonstrated
ConceptWhere UsedInterfaceExpenseService.javaEncapsulationPrivate fields in Expense.javaAbstractionGUI calls interface methods onlyInheritanceExpenseTrackerGUI extends JFramePolymorphismExpenseService variable holds ExpenseManager objectConstructor OverloadingTwo constructors in Expense.javaStatic Memberscounter variable in Expense.javaMulti-threadingBackgroundSaver.java

Jai Aditya Deo. D

Lead Developer — Coding and GUI
