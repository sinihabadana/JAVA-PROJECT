package jdbc;

import java.sql.*;
import java.util.Scanner;

public class AdminModule {

    static Scanner sc = new Scanner(System.in);

    public static void adminLogin(Connection con) throws Exception {

        System.out.print("Admin Username: ");
        String u = sc.next();
        System.out.print("Admin Password: ");
        String p = sc.next();

//        if (!u.equals("admin") || !p.equals("admin123")) {
//            System.out.println("Invalid Admin Credentials!");
//            return;
//        }
        
        PreparedStatement ps =
        	    con.prepareStatement(
        	        "SELECT * FROM admin WHERE username=? AND password=?");
        	ps.setString(1, u);
        	ps.setString(2, p);

        	ResultSet rs = ps.executeQuery();

        	if (!rs.next()) {
        	    System.out.println("Invalid Admin Credentials!");
        	    return;
        	}

        showPendingLeaves(con);

        while (true) {
            System.out.println("\n--- ADMIN MENU ---");
            System.out.println("1. Approve Leave");
            System.out.println("2. Reject Leave");
            System.out.println("3. Add Employee");
            System.out.println("4. Delete Employee");
            System.out.println("5. View Employee Details");
            System.out.println("6. Logout");
            System.out.print("Enter choice: ");

            int ch = sc.nextInt();

            switch (ch) {
                case 1:
                    approveLeave(con);
                    showPendingLeaves(con);
                    break;
                case 2:
                    rejectLeave(con);
                    showPendingLeaves(con);
                    break;
                case 3:
                	addEmployee(con);
                	break;
                case 4:
                	deleteEmployee(con);
                	break;
                case 5:
                	viewEmployee(con);
                	break;
                case 6:
                    return;
                default:
                    System.out.println("Invalid choice!");
            }
        }
    }

    static void showPendingLeaves(Connection con) throws Exception {

        PreparedStatement ps =
            con.prepareStatement(
                "SELECT * FROM leave_request WHERE status='Pending'");
        ResultSet rs = ps.executeQuery();

        System.out.println("\n===== PENDING LEAVE REQUESTS =====");
        System.out.println("LeaveID EmpID Days Type SalaryDeducted");

        boolean found = false;
        while (rs.next()) {
            found = true;
            System.out.println(
                rs.getInt("leave_id") + "   " +
                rs.getInt("emp_id") + "   " +
                rs.getInt("leave_days") + "   " +
                rs.getString("leave_type") + "   ₹" +
                rs.getDouble("salary_deducted"));
        }

        if (!found) {
            System.out.println("No pending leave requests.");
        }
    }

    static void approveLeave(Connection con) throws Exception {

        System.out.print("Enter Leave ID to Approve: ");
        int leaveId = sc.nextInt();

        // Step 1: Fetch leave details
        PreparedStatement fetch =
            con.prepareStatement(
                "SELECT emp_id, leave_days FROM leave_request " +
                "WHERE leave_id=? AND status='Pending'");
        fetch.setInt(1, leaveId);
        ResultSet rs = fetch.executeQuery();

        if (!rs.next()) {
            System.out.println("Invalid or already processed leave!");
            return;
        }

        int empId = rs.getInt("emp_id");
        int days = rs.getInt("leave_days");

        // Step 2: Approve leave
        PreparedStatement approve =
            con.prepareStatement(
                "UPDATE leave_request SET status='Approved' WHERE leave_id=?");
        approve.setInt(1, leaveId);

        int updated = approve.executeUpdate();

        if (updated > 0) {

            // Step 3: Update leave balance
            PreparedStatement updateBalance =
                con.prepareStatement(
                    "UPDATE employee SET remaining_leaves = " +
                    "GREATEST(remaining_leaves - ?, 0) WHERE emp_id=?");
            updateBalance.setInt(1, days);
            updateBalance.setInt(2, empId);
            updateBalance.executeUpdate();

            // Step 4: Insert into history
            PreparedStatement historyInsert =
                con.prepareStatement(
                    "INSERT INTO leave_history " +
                    "(emp_id, leave_id, leave_days, final_status, action_date) " +
                    "VALUES (?, ?, ?, 'Approved', CURDATE())");

            historyInsert.setInt(1, empId);
            historyInsert.setInt(2, leaveId);
            historyInsert.setInt(3, days);
            historyInsert.executeUpdate();

            System.out.println("Leave approved successfully!");

        } else {
            System.out.println("Leave approval failed!");
        }
    }


    static void rejectLeave(Connection con) throws Exception {

        System.out.print("Enter Leave ID to Reject: ");
        int leaveId = sc.nextInt();

        // Step 1: Fetch details BEFORE update
        PreparedStatement fetch =
            con.prepareStatement(
                "SELECT emp_id, leave_days FROM leave_request " +
                "WHERE leave_id=? AND status='Pending'");
        fetch.setInt(1, leaveId);
        ResultSet rs = fetch.executeQuery();

        if (!rs.next()) {
            System.out.println("Invalid Leave ID or already processed!");
            return;
        }

        int empId = rs.getInt("emp_id");
        int leaveDays = rs.getInt("leave_days");

        // Step 2: Reject leave
        PreparedStatement ps =
            con.prepareStatement(
                "UPDATE leave_request SET status='Rejected' WHERE leave_id=?");
        ps.setInt(1, leaveId);
        ps.executeUpdate();

        // Step 3: Insert into history
        PreparedStatement historyInsert =
            con.prepareStatement(
                "INSERT INTO leave_history " +
                "(emp_id, leave_id, leave_days, final_status, action_date) " +
                "VALUES (?, ?, ?, 'Rejected', CURDATE())");

        historyInsert.setInt(1, empId);
        historyInsert.setInt(2, leaveId);
        historyInsert.setInt(3, leaveDays);
        historyInsert.executeUpdate();

        System.out.println("Leave rejected successfully!");
    }
    
    static void addEmployee(Connection con) throws Exception {

        sc.nextLine();
        System.out.print("Enter Name: ");
        String name = sc.nextLine();

        System.out.print("Enter Department: ");
        String dept = sc.nextLine();

        System.out.print("Enter Total Leaves: ");
        int total = sc.nextInt();

        System.out.print("Enter Salary: ");
        double salary = sc.nextDouble();

        PreparedStatement ps =
            con.prepareStatement(
                "INSERT INTO employee (emp_name, department, total_leaves, remaining_leaves, salary) " +
                "VALUES (?, ?, ?, ?, ?)");

        ps.setString(1, name);
        ps.setString(2, dept);
        ps.setInt(3, total);
        ps.setInt(4, total);
        ps.setDouble(5, salary);

        ps.executeUpdate();
        System.out.println("Employee added successfully!");
    }

    // ✅ Delete Employee
    static void deleteEmployee(Connection con) throws Exception {

        System.out.print("Enter Employee ID to Delete: ");
        int empId = sc.nextInt();

        PreparedStatement ps =
            con.prepareStatement("DELETE FROM employee WHERE emp_id=?");
        ps.setInt(1, empId);

        int rows = ps.executeUpdate();

        if (rows > 0)
            System.out.println("Employee deleted successfully!");
        else
            System.out.println("Employee not found!");
    }

    // ✅ View Any Employee
    static void viewEmployee(Connection con) throws Exception {

        System.out.print("Enter Employee ID: ");
        int empId = sc.nextInt();

        PreparedStatement ps =
            con.prepareStatement("SELECT * FROM employee WHERE emp_id=?");
        ps.setInt(1, empId);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            System.out.println("\n===== EMPLOYEE DETAILS =====");
            System.out.println("ID           : " + rs.getInt("emp_id"));
            System.out.println("Name         : " + rs.getString("emp_name"));
            System.out.println("Department   : " + rs.getString("department"));
            System.out.println("Total Leaves : " + rs.getInt("total_leaves"));
            System.out.println("Remaining    : " + rs.getInt("remaining_leaves"));
            System.out.println("Salary       : ₹" + rs.getDouble("salary"));
        } else {
            System.out.println("Employee not found!");
        }
    }
}
