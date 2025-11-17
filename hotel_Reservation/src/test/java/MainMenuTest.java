/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit4TestClass.java to edit this template
 */

import api.AdminResource;
import api.HotelResource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Scanner;
import model.room.IRoom;
import model.room.Room;
import model.room.enums.RoomType;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/** The test methods include:
 * MainMenu() :
 * - testInvalidNumberInputMainMenu(): To test when the user enters an invalid number
 * - testCharacterInputMainMenu(): To test when the user enters a character instead of a number
 * 
 * findAndReserveRoom():
 * - testFindAndReserveRoom_WithAvailableRoom_NoBooking(): To test valid input with available rooms and the user says "no" to booking
 * - testFindAndReserveRoom_WithAvailableRoom_InvalidDate(): To test invalid date with available rooms
 * 
 * CreateAccount(scanner):
 * - testCreateAccount_InvalidThenValid(): To test invalid email then valid
 */
public class MainMenuTest {
    
    public MainMenuTest() {
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
     * Test entering an invalid number (e.g. 6)
     */
    @Test
    public void testInvalidNumberInputMainMenu() {
       // 6 -> invalid, then 5 -> exit
       // Since MainMenu doesn't accept any parameters I used the following method
        ByteArrayInputStream input = new ByteArrayInputStream("6\n5\n".getBytes());
        System.setIn(input); //replace the scanner's input with this fake input

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output)); // take what is printed on the console

        MainMenu.mainMenu(); // call the method
        String printed = output.toString(); //convert to string
        assertTrue(
            printed.contains("Unknown action") || printed.contains("Error: Invalid action")
        );
    }

    /**
     * Test entering a character (e.g. "A")
     */
    @Test
    public void testCharacterInputMainMenu() {
        // A -> invalid, then 5 -> exit
       // Since MainMenu doesn't accept any parameters I used the same method i used above
        ByteArrayInputStream input = new ByteArrayInputStream("A\n5\n".getBytes());
        System.setIn(input);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));

        MainMenu.mainMenu();

        String printed = output.toString();
        assertTrue(
            printed.contains("Unknown action") || printed.contains("Error: Invalid action") || printed.contains("Error: Invalid date.")
        );    
    }
    
    /**
     * Test valid input with available rooms and the user says "no" to booking
     */
    @Test
    public void testFindAndReserveRoom_WithAvailableRoom_NoBooking() {
        // add a fake room 
        IRoom fakeRoom = new Room("101", 100.0, RoomType.SINGLE);
        AdminResource.getSingleton().addRoom(Collections.singletonList(fakeRoom));

        // fake user input
        String fakeInput =
            "02/01/2025\n" +   //check-in
            "02/03/2025\n" +   //check-out
            "n\n" +            //user says "no" to booking
            "5\n";             //exit

        System.setIn(new ByteArrayInputStream(fakeInput.getBytes()));//replace the scanner's input with this fake input

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output)); // take what is printed on the console

        MainMenu.findAndReserveRoom(); // call the method

        String printed = output.toString();

        assertTrue(printed.contains("Enter Check-In Date"));
        assertTrue(printed.contains("Enter Check-Out Date"));
        assertTrue(printed.contains("101")); // room number
        assertTrue(printed.contains("Would you like to book?"));
    }
    /**
     * Test invalid date with available rooms
     */
    @Test
    public void testFindAndReserveRoom_WithAvailableRoom_InvalidDate() {
        // add a fake room 
        IRoom fakeRoom = new Room("101", 100.0, RoomType.SINGLE);
        AdminResource.getSingleton().addRoom(Collections.singletonList(fakeRoom));
        
        // fake user input
        String fakeInput =
                "02/01/\n" +    // invalid check-in date
                "02/01/2025\n" +    // valid check-in date retry
                "02/03/2025\n" +    // valid check-out date retry
                "n\n" +             // do not book
                "5\n";              // exit

        System.setIn(new ByteArrayInputStream(fakeInput.getBytes())); //replace the scanner's input with this fake input

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output)); // take what is printed on the console

        MainMenu.findAndReserveRoom();  // call the method

        String printed = output.toString();
        assertTrue(printed.contains("Error: Invalid date."));
    }
    /**
     * Test invalid email then valid
     */
     @Test
    public void testCreateAccount_InvalidThenValid() {
        String fakeInput =
                "johnexample.com\n" +       // invalid email
                "John\n" +               // first name (will be retried)
                "Doe\n" +                // last name
                "john@example.com\n" +   // valid email retry
                "John\n" +               // first name retry
                "Doe\n";                 // last name retry

        // move fake input to a Scanner
        Scanner scanner = new Scanner(new ByteArrayInputStream(fakeInput.getBytes()));

        // take what is printed on the console
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));

        MainMenu.createAccount(scanner); // call the method

        String printed = output.toString();

        assertTrue(printed.contains("Invalid email") || printed.contains("IllegalArgumentException"));
        assertTrue(printed.contains("Account created successfully!"));
    }
}