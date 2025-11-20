/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit4TestClass.java to edit this template
 */

import api.AdminResource;
import api.HotelResource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.Scanner;
import model.customer.Customer;
import model.room.IRoom;
import model.room.Room;
import model.room.enums.RoomType;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import service.reservation.ReservationService;

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
    public void setUp() throws Exception {
        // Clear ReservationService singleton state before each test
        ReservationService service = ReservationService.getSingleton();

        Field roomsField = ReservationService.class.getDeclaredField("rooms");
        roomsField.setAccessible(true);
        ((Map<?, ?>) roomsField.get(service)).clear();

        Field reservationsField = ReservationService.class.getDeclaredField("reservations");
        reservationsField.setAccessible(true);
        ((Map<?, ?>) reservationsField.get(service)).clear();
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
            printed.contains("Unknown action") || printed.contains("Error: Invalid action")
        );    
    }
    
    /**
     * Test entering empty input (just Enter)
     */
    @Test
    public void testEmptyInput() {
        ByteArrayInputStream input = new ByteArrayInputStream("\n5\n".getBytes());
        System.setIn(input);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));

        MainMenu.mainMenu();

        String printed = output.toString();

        assertTrue(printed.contains("Empty input received. Exiting program..."));
    }

    /**
     * Test entering valid input 
     */
    @Test
    public void testOptionOne_FindRoom_NoBooking() {
    String fakeInput =
        "1\n" +                 // find room
        "02/01/2025\n" +        // check-in
        "02/03/2025\n" +        // check-out
        "n\n" +                 // do not book
        "5\n";                  // exit
    ByteArrayInputStream input = new ByteArrayInputStream(fakeInput.getBytes());
    System.setIn(input);

    ByteArrayOutputStream output = new ByteArrayOutputStream();
    System.setOut(new PrintStream(output));

    MainMenu.mainMenu();

    String printed = output.toString();
    assertTrue(printed.contains("Enter Check-In Date"));

}

    @Test
    public void testOptionTwo_SeeReservations_NoReservations() {
        String email = "test@example.com";
        HotelResource.getSingleton().createACustomer(email, "John", "Doe");
        String fakeInput =
            "2\n" +       // see my reservations
            email+"\n" +  // email
            "5\n";        // exit
        ByteArrayInputStream input = new ByteArrayInputStream(fakeInput.getBytes());
        System.setIn(input);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));

        MainMenu.mainMenu();

        String printed = output.toString();
        assertTrue(printed.contains("Enter your Email format: name@domain.com"));
    }

    @Test
    public void testOptionThree_CreateAccount() {
        String fakeInput =
            "3\n" +                     // create account
            "newuser@example.com\n" +   // email
            "Alice\n" +                 // first name
            "Smith\n" +                 // last name
            "5\n";                      // exit
        ByteArrayInputStream input = new ByteArrayInputStream(fakeInput.getBytes());
        System.setIn(input);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));

        MainMenu.mainMenu();

        String printed = output.toString();
        assertTrue(printed.contains("Account created successfully!"));
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

        Scanner scanner = new Scanner(new ByteArrayInputStream(fakeInput.getBytes()));//replace the scanner's input with this fake input

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output)); // take what is printed on the console

        MainMenu.findAndReserveRoom(scanner); // call the method

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

        Scanner scanner = new Scanner(new ByteArrayInputStream(fakeInput.getBytes())); //replace the scanner's input with this fake input

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output)); // take what is printed on the console

        MainMenu.findAndReserveRoom(scanner);  // call the method

        String printed = output.toString();
        assertTrue(printed.contains("Error: Invalid date."));
    }
    
    /**
     * Test valid date without available rooms
     */
    @Test
    public void testFindAndReserveRoom_WithoutAvailableRoom_validDate() throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        String fakeInput =
                "02/10/2025\n" +
                "02/13/2025\n"+
                "\n";

        Scanner scanner = new Scanner(new ByteArrayInputStream(fakeInput.getBytes()));
        
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));
        
        MainMenu.findAndReserveRoom(scanner);
        String printed = output.toString();
        assertTrue(printed.contains("No rooms found."));
    }

    /**
     * Test valid input 
     */
    @Test
    public void testReserveRoom_ValidInput() {
        // add a fake room & Fake Customer
        IRoom fakeRoom = new Room("101", 100.0, RoomType.SINGLE);
        String email = "test@example.com";
        AdminResource.getSingleton().addRoom(Collections.singletonList(fakeRoom));
        HotelResource.getSingleton().createACustomer(email, "John", "Doe");

        String fakeInput =
                "y\n" +           // I Would you like to book
                "y\n" +           // I have an account
                email + "\n" +    // Email
                "101\n";          // Room number

        
        ByteArrayInputStream input = new ByteArrayInputStream(fakeInput.getBytes());
        System.setIn(input);

       
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));

        // since it takes a Date we have to create a parametar of the type date
        Scanner scanner = new Scanner(System.in);
        Date checkIn = new GregorianCalendar(2025, Calendar.FEBRUARY, 1).getTime();
        Date checkOut = new GregorianCalendar(2025, Calendar.FEBRUARY, 3).getTime();

        MainMenu.reserveRoom(scanner, checkIn, checkOut, AdminResource.getSingleton().getAllRooms());

        String printed = output.toString();
        assertTrue(printed.contains("Reservation created successfully!"));
    }

    /**
     * Test valid input but wrong room Number
     */
    @Test
    public void testReserveRoom_ValidInput_InvalidRoomNumber() {
        // add a fake room & Fake Customer
        IRoom fakeRoom = new Room("101", 100.0, RoomType.SINGLE);
        String email = "test@example.com";
        AdminResource.getSingleton().addRoom(Collections.singletonList(fakeRoom));
        HotelResource.getSingleton().createACustomer(email, "John", "Doe");

        String fakeInput =
                "y\n" +           // I Would you like to book
                "y\n" +           // I have an account
                email + "\n" +    // Email
                "102\n";          // Invalid Room number

        
        ByteArrayInputStream input = new ByteArrayInputStream(fakeInput.getBytes());
        System.setIn(input);

       
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));

        // since it takes a Date we have to create a parametar of the type date
        Scanner scanner = new Scanner(System.in);
        Date checkIn = new GregorianCalendar(2025, Calendar.FEBRUARY, 1).getTime();
        Date checkOut = new GregorianCalendar(2025, Calendar.FEBRUARY, 3).getTime();

        MainMenu.reserveRoom(scanner, checkIn, checkOut, AdminResource.getSingleton().getAllRooms());

        String printed = output.toString();
        assertTrue(printed.contains("Error: room number not available.\nStart reservation again."));
    }

    /**
     * Test invalid character input other than Y/N
     */
    public void testReserveRoom_InvalidCharacterInput() {
        // add a fake room & Fake Customer
        IRoom fakeRoom = new Room("101", 100.0, RoomType.SINGLE);
        String email = "test@example.com";
        AdminResource.getSingleton().addRoom(Collections.singletonList(fakeRoom));
        HotelResource.getSingleton().createACustomer(email, "John", "Doe");

        String fakeInput =
                "A\n";          // Would you like to book (y/n) but the user enters a letter "A" instead

        ByteArrayInputStream input = new ByteArrayInputStream(fakeInput.getBytes());
        System.setIn(input);

       
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));

        // since it takes a Date we have to create a parametar of the type date
        Scanner scanner = new Scanner(System.in);
        Date checkIn = new GregorianCalendar(2025, Calendar.FEBRUARY, 1).getTime();
        Date checkOut = new GregorianCalendar(2025, Calendar.FEBRUARY, 3).getTime();

        MainMenu.reserveRoom(scanner, checkIn, checkOut, AdminResource.getSingleton().getAllRooms());

        String printed = output.toString();
        assertTrue(printed.contains("Would you like to book? y/n")); //restart
    }

    /**
     * Test valid input but the user doesn't have an account 
     */
    @Test
    public void testReserveRoom_ValidInput_WithoutAccount() {
        // add a fake room
        IRoom fakeRoom = new Room("101", 100.0, RoomType.SINGLE);
        AdminResource.getSingleton().addRoom(Collections.singletonList(fakeRoom));

        String fakeInput =
                "y\n" +           // I Would you like to book
                "n\n" ;           // I Don't have an account

        ByteArrayInputStream input = new ByteArrayInputStream(fakeInput.getBytes());
        System.setIn(input);

       
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));

        // since it takes a Date we have to create a parametar of the type date
        Scanner scanner = new Scanner(System.in);
        Date checkIn = new GregorianCalendar(2025, Calendar.FEBRUARY, 1).getTime();
        Date checkOut = new GregorianCalendar(2025, Calendar.FEBRUARY, 3).getTime();

        MainMenu.reserveRoom(scanner, checkIn, checkOut, AdminResource.getSingleton().getAllRooms());

        String printed = output.toString();
        assertTrue(printed.contains("Please, create an account."));
    }
    
    @Test
    public void testReserveRoom_InValidEmail_CustomerNotFound() {
        // add a fake room
        IRoom fakeRoom = new Room("101", 100.0, RoomType.SINGLE);
        AdminResource.getSingleton().addRoom(Collections.singletonList(fakeRoom));

        String fakeInput =
                "y\n" +           // I Would you like to book
                "y\n" +           // I have an account
                "invalid@example.com\n"; // invalid email

        Scanner scanner = new Scanner(new ByteArrayInputStream(fakeInput.getBytes()));
       
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));

        // since it takes a Date we have to create a parametar of the type date
        Date checkIn = new GregorianCalendar(2025, Calendar.FEBRUARY, 1).getTime();
        Date checkOut = new GregorianCalendar(2025, Calendar.FEBRUARY, 3).getTime();

        MainMenu.reserveRoom(scanner, checkIn, checkOut, AdminResource.getSingleton().getAllRooms());

        String printed = output.toString();
        assertTrue(printed.contains("Customer not found.\n"));
    }

    /**
     * Test with valid email but no results were found
    **/
     @Test
     public void testseeMyReservation_noResult(){
         String fakeInput = "Invalid@example.com\n";
         Scanner scanner = new Scanner(new ByteArrayInputStream(fakeInput.getBytes()));

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output)); // take what is printed on the console

        MainMenu.seeMyReservation(scanner);// call the method
        String printed = output.toString(); //convert to string
        assertTrue(printed.contains("No reservations found."));
     }
     
    /**
     * Test with valid input and cancel the reservation
    **/
     @Test
    public void testSeeMyReservation_WithReservation_AndCancel(){
        // add a fake room & Fake Customer
        String email = "test@example.com";
        HotelResource.getSingleton().createACustomer(email, "John", "Doe");
        IRoom room = new Room("101", 100.0, RoomType.SINGLE);
        AdminResource.getSingleton().addRoom(Collections.singletonList(room));

        // Book a reservation
        Date checkIn = new GregorianCalendar(2025, Calendar.FEBRUARY, 1).getTime();
        Date checkOut = new GregorianCalendar(2025, Calendar.FEBRUARY, 3).getTime();
        HotelResource.getSingleton().bookARoom(email, room, checkIn, checkOut);

        // Fake input
        String fakeInput =
                email + "\n" + // email
                "y\n" +      // yes to cancel
                "101\n" +      // room number
                "02/01/2025\n";// check-in date

        Scanner scanner = new Scanner(new ByteArrayInputStream(fakeInput.getBytes()));
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));  // take what is printed on the console

        MainMenu.seeMyReservation(scanner);

        String printed = output.toString();
        assertTrue(printed.contains("Reservation for room 101 cancelled successfully."));
    }
    
    /**
     * Test with valid Customer that have a reservation but unable to cancel due to a wrong room number
    **/
     @Test
    public void testcancelReservation_WrongRoomNumber(){
        // add a fake room & Fake Customer
        String email = "test@example.com";
        HotelResource.getSingleton().createACustomer(email, "John", "Doe");
        IRoom room = new Room("101", 100.0, RoomType.SINGLE);
        AdminResource.getSingleton().addRoom(Collections.singletonList(room));

        // Book a reservation
        Date checkIn = new GregorianCalendar(2025, Calendar.FEBRUARY, 1).getTime();
        Date checkOut = new GregorianCalendar(2025, Calendar.FEBRUARY, 3).getTime();
        HotelResource.getSingleton().bookARoom(email, room, checkIn, checkOut);

        // Fake input
        String fakeInput =
                "102\n" +      // room number
                "02/01/2025\n";// check-in date

        Scanner scanner = new Scanner(new ByteArrayInputStream(fakeInput.getBytes()));
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));  // take what is printed on the console

        MainMenu.cancelReservation(scanner, email);

        String printed = output.toString();
        assertTrue(printed.contains("Error: Reservation not found or unable to cancel. Check room number and date."));
    }

    /**
     * Test invalid email then valid user input
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