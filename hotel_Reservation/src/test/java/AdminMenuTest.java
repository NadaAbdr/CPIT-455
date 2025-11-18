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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.io.PrintWriter;


/**
 *
 * @author hp
 */
public class AdminMenuTest {

    private final InputStream originalSystemIn = System.in;
    private final PrintStream originalSystemOut = System.out;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        // Redirect System.out to capture the output
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @After
    public void tearDown() {
        // Restore the original input and output streams
        System.setIn(originalSystemIn);
        System.setOut(originalSystemOut);
    }

    /**
     * Helper method to simulate user input using \r\n to ensure nextLine() works.
     */
    private void setInput(String data) {
        // We use \r\n to ensure line break recognition in different testing environments
        ByteArrayInputStream inputStream = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));
        System.setIn(inputStream);
    }
    
    // 1. Coverage for all valid options (1, 2, 3, 4, 5) and the exit option (6)
    @Test
    public void test1_ValidOptionsAndExit() {
        // Inputs: 1, 2, 3, 4, 5, then 6 to exit
        String fullInput = 
            "1\r\n" + 
            "2\r\n" + 
            "3\r\n" + 
            "4\r\n" + 
            "101\r\n" + "100.0\r\n" + "1\r\n" + "N\r\n" + // Specific inputs for addRoom
            "5\r\n" + 
            "6\r\n";

        setInput(fullInput);

        // Scanner and PrintWriter must be created in the test
        Scanner scanner = new Scanner(System.in);
        PrintWriter out = new PrintWriter(System.out, true);
        
        // Calling the new method to enable coverage
        AdminMenu.adminMenu(scanner, out);

        // Stream must be flushed to ensure all output is captured
        out.flush();

        String output = outputStreamCaptor.toString();

        // Verify the options execution (now depends on the actual method outputs)
        assertTrue("The menu should start by printing.", output.contains("Admin Menu")); 
        assertTrue("Option 1 should be executed", output.contains("No customers found.")); // Assumption: No customers found
        assertTrue("Option 4 should be executed", output.contains("Room added successfully!"));
        assertTrue("Option 5 should be executed", output.contains("No reservations found to determine the most popular room.")); // Assumption: No reservations
        
        // Verify the exit (case '6')
        // *Note*: Since we used PrintWriter in the method, the output no longer shows ACTION:
        assertTrue("Exit should be successful", output.contains("Back to main menu"));
        assertFalse("There should be no unknown errors.", output.contains("Unknown action"));
    }
    
    // 2. Coverage for incorrect length input and Unknown action
    @Test
    public void test2_InvalidLengthAndUnknownAction() {
        setInput("12\r\n0\r\n6\r\n");
        Scanner scanner = new Scanner(System.in);
        PrintWriter out = new PrintWriter(System.out, true);

        AdminMenu.adminMenu(scanner, out);
        out.flush();

        String output = outputStreamCaptor.toString();

        assertTrue("Should display the 'Error: Invalid action' message", output.contains("Error: Invalid action\n"));
        assertTrue("Should display the 'Unknown action' message", output.contains("Unknown action\n"));
        assertTrue("Exit should be successful", output.contains("Back to main menu"));
    }

    // 3. Coverage for StringIndexOutOfBoundsException (empty line input)
    @Test
    public void test3_EmptyInputTriggersCatch() {
        setInput("\r\n");
        Scanner scanner = new Scanner(System.in);
        PrintWriter out = new PrintWriter(System.out, true);

        AdminMenu.adminMenu(scanner, out);
        out.flush();

        String output = outputStreamCaptor.toString();

        assertTrue("Should display the exception handling message for empty line", output.contains("Empty input received. Exiting program..."));
    }

    // 4. Coverage for break condition: (!scanner.hasNextLine())
    @Test
    public void test4_NoInputReceivedBreak() {
        setInput("");
        Scanner scanner = new Scanner(System.in);
        PrintWriter out = new PrintWriter(System.out, true);

        AdminMenu.adminMenu(scanner, out);
        out.flush();

        String output = outputStreamCaptor.toString();

        assertTrue("Should display the 'No input received' message", output.contains("No input received. Exiting admin menu..."));
    }

    // Adding a test to cover other cases in adminMenu()
//    @Test
//    public void test5_CheckDisplayMethods() {
//        // Inputs: 1, 2, 3, 5, 6
//        String input = "1\r\n2\r\n3\r\n5\r\n6\r\n";
//        setInput(input);
//        
//        Scanner scanner = new Scanner(System.in);
//        PrintWriter out = new PrintWriter(System.out, true);
//
//        AdminMenu.adminMenu(scanner, out);
//        out.flush();
//
//        String output = outputStreamCaptor.toString();
//
//        // Assertions based on actual outputs
//        assertTrue(output.contains("No customers found."));
//        assertTrue(output.contains("No rooms found"));
//        // displayAllReservations expected to be called
//        assertTrue(output.contains("No reservations found to determine the most popular room."));
//        assertTrue(output.contains("Back to main menu"));
//    }


@Test
public void testEnterRoomPrice_ValidInput() {
    setInput("150.5\r\n"); // Use setInput instead of a direct Scanner
    
    // A new PrintWriter must be created here only to represent the output
    // (Although outputStreamCaptor is used for capturing, the argument must be passed)
    PrintWriter dummyOut = new PrintWriter(System.out); 
    
    double price = AdminMenu.enterRoomPrice(new Scanner(System.in), dummyOut);

    // Use a small delta for comparing floating point numbers
    assertEquals(150.5, price, 0.0001); 
}

// Test invalid input followed by valid input
@Test
public void testEnterRoomPrice_InvalidThenValid() {
    // abc\n200.75\n -> Should display one error message and then return the correct value
    setInput("abc\r\n200.75\r\n");

    PrintWriter out = new PrintWriter(System.out, true); 
    
    double price = AdminMenu.enterRoomPrice(new Scanner(System.in), out);
    
    // Flush the Stream to ensure all output is captured
    out.flush();

    String output = outputStreamCaptor.toString();

    // Verify that the output contains the expected error message
    assertTrue("Should display an error message for 'abc'",
               output.contains("Invalid room price! Please, enter a valid double number."));

    assertEquals(200.75, price, 0.0001);
}

// New test: Coverage for the first condition (!scanner.hasNextLine()) at the beginning of the method
@Test
public void testAddAnotherRoom_NoInitialInput() {
    // Simulate completely empty input
    setInput(""); 

    Scanner scanner = new Scanner(System.in);
    PrintWriter out = new PrintWriter(System.out, true);

    AdminMenu.addAnotherRoom(scanner, out);

    out.flush();
    String output = outputStreamCaptor.toString();

    // Verify that the output contains the early exit message
    assertTrue("Should exit immediately when no input is provided", 
               output.contains("No input received. Returning to menu."));
    assertTrue("The menu should be printed after the early exit", 
               output.contains("Admin Menu"));
}

@Test
public void testAddAnotherRoom_InvalidInput() {
    // Inputs: "123" (invalid) then "N" (to exit)
    setInput("123\r\nN\r\n"); 
    
    Scanner scanner = new Scanner(System.in);
    PrintWriter out = new PrintWriter(System.out, true); 

    AdminMenu.addAnotherRoom(scanner, out);

    out.flush();
    String output = outputStreamCaptor.toString(); // Use outputStreamCaptor to standardize the output capturing method

    assertTrue("Expected prompt to enter Y or N after invalid input",
               output.contains("Please enter Y (Yes) or N (No)"));

    // Expect to exit after entering 'N' and print the menu (printMenu)
    assertTrue("Menu should be printed after user enters N",
               output.contains("Admin Menu"));
}

// Test 'N' input to exit immediately
@Test
public void testAddAnotherRoom_NInput() {
    setInput("N\r\n"); 

    Scanner scanner = new Scanner(System.in);
    PrintWriter out = new PrintWriter(System.out, true);
    
    AdminMenu.addAnotherRoom(scanner, out);

    out.flush();
    String output = outputStreamCaptor.toString();
    
    assertTrue("Menu should be printed when user enters N",
               output.contains("Admin Menu"));

    assertFalse("Should not show invalid input message for correct input",
                 output.contains("Please enter Y (Yes) or N (No)"));
}

//  New test: Coverage for 'Y' (Yes) to continue adding rooms
@Test
public void testAddAnotherRoom_YInput() {
    // Input sequence:
    // 1. First input for addAnotherRoom: "Y"
    // 2. Inputs for the subsequent addRoom() call:
    //    - Room Number: "202"
    //    - Price: "120"
    //    - Type: "2" (Double)
    // 3. Last input for addAnotherRoom (second time): "N" (to exit)

    // Use setInput to standardize the testing approach
    setInput("Y\r\n" + "202\r\n" + "120\r\n" + "2\r\n" + "N\r\n");

    Scanner scanner = new Scanner(System.in);
    PrintWriter out = new PrintWriter(System.out, true);

    // Note: addRoom() is called naturally inside addAnotherRoom()
    AdminMenu.addAnotherRoom(scanner, out);

    out.flush();
    String output = outputStreamCaptor.toString();

    // Verify that the second room addition occurred (confirming the 'Y' path was executed)
    assertTrue("The 'Y' path should be executed and a new room added", 
               output.contains("Enter room number:"));
    assertTrue("The successful room addition should be confirmed", 
               output.contains("Room added successfully!"));
    // Verify that the exit operation occurred after entering 'N'
    assertTrue("Should return to the menu after entering 'N'", 
               output.contains("Admin Menu"));
}

// New test: Coverage for the no input case inside the while loop (to cover the second if (!scanner.hasNextLine()) path)
@Test
public void testAddAnotherRoom_InvalidThenNoInput() {
    // Input sequence:
    // 1. First input: "X" (invalid, enters while loop)
    // 2. Second input: Stream is closed (No Input)

    // This input simulates one line "X" followed by end-of-stream
    setInput("X\r\n"); 

    Scanner scanner = new Scanner(System.in);
    PrintWriter out = new PrintWriter(System.out, true);

    AdminMenu.addAnotherRoom(scanner, out);

    out.flush();
    String output = outputStreamCaptor.toString();

    // Verify the error message appears and the while loop is entered
    assertTrue("Should prompt for Y or N after invalid input 'X'", 
               output.contains("Please enter Y (Yes) or N (No)"));
    
    // Verify exiting by breaking the loop
    assertTrue("Should display the 'no input received' message and return to the menu", 
               output.contains("No input received. Returning to menu."));
    assertTrue("The menu should be printed after the forced exit", 
               output.contains("Admin Menu"));
}
}