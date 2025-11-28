import api.AdminResource;
import api.HotelResource;
import model.room.IRoom;
import model.room.Room;
import model.room.enums.RoomType;
import org.junit.Before;
import org.junit.Test;

import service.customer.CustomerService;
import service.reservation.ReservationService;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;

import java.util.*;
import model.customer.Customer;
import org.junit.After;

import static org.junit.Assert.*;

public class MainMenuComponentTest {

    private CustomerService customerService;
    private ReservationService reservationService;

    private HotelResource hotelResource;
    private AdminResource adminResource;

    private static final String TEST_EMAIL = "unit.test@example.com";
    private static final String TEST_FIRST_NAME = "John";
    private static final String TEST_LAST_NAME = "Doe";
    private static final String TEST_ROOM_NUMBER = "101";

    private IRoom testRoom;
    private final InputStream originalIn = System.in;
    private final PrintStream originalOut = System.out;
    private ByteArrayInputStream outputStreamInput;
    private ByteArrayOutputStream output;

    @Before
    public void setUp() {
        CustomerService.getSingleton().clearAllCustomers();
        ReservationService.getSingleton().clearAllReservations();

        // REAL services
        customerService = CustomerService.getSingleton();
        reservationService = ReservationService.getSingleton();

        // REAL resources
        hotelResource = HotelResource.getSingleton();
        adminResource = AdminResource.getSingleton();

        // Clear data before each test
        customerService.clearAllCustomers();
        reservationService.clearAllReservations();

        // Create test customer + room for all tests
        customerService.addCustomer(TEST_EMAIL,TEST_FIRST_NAME, TEST_LAST_NAME);

        testRoom = new Room(TEST_ROOM_NUMBER, 150.0, RoomType.SINGLE);
        reservationService.addRoom(testRoom);
    }
    @After
    public void tearDown() {
        System.setIn(originalIn);
        System.setOut(originalOut);
    }

    // Utility: fake input + capture output

    private ByteArrayOutputStream setFakeInput(String data) {
        output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));

        outputStreamInput = new ByteArrayInputStream(data.getBytes());
        System.setIn(outputStreamInput);

        return output;
    }

    
    
    // ==================== MAIN MENU INPUT TESTS ====================

    @Test
    public void testInvalidNumberInputMainMenu() {
        ByteArrayOutputStream output = setFakeInput("6\n5\n");
        MainMenu.mainMenu();
        String printed = output.toString();
        assertTrue(printed.contains("Unknown action") || printed.contains("Error: Invalid action"));
    }

    @Test
    public void testCharacterInputMainMenu() {
        ByteArrayOutputStream output = setFakeInput("A\n5\n");
        MainMenu.mainMenu();
        String printed = output.toString();
        assertTrue(printed.contains("Unknown action") || printed.contains("Error: Invalid action"));
    }

    @Test
    public void testEmptyInput() {
        ByteArrayOutputStream output = setFakeInput("\n5\n");
        MainMenu.mainMenu();
        String printed = output.toString();
        assertTrue(printed.contains("Empty input received. Exiting program..."));
    }
    // ================== VALID OPTIONS =======================
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
            "john@example.com\n" +   // valid email retry
            "John\n" +               // first name retry
            "Doe\n"+                 // last name
            "5\n";                      // exit
        ByteArrayInputStream input = new ByteArrayInputStream(fakeInput.getBytes());
        System.setIn(input);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));

        MainMenu.mainMenu();

        String printed = output.toString();
        assertTrue(printed.contains("Account created successfully!"));
    }
    @Test
    public void testMainMenu_AdminOption() {
        // Simulate choosing Admin menu (option 4) and then exit
        ByteArrayOutputStream output = setFakeInput("4\n5\n");
        MainMenu.mainMenu();
        String printed = output.toString();
        assertTrue(printed.contains("Admin Menu"));
    }
    // ==================== FIND ROOM / RESERVE ROOM TESTS ====================

     @Test
    public void testFindAndReserveRoom_NoBooking() {

        ByteArrayOutputStream output =
                setFakeInput("02/01/2025\n02/03/2025\nn\n");

        MainMenu.findAndReserveRoom(new Scanner(System.in));

        assertTrue(output.toString().contains("Enter Check-In Date"));
    }

    @Test
    public void testFindAndReserveRoom_AlternativeRooms() {
        // no available rooms for requested date
        reservationService.reserveARoom(
            customerService.getCustomer(TEST_EMAIL),
            testRoom,
            new GregorianCalendar(2025, Calendar.JANUARY, 1).getTime(),
            new GregorianCalendar(2025, Calendar.JANUARY, 10).getTime()
        );

        output = setFakeInput("01/02/2025\n"
                    + "01/05/2025\n"
                    + "n\n");


        MainMenu.findAndReserveRoom(new Scanner(System.in));
        String printed = output.toString();
        assertTrue(printed.contains("No rooms found."));
    }

    @Test
    public void testFindAndReserveRoom_WithAvailableRooms() {
        output = setFakeInput("02/01/2025\n02/05/2025\nn\n");

        MainMenu.findAndReserveRoom(new Scanner(System.in));

        String console = output.toString();
        assertTrue(console.contains("Enter Check-In Date"));
        assertTrue(console.contains("Enter Check-Out Date"));
    }


    @Test
    public void testReserveRoom_ValidInput() {
        output = setFakeInput(
                "y\n" +
                "y\n" +
                TEST_EMAIL + "\n" +
                TEST_ROOM_NUMBER + "\n"
        );

        Date in = new GregorianCalendar(2025, 1, 1).getTime();
        Date outDate = new GregorianCalendar(2025, 1, 3).getTime();

        MainMenu.reserveRoom(new Scanner(System.in), in, outDate, Collections.singleton(testRoom));

        assertTrue(output.toString().contains("Reservation created successfully!"));
    }

    @Test
    public void testReserveRoom_InvalidEmail() {

        output = setFakeInput("y\ny\ninvalid@example.com\n");

        Date in = new GregorianCalendar(2025, 1, 1).getTime();
        Date outDate = new GregorianCalendar(2025, 1, 3).getTime();

        MainMenu.reserveRoom(new Scanner(System.in), in, outDate, Collections.singleton(testRoom));

        assertTrue(output.toString().contains("Customer not found. You may need to create a new account."));
    }

    @Test
    public void testReserveRoom_InvalidYNInput() {

        String input = "y\n" +
                        "maybe\n" +
                        "n\n";
        ByteArrayOutputStream out = setFakeInput(input);
        Collection<IRoom> rooms = Collections.singletonList(testRoom);
        MainMenu.reserveRoom(new Scanner(System.in), new Date(), new Date(), rooms);
        assertTrue(out.toString().contains("Invalid input"));
    }

    @Test
    public void testReserveRoom_HaveNoAccount() {
        output = setFakeInput("y\nn\n");
        Collection<IRoom> rooms = Collections.singletonList(testRoom);
        MainMenu.reserveRoom(new Scanner(System.in), new Date(), new Date(), rooms);

        assertTrue(output.toString().contains("Please, create an account."));
    }

    @Test
    public void testReserveRoom_InvalidRoomNumber() {

        output = setFakeInput(
                "y\ny\n" +
                TEST_EMAIL + "\n999\n"
        );
        Collection<IRoom> rooms = Collections.singletonList(testRoom);
        MainMenu.reserveRoom(new Scanner(System.in), new Date(), new Date(), rooms);

        assertTrue(output.toString().contains("Error: room number not available"));
    }

    @Test
    public void testReserveRoom_InvalidInputBranch() {

        ByteArrayOutputStream out = setFakeInput("x\nn\n");
        Collection<IRoom> rooms = Collections.singletonList(testRoom);
        MainMenu.reserveRoom(new Scanner(System.in), new Date(), new Date(), rooms);

        assertTrue(out.toString().contains("Invalid input"));
    }


    // ==================== SEE RESERVATIONS TESTS ====================

    @Test
    public void testSeeMyReservation_NoResult() {
        ByteArrayOutputStream output = setFakeInput("invalid@example.com\n");
        Scanner scanner = new Scanner(System.in);

        MainMenu.seeMyReservation(scanner);

        String printed = output.toString();
        assertTrue(printed.contains("No reservations found."));
    }
/*
    @Test
   public void testSeeMyReservation_UserCancels() {
       Customer customer = new Customer("Unit", "Test", TEST_EMAIL);
       customerService.addCustomer(TEST_EMAIL,"Unit", "Test");

       Date checkIn = new GregorianCalendar(2024, Calendar.JUNE, 10).getTime();
       Date checkOut = new GregorianCalendar(2024, Calendar.JUNE, 12).getTime();
       reservationService.reserveARoom(customer, testRoom, checkIn, checkOut);

       output = setFakeInput(TEST_EMAIL + "\nn\n");
       Scanner scanner = new Scanner(outputStreamInput);

       MainMenu.seeMyReservation(scanner);

       String printed = output.toString();
       assertTrue(printed.contains("No cancellation performed."));
   }

    @Test
    public void testSeeMyReservation_InvalidYNInput() {
        Customer customer = new Customer("Unit", "Test", TEST_EMAIL);
       customerService.addCustomer(TEST_EMAIL,"Unit", "Test");

        Date checkIn = new GregorianCalendar(2024, Calendar.JULY, 5).getTime();
        Date checkOut = new GregorianCalendar(2024, Calendar.JULY, 7).getTime();
        reservationService.reserveARoom(customer, testRoom, checkIn, checkOut);

        output = setFakeInput(TEST_EMAIL + "\nH\nn\n");
        Scanner scanner = new Scanner(outputStreamInput);
        MainMenu.seeMyReservation(scanner);

        String printed = output.toString();
        assertTrue(printed.contains("Invalid input. Please enter 'y' or 'n'."));
    }
*/
    // ==================== CANCEL RESERVATION TESTS ====================
/*
    @Test
    public void testCancelReservation_ValidRoom() {
        Customer customer = new Customer("Unit", "Test", TEST_EMAIL);
        customerService.addCustomer(TEST_EMAIL,"Unit", "Test");

        Date checkIn = new GregorianCalendar(2024, Calendar.AUGUST, 1).getTime();
        Date checkOut = new GregorianCalendar(2024, Calendar.AUGUST, 3).getTime();
        reservationService.reserveARoom(customer, testRoom, checkIn, checkOut);

        // Must use scanner from fake input
        output =
            setFakeInput("101\n08/01/2024\n");
        Scanner scanner = new Scanner(outputStreamInput);

        MainMenu.cancelReservation(scanner, TEST_EMAIL);

        String printed = output.toString();
        assertTrue(printed.contains("Reservation for room " + testRoom.getRoomNumber() + " cancelled successfully."));
    }
*/
    @Test
    public void testCancelReservation_WrongRoomNumber() {
        // Create real reservation
        Date date = new GregorianCalendar(2025, Calendar.FEBRUARY, 1).getTime();
        reservationService.reserveARoom(
                customerService.getCustomer(TEST_EMAIL),
                testRoom,
                date,
                new GregorianCalendar(2025, Calendar.FEBRUARY, 3).getTime()
        );

        // User enters wrong room number
        String fakeInput = "999\n02/01/2025\n";
        output = setFakeInput(fakeInput);
        Scanner scanner = new Scanner(System.in);

        MainMenu.cancelReservation(scanner, TEST_EMAIL);

        String printed = output.toString();
        assertTrue(printed.contains("Error: Reservation not found or unable to cancel."));
    }

    @Test
    public void testCancelReservation_InvalidDateFormat() {
        // Create real reservation
        Date date = new GregorianCalendar(2025, Calendar.FEBRUARY, 1).getTime();
        reservationService.reserveARoom(
                customerService.getCustomer(TEST_EMAIL),
                testRoom,
                date,
                new GregorianCalendar(2025, Calendar.FEBRUARY, 3).getTime()
        );

        // Invalid date format
        String fakeInput = TEST_ROOM_NUMBER + "\n02/01/\n";
        ByteArrayOutputStream output = setFakeInput(fakeInput);
        Scanner scanner = new Scanner(System.in);

        MainMenu.cancelReservation(scanner, TEST_EMAIL);

        String printed = output.toString();
        assertTrue(printed.contains("Error: Invalid date."));
    }

    
    // ===================PRINTING METHODS TESTS ============================
    @Test
    public void testPrintReservations_Empty() {
        MainMenu.printReservations(Collections.emptyList());
        MainMenu.printReservations(null); // cover null branch
    }
    @Test
    public void testPrintRooms_EmptyCollection() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));

        MainMenu.printRooms(Collections.emptyList());

        String console = out.toString();
        assertTrue(console.contains("No rooms found."));
    }
    // ===================CREATE ACCOUNTS TESTS ============================
    @Test
    public void testCreateAccount_Valid() {
        output = setFakeInput(TEST_EMAIL + "\n" + TEST_FIRST_NAME + "\n" + TEST_LAST_NAME + "\n");
        Scanner scanner = new Scanner(System.in);
        MainMenu.createAccount(scanner);

        String printed = output.toString();
        assertTrue(printed.contains("Account created successfully!"));
    }
/*
    @Test
    public void testCreateAccount_ExceptionBranch() {
        String fakeInput = "invalidEmail\nJohn\nDoe\n";
        output = setFakeInput(fakeInput);

        Scanner scanner = new Scanner(outputStreamInput);
        MainMenu.createAccount(scanner);

        String printed = output.toString();
        assertTrue(printed.contains("Invalid email"));
    }
*/
}

