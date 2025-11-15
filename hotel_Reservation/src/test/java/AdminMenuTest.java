/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Scanner;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author hp
 */
public class AdminMenuTest {
    
    public AdminMenuTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of adminMenu method, of class AdminMenu.
     */
    @Test
    public void testInvaledInputAdminMenu() {
        Scanner scanner = new Scanner("11");

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        AdminMenu.adminMenu(scanner, pw);

        String output = sw.toString();

        assertTrue("Expected error for invalid length input", output.contains("Error: Invalid action"));
    }

    @Test
public void testCharacterInputAdminMenu() {
    // A (حرف) → invalid
    // 5 → خروج
    Scanner scanner = new Scanner("A\\n5\\n");

    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);

    AdminMenu.adminMenu(scanner, pw);

    String output = sw.toString();

    assertTrue("Expected invalid action message when entering a character",
               output.contains("Error: Invalid action"));
}

@Test
public void testEnterRoomPriceValidInput() {
    Scanner scanner = new Scanner("150.5\n");

    double price = AdminMenu.enterRoomPrice(scanner);

    assertEquals(150.5, price, 0.0001);
}

@Test
public void testEnterRoomPriceInvalidThenValid() {
    Scanner scanner = new Scanner("abc\n200.75\n");

    double price = AdminMenu.enterRoomPrice(scanner);

    assertEquals(200.75, price, 0.0001);
}



@Test
public void testAddAnotherRoom_InvalidInputsThenNo() {
    Scanner scanner = new Scanner("123");

    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);

    AdminMenu.addAnotherRoom(scanner, pw);

    pw.flush();
    String output = sw.toString();

    assertTrue("Expected prompt to enter Y or N after invalid input",
               output.contains("Please enter Y (Yes) or N (No)"));

    assertTrue("Menu should be printed after user enters N",
               output.contains("Admin Menu"));
}

@Test
public void testAddAnotherRoom_NoInput() {
    // المستخدم يكتب N يعني لا يضيف غرفة أخرى → يرجع للقائمة
    Scanner scanner = new Scanner("123");

    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    AdminMenu.addAnotherRoom(scanner, pw);

    pw.flush();
    String output = sw.toString();

  
    assertTrue("Menu should be printed when user enters N",
               output.contains("Admin Menu"));

    assertFalse("Should not show invalid input message for correct input",
                output.contains("Please enter Y (Yes) or N (No)"));
}

}
