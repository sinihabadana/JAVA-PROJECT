package jdbc;

import java.sql.*;
import java.util.Scanner;

public class EmployeeModule {

    static Scanner sc = new Scanner(System.in);

    public static void employeeLogin(Connection con) throws Exception {

        System.out.print("Enter Employee ID: ");
        int empId = sc.nextInt();

        PreparedStatement ps =
            con.prepareStatement("SELECT * FROM employee WHERE emp_id=?");
        ps.setInt(1, empId);
        ResultSet rs = ps.executeQuery();

        if (!rs.next()) {
            System.out.println("Invalid Employee ID!");
            return;
        }

        System.out.println("\n===== EMPLOYEE DETAILS =====");
        System.out.println("ID           : " + rs.getInt("emp_id"));
        System.out.println("Name         : " + rs.getString("emp_name"));
        System.out.println("Department   : " + rs.getString("department"));
        System.out.println("Total Leaves : " + rs.getInt("total_leaves"));
        System.out.println("Remaining    : " + rs.getInt("remaining_leaves"));
        System.out.println("Salary       : ₹" + rs.getDouble("salary"));

        while (true) {
            System.out.println("\n--- EMPLOYEE MENU ---");
            System.out.println("1. View My Leave Records");
            System.out.println("2. Apply Leave");
            System.out.println("3. Cancel Pending Leave");
            System.out.println("4. View Salary Deduction Summary");
            System.out.println("5. Logout");
            System.out.print("Enter choice: ");

            int ch = sc.nextInt();

            switch (ch) {
                case 1:
                    LeaveModule.viewEmployeeLeaves(con, empId);
                    break;
                case 2:
                    LeaveModule.applyLeave(con, empId);
                    break;
                case 3:
                	cancelPendingLeave(con, empId);
                	break;
                case 4:
                	viewSalarySummary(con, empId);
                	break;
                case 5:
                    return;
                default:
                    System.out.println("Invalid choice!");
            }
        }
    }
    static void cancelPendingLeave(Connection con, int empId) throws Exception {

        System.out.print("Enter Leave ID to Cancel: ");
        int leaveId = sc.nextInt();

        PreparedStatement ps =
            con.prepareStatement(
                "UPDATE leave_request SET status='Cancelled' " +
                "WHERE leave_id=? AND emp_id=? AND status='Pending'");
        ps.setInt(1, leaveId);
        ps.setInt(2, empId);

        int rows = ps.executeUpdate();

        if (rows > 0) {
            System.out.println("Leave cancelled successfully!");
        } else {
            System.out.println("Cannot cancel (Invalid ID or not Pending).");
        }
    }

    // ✅ Salary Deduction Summary
    static void viewSalarySummary(Connection con, int empId) throws Exception {

        PreparedStatement ps =
            con.prepareStatement(
                "SELECT SUM(salary_deducted) AS total FROM leave_request WHERE emp_id=?");
        ps.setInt(1, empId);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            double total = rs.getDouble("total");
            System.out.println("Total Salary Deducted: ₹" + total);
        }
    }
}
