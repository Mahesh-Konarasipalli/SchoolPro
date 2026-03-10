📂 SchoolPro | Full-Stack Academic Management System
SchoolPro is a comprehensive, gamified academic management platform designed for the modern educational era. It bridges the gap between administrators, educators, and students with a sleek, responsive, and time-aware user interface.

🚀 Key Features
🔐 Advanced Security & Auth
Multi-Role Access Control: Custom dashboards for Admins, Teachers, and Students.

Multi-Factor Authentication: OTP-based account verification using Spring Mail with HTML templates.

Secure Recovery: Tokenized "Forgot Password" workflow with automated email delivery.

🎨 Premium UI/UX
Dynamic Theme Engine: Seamless Light/Dark mode transitions synced across the entire application via localStorage.

Auto-Time Perception: UI automatically shifts to Dark Mode after 6:00 PM and greets users based on the local time.

Gamified Student Hub: Animated attendance rings, interactive "Quest" calendars, and visual grade tracking.

📊 Administrative Power
Attendance Analytics: Class-wide summary reports with dynamic percentage calculations.

PDF Export Engine: Automated generation of attendance reports and student report cards.

Dynamic Academic Calendar: Real-time event management (Exams, Holidays, Activities) that updates globally.

🛠️ Tech Stack
Backend: Java 17, Spring Boot 3, Spring Security, Spring Data JPA.

Database: MySQL (Relational schema with many-to-one mapping for Classes/Students).

Frontend: Thymeleaf, Tailwind CSS (Custom Dark Mode Configuration), JavaScript (ES6+).

Communication: SMTP Server integration for automated transactional emails.

Tools: Maven, Phosphor Icons, DiceBear API for dynamic user avatars.

🏗️ Database Architecture
The system utilizes a relational database structure to ensure data integrity:

User Table: Stores credentials, roles, and verification status.

Class Table: Manages classroom details and teacher assignments.

Attendance Table: Maps students to specific dates with status (Present, Absent, Late).

Grades Table: Tracks academic performance across various semesters and subjects.

⚙️ Installation & Setup
Clone the Repository:

Bash
git clone https://github.com/Mahesh-Konarasipalli/SchoolPro.git
Configure Database:
Update src/main/resources/application.properties with your MySQL credentials.

Setup Email Service:
Enter your SMTP credentials and App Password in application.properties.

Run the App:

Bash
mvn spring-boot:run
💡 Technical Challenges Overcome
State Persistence: Implemented a JavaScript-based theme manager that prevents "white flashes" by checking system preferences and localStorage before the DOM fully loads.

Dynamic Reports: Built a logic-heavy attendance summary engine that calculates percentages on the fly for PDF generation.

Asynchronous UX: Integrated AJAX fetches for student grade management to allow real-time updates without page reloads.

👨‍💻 Contributor
Mahesh Aspiring Software Engineer | Java Full-Stack Developer linkedin.com/in/mahesh-konarasipalli-6797882a2

Some Imgs of application
## 📸 Project Gallery

<p align="center">
  <img src="screenshots/admin_loginpage.png" width="400" title="Login Dark Mode">
  <img src="screenshots/Admin_dashboard.png" width="400" title="Admin Dashboard">
  <img src="screenshots/Student_dashboard.png" width="400" title="Student Dashboard">
  <img src="screenshots/student_loginpage.png" width="400" title="Student Login">
</p>

### ✨ Key UI Features
* **Dynamic Theme Engine:** Real-time Light/Dark mode switching.
* **Responsive Design:** Optimized for both Desktop and Mobile users.
* **Interactive Analytics:** Visual attendance rings and grade tracking.