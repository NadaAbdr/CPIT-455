import api.AdminResource;
import api.HotelResource;
import model.customer.Customer;
import model.room.IRoom;
import model.room.enums.RoomType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import service.customer.CustomerService;
import service.reservation.ReservationService;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Scanner;
import java.util.Calendar;
import java.util.Collection;
import model.reservation.Reservation;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MainMenuTest {

    @Mock
    private CustomerService mockCustomerService;

    @Mock
    private ReservationService mockReservationService;

    @Mock
    private Customer mockCustomer;

    @Mock
    private IRoom mockRoom;

    @Captor
    private ArgumentCaptor<String> emailCaptor;

    private HotelResource hotelResource;
    private AdminResource adminResource;

    private static final String TEST_EMAIL = "unit.test@example.com";
    private static final String TEST_FIRST_NAME = "John";
    private static final String TEST_LAST_NAME = "Doe";
    private static final String TEST_ROOM_NUMBER = "101";
    private static final Double TEST_ROOM_PRICE = 150.0;

    @Before
    public void setUp() throws Exception {
        // Get singleton instances
        hotelResource = HotelResource.getSingleton();
        adminResource = AdminResource.getSingleton();

        // Inject mock services using reflection
        injectMockService(hotelResource, "customerService", mockCustomerService);
        injectMockService(hotelResource, "reservationService", mockReservationService);

        injectMockService(adminResource, "customerService", mockCustomerService);
        injectMockService(adminResource, "reservationService", mockReservationService);

        // Setup default mock behaviors
        setupDefaultMockBehaviors();
    }

    private void injectMockService(Object singleton, String fieldName, Object mockService) throws Exception {
        Field field = singleton.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(singleton, mockService);
    }

    private void setupDefaultMockBehaviors() {
        when(mockCustomer.getEmail()).thenReturn(TEST_EMAIL);
        when(mockRoom.getRoomNumber()).thenReturn(TEST_ROOM_NUMBER);
        when(mockRoom.getRoomPrice()).thenReturn(TEST_ROOM_PRICE);
        when(mockRoom.getRoomType()).thenReturn(RoomType.SINGLE);
    }

    private ByteArrayOutputStream setFakeInput(String input) {
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));

        return out;
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
        when(mockCustomerService.getCustomer(TEST_EMAIL)).thenReturn(mockCustomer);
        when(mockRoom.getRoomNumber()).thenReturn(TEST_ROOM_NUMBER);

        ByteArrayOutputStream output = setFakeInput("02/01/2025\n02/03/2025\nn\n");

        Scanner scanner = new Scanner(System.in);
        MainMenu.findAndReserveRoom(scanner);

        String printed = output.toString();
        assertTrue(printed.contains("Enter Check-In Date"));
    }
    @Test
    public void testFindAndReserveRoom_AlternativeRooms() {
        // Mock availableRooms empty, alternativeRooms not empty
        when(hotelResource.findARoom(any(), any())).thenReturn(Collections.emptyList());
        when(hotelResource.findAlternativeRooms(any(), any())).thenReturn(Collections.singletonList(mockRoom));
        when(mockRoom.getRoomNumber()).thenReturn(TEST_ROOM_NUMBER);

        ByteArrayOutputStream output = setFakeInput("02/01/2025\n02/03/2025\nn\n");
        Scanner scanner = new Scanner(System.in);

        MainMenu.findAndReserveRoom(scanner);

        String printed = output.toString();
        assertTrue(printed.contains("We've only found rooms on alternative dates"));
    }
    @Test
    public void testFindAndReserveRoom_WithAvailableRooms() {
        // Mock one available room
        when(mockReservationService.findRooms(any(Date.class), any(Date.class)))
                .thenReturn(Collections.singletonList(mockRoom));

        // Input: check-in + check-out + 'n' to skip booking
        String input = "02/01/2025\n02/05/2025\nn\n";
        ByteArrayOutputStream out = setFakeInput(input);
        Scanner scanner = new Scanner(System.in);

        MainMenu.findAndReserveRoom(scanner);

        // Verify findARoom was called
        verify(mockReservationService, times(1)).findRooms(any(Date.class), any(Date.class));
        String console = out.toString();
        assertTrue(console.contains("Enter Check-In Date"));
        assertTrue(console.contains("Enter Check-Out Date"));
    }
    @Test
    public void testReserveRoom_ValidInput() {
        when(mockCustomerService.getCustomer(TEST_EMAIL)).thenReturn(mockCustomer);

        ByteArrayOutputStream output = setFakeInput("y\ny\n" + TEST_EMAIL + "\n" + TEST_ROOM_NUMBER + "\n");

        Date checkIn = new GregorianCalendar(2025, Calendar.FEBRUARY, 1).getTime();
        Date checkOut = new GregorianCalendar(2025, Calendar.FEBRUARY, 3).getTime();

        Scanner scanner = new Scanner(System.in);
        MainMenu.reserveRoom(scanner, checkIn, checkOut, Collections.singletonList(mockRoom));

        String printed = output.toString();
        assertTrue(printed.contains("Reservation created successfully!"));
    }

    @Test
    public void testReserveRoom_InvalidEmail() {
        when(mockCustomerService.getCustomer("invalid@example.com")).thenReturn(null);

        ByteArrayOutputStream output = setFakeInput("y\ny\ninvalid@example.com\n");

        Date checkIn = new GregorianCalendar(2025, Calendar.FEBRUARY, 1).getTime();
        Date checkOut = new GregorianCalendar(2025, Calendar.FEBRUARY, 3).getTime();

        Scanner scanner = new Scanner(System.in);
        MainMenu.reserveRoom(scanner, checkIn, checkOut, Collections.singletonList(mockRoom));

        String printed = output.toString();
        assertTrue(printed.contains("Customer not found. You may need to create a new account."));
    }
    @Test
    public void testReserveRoom_InvalidYNInput() {
        when(mockCustomerService.getCustomer(TEST_EMAIL)).thenReturn(mockCustomer);

        Collection<IRoom> rooms = Collections.singletonList(mockRoom);
        String input = "y\nmaybe\nn\n"; // invalid input first, then cancel
        ByteArrayOutputStream out = setFakeInput(input);
        Scanner scanner = new Scanner(System.in);

        MainMenu.reserveRoom(scanner, new Date(), new Date(), rooms);
    }
    @Test
    public void testReserveRoom_HaveNoAccount() {
        // Simulate booking flow: y -> n (no account)
        ByteArrayOutputStream output = setFakeInput("y\nn\n");
        Scanner scanner = new Scanner(System.in);

        Date checkIn = new GregorianCalendar(2025, Calendar.FEBRUARY, 1).getTime();
        Date checkOut = new GregorianCalendar(2025, Calendar.FEBRUARY, 3).getTime();

        MainMenu.reserveRoom(scanner, checkIn, checkOut, Collections.singletonList(mockRoom));

        String printed = output.toString();
        assertTrue(printed.contains("Please, create an account."));
    }
    @Test
    public void testReserveRoom_InvalidRoomNumber() {
        // Simulate booking flow: y -> y -> valid account -> invalid room number
        when(mockCustomerService.getCustomer(TEST_EMAIL)).thenReturn(mockCustomer);

        ByteArrayOutputStream output = setFakeInput("y\ny\n" + TEST_EMAIL + "\n999\n");
        Scanner scanner = new Scanner(System.in);

        Date checkIn = new GregorianCalendar(2025, Calendar.FEBRUARY, 1).getTime();
        Date checkOut = new GregorianCalendar(2025, Calendar.FEBRUARY, 3).getTime();

        MainMenu.reserveRoom(scanner, checkIn, checkOut, Collections.singletonList(mockRoom));

        String printed = output.toString();
        assertTrue(printed.contains("Error: room number not available"));
    }
    @Test
    public void testReserveRoom_InvalidInputBranch() {
        // Input: invalid bookRoom input, then 'n' to exit
        String input = "x\nn\n";
        ByteArrayOutputStream out = setFakeInput(input);
        Scanner scanner = new Scanner(System.in);

        MainMenu.reserveRoom(scanner, new Date(), new Date(), Collections.singleton(mockRoom));

        String console = out.toString();
        assertTrue(console.contains("Invalid input. Please enter 'y' or 'n'."));
    }
    // ==================== SEE RESERVATIONS TESTS ====================

    @Test
    public void testSeeMyReservation_NoResult() {
        when(mockCustomerService.getCustomer("invalid@example.com")).thenReturn(null);

        ByteArrayOutputStream output = setFakeInput("invalid@example.com\n");
        Scanner scanner = new Scanner(System.in);
        MainMenu.seeMyReservation(scanner);

        String printed = output.toString();
        assertTrue(printed.contains("No reservations found."));
    }
    @Test
    public void testSeeMyReservation_UserCancels() {
        // Mock a reservation
        Reservation mockReservation = mock(Reservation.class);
        when(hotelResource.getCustomer(TEST_EMAIL)).thenReturn(mockCustomer);
        when(hotelResource.getCustomersReservations(TEST_EMAIL))
        .thenReturn(Collections.singletonList(mockReservation));


        ByteArrayOutputStream output = setFakeInput(TEST_EMAIL + "\nn\n");
        Scanner scanner = new Scanner(System.in);

        MainMenu.seeMyReservation(scanner);

        String printed = output.toString();
        assertTrue(printed.contains("No cancellation performed."));
    }

    @Test
    public void testSeeMyReservation_InvalidYNInput() {
        Reservation mockReservation = mock(Reservation.class);
        when(hotelResource.getCustomer(TEST_EMAIL)).thenReturn(mockCustomer);
        when(hotelResource.getCustomersReservations(TEST_EMAIL)).thenReturn(Collections.singletonList(mockReservation));
        ByteArrayOutputStream output = setFakeInput(TEST_EMAIL + "\nH\nn\n"); //user enters H instead of y/n
        Scanner scanner = new Scanner(System.in);

        MainMenu.seeMyReservation(scanner);

        String printed = output.toString();
        assertTrue(printed.contains("Invalid input. Please enter 'y' or 'n'."));
    }
    // ==================== CANCEL RESERVATION TESTS ====================
    @Test
    public void testCancelReservation_ValidRoom() {
        when(mockCustomerService.getCustomer(TEST_EMAIL)).thenReturn(mockCustomer);

        Reservation mockReservation = mock(Reservation.class);
        when(mockReservationService.getCustomersReservation(mockCustomer))
            .thenReturn(Collections.singletonList(mockReservation));

        // Simple stub with exact values — no matchers needed
        when(hotelResource.cancelReservation(TEST_EMAIL, TEST_ROOM_NUMBER, new GregorianCalendar(2025, Calendar.FEBRUARY, 1).getTime()))
            .thenReturn(true);

        String fakeInput = TEST_ROOM_NUMBER + "\n02/01/2025\n";
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));
        Scanner scanner = new Scanner(new ByteArrayInputStream(fakeInput.getBytes()));

        MainMenu.cancelReservation(scanner, TEST_EMAIL);

        String printed = output.toString();
        assertTrue(printed.contains("Reservation for room " + TEST_ROOM_NUMBER + " cancelled successfully."));
    }

    @Test
    public void testCancelReservation_WrongRoomNumber() {
        when(mockCustomerService.getCustomer(TEST_EMAIL)).thenReturn(mockCustomer);

        Reservation mockReservation = mock(Reservation.class);
        when(mockReservationService.getCustomersReservation(mockCustomer))
        .thenReturn(Collections.singletonList(mockReservation));

        // wrong room number
        String fakeInput = "999\n02/01/2025\n";
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));
        Scanner scanner = new Scanner(new ByteArrayInputStream(fakeInput.getBytes()));

        MainMenu.cancelReservation(scanner, TEST_EMAIL);

        String printed = output.toString();
        assertTrue(printed.contains("Error: Reservation not found or unable to cancel. Check room number and date."));
    }

    @Test
    public void testCancelReservation_InvalidDateFormat() {
        when(mockCustomerService.getCustomer(TEST_EMAIL)).thenReturn(mockCustomer);

        Reservation mockReservation = mock(Reservation.class);
        when(mockReservationService.getCustomersReservation(mockCustomer))
        .thenReturn(Collections.singletonList(mockReservation));

        // Fake input: valid room number + invalid date
        String fakeInput = TEST_ROOM_NUMBER + "\n02/01/\n";
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));
        Scanner scanner = new Scanner(new ByteArrayInputStream(fakeInput.getBytes()));

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
        ByteArrayOutputStream output = setFakeInput(TEST_EMAIL + "\n" + TEST_FIRST_NAME + "\n" + TEST_LAST_NAME + "\n");
        Scanner scanner = new Scanner(System.in);
        MainMenu.createAccount(scanner);

        String printed = output.toString();
        assertTrue(printed.contains("Account created successfully!"));
    }
    @Test
    public void testCreateAccount_ExceptionBranch() throws Exception {
        // Throw exception on first call, do nothing on second call
        doThrow(new IllegalArgumentException("Email invalid"))
            .doNothing()
            .when(mockCustomerService).addCustomer(anyString(), anyString(), anyString());
        String input = TEST_EMAIL + "\nJohn\nDoe\n"   // first attempt faild soo throw!
                     + TEST_EMAIL + "\nJohn2\nDoe2\n"; // second attempt succeeds!

        ByteArrayOutputStream out = setFakeInput(input);
        Scanner scanner = new Scanner(System.in);

        MainMenu.createAccount(scanner);

        // verify createACustomer called twice
        verify(mockCustomerService, times(1)).addCustomer(anyString(), anyString(), anyString());
    }
}
