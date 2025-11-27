package api;

import model.customer.Customer;
import model.reservation.Reservation;
import model.room.IRoom;
import model.room.Room;
import model.room.FreeRoom;
import model.room.enums.RoomType;
import service.customer.CustomerService;
import service.reservation.ReservationService;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import static org.junit.Assert.*;

/**
 * @author Nada Alsulami
 */
public class HotelResourceComponentTest {

    private HotelResource hotelResource;
    private CustomerService customerService;
    private ReservationService reservationService;

    // Test data constants
    private static final String TEST_EMAIL = "component.test@example.com";
    private static final String TEST_EMAIL_2 = "component.test2@example.com";
    private static final String TEST_EMAIL_3 = "component.test3@example.com";
    private static final String TEST_FIRST_NAME = "Nada";
    private static final String TEST_LAST_NAME = "Alsulami";
    private static final String TEST_ROOM_NUMBER = "101";
    private static final String TEST_ROOM_NUMBER_2 = "102";
    private static final String TEST_ROOM_NUMBER_3 = "103";
    private static final String TEST_ROOM_NUMBER_4 = "104";
    private static final Double TEST_ROOM_PRICE = 150.0;
    private static final Double TEST_ROOM_PRICE_2 = 250.0;

    @Before
    public void setUp() {
        // Get the singleton instances
        hotelResource = HotelResource.getSingleton();
        customerService = CustomerService.getSingleton();
        reservationService = ReservationService.getSingleton();

        // Clear any existing data to ensure test isolation
        clearAllData();
    }

    @After
    public void tearDown() {
        // Clean up after each test
        clearAllData();
    }

    /**
     * Helper method to clear all data from singleton services. This ensures
     * test isolation and prevents state pollution between tests.
     */
    private void clearAllData() {
        try {
            customerService.clearAllCustomers();
            reservationService.clearAllRooms();
            reservationService.clearAllReservations();
        } catch (Exception e) {
            System.err.println("Error during cleanup: " + e.getMessage());
        }
    }

    // ==================== TEST: getSingleton() ====================
    @Test
    public void testGetSingleton_ShouldReturnSameInstance() {
        // Given: We call getSingleton twice
        HotelResource instance1 = HotelResource.getSingleton();
        HotelResource instance2 = HotelResource.getSingleton();

        // Then: Both should be the exact same object
        assertNotNull("Singleton should not be null", instance1);
        assertSame("Should return same singleton instance", instance1, instance2);
    }

    @Test
    public void testGetSingleton_ShouldNeverReturnNull() {
        // When: Get singleton
        HotelResource instance = HotelResource.getSingleton();

        // Then: Should never be null
        assertNotNull("Singleton should never be null", instance);
    }

    @Test
    public void testGetSingleton_MultipleCallsShouldReturnIdenticalInstance() {
        // When: Call getSingleton multiple times
        HotelResource instance1 = HotelResource.getSingleton();
        HotelResource instance2 = HotelResource.getSingleton();
        HotelResource instance3 = HotelResource.getSingleton();

        // Then: All should be identical
        assertSame("All instances should be identical", instance1, instance2);
        assertSame("All instances should be identical", instance2, instance3);
    }

    // ==================== TEST: createACustomer() ====================
    @Test
    public void testCreateACustomer_ShouldCreateCustomerSuccessfully() {
        // When: Create a customer
        hotelResource.createACustomer(TEST_EMAIL, TEST_FIRST_NAME, TEST_LAST_NAME);

        // Then: Customer should exist and be retrievable
        Customer customer = hotelResource.getCustomer(TEST_EMAIL);
        assertNotNull("Customer should be created", customer);
        assertEquals("Email should match", TEST_EMAIL, customer.getEmail());
    }

    @Test
    public void testCreateACustomer_ShouldStoreCorrectFirstName() {
        // When: Create a customer
        hotelResource.createACustomer(TEST_EMAIL, TEST_FIRST_NAME, TEST_LAST_NAME);

        // Then: First name should be stored correctly
        Customer customer = hotelResource.getCustomer(TEST_EMAIL);
        assertEquals("First name should match", TEST_FIRST_NAME, customer.getFirstName());
    }

    @Test
    public void testCreateACustomer_ShouldStoreCorrectLastName() {
        // When: Create a customer
        hotelResource.createACustomer(TEST_EMAIL, TEST_FIRST_NAME, TEST_LAST_NAME);

        // Then: Last name should be stored correctly
        Customer customer = hotelResource.getCustomer(TEST_EMAIL);
        assertEquals("Last name should match", TEST_LAST_NAME, customer.getLastName());
    }

    @Test
    public void testCreateACustomer_MultipleCustomers_ShouldCreateAll() {
        // When: Create multiple customers
        hotelResource.createACustomer(TEST_EMAIL, TEST_FIRST_NAME, TEST_LAST_NAME);
        hotelResource.createACustomer(TEST_EMAIL_2, "Ahmed", "Ali");
        hotelResource.createACustomer(TEST_EMAIL_3, "Sara", "Hassan");

        // Then: All customers should exist
        assertNotNull("First customer should exist", hotelResource.getCustomer(TEST_EMAIL));
        assertNotNull("Second customer should exist", hotelResource.getCustomer(TEST_EMAIL_2));
        assertNotNull("Third customer should exist", hotelResource.getCustomer(TEST_EMAIL_3));
    }

    @Test
    public void testCreateACustomer_WithDifferentData_ShouldStoreCorrectly() {
        // Given: Different customer data
        String email = "unique@test.com";
        String firstName = "Mohammed";
        String lastName = "Abdullah";

        // When: Create customer
        hotelResource.createACustomer(email, firstName, lastName);

        // Then: All data should be stored correctly
        Customer customer = hotelResource.getCustomer(email);
        assertNotNull("Customer should exist", customer);
        assertEquals("Email should match", email, customer.getEmail());
        assertEquals("First name should match", firstName, customer.getFirstName());
        assertEquals("Last name should match", lastName, customer.getLastName());
    }

    // ==================== TEST: getCustomer() ====================
    @Test
    public void testGetCustomer_WhenExists_ShouldReturnCustomer() {
        // Given: A customer is created
        hotelResource.createACustomer(TEST_EMAIL, TEST_FIRST_NAME, TEST_LAST_NAME);

        // When: We get the customer
        Customer result = hotelResource.getCustomer(TEST_EMAIL);

        // Then: Should return the customer
        assertNotNull("Customer should not be null", result);
        assertEquals("Email should match", TEST_EMAIL, result.getEmail());
    }

    @Test
    public void testGetCustomer_WhenNotExists_ShouldReturnNull() {
        // When: We get a non-existent customer
        Customer result = hotelResource.getCustomer("nonexistent@example.com");

        // Then: Should return null
        assertNull("Customer should be null for non-existent email", result);
    }

    @Test
    public void testGetCustomer_WithDifferentEmails_ShouldReturnCorrectCustomers() {
        // Given: Multiple customers exist
        hotelResource.createACustomer(TEST_EMAIL, "Ahmed", "Ali");
        hotelResource.createACustomer(TEST_EMAIL_2, "Sara", "Hassan");

        // When: We get each customer
        Customer customer1 = hotelResource.getCustomer(TEST_EMAIL);
        Customer customer2 = hotelResource.getCustomer(TEST_EMAIL_2);

        // Then: Should return correct customers
        assertNotNull("First customer should exist", customer1);
        assertNotNull("Second customer should exist", customer2);
        assertEquals("First email should match", TEST_EMAIL, customer1.getEmail());
        assertEquals("Second email should match", TEST_EMAIL_2, customer2.getEmail());
    }

    @Test
    public void testGetCustomer_MultipleCalls_ShouldReturnSameCustomer() {
        // Given: A customer exists
        hotelResource.createACustomer(TEST_EMAIL, TEST_FIRST_NAME, TEST_LAST_NAME);

        // When: Get customer multiple times
        Customer customer1 = hotelResource.getCustomer(TEST_EMAIL);
        Customer customer2 = hotelResource.getCustomer(TEST_EMAIL);

        // Then: Should return same customer object
        assertSame("Should return same customer instance", customer1, customer2);
    }

    // ==================== TEST: getRoom() ====================
    @Test
    public void testGetRoom_WhenExists_ShouldReturnRoom() {
        // Given: A room is added
        IRoom room = new Room(TEST_ROOM_NUMBER, TEST_ROOM_PRICE, RoomType.SINGLE);
        reservationService.addRoom(room);

        // When: We get the room
        IRoom result = hotelResource.getRoom(TEST_ROOM_NUMBER);

        // Then: Should return the room
        assertNotNull("Room should not be null", result);
        assertEquals("Room number should match", TEST_ROOM_NUMBER, result.getRoomNumber());
    }

    @Test
    public void testGetRoom_WhenNotExists_ShouldReturnNull() {
        // When: We get a non-existent room
        IRoom result = hotelResource.getRoom("999");

        // Then: Should return null
        assertNull("Room should be null for non-existent room number", result);
    }

    @Test
    public void testGetRoom_ShouldReturnCorrectRoomType() {
        // Given: A single room is added
        IRoom room = new Room(TEST_ROOM_NUMBER, TEST_ROOM_PRICE, RoomType.SINGLE);
        reservationService.addRoom(room);

        // When: Get the room
        IRoom result = hotelResource.getRoom(TEST_ROOM_NUMBER);

        // Then: Room type should match
        assertEquals("Room type should be SINGLE", RoomType.SINGLE, result.getRoomType());
    }

    @Test
    public void testGetRoom_ShouldReturnCorrectRoomPrice() {
        // Given: A room with specific price is added
        IRoom room = new Room(TEST_ROOM_NUMBER, TEST_ROOM_PRICE, RoomType.SINGLE);
        reservationService.addRoom(room);

        // When: Get the room
        IRoom result = hotelResource.getRoom(TEST_ROOM_NUMBER);

        // Then: Room price should match
        assertEquals("Room price should match", TEST_ROOM_PRICE, result.getRoomPrice(), 0.001);
    }

    @Test
    public void testGetRoom_WithDifferentRooms_ShouldReturnCorrectRoom() {
        // Given: Multiple rooms exist
        IRoom room1 = new Room(TEST_ROOM_NUMBER, TEST_ROOM_PRICE, RoomType.SINGLE);
        IRoom room2 = new Room(TEST_ROOM_NUMBER_2, TEST_ROOM_PRICE_2, RoomType.DOUBLE);
        reservationService.addRoom(room1);
        reservationService.addRoom(room2);

        // When: Get each room
        IRoom result1 = hotelResource.getRoom(TEST_ROOM_NUMBER);
        IRoom result2 = hotelResource.getRoom(TEST_ROOM_NUMBER_2);

        // Then: Should return correct rooms
        assertEquals("First room number should match", TEST_ROOM_NUMBER, result1.getRoomNumber());
        assertEquals("Second room number should match", TEST_ROOM_NUMBER_2, result2.getRoomNumber());
    }

    @Test
    public void testGetRoom_WithFreeRoom_ShouldReturnFreeRoom() {
        // Given: A free room is added
        IRoom freeRoom = new FreeRoom(TEST_ROOM_NUMBER, RoomType.SINGLE);
        reservationService.addRoom(freeRoom);

        // When: Get the room
        IRoom result = hotelResource.getRoom(TEST_ROOM_NUMBER);

        // Then: Should return free room with zero price
        assertNotNull("Room should not be null", result);
        assertEquals("Room price should be 0", 0.0, result.getRoomPrice(), 0.001);
    }

    // ==================== TEST: bookARoom() ====================
    @Test
    public void testBookARoom_WithValidData_ShouldCreateReservation() {
        // Given: Customer and room exist
        hotelResource.createACustomer(TEST_EMAIL, TEST_FIRST_NAME, TEST_LAST_NAME);
        IRoom room = new Room(TEST_ROOM_NUMBER, TEST_ROOM_PRICE, RoomType.SINGLE);
        reservationService.addRoom(room);

        Date checkIn = createDate(2025, Calendar.MARCH, 1);
        Date checkOut = createDate(2025, Calendar.MARCH, 5);

        // When: We book the room
        Reservation result = hotelResource.bookARoom(TEST_EMAIL, room, checkIn, checkOut);

        // Then: Reservation should be created
        assertNotNull("Reservation should be created", result);
    }

    @Test
    public void testBookARoom_ShouldStoreCorrectRoom() {
        // Given: Customer and room exist
        hotelResource.createACustomer(TEST_EMAIL, TEST_FIRST_NAME, TEST_LAST_NAME);
        IRoom room = new Room(TEST_ROOM_NUMBER, TEST_ROOM_PRICE, RoomType.SINGLE);
        reservationService.addRoom(room);

        Date checkIn = createDate(2025, Calendar.APRIL, 1);
        Date checkOut = createDate(2025, Calendar.APRIL, 5);

        // When: Book the room
        Reservation result = hotelResource.bookARoom(TEST_EMAIL, room, checkIn, checkOut);

        // Then: Room should match
        assertEquals("Room number should match", TEST_ROOM_NUMBER, result.getRoom().getRoomNumber());
    }

    @Test
    public void testBookARoom_ShouldStoreCorrectCheckInDate() {
        // Given: Customer and room exist
        hotelResource.createACustomer(TEST_EMAIL, TEST_FIRST_NAME, TEST_LAST_NAME);
        IRoom room = new Room(TEST_ROOM_NUMBER, TEST_ROOM_PRICE, RoomType.SINGLE);
        reservationService.addRoom(room);

        Date checkIn = createDate(2025, Calendar.JUNE, 1);
        Date checkOut = createDate(2025, Calendar.JUNE, 5);

        // When: Book the room
        Reservation result = hotelResource.bookARoom(TEST_EMAIL, room, checkIn, checkOut);

        // Then: Check-in date should match
        assertEquals("Check-in date should match", checkIn, result.getCheckInDate());
    }

    @Test
    public void testBookARoom_ShouldStoreCorrectCheckOutDate() {
        // Given: Customer and room exist
        hotelResource.createACustomer(TEST_EMAIL, TEST_FIRST_NAME, TEST_LAST_NAME);
        IRoom room = new Room(TEST_ROOM_NUMBER, TEST_ROOM_PRICE, RoomType.SINGLE);
        reservationService.addRoom(room);

        Date checkIn = createDate(2025, Calendar.JULY, 1);
        Date checkOut = createDate(2025, Calendar.JULY, 5);

        // When: Book the room
        Reservation result = hotelResource.bookARoom(TEST_EMAIL, room, checkIn, checkOut);

        // Then: Check-out date should match
        assertEquals("Check-out date should match", checkOut, result.getCheckOutDate());
    }

    @Test
    public void testBookARoom_MultipleReservations_ShouldCreateAll() {
        // Given: Customer and rooms exist
        hotelResource.createACustomer(TEST_EMAIL, TEST_FIRST_NAME, TEST_LAST_NAME);
        IRoom room1 = new Room(TEST_ROOM_NUMBER, TEST_ROOM_PRICE, RoomType.SINGLE);
        IRoom room2 = new Room(TEST_ROOM_NUMBER_2, TEST_ROOM_PRICE_2, RoomType.DOUBLE);
        reservationService.addRoom(room1);
        reservationService.addRoom(room2);

        Date checkIn1 = createDate(2025, Calendar.AUGUST, 1);
        Date checkOut1 = createDate(2025, Calendar.AUGUST, 5);
        Date checkIn2 = createDate(2025, Calendar.SEPTEMBER, 1);
        Date checkOut2 = createDate(2025, Calendar.SEPTEMBER, 5);

        // When: Book multiple rooms
        Reservation reservation1 = hotelResource.bookARoom(TEST_EMAIL, room1, checkIn1, checkOut1);
        Reservation reservation2 = hotelResource.bookARoom(TEST_EMAIL, room2, checkIn2, checkOut2);

        // Then: Both reservations should be created
        assertNotNull("First reservation should be created", reservation1);
        assertNotNull("Second reservation should be created", reservation2);
    }

    // ==================== TEST: getCustomersReservations() ====================
    @Test
    public void testGetCustomersReservations_WhenCustomerHasReservations_ShouldReturnThem() {
        // Given: Customer exists with a reservation
        hotelResource.createACustomer(TEST_EMAIL, TEST_FIRST_NAME, TEST_LAST_NAME);
        IRoom room = new Room(TEST_ROOM_NUMBER, TEST_ROOM_PRICE, RoomType.SINGLE);
        reservationService.addRoom(room);

        Date checkIn = createDate(2025, Calendar.OCTOBER, 1);
        Date checkOut = createDate(2025, Calendar.OCTOBER, 5);
        hotelResource.bookARoom(TEST_EMAIL, room, checkIn, checkOut);

        // When: Get customer's reservations
        Collection<Reservation> result = hotelResource.getCustomersReservations(TEST_EMAIL);

        // Then: Should return the reservation
        assertNotNull("Result should not be null", result);
        assertFalse("Result should not be empty", result.isEmpty());
        assertEquals("Should have 1 reservation", 1, result.size());
    }

    @Test
    public void testGetCustomersReservations_WhenCustomerNotExists_ShouldReturnEmptyCollection() {
        // When: Get reservations for non-existent customer
        Collection<Reservation> result = hotelResource.getCustomersReservations("nonexistent@example.com");

        // Then: Should return empty collection
        assertNotNull("Result should not be null", result);
        assertTrue("Result should be empty for non-existent customer", result.isEmpty());
    }

    @Test
    public void testGetCustomersReservations_WhenCustomerExistsWithNoReservations_ShouldReturnEmptyOrNull() {
        // Given: Customer exists but has no reservations
        hotelResource.createACustomer(TEST_EMAIL, TEST_FIRST_NAME, TEST_LAST_NAME);

        // When: Get customer's reservations
        Collection<Reservation> result = hotelResource.getCustomersReservations(TEST_EMAIL);

        // Then: Should return empty collection or null
        assertTrue("Result should be null or empty", result == null || result.isEmpty());
    }

    @Test
    public void testGetCustomersReservations_WithMultipleReservations_ShouldReturnAll() {
        // Given: Customer has multiple reservations
        hotelResource.createACustomer(TEST_EMAIL, TEST_FIRST_NAME, TEST_LAST_NAME);
        IRoom room1 = new Room(TEST_ROOM_NUMBER, TEST_ROOM_PRICE, RoomType.SINGLE);
        IRoom room2 = new Room(TEST_ROOM_NUMBER_2, TEST_ROOM_PRICE_2, RoomType.DOUBLE);
        reservationService.addRoom(room1);
        reservationService.addRoom(room2);

        Date checkIn1 = createDate(2025, Calendar.NOVEMBER, 1);
        Date checkOut1 = createDate(2025, Calendar.NOVEMBER, 5);
        Date checkIn2 = createDate(2025, Calendar.DECEMBER, 1);
        Date checkOut2 = createDate(2025, Calendar.DECEMBER, 5);

        hotelResource.bookARoom(TEST_EMAIL, room1, checkIn1, checkOut1);
        hotelResource.bookARoom(TEST_EMAIL, room2, checkIn2, checkOut2);

        // When: Get customer's reservations
        Collection<Reservation> result = hotelResource.getCustomersReservations(TEST_EMAIL);

        // Then: Should return all reservations
        assertNotNull("Result should not be null", result);
        assertEquals("Should have 2 reservations", 2, result.size());
    }

    @Test
    public void testGetCustomersReservations_DifferentCustomers_ShouldReturnOnlyTheirReservations() {
        // Given: Two customers with reservations
        hotelResource.createACustomer(TEST_EMAIL, "Ahmed", "Ali");
        hotelResource.createACustomer(TEST_EMAIL_2, "Sara", "Hassan");

        IRoom room1 = new Room(TEST_ROOM_NUMBER, TEST_ROOM_PRICE, RoomType.SINGLE);
        IRoom room2 = new Room(TEST_ROOM_NUMBER_2, TEST_ROOM_PRICE_2, RoomType.DOUBLE);
        reservationService.addRoom(room1);
        reservationService.addRoom(room2);

        Date checkIn = createDate(2026, Calendar.JANUARY, 1);
        Date checkOut = createDate(2026, Calendar.JANUARY, 5);

        hotelResource.bookARoom(TEST_EMAIL, room1, checkIn, checkOut);
        hotelResource.bookARoom(TEST_EMAIL_2, room2, checkIn, checkOut);

        // When: Get each customer's reservations
        Collection<Reservation> result1 = hotelResource.getCustomersReservations(TEST_EMAIL);
        Collection<Reservation> result2 = hotelResource.getCustomersReservations(TEST_EMAIL_2);

        // Then: Each should have only their own reservation
        assertEquals("First customer should have 1 reservation", 1, result1.size());
        assertEquals("Second customer should have 1 reservation", 1, result2.size());
    }

    // ==================== TEST: findARoom() ====================
    @Test
    public void testFindARoom_WhenRoomsAvailable_ShouldReturnThem() {
        // Given: Rooms exist with no reservations
        IRoom room = new Room(TEST_ROOM_NUMBER, TEST_ROOM_PRICE, RoomType.SINGLE);
        reservationService.addRoom(room);

        Date checkIn = createDate(2026, Calendar.FEBRUARY, 1);
        Date checkOut = createDate(2026, Calendar.FEBRUARY, 5);

        // When: Search for rooms
        Collection<IRoom> result = hotelResource.findARoom(checkIn, checkOut);

        // Then: Should return available rooms
        assertNotNull("Result should not be null", result);
        assertFalse("Result should not be empty", result.isEmpty());
    }

    @Test
    public void testFindARoom_WhenNoRoomsExist_ShouldReturnEmptyCollection() {
        // Given: No rooms exist
        Date checkIn = createDate(2026, Calendar.MARCH, 1);
        Date checkOut = createDate(2026, Calendar.MARCH, 5);

        // When: Search for rooms
        Collection<IRoom> result = hotelResource.findARoom(checkIn, checkOut);

        // Then: Should return empty collection
        assertNotNull("Result should not be null", result);
        assertTrue("Result should be empty", result.isEmpty());
    }

    @Test
    public void testFindARoom_WhenRoomIsBooked_ShouldNotReturnIt() {
        // Given: Room is booked for the requested dates
        hotelResource.createACustomer(TEST_EMAIL, TEST_FIRST_NAME, TEST_LAST_NAME);
        IRoom room = new Room(TEST_ROOM_NUMBER, TEST_ROOM_PRICE, RoomType.SINGLE);
        reservationService.addRoom(room);

        Date checkIn = createDate(2026, Calendar.APRIL, 1);
        Date checkOut = createDate(2026, Calendar.APRIL, 5);
        hotelResource.bookARoom(TEST_EMAIL, room, checkIn, checkOut);

        // When: Search for rooms on same dates
        Collection<IRoom> result = hotelResource.findARoom(checkIn, checkOut);

        // Then: Should not contain the booked room
        assertTrue("Booked room should not be available", result.isEmpty()
                || !result.stream().anyMatch(r -> r.getRoomNumber().equals(TEST_ROOM_NUMBER)));
    }

    @Test
    public void testFindARoom_WithMultipleAvailableRooms_ShouldReturnAll() {
        // Given: Multiple rooms exist
        IRoom room1 = new Room(TEST_ROOM_NUMBER, TEST_ROOM_PRICE, RoomType.SINGLE);
        IRoom room2 = new Room(TEST_ROOM_NUMBER_2, TEST_ROOM_PRICE_2, RoomType.DOUBLE);
        IRoom room3 = new Room(TEST_ROOM_NUMBER_3, 300.0, RoomType.SINGLE);
        reservationService.addRoom(room1);
        reservationService.addRoom(room2);
        reservationService.addRoom(room3);

        Date checkIn = createDate(2026, Calendar.MAY, 1);
        Date checkOut = createDate(2026, Calendar.MAY, 5);

        // When: Search for rooms
        Collection<IRoom> result = hotelResource.findARoom(checkIn, checkOut);

        // Then: Should return all available rooms
        assertNotNull("Result should not be null", result);
        assertEquals("Should have 3 available rooms", 3, result.size());
    }

    @Test
    public void testFindARoom_WithDifferentDateRanges_ShouldWork() {
        // Given: Room exists
        IRoom room = new Room(TEST_ROOM_NUMBER, TEST_ROOM_PRICE, RoomType.SINGLE);
        reservationService.addRoom(room);

        // When: Search with different date ranges
        Date checkIn1 = createDate(2026, Calendar.JUNE, 1);
        Date checkOut1 = createDate(2026, Calendar.JUNE, 5);
        Date checkIn2 = createDate(2026, Calendar.JULY, 15);
        Date checkOut2 = createDate(2026, Calendar.JULY, 20);

        Collection<IRoom> result1 = hotelResource.findARoom(checkIn1, checkOut1);
        Collection<IRoom> result2 = hotelResource.findARoom(checkIn2, checkOut2);

        // Then: Both searches should return rooms
        assertNotNull("First result should not be null", result1);
        assertNotNull("Second result should not be null", result2);
    }

    // ==================== TEST: findAlternativeRooms() ====================
    @Test
    public void testFindAlternativeRooms_ShouldReturnCollection() {
        // Given: Date range
        Date checkIn = createDate(2026, Calendar.AUGUST, 1);
        Date checkOut = createDate(2026, Calendar.AUGUST, 5);

        // When: Search for alternative rooms
        Collection<IRoom> result = hotelResource.findAlternativeRooms(checkIn, checkOut);

        // Then: Should return a collection (empty or with rooms)
        assertNotNull("Result should not be null", result);
    }

    @Test
    public void testFindAlternativeRooms_WithRoomsAvailable_ShouldReturnThem() {
        // Given: Room exists
        IRoom room = new Room(TEST_ROOM_NUMBER, TEST_ROOM_PRICE, RoomType.SINGLE);
        reservationService.addRoom(room);

        Date checkIn = createDate(2026, Calendar.SEPTEMBER, 1);
        Date checkOut = createDate(2026, Calendar.SEPTEMBER, 5);

        // When: Search for alternative rooms
        Collection<IRoom> result = hotelResource.findAlternativeRooms(checkIn, checkOut);

        // Then: Should complete without exception
        assertNotNull("Result should not be null", result);
    }

    @Test
    public void testFindAlternativeRooms_WithDifferentDates_ShouldWork() {
        // Given: Different date range
        Date checkIn = createDate(2026, Calendar.OCTOBER, 10);
        Date checkOut = createDate(2026, Calendar.OCTOBER, 15);

        // When: Search for alternative rooms
        Collection<IRoom> result = hotelResource.findAlternativeRooms(checkIn, checkOut);

        // Then: Should complete without exception
        assertNotNull("Result should not be null", result);
    }

    // ==================== TEST: addDefaultPlusDays() ====================
    @Test
    public void testAddDefaultPlusDays_ShouldReturnLaterDate() {
        // Given: A date
        Date originalDate = createDate(2026, Calendar.NOVEMBER, 1);

        // When: Add default plus days
        Date result = hotelResource.addDefaultPlusDays(originalDate);

        // Then: Should return a date after the original
        assertNotNull("Result should not be null", result);
        assertTrue("Result should be after original date", result.after(originalDate));
    }

    @Test
    public void testAddDefaultPlusDays_ShouldNotReturnNull() {
        // Given: A date
        Date originalDate = createDate(2026, Calendar.DECEMBER, 15);

        // When: Add default plus days
        Date result = hotelResource.addDefaultPlusDays(originalDate);

        // Then: Should not return null
        assertNotNull("Result should not be null", result);
    }

    @Test
    public void testAddDefaultPlusDays_WithDifferentDates_ShouldWork() {
        // Given: Different dates
        Date date1 = createDate(2027, Calendar.JANUARY, 1);
        Date date2 = createDate(2027, Calendar.FEBRUARY, 15);
        Date date3 = createDate(2027, Calendar.MARCH, 31);

        // When: Add default plus days to each
        Date result1 = hotelResource.addDefaultPlusDays(date1);
        Date result2 = hotelResource.addDefaultPlusDays(date2);
        Date result3 = hotelResource.addDefaultPlusDays(date3);

        // Then: All should return later dates
        assertTrue("Result1 should be after date1", result1.after(date1));
        assertTrue("Result2 should be after date2", result2.after(date2));
        assertTrue("Result3 should be after date3", result3.after(date3));
    }

    @Test
    public void testAddDefaultPlusDays_ConsistentDayAddition() {
        // Given: Two dates exactly 10 days apart
        Date date1 = createDate(2027, Calendar.APRIL, 1);
        Date date2 = createDate(2027, Calendar.APRIL, 11);

        // When: Add default plus days to both
        Date result1 = hotelResource.addDefaultPlusDays(date1);
        Date result2 = hotelResource.addDefaultPlusDays(date2);

        // Then: The difference between results should be same as difference between inputs
        long diff1 = date2.getTime() - date1.getTime();
        long diff2 = result2.getTime() - result1.getTime();
        assertEquals("Day difference should be preserved", diff1, diff2);
    }

    // ==================== TEST: cancelReservation() ====================
    @Test
    public void testCancelReservation_WhenCustomerNotExists_ShouldReturnFalse() {
        // Given: Non-existent customer
        Date checkIn = createDate(2027, Calendar.MAY, 1);

        // When: Try to cancel reservation for non-existent customer
        boolean result = hotelResource.cancelReservation("nonexistent@example.com", TEST_ROOM_NUMBER, checkIn);

        // Then: Should return false
        assertFalse("Should return false for non-existent customer", result);
    }

    @Test
    public void testCancelReservation_WhenCustomerExistsButNoReservation_ShouldReturnFalse() {
        // Given: Customer exists but has no reservations
        hotelResource.createACustomer(TEST_EMAIL, TEST_FIRST_NAME, TEST_LAST_NAME);
        Date checkIn = createDate(2027, Calendar.JUNE, 1);

        // When: Try to cancel non-existent reservation
        boolean result = hotelResource.cancelReservation(TEST_EMAIL, TEST_ROOM_NUMBER, checkIn);

        // Then: Should return false
        assertFalse("Should return false when no reservation exists", result);
    }

    @Test
    public void testCancelReservation_WhenReservationExists_ShouldReturnTrue() {
        // Given: Customer and room exist, and reservation is made
        hotelResource.createACustomer(TEST_EMAIL, TEST_FIRST_NAME, TEST_LAST_NAME);
        IRoom room = new Room(TEST_ROOM_NUMBER, TEST_ROOM_PRICE, RoomType.SINGLE);
        reservationService.addRoom(room);

        Date checkIn = createDate(2027, Calendar.JULY, 1);
        Date checkOut = createDate(2027, Calendar.JULY, 5);
        hotelResource.bookARoom(TEST_EMAIL, room, checkIn, checkOut);

        // When: Cancel the reservation
        boolean result = hotelResource.cancelReservation(TEST_EMAIL, TEST_ROOM_NUMBER, checkIn);

        // Then: Should return true
        assertTrue("Should return true when reservation is cancelled", result);
    }

    @Test
    public void testCancelReservation_ShouldRemoveReservation() {
        // Given: Customer has a reservation
        hotelResource.createACustomer(TEST_EMAIL, TEST_FIRST_NAME, TEST_LAST_NAME);
        IRoom room = new Room(TEST_ROOM_NUMBER, TEST_ROOM_PRICE, RoomType.SINGLE);
        reservationService.addRoom(room);

        Date checkIn = createDate(2027, Calendar.AUGUST, 1);
        Date checkOut = createDate(2027, Calendar.AUGUST, 5);
        hotelResource.bookARoom(TEST_EMAIL, room, checkIn, checkOut);

        // Verify reservation exists
        Collection<Reservation> beforeCancel = hotelResource.getCustomersReservations(TEST_EMAIL);
        assertEquals("Should have 1 reservation before cancel", 1, beforeCancel.size());

        // When: Cancel the reservation
        hotelResource.cancelReservation(TEST_EMAIL, TEST_ROOM_NUMBER, checkIn);

        // Then: Reservation should be removed
        Collection<Reservation> afterCancel = hotelResource.getCustomersReservations(TEST_EMAIL);
        assertTrue("Should have no reservations after cancel",
                afterCancel == null || afterCancel.isEmpty());
    }

    @Test
    public void testCancelReservation_WithWrongRoomNumber_ShouldReturnFalse() {
        // Given: Customer has a reservation for a specific room
        hotelResource.createACustomer(TEST_EMAIL, TEST_FIRST_NAME, TEST_LAST_NAME);
        IRoom room = new Room(TEST_ROOM_NUMBER, TEST_ROOM_PRICE, RoomType.SINGLE);
        reservationService.addRoom(room);

        Date checkIn = createDate(2027, Calendar.SEPTEMBER, 1);
        Date checkOut = createDate(2027, Calendar.SEPTEMBER, 5);
        hotelResource.bookARoom(TEST_EMAIL, room, checkIn, checkOut);

        // When: Try to cancel with wrong room number
        boolean result = hotelResource.cancelReservation(TEST_EMAIL, "999", checkIn);

        // Then: Should return false
        assertFalse("Should return false for wrong room number", result);
    }

    @Test
    public void testCancelReservation_WithWrongCheckInDate_ShouldReturnFalse() {
        // Given: Customer has a reservation for a specific date
        hotelResource.createACustomer(TEST_EMAIL, TEST_FIRST_NAME, TEST_LAST_NAME);
        IRoom room = new Room(TEST_ROOM_NUMBER, TEST_ROOM_PRICE, RoomType.SINGLE);
        reservationService.addRoom(room);

        Date checkIn = createDate(2027, Calendar.OCTOBER, 1);
        Date checkOut = createDate(2027, Calendar.OCTOBER, 5);
        hotelResource.bookARoom(TEST_EMAIL, room, checkIn, checkOut);

        // When: Try to cancel with wrong check-in date
        Date wrongCheckIn = createDate(2027, Calendar.OCTOBER, 15);
        boolean result = hotelResource.cancelReservation(TEST_EMAIL, TEST_ROOM_NUMBER, wrongCheckIn);

        // Then: Should return false
        assertFalse("Should return false for wrong check-in date", result);
    }

    // ==================== INTEGRATION TESTS ====================
    @Test
    public void testIntegration_CompleteBookingWorkflow() {
        // Step 1: Create a customer
        hotelResource.createACustomer(TEST_EMAIL, TEST_FIRST_NAME, TEST_LAST_NAME);
        Customer customer = hotelResource.getCustomer(TEST_EMAIL);
        assertNotNull("Customer should be created", customer);

        // Step 2: Add a room
        IRoom room = new Room(TEST_ROOM_NUMBER, TEST_ROOM_PRICE, RoomType.SINGLE);
        reservationService.addRoom(room);
        IRoom retrievedRoom = hotelResource.getRoom(TEST_ROOM_NUMBER);
        assertNotNull("Room should be added", retrievedRoom);

        // Step 3: Search for available rooms
        Date checkIn = createDate(2027, Calendar.NOVEMBER, 1);
        Date checkOut = createDate(2027, Calendar.NOVEMBER, 5);
        Collection<IRoom> availableRooms = hotelResource.findARoom(checkIn, checkOut);
        assertFalse("Should find available rooms", availableRooms.isEmpty());

        // Step 4: Book the room
        Reservation reservation = hotelResource.bookARoom(TEST_EMAIL, room, checkIn, checkOut);
        assertNotNull("Reservation should be created", reservation);

        // Step 5: Verify reservation exists
        Collection<Reservation> reservations = hotelResource.getCustomersReservations(TEST_EMAIL);
        assertFalse("Customer should have reservations", reservations.isEmpty());

        // Step 6: Cancel reservation
        boolean cancelled = hotelResource.cancelReservation(TEST_EMAIL, TEST_ROOM_NUMBER, checkIn);
        assertTrue("Cancellation should succeed", cancelled);
    }

    @Test
    public void testIntegration_MultipleCustomersMultipleRooms() {
        // Create customers
        hotelResource.createACustomer(TEST_EMAIL, "Ahmed", "Ali");
        hotelResource.createACustomer(TEST_EMAIL_2, "Sara", "Hassan");

        // Add rooms
        IRoom room1 = new Room(TEST_ROOM_NUMBER, TEST_ROOM_PRICE, RoomType.SINGLE);
        IRoom room2 = new Room(TEST_ROOM_NUMBER_2, TEST_ROOM_PRICE_2, RoomType.DOUBLE);
        reservationService.addRoom(room1);
        reservationService.addRoom(room2);

        // Book rooms for different customers
        Date checkIn = createDate(2027, Calendar.DECEMBER, 1);
        Date checkOut = createDate(2027, Calendar.DECEMBER, 5);

        Reservation res1 = hotelResource.bookARoom(TEST_EMAIL, room1, checkIn, checkOut);
        Reservation res2 = hotelResource.bookARoom(TEST_EMAIL_2, room2, checkIn, checkOut);

        // Verify each customer has their reservation
        Collection<Reservation> customer1Reservations = hotelResource.getCustomersReservations(TEST_EMAIL);
        Collection<Reservation> customer2Reservations = hotelResource.getCustomersReservations(TEST_EMAIL_2);

        assertEquals("First customer should have 1 reservation", 1, customer1Reservations.size());
        assertEquals("Second customer should have 1 reservation", 1, customer2Reservations.size());
    }

    @Test
    public void testIntegration_SearchBookSearchAgain() {
        // Add rooms
        IRoom room1 = new Room(TEST_ROOM_NUMBER, TEST_ROOM_PRICE, RoomType.SINGLE);
        IRoom room2 = new Room(TEST_ROOM_NUMBER_2, TEST_ROOM_PRICE_2, RoomType.DOUBLE);
        reservationService.addRoom(room1);
        reservationService.addRoom(room2);

        Date checkIn = createDate(2028, Calendar.JANUARY, 1);
        Date checkOut = createDate(2028, Calendar.JANUARY, 5);

        // Initial search - should find 2 rooms
        Collection<IRoom> initialSearch = hotelResource.findARoom(checkIn, checkOut);
        assertEquals("Should find 2 available rooms initially", 2, initialSearch.size());

        // Book one room
        hotelResource.createACustomer(TEST_EMAIL, TEST_FIRST_NAME, TEST_LAST_NAME);
        hotelResource.bookARoom(TEST_EMAIL, room1, checkIn, checkOut);

        // Search again - should find 1 room
        Collection<IRoom> secondSearch = hotelResource.findARoom(checkIn, checkOut);
        assertEquals("Should find 1 available room after booking", 1, secondSearch.size());
    }

    // ==================== HELPER METHODS ====================
    /**
     * Helper method to create dates consistently
     */
    private Date createDate(int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, day, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }
}
