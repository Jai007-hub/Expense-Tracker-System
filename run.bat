@echo off
cd "C:\Docs\UPES\Sem - 4\Oops\Oops Project\ExpenseTracker"
echo Compiling...
javac -d out src\Expense.java src\ExpenseService.java src\ExpenseManager.java src\BackgroundSaver.java src\UserManager.java src\ExpenseTrackerGUI.java
if %errorlevel% neq 0 (
    echo.
    echo COMPILATION FAILED
    pause
    exit /b
)
echo Running...
java -cp out ExpenseTrackerGUI
pause