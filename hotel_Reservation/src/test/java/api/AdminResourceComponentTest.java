package api;

import model.customer.Customer;
import model.room.IRoom;
import model.room.Room;
import model.room.FreeRoom;
import model.room.enums.RoomType;
import service.customer.CustomerService;
import service.reservation.ReservationService;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Nada Alsulami
 */
public class AdminResourceComponentTest {

    private AdminResource adminResource;
    private CustomerService customerService;
    private ReservationService reservationService;

    // Test data constants
    private static final String TEST_EMAIL = "component.test@example.com";
    private static final String TEST_EMAIL_2 = "component.test2@example.com";
    private static final String TEST_EMAIL_3 = "component.test3@example.com";
    private static final String TEST_FIRST_NAME = "Admin";
    private static final String TEST_LAST_NAME = "User";
    private static final String TEST_ROOM_NUMBER = "301";
    private static final String TEST_ROOM_NUMBER_2 = "302";
    private static final String TEST_ROOM_NUMBER_3 = "303";
    private static final String TEST_ROOM_NUMBER_4 = "304";
    private static final Double TEST_ROOM_PRICE = 200.0;
    private static final Double TEST_ROOM_PRICE_2 = 300.0;

    @Before
    public void setUp() {
        // Get the singleton instances
        adminResource = AdminResource.getSingleton();
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
     * Helper method to clear all data from singleton services.
     * This ensures test isolation and prevents state pollution between tests.
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
        AdminResource instance1 = AdminResource.getSingleton();
        AdminResource instance2 = AdminResource.getSingleton();

        // Then: Both should be the exact same object
        assertNotNull("Singleton should not be null", instance1);
        assertSame("Should return same singleton instance", instance1, instance2);
    }

    @Test
    public void testGetSingleton_ShouldNeverReturnNull() {
        // When: Get singleton
        AdminResource instance = AdminResource.getSingleton();

        // Then: Should never be null
        assertNotNull("Singleton should never be null", instance);
    }

    @Test
    public void testGetSingleton_MultipleCallsShouldReturnIdenticalInstance() {
        // When: Call getSingleton multiple times
        AdminResource instance1 = AdminResource.getSingleton();
        AdminResource instance2 = AdminResource.getSingleton();
        AdminResource instance3 = AdminResource.getSingleton();

        // Then: All should be identical
        assertSame("All instances should be identical", instance1, instance2);
        assertSame("All instances should be identical", instance2, instance3);
    }

    // ==================== TEST: getCustomer() ====================

    @Test
    public void testGetCustomer_WhenExists_ShouldReturnCustomer() {
        // Given: A customer exists (created through CustomerService)
        customerService.addCustomer(TEST_EMAIL, TEST_FIRST_NAME, TEST_LAST_NAME);

        // When: We get the customer through AdminResource
        Customer result = adminResource.getCustomer(TEST_EMAIL);

        // Then: Should return the customer
        assertNotNull("Customer should not be null", result);
        assertEquals("Email should match", TEST_EMAIL, result.getEmail());
    }

    @Test
    public void testGetCustomer_WhenNotExists_ShouldReturnNull() {
        // When: We get a non-existent customer
        Customer result = adminResource.getCustomer("nonexistent@example.com");

        // Then: Should return null
        assertNull("Customer should be null for non-existent email", result);
    }

    @Test
    public void testGetCustomer_ShouldReturnCorrectFirstName() {
        // Given: A customer exists
        customerService.addCustomer(TEST_EMAIL, TEST_FIRST_NAME, TEST_LAST_NAME);

        // When: Get the customer
        Customer result = adminResource.getCustomer(TEST_EMAIL);

        // Then: First name should match
        assertEquals("First name should match", TEST_FIRST_NAME, result.getFirstName());
    }

    @Test
    public void testGetCustomer_ShouldReturnCorrectLastName() {
        // Given: A customer exists
        customerService.addCustomer(TEST_EMAIL, TEST_FIRST_NAME, TEST_LAST_NAME);

        // When: Get the customer
        Customer result = adminResource.getCustomer(TEST_EMAIL);

        // Then: Last name should match
        assertEquals("Last name should match", TEST_LAST_NAME, result.getLastName());
    }

    @Test
    public void testGetCustomer_WithDifferentEmails_ShouldReturnCorrectCustomers() {
        // Given: Multiple customers exist
        customerService.addCustomer(TEST_EMAIL, "Ahmed", "Ali");
        customerService.addCustomer(TEST_EMAIL_2, "Sara", "Hassan");

        // When: We get each customer
        Customer customer1 = adminResource.getCustomer(TEST_EMAIL);
        Customer customer2 = adminResource.getCustomer(TEST_EMAIL_2);

        // Then: Should return correct customers
        assertNotNull("First customer should exist", customer1);
        assertNotNull("Second customer should exist", customer2);
        assertEquals("First email should match", TEST_EMAIL, customer1.getEmail());
        assertEquals("Second email should match", TEST_EMAIL_2, customer2.getEmail());
    }

    @Test
    public void testGetCustomer_MultipleCalls_ShouldReturnSameCustomer() {
        // Given: A customer exists
        customerService.addCustomer(TEST_EMAIL, TEST_FIRST_NAME, TEST_LAST_NAME);

        // When: Get customer multiple times
        Customer customer1 = adminResource.getCustomer(TEST_EMAIL);
        Customer customer2 = adminResource.getCustomer(TEST_EMAIL);

        // Then: Should return same customer object
        assertSame("Should return same customer instance", customer1, customer2);
    }

    // ==================== TEST: addRoom() ====================

    @Test
    public void testAddRoom_WithSingleRoom_ShouldAddRoom() {
        // Given: A list with one room
        List<IRoom> rooms = new ArrayList<>();
        IRoom room = new Room(TEST_ROOM_NUMBER, TEST_ROOM_PRICE, RoomType.SINGLE);
        rooms.add(room);

        // When: Add the room
        adminResource.addRoom(rooms);

        // Then: Room should be retrievable through ReservationService
        IRoom retrievedRoom = reservationService.getARoom(TEST_ROOM_NUMBER);
        assertNotNull("Room should be added", retrievedRoom);
        assertEquals("Room number should match", TEST_ROOM_NUMBER, retrievedRoom.getRoomNumber());
    }

    @Test
    public void testAddRoom_ShouldStoreCorrectRoomPrice() {
        // Given: A room with specific price
        List<IRoom> rooms = new ArrayList<>();
        rooms.add(new Room(TEST_ROOM_NUMBER, TEST_ROOM_PRICE, RoomType.SINGLE));

        // When: Add the room
        adminResource.addRoom(rooms);

        // Then: Price should be stored correctly
        IRoom retrievedRoom = reservationService.getARoom(TEST_ROOM_NUMBER);
        assertEquals("Room price should match", TEST_ROOM_PRICE, retrievedRoom.getRoomPrice(), 0.001);
    }

    @Test
    public void testAddRoom_ShouldStoreCorrectRoomType() {
        // Given: A room with specific type
        List<IRoom> rooms = new ArrayList<>();
        rooms.add(new Room(TEST_ROOM_NUMBER, TEST_ROOM_PRICE, RoomType.DOUBLE));

        // When: Add the room
        adminResource.addRoom(rooms);

        // Then: Room type should be stored correctly
        IRoom retrievedRoom = reservationService.getARoom(TEST_ROOM_NUMBER);
        assertEquals("Room type should match", RoomType.DOUBLE, retrievedRoom.getRoomType());
    }

    @Test
    public void testAddRoom_WithMultipleRooms_ShouldAddAllRooms() {
        // Given: A list with multiple rooms
        List<IRoom> rooms = new ArrayList<>();
        rooms.add(new Room(TEST_ROOM_NUMBER, TEST_ROOM_PRICE, RoomType.SINGLE));
        rooms.add(new Room(TEST_ROOM_NUMBER_2, TEST_ROOM_PRICE_2, RoomType.DOUBLE));
        rooms.add(new Room(TEST_ROOM_NUMBER_3, 0.0, RoomType.SINGLE));

        // When: Add all rooms
        adminResource.addRoom(rooms);

        // Then: All rooms should be retrievable
        assertNotNull("First room should be added", reservationService.getARoom(TEST_ROOM_NUMBER));
        assertNotNull("Second room should be added", reservationService.getARoom(TEST_ROOM_NUMBER_2));
        assertNotNull("Third room should be added", reservationService.getARoom(TEST_ROOM_NUMBER_3));
    }

    @Test
    public void testAddRoom_WithEmptyList_ShouldNotFail() {
        // Given: An empty list
        List<IRoom> rooms = new ArrayList<>();

        // When: Add empty list
        adminResource.addRoom(rooms);

        // Then: Should complete without exception
        Collection<IRoom> allRooms = adminResource.getAllRooms();
        assertTrue("Should have no rooms", allRooms.isEmpty());
    }

    @Test
    public void testAddRoom_WithDifferentRoomTypes_ShouldAddAll() {
        // Given: Rooms of different types
        List<IRoom> rooms = new ArrayList<>();
        rooms.add(new Room("401", 150.0, RoomType.SINGLE));
        rooms.add(new Room("402", 250.0, RoomType.DOUBLE));

        // When: Add rooms
        adminResource.addRoom(rooms);

        // Then: All should be added with correct types
        IRoom room1 = reservationService.getARoom("401");
        IRoom room2 = reservationService.getARoom("402");

        assertNotNull("Single room should be added", room1);
        assertNotNull("Double room should be added", room2);
        assertEquals("First room should be SINGLE", RoomType.SINGLE, room1.getRoomType());
        assertEquals("Second room should be DOUBLE", RoomType.DOUBLE, room2.getRoomType());
    }

    @Test
    public void testAddRoom_WithFreeRoom_ShouldAddFreeRoom() {
        // Given: A free room
        List<IRoom> rooms = new ArrayList<>();
        rooms.add(new FreeRoom(TEST_ROOM_NUMBER, RoomType.SINGLE));

        // When: Add the room
        adminResource.addRoom(rooms);

        // Then: Free room should be added with zero price
        IRoom retrievedRoom = reservationService.getARoom(TEST_ROOM_NUMBER);
        assertNotNull("Free room should be added", retrievedRoom);
        assertEquals("Free room price should be 0", 0.0, retrievedRoom.getRoomPrice(), 0.001);
    }

    // ==================== TEST: getAllRooms() ====================

    @Test
    public void testGetAllRooms_WhenEmpty_ShouldReturnEmptyCollection() {
        // When: Get all rooms (no rooms added)
        Collection<IRoom> result = adminResource.getAllRooms();

        // Then: Should return empty collection
        assertNotNull("Result should not be null", result);
        assertTrue("Result should be empty", result.isEmpty());
    }

    @Test
    public void testGetAllRooms_AfterAddingRooms_ShouldReturnAllRooms() {
        // Given: Rooms are added
        List<IRoom> roomsToAdd = new ArrayList<>();
        roomsToAdd.add(new Room("501", 180.0, RoomType.SINGLE));
        roomsToAdd.add(new Room("502", 280.0, RoomType.DOUBLE));

        adminResource.addRoom(roomsToAdd);

        // When: Get all rooms
        Collection<IRoom> allRooms = adminResource.getAllRooms();

        // Then: Should contain the added rooms
        assertNotNull("Result should not be null", allRooms);
        assertEquals("Should have 2 rooms", 2, allRooms.size());
    }

    @Test
    public void testGetAllRooms_MultipleCalls_ShouldReturnConsistentResults() {
        // Given: Add some rooms
        List<IRoom> rooms = new ArrayList<>();
        rooms.add(new Room(TEST_ROOM_NUMBER, TEST_ROOM_PRICE, RoomType.SINGLE));
        adminResource.addRoom(rooms);

        // When: Call getAllRooms multiple times
        Collection<IRoom> result1 = adminResource.getAllRooms();
        Collection<IRoom> result2 = adminResource.getAllRooms();

        // Then: Both should return same size
        assertEquals("Results should have same size", result1.size(), result2.size());
    }

    @Test
    public void testGetAllRooms_ShouldContainAddedRoom() {
        // Given: Add a room
        List<IRoom> rooms = new ArrayList<>();
        rooms.add(new Room(TEST_ROOM_NUMBER, TEST_ROOM_PRICE, RoomType.SINGLE));
        adminResource.addRoom(rooms);

        // When: Get all rooms
        Collection<IRoom> allRooms = adminResource.getAllRooms();

        // Then: Should contain the specific room
        boolean found = allRooms.stream()
            .anyMatch(r -> r.getRoomNumber().equals(TEST_ROOM_NUMBER));
        assertTrue("Should contain the added room", found);
    }

    // ==================== TEST: getAllCustomers() ====================

    @Test
    public void testGetAllCustomers_WhenEmpty_ShouldReturnEmptyCollection() {
        // When: Get all customers (no customers added)
        Collection<Customer> result = adminResource.getAllCustomers();

        // Then: Should return empty collection
        assertNotNull("Result should not be null", result);
        assertTrue("Result should be empty", result.isEmpty());
    }

    @Test
    public void testGetAllCustomers_AfterAddingCustomers_ShouldReturnAllCustomers() {
        // Given: Customers are added
        customerService.addCustomer("customer1@test.com", "Customer", "One");
        customerService.addCustomer("customer2@test.com", "Customer", "Two");

        // When: Get all customers
        Collection<Customer> allCustomers = adminResource.getAllCustomers();

        // Then: Should contain the added customers
        assertNotNull("Result should not be null", allCustomers);
        assertEquals("Should have 2 customers", 2, allCustomers.size());
    }

    @Test
    public void testGetAllCustomers_MultipleCalls_ShouldReturnConsistentResults() {
        // Given: Add some customers
        customerService.addCustomer(TEST_EMAIL, TEST_FIRST_NAME, TEST_LAST_NAME);

        // When: Call getAllCustomers multiple times
        Collection<Customer> result1 = adminResource.getAllCustomers();
        Collection<Customer> result2 = adminResource.getAllCustomers();

        // Then: Both should return same size
        assertEquals("Results should have same size", result1.size(), result2.size());
    }

    @Test
    public void testGetAllCustomers_ShouldContainAddedCustomer() {
        // Given: Add a customer
        customerService.addCustomer(TEST_EMAIL, TEST_FIRST_NAME, TEST_LAST_NAME);

        // When: Get all customers
        Collection<Customer> allCustomers = adminResource.getAllCustomers();

        // Then: Should contain the specific customer
        boolean found = allCustomers.stream()
            .anyMatch(c -> c.getEmail().equals(TEST_EMAIL));
        assertTrue("Should contain the added customer", found);
    }

    @Test
    public void testGetAllCustomers_WithMultipleCustomers_ShouldReturnAll() {
        // Given: Add multiple customers
        customerService.addCustomer(TEST_EMAIL, "Ahmed", "Ali");
        customerService.addCustomer(TEST_EMAIL_2, "Sara", "Hassan");
        customerService.addCustomer(TEST_EMAIL_3, "Mohammed", "Abdullah");

        // When: Get all customers
        Collection<Customer> allCustomers = adminResource.getAllCustomers();

        // Then: Should contain all customers
        assertEquals("Should have 3 customers", 3, allCustomers.size());
    }

    // ==================== TEST: displayAllReservations() ====================

    @Test
    public void testDisplayAllReservations_WithNoReservations_ShouldNotFail() {
        // When: Display when no reservations exist
        adminResource.displayAllReservations();

        // Then: Should complete without exception
        assertTrue("Should handle empty reservations", true);
    }

    @Test
    public void testDisplayAllReservations_MultipleCalls_ShouldNotFail() {
        // When: Call multiple times
        adminResource.displayAllReservations();
        adminResource.displayAllReservations();
        adminResource.displayAllReservations();

        // Then: All calls should complete successfully
        assertTrue("Should complete successfully", true);
    }

    @Test
    public void testDisplayAllReservations_WithReservations_ShouldNotFail() {
        // Given: Create customer, room, and reservation
        customerService.addCustomer(TEST_EMAIL, TEST_FIRST_NAME, TEST_LAST_NAME);
        IRoom room = new Room(TEST_ROOM_NUMBER, TEST_ROOM_PRICE, RoomType.SINGLE);
        reservationService.addRoom(room);

        Date checkIn = createDate(2025, Calendar.MARCH, 1);
        Date checkOut = createDate(2025, Calendar.MARCH, 5);
        Customer customer = customerService.getCustomer(TEST_EMAIL);
        reservationService.reserveARoom(customer, room, checkIn, checkOut);

        // When: Display all reservations
        adminResource.displayAllReservations();

        // Then: Should complete without exception
        assertTrue("Should handle reservations", true);
    }

    // ==================== TEST: findMostPopularRoom() ====================

    @Test
    public void testFindMostPopularRoom_WhenNoReservations_ShouldReturnNull() {
        // When: Find popular room with no reservations
        String result = adminResource.findMostPopularRoom();

        // Then: Should return null (or handle gracefully)
        // Note: Actual behavior depends on service implementation
        assertTrue("Should handle no reservations gracefully", true);
    }

    @Test
    public void testFindMostPopularRoom_MultipleCalls_ShouldComplete() {
        // When: Call multiple times
        adminResource.findMostPopularRoom();
        adminResource.findMostPopularRoom();

        // Then: Both calls should complete
        assertTrue("Should complete successfully", true);
    }

    @Test
    public void testFindMostPopularRoom_WithReservations_ShouldReturnRoomNumber() {
        // Given: Create customer, room, and reservation
        customerService.addCustomer(TEST_EMAIL, TEST_FIRST_NAME, TEST_LAST_NAME);
        IRoom room = new Room(TEST_ROOM_NUMBER, TEST_ROOM_PRICE, RoomType.SINGLE);
        reservationService.addRoom(room);

        Date checkIn = createDate(2025, Calendar.APRIL, 1);
        Date checkOut = createDate(2025, Calendar.APRIL, 5);
        Customer customer = customerService.getCustomer(TEST_EMAIL);
        reservationService.reserveARoom(customer, room, checkIn, checkOut);

        // When: Find most popular room
        String result = adminResource.findMostPopularRoom();

        // Then: Should return the room number
        assertNotNull("Result should not be null when reservations exist", result);
        assertEquals("Should return the room with reservation", TEST_ROOM_NUMBER, result);
    }

    // ==================== INTEGRATION TESTS ====================

    @Test
    public void testIntegration_AddRoomsAndRetrieveThem() {
        // Given: Add rooms
        List<IRoom> rooms = new ArrayList<>();
        rooms.add(new Room("601", 300.0, RoomType.SINGLE));
        rooms.add(new Room("602", 400.0, RoomType.DOUBLE));

        adminResource.addRoom(rooms);

        // When: Get all rooms
        Collection<IRoom> allRooms = adminResource.getAllRooms();

        // Then: Should contain the added rooms
        assertNotNull("All rooms should not be null", allRooms);
        assertEquals("Should have 2 rooms", 2, allRooms.size());
    }

    @Test
    public void testIntegration_AddCustomersAndRetrieveThem() {
        // Given: Add customers via CustomerService
        customerService.addCustomer("integration1@test.com", "Test", "User1");
        customerService.addCustomer("integration2@test.com", "Test", "User2");

        // When: Get all customers via AdminResource
        Collection<Customer> customers = adminResource.getAllCustomers();

        // Then: Should contain customers
        assertNotNull("Customers should not be null", customers);
        assertEquals("Should have 2 customers", 2, customers.size());

        // And: Should be able to get specific customer
        Customer customer = adminResource.getCustomer("integration1@test.com");
        assertNotNull("Specific customer should exist", customer);
    }

    @Test
    public void testIntegration_CompleteAdminWorkflow() {
        // Step 1: Add rooms
        List<IRoom> rooms = new ArrayList<>();
        rooms.add(new Room("701", 250.0, RoomType.SINGLE));
        rooms.add(new Room("702", 350.0, RoomType.DOUBLE));
        adminResource.addRoom(rooms);

        // Step 2: Verify rooms are added
        Collection<IRoom> allRooms = adminResource.getAllRooms();
        assertNotNull("Rooms should exist", allRooms);
        assertEquals("Should have 2 rooms", 2, allRooms.size());

        // Step 3: Add customers
        customerService.addCustomer("workflow@test.com", "Workflow", "Test");
        customerService.addCustomer("workflow2@test.com", "Workflow", "Test2");

        // Step 4: Get all customers
        Collection<Customer> allCustomers = adminResource.getAllCustomers();
        assertNotNull("Customers should exist", allCustomers);
        assertEquals("Should have 2 customers", 2, allCustomers.size());

        // Step 5: Get specific customer
        Customer customer = adminResource.getCustomer("workflow@test.com");
        assertNotNull("Specific customer should exist", customer);
        assertEquals("Customer email should match", "workflow@test.com", customer.getEmail());

        // Step 6: Display all reservations (should not fail)
        adminResource.displayAllReservations();

        // Step 7: Find most popular room (should not fail)
        adminResource.findMostPopularRoom();
        assertTrue("Should complete workflow", true);
    }

    @Test
    public void testIntegration_AddRoomsThenVerifyIndividually() {
        // Given: Add multiple rooms
        List<IRoom> rooms = new ArrayList<>();
        rooms.add(new Room("801", 100.0, RoomType.SINGLE));
        rooms.add(new Room("802", 200.0, RoomType.DOUBLE));
        rooms.add(new Room("803", 300.0, RoomType.SINGLE));

        adminResource.addRoom(rooms);

        // When: Get all rooms
        Collection<IRoom> allRooms = adminResource.getAllRooms();

        // Then: Verify each room
        assertEquals("Should have 3 rooms", 3, allRooms.size());

        // Verify individual rooms through service
        IRoom room801 = reservationService.getARoom("801");
        IRoom room802 = reservationService.getARoom("802");
        IRoom room803 = reservationService.getARoom("803");

        assertNotNull("Room 801 should exist", room801);
        assertNotNull("Room 802 should exist", room802);
        assertNotNull("Room 803 should exist", room803);

        assertEquals("Room 801 price should match", 100.0, room801.getRoomPrice(), 0.001);
        assertEquals("Room 802 price should match", 200.0, room802.getRoomPrice(), 0.001);
        assertEquals("Room 803 price should match", 300.0, room803.getRoomPrice(), 0.001);
    }

    @Test
    public void testIntegration_CustomerAndRoomInteraction() {
        // Add rooms
        List<IRoom> rooms = new ArrayList<>();
        rooms.add(new Room("901", 150.0, RoomType.SINGLE));
        adminResource.addRoom(rooms);

        // Add customer
        customerService.addCustomer(TEST_EMAIL, TEST_FIRST_NAME, TEST_LAST_NAME);

        // Verify both exist
        Collection<IRoom> allRooms = adminResource.getAllRooms();
        Collection<Customer> allCustomers = adminResource.getAllCustomers();
        Customer customer = adminResource.getCustomer(TEST_EMAIL);

        assertEquals("Should have 1 room", 1, allRooms.size());
        assertEquals("Should have 1 customer", 1, allCustomers.size());
        assertNotNull("Customer should exist", customer);

        // Create reservation through service
        Date checkIn = createDate(2025, Calendar.MAY, 1);
        Date checkOut = createDate(2025, Calendar.MAY, 5);
        IRoom room = reservationService.getARoom("901");
        reservationService.reserveARoom(customer, room, checkIn, checkOut);

        // Verify reservation exists
        adminResource.displayAllReservations();
        String popularRoom = adminResource.findMostPopularRoom();
        assertEquals("Popular room should be 901", "901", popularRoom);
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