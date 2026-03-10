package com.organization.school_api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDate;
import java.util.*;

import com.organization.school_api.repository.AcademicEventRepository;
import com.organization.school_api.repository.AttendanceRepository;
import com.organization.school_api.repository.ClassRepository;
import com.organization.school_api.repository.gradeRepository;
import com.organization.school_api.repository.userRepository;
import com.organization.school_api.service.EmailService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.servlet.http.HttpServletResponse;
import java.awt.Color;

import com.organization.school_api.Entity.AcademicEvent;
import com.organization.school_api.Entity.Attendance;
import com.organization.school_api.Entity.ClassEntity;
import com.organization.school_api.Entity.Grade;
import com.organization.school_api.Entity.User;

@Controller
public class pageController {
    @Autowired
    private userRepository userRepository;
    @Autowired
    private ClassRepository classRepo;
    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    @Autowired
    private gradeRepository gradeRepo;
    @Autowired
    private EmailService emailService;
    @Autowired
    private AttendanceRepository attendanceRepository;
    @Autowired
    private AcademicEventRepository eventRepository;

    @GetMapping("/login")
    public String showLogin(){
        return "login";
    }

    @GetMapping("/signup")
    public String showSignup(){
        return "signup";
    }

   @PostMapping("/signup")
    public String registerUser(@ModelAttribute User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            return "redirect:/signup?error=exists"; 
        }
        
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("ADMIN");
        
        // 1. Generate a random 6-digit OTP
        String otp = String.format("%06d", new java.util.Random().nextInt(999999));
        
        // 2. Save the OTP to the user and mark as UNVERIFIED
        user.setOtp(otp);
        user.setVerified(false);
        userRepository.save(user);
        
        // 3. Send the email!
        emailService.sendOtpEmail(user.getEmail(), otp);
        
        // 4. Send them to the verification screen instead of login
        return "redirect:/verify-otp?email=" + user.getEmail();
    }
    // Shows the OTP entry page
    @GetMapping("/verify-otp")
    public String showVerifyPage(@RequestParam String email, org.springframework.ui.Model model) {
        model.addAttribute("email", email);
        return "verify-otp"; // We will build this HTML next!
    }

    // Checks if the OTP they typed matches the database
    @PostMapping("/verify-otp")
    public String checkOtp(@RequestParam String email, @RequestParam String otp) {
        User user = userRepository.findByEmail(email).orElse(null);
        
        if (user != null && user.getOtp() != null && user.getOtp().equals(otp)) {
            // Success! Mark as verified and clear the OTP so it can't be used again
            user.setVerified(true);
            user.setOtp(null); 
            userRepository.save(user);
            
            return "redirect:/login?verified=true";
        }
        
        // Failed! Send back to the page with an error
        return "redirect:/verify-otp?email=" + email + "&error=invalid";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // Clears everything in the session
        return "redirect:/login?logout";
    }

    @GetMapping("/admin/dashboard")
    public String showAdminDashboard(Model model){
        List<User> onlyStudents = userRepository.findByRole("STUDENT");
        Collections.reverse(onlyStudents);
        model.addAttribute("students", onlyStudents);
        System.out.println("Displaying " + onlyStudents.size() + " students on dashboard.");
        model.addAttribute("students", userRepository.findByRole("STUDENT"));
        model.addAttribute("classes", classRepo.findAll());
        // Fetch upcoming events (Only today and future dates, sorted closest first)
    model.addAttribute("events", eventRepository.findByEventDateGreaterThanEqualOrderByEventDateAsc(java.time.LocalDate.now()));
        
        return "admin-dashboard";
    }

   @GetMapping("/admin/delete/{id}")
    @org.springframework.transaction.annotation.Transactional // Ensures all deletes happen or none do
    public String deleteUser(@PathVariable Long id){
        // 1. Find the student first
        User student = userRepository.findById(id).orElse(null);
        if (student != null) {
            // 2. Manually wipe out their related records to satisfy Foreign Key constraints
            // This clears the "child" rows so the "parent" (User) can be deleted
            attendanceRepository.deleteByStudent(student);
            gradeRepo.deleteByStudent(student);

            // 3. Now it is safe to delete the student
            userRepository.delete(student);
            System.out.println("SUCCESS: Student and all related records removed.");
        }
        
        return "redirect:/admin/dashboard?deleted";
    }

    @PostMapping("/admin/add-student")
    public String adminAddStudent(@ModelAttribute("user") User user){
        System.out.println("ADMIN ACTION: Adding student -> " + user.getFullName());

        try {
            // 2. Set default security details
            user.setPassword(passwordEncoder.encode("Student123"));
            user.setRole("STUDENT");

            // 3. Force save
            userRepository.save(user);
            System.out.println("SUCCESS: Student saved to MySQL.");
            
        } catch (Exception e) {
            // 4. This will tell us WHY MySQL rejected it (e.g., Duplicate Email)
            System.out.println("ERROR SAVING: " + e.getMessage());
        }

        return "redirect:/admin/dashboard?added";
    }

    @GetMapping("/admin/classes")
    public String showClasses(Model model){
        List<ClassEntity> allClassEntities = classRepo.findAll();
        model.addAttribute("classes",allClassEntities);
        return "admin-classes";
    }

    @PostMapping("/admin/add-class")
    public String addClass(@ModelAttribute ClassEntity classEntity){
       try {
            classRepo.save(classEntity);
            System.out.println("SUCCESS: class saved to MySQL.");
            
        } catch (Exception e) {
            // 4. This will tell us WHY MySQL rejected it (e.g., Duplicate Email)
            System.out.println("ERROR SAVING: " + e.getMessage());
        }

        return "redirect:/admin/classes?added";
    }

    @GetMapping("/admin/delete-class/{id}")
    public String deleteClass(@PathVariable Long id){
        classRepo.deleteById(id);
        return "redirect:/admin/classes?deleted";
    }

    @PostMapping("/admin/add-grade")
    public String addGrade(@ModelAttribute Grade grade){
        gradeRepo.save(grade);
        return "redirect:/admin/dashboard?gradeAdded";
    }

    @GetMapping("/admin/student-grades/{id}")
    @ResponseBody
    public List<Grade> getGradesForStudent(@PathVariable Long id){
        return gradeRepo.findByStudentId(id);
    }
    @PostMapping("/admin/update-grade")
    public String updateGrade(@ModelAttribute Grade grade) {
        // We need to re-link the student because the hidden form only has grade fields
        // Find the existing grade to get the student reference
        Grade existing = gradeRepo.findById(grade.getId()).orElse(null);
        if (existing != null) {
            existing.setSubject(grade.getSubject());
            existing.setMarks(grade.getMarks());
            gradeRepo.save(existing);
        }
        return "redirect:/admin/dashboard?updated";
    }
    @GetMapping("/admin/delete-grade/{id}")
    public String deleteGrade(@PathVariable("id") Long id) {
        try {
            gradeRepo.deleteById(id);
            // Ensure there are NO spaces or extra colons before 'redirect:'
            return "redirect:/admin/dashboard?gradeDeleted"; 
        } catch (Exception e) {
            return "redirect:/admin/dashboard?error";
        }
    }

    @GetMapping("/student/dashboard")
    public String studentDashboard(HttpSession session, Model model) {
        String email = (String) session.getAttribute("email");
        User student = userRepository.findByEmail(email)
                       .orElseThrow(() -> new RuntimeException("Student not found!"));
        List<Grade> grades = gradeRepo.findByStudentId(student.getId());

        // Calculate Average for a "Performance Level"
        double average = grades.stream().mapToInt(Grade::getMarks).average().orElse(0.0);
        
        String status = (average >= 75) ? "Pro Level 🚀" : (average >= 40) ? "On the Rise 📈" : "Keep Grinding 💪";
        // --- PUT THIS INSIDE YOUR /admin/dashboard GET MAPPING ---

        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate lookAhead = today.plusDays(60); // Generate Sundays for the next 60 days

        // 1. Get real events from the database
        java.util.List<AcademicEvent> dbEvents = eventRepository.findByEventDateGreaterThanEqualOrderByEventDateAsc(today);
        java.util.List<AcademicEvent> allEvents = new java.util.ArrayList<>(dbEvents);

        // 2. Generate Sundays dynamically
        java.time.LocalDate dateChecker = today;
        while (dateChecker.isBefore(lookAhead)) {
            if (dateChecker.getDayOfWeek() == java.time.DayOfWeek.SUNDAY) {
                AcademicEvent sunday = new AcademicEvent();
                sunday.setId(-1L); 
                sunday.setEventDate(dateChecker);
                sunday.setTitle("Sunday Weekend");
                sunday.setEventType("HOLIDAY");
                allEvents.add(sunday);
            }
            dateChecker = dateChecker.plusDays(1);
        }

        // 3. Sort chronologically
        allEvents.sort(java.util.Comparator.comparing(AcademicEvent::getEventDate));

        // 4. Limit to the next 4 events so it looks neat on the student dashboard!
        model.addAttribute("events", allEvents.stream().limit(4).toList());

        // 3. Sort everything perfectly by date
        allEvents.sort(java.util.Comparator.comparing(AcademicEvent::getEventDate));

        // 4. Limit the display to the next 8 events so it doesn't flood the UI
        java.util.List<AcademicEvent> upcomingEvents = allEvents.stream().limit(8).toList();

        model.addAttribute("events", upcomingEvents);

        model.addAttribute("student", student);
        model.addAttribute("grades", grades);
        model.addAttribute("average", String.format("%.1f", average));
        model.addAttribute("status", status);
        java.util.List<Attendance> myAttendance = attendanceRepository.findByStudentIdOrderByDateDesc(student.getId());

        long myPresent = myAttendance.stream().filter(a -> "PRESENT".equals(a.getStatus())).count();
        long myAbsent = myAttendance.stream().filter(a -> "ABSENT".equals(a.getStatus())).count();
        long myLate = myAttendance.stream().filter(a -> "LATE".equals(a.getStatus())).count();
        long totalDays = myPresent + myAbsent + myLate;

        long myPercentage = totalDays == 0 ? 0 : Math.round(((double) (myPresent + myLate) / totalDays) * 100.0);

        model.addAttribute("attPresent", myPresent);
        model.addAttribute("attAbsent", myAbsent);
        model.addAttribute("attLate", myLate);
        model.addAttribute("attPercentage", myPercentage);

        // Send the 5 most recent records to show a "Recent History" table
        model.addAttribute("recentAttendance", myAttendance.stream().limit(5).toList());
        
        return "Student-dashboard";
    }
    @PostMapping("/change-password")
    public String changePassword(@RequestParam String oldPassword,
                                @RequestParam String newPassword,
                                org.springframework.security.core.Authentication auth) {
        
        // 1. Get the currently logged-in user
        String email = auth.getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        
        String role = user.getRole();
        String dashboardRedirect = role.equals("ADMIN") ? "/admin/dashboard" : "/student/dashboard";

        // 2. Verify the old password matches the database
        if (passwordEncoder.matches(oldPassword, user.getPassword())) {
            
            // 3. Encode and save the new password
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            
            System.out.println("Password updated securely for: " + email);
            
            // 4. Force them to log in again with their new password
            return "redirect:/logout?passwordChanged=true"; 
        }
        
        // 5. If old password was wrong, send them back to their dashboard with an error
        return "redirect:" + dashboardRedirect + "?passwordError=true";
    }
    // 1. Show the "Enter your email" page
    @GetMapping("/forgot-password")
    public String showForgotPassword() {
        return "forgot-password"; 
    }

    // 2. Process the email and send the link
    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        
        if (user != null) {
            // Generate a random, unique token
            String token = java.util.UUID.randomUUID().toString();
            user.setResetToken(token);
            userRepository.save(user);

            // Create the clickable link and email it
            String resetLink = "http://localhost:8080/reset-password?token=" + token;
            emailService.sendPasswordResetEmail(email, resetLink);
        }
        
        // We always say "If the email exists, we sent a link" so hackers can't guess which emails are registered!
        return "redirect:/login?resetMailSent=true";
    }

    // 3. The user clicks the link in their email
    @GetMapping("/reset-password")
    public String showResetPassword(@RequestParam String token, org.springframework.ui.Model model) {
        // Check if the token is real
        User user = userRepository.findByResetToken(token).orElse(null);
        if (user == null) {
            return "redirect:/login?error=invalidToken";
        }
        
        // Pass the token to the HTML page
        model.addAttribute("token", token);
        return "reset-password";
    }

    // 4. Save the brand new password
    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam String token, @RequestParam String newPassword) {
        User user = userRepository.findByResetToken(token).orElse(null);
        
        if (user != null) {
            // Encode and save the new password
            user.setPassword(passwordEncoder.encode(newPassword));
            user.setResetToken(null); // Clear the token so it can't be used twice!
            userRepository.save(user);
            
            return "redirect:/login?passwordReset=true";
        }
        return "redirect:/login?error=invalidToken";
    }

   @PostMapping("/admin/update-class")
    public String updateClass(
            @RequestParam Long id, 
            @RequestParam String className, 
            @RequestParam String teacherName, 
            @RequestParam String roomNumber) {
        
        // Find the existing class by its ID
        ClassEntity existingClass = classRepo.findById(id).orElse(null); // Use your actual Repository name
        
        if (existingClass != null) {
            // Update ALL the fields
            existingClass.setClassName(className);
            existingClass.setTeacherName(teacherName);
            existingClass.setRoomNumber(roomNumber);
            
            // Save back to database
            classRepo.save(existingClass);
        }
        
        return "redirect:/admin/classes?updated=true";
    }
    @PostMapping("/admin/update-student")
    public String updateStudent(
            @RequestParam Long id, 
            @RequestParam String studentId, // NEW: Catch the Student ID
            @RequestParam String fullName, 
            @RequestParam String email, 
            @RequestParam(required = false) Long classId) { 
        
        User existingStudent = userRepository.findById(id).orElse(null);
        
        if (existingStudent != null) {
            existingStudent.setStudentId(studentId); // NEW: Update the ID
            existingStudent.setFullName(fullName);
            existingStudent.setEmail(email);
            
            if (classId != null) {
                ClassEntity assignedClass = classRepo.findById(classId).orElse(null);
                existingStudent.setStudentClass(assignedClass);
            } else {
                existingStudent.setStudentClass(null);
            }
            
            userRepository.save(existingStudent);
        }
        
        return "redirect:/admin/dashboard?studentUpdated=true";
    }
    @GetMapping("/admin/attendance")
    public String showAttendancePage(
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) LocalDate date,
            org.springframework.ui.Model model) {
        
        // Always pass the list of classes for the dropdown
        model.addAttribute("classes", classRepo.findAll());
        
        // Default to today's date if none is selected
        if (date == null) {
            date = LocalDate.now();
        }
        model.addAttribute("selectedDate", date);
        
        // If a class is selected, load its students and existing attendance
        if (classId != null) {
            ClassEntity selectedClass = classRepo.findById(classId).orElse(null);
            if (selectedClass != null) {
                model.addAttribute("selectedClass", selectedClass);
                
                // Fetch existing attendance to pre-fill the form
                List<Attendance> existingRecords = attendanceRepository.findByStudentClassIdAndDate(classId, date);
                
                // Map Student ID to their Status (e.g., ID 5 -> "PRESENT")
                Map<Long, String> attendanceMap = new HashMap<>();
                for (Attendance a : existingRecords) {
                    attendanceMap.put(a.getStudent().getId(), a.getStatus());
                }
                model.addAttribute("attendanceMap", attendanceMap);
            }
        }
        
        return "admin-attendance";
    }

    // 2. Save the Attendance
    @PostMapping("/admin/save-attendance")
    public String saveAttendance(
            @RequestParam Long classId,
            @RequestParam LocalDate date,
            HttpServletRequest request) { // We use request to grab dynamic input names
        
        ClassEntity studentClass = classRepo.findById(classId).orElse(null);
        if (studentClass == null) return "redirect:/admin/attendance?error=classNotFound";

        // Loop through every student in this class
        for (User student : studentClass.getStudents()) {
            // The HTML inputs will be named "status_1", "status_2", etc.
            String status = request.getParameter("status_" + student.getId());
            
            if (status != null) {
                // Find existing record, or create a new one if it's the first time
                Attendance attendance = attendanceRepository.findByStudentIdAndDate(student.getId(), date)
                        .orElse(new Attendance());
                
                attendance.setStudent(student);
                attendance.setStudentClass(studentClass);
                attendance.setDate(date);
                attendance.setStatus(status); // "PRESENT", "ABSENT", or "LATE"
                
                attendanceRepository.save(attendance);
            }
        }
        
        return "redirect:/admin/attendance?classId=" + classId + "&date=" + date + "&saved=true";
    }
    // 1. Show the On-Screen Report (Now with Monthly Filtering!)
    @GetMapping("/admin/attendance-report")
    public String showAttendanceReport(
            @RequestParam(required = false) Long classId, 
            @RequestParam(required = false) String monthFilter, // e.g., "2026-03"
            org.springframework.ui.Model model) {
        
        model.addAttribute("classes", classRepo.findAll());
        model.addAttribute("monthFilter", monthFilter);
        
        if (classId != null) {
            ClassEntity selectedClass = classRepo.findById(classId).orElse(null);
            if (selectedClass != null) {
                model.addAttribute("selectedClass", selectedClass);
                
                java.util.List<java.util.Map<String, Object>> summaryList = new java.util.ArrayList<>();
                
                for (User student : selectedClass.getStudents()) {
                    java.util.List<Attendance> records = attendanceRepository.findByStudentIdOrderByDateDesc(student.getId());
                    
                    // NEW: If a month is selected, filter the records!
                    if (monthFilter != null && !monthFilter.isEmpty()) {
                        records = records.stream()
                            .filter(a -> {
                                String recordMonth = a.getDate().getYear() + "-" + String.format("%02d", a.getDate().getMonthValue());
                                return recordMonth.equals(monthFilter);
                            }).toList();
                    }
                    
                    long present = records.stream().filter(a -> "PRESENT".equals(a.getStatus())).count();
                    long absent = records.stream().filter(a -> "ABSENT".equals(a.getStatus())).count();
                    long late = records.stream().filter(a -> "LATE".equals(a.getStatus())).count();
                    long total = present + absent + late;
                    long percentage = total == 0 ? 0 : Math.round(((double) (present + late) / total) * 100.0);
                    
                    java.util.Map<String, Object> stat = new java.util.HashMap<>();
                    stat.put("student", student);
                    stat.put("present", present);
                    stat.put("absent", absent);
                    stat.put("late", late);
                    stat.put("total", total);
                    stat.put("percentage", percentage);
                    
                    summaryList.add(stat);
                }
                model.addAttribute("summaryList", summaryList);
            }
        }
        return "admin-attendance-report";
    }

    // 2. Generate the PDF Download
    @GetMapping("/admin/download-attendance")
    public void downloadAttendanceReport(
            @RequestParam Long classId,
            @RequestParam(required = false) String monthFilter,
            HttpServletResponse response) throws Exception {

        ClassEntity selectedClass = classRepo.findById(classId).orElse(null);
        if (selectedClass == null) return;

        // Set Response Headers for PDF Download
        response.setContentType("application/pdf");
        String fileName = "Attendance_" + selectedClass.getClassName().replace(" ", "_") + 
                        (monthFilter != null && !monthFilter.isEmpty() ? "_" + monthFilter : "_Overall") + ".pdf";
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

        Document document = new Document();
        PdfWriter.getInstance(document, response.getOutputStream());
        document.open();

        // Fonts
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, java.awt.Color.DARK_GRAY);
        Font subFont = FontFactory.getFont(FontFactory.HELVETICA, 12, java.awt.Color.GRAY);
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, java.awt.Color.WHITE);

        // Title & Subtitle
        Paragraph title = new Paragraph("Class Attendance Report", titleFont);
        title.setAlignment(Paragraph.ALIGN_CENTER);
        document.add(title);

        String subtitleText = "Class: " + selectedClass.getClassName() + 
                            (monthFilter != null && !monthFilter.isEmpty() ? " | Month: " + monthFilter : " | All-Time Overall");
        Paragraph subtitle = new Paragraph(subtitleText, subFont);
        subtitle.setAlignment(Paragraph.ALIGN_CENTER);
        subtitle.setSpacingAfter(20);
        document.add(subtitle);

        // Build the Table
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{3f, 1f, 1f, 1f, 1.5f}); // Adjust column widths

        String[] headers = {"Student Name", "Present", "Late", "Absent", "Attendance %"};
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
            cell.setBackgroundColor(new java.awt.Color(79, 70, 229)); // Indigo
            cell.setPadding(8);
            table.addCell(cell);
        }

        // Fill the Table Data
        for (User student : selectedClass.getStudents()) {
            java.util.List<Attendance> records = attendanceRepository.findByStudentIdOrderByDateDesc(student.getId());

            if (monthFilter != null && !monthFilter.isEmpty()) {
                records = records.stream()
                    .filter(a -> (a.getDate().getYear() + "-" + String.format("%02d", a.getDate().getMonthValue())).equals(monthFilter))
                    .toList();
            }

            long present = records.stream().filter(a -> "PRESENT".equals(a.getStatus())).count();
            long absent = records.stream().filter(a -> "ABSENT".equals(a.getStatus())).count();
            long late = records.stream().filter(a -> "LATE".equals(a.getStatus())).count();
            long total = present + absent + late;
            long percentage = total == 0 ? 0 : Math.round(((double) (present + late) / total) * 100.0);

            table.addCell(student.getFullName());
            table.addCell(String.valueOf(present));
            table.addCell(String.valueOf(late));
            table.addCell(String.valueOf(absent));
            table.addCell(percentage + "%");
        }

        document.add(table);
        document.close();
    }
        @GetMapping("/student/download-report")
    public void downloadReportCard(HttpServletResponse response, java.security.Principal principal) throws Exception {
        
        // 1. Get the logged-in student using Spring Security
        if (principal == null) {
            response.sendRedirect("/login");
            return;
        }

        // Find the user in the database using their logged-in email
        User user = userRepository.findByEmail(principal.getName()).orElse(null);
        if (user == null) {
            response.sendRedirect("/login");
            return;
        }

        // 2. Set the Response Headers so the browser knows it's downloading a PDF
        response.setContentType("application/pdf");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=ReportCard_" + user.getFullName().replace(" ", "_") + ".pdf";
        response.setHeader(headerKey, headerValue);

        // 3. Initialize the PDF Document
        Document document = new Document();
        PdfWriter.getInstance(document, response.getOutputStream());
        document.open();

        // 4. Define Fonts
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24, Color.DARK_GRAY);
        Font subFont = FontFactory.getFont(FontFactory.HELVETICA, 14, Color.GRAY);
        Font tableHeaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.WHITE);

        // 5. Add Header Info
        Paragraph title = new Paragraph("Official Report Card", titleFont);
        title.setAlignment(Paragraph.ALIGN_CENTER);
        document.add(title);

        Paragraph studentName = new Paragraph("Student: " + user.getFullName(), subFont);
        studentName.setAlignment(Paragraph.ALIGN_CENTER);
        studentName.setSpacingAfter(20);
        document.add(studentName);

        // 6. Create the Grades Table (3 Columns)
        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);

        // Table Headers
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(new Color(79, 70, 229)); // Indigo color
        cell.setPadding(8);

        cell.setPhrase(new Phrase("Term / Semester", tableHeaderFont));
        table.addCell(cell);
        cell.setPhrase(new Phrase("Subject", tableHeaderFont));
        table.addCell(cell);
        cell.setPhrase(new Phrase("Marks", tableHeaderFont));
        table.addCell(cell);

        // 7. Fill the Table with the Student's Grades
        java.util.List<Grade> grades = gradeRepo.findByStudentId(user.getId());
        for (Grade grade : grades) {
            table.addCell(grade.getSemester() != null ? grade.getSemester() : "N/A");
            table.addCell(grade.getSubject());
            table.addCell(String.valueOf(grade.getMarks()) + "%");
        }

        document.add(table);

        // 8. Add a footer
        Paragraph footer = new Paragraph("Generated by SchoolPro System on " + java.time.LocalDate.now());
        footer.setAlignment(Paragraph.ALIGN_CENTER);
        footer.setSpacingBefore(30);
        document.add(footer);

        // 9. Close and finalize the document
        document.close();
    }
    @PostMapping("/admin/add-event")
    public String addEvent(
            @RequestParam java.time.LocalDate eventDate, 
            @RequestParam String title, 
            @RequestParam String eventType) {
        
        AcademicEvent event = new AcademicEvent();
        event.setEventDate(eventDate);
        event.setTitle(title);
        event.setEventType(eventType);
        
        eventRepository.save(event);
        return "redirect:/admin/dashboard?eventAdded=true";
    }

    @GetMapping("/admin/delete-event/{id}")
    public String deleteEvent(@PathVariable Long id) {
        eventRepository.deleteById(id);
        return "redirect:/admin/dashboard?eventDeleted=true";
    }
}
