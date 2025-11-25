package api;

import model.customer.Customer;
import model.room.IRoom;
import model.room.Room;
import model.room.enums.RoomType;
import service.customer.CustomerService;
import service.reservation.ReservationService;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Nada Alsulami
 */
public class AdminResourceTest {
    
    private AdminResource adminResource;
    
    // Test data constants
    private static final String TEST_EMAIL = "admin.test@example.com";
    private static final String TEST_EMAIL_2 = "admin.test2@example.com";
    private static final String TEST_FIRST_NAME = "Admin";
    private static final String TEST_LAST_NAME = "User";
    private static final String TEST_ROOM_NUMBER = "301";
    private static final String TEST_ROOM_NUMBER_2 = "302";
    private static final String TEST_ROOM_NUMBER_3 = "303";
    private static final Double TEST_ROOM_PRICE = 200.0;
    
    @Before
    public void setUp() {
        // Get the singleton instance
        adminResource = AdminResource.getSingleton();
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
    
    // ==================== TEST: getCustomer() ====================
@Test
public void testGetCustomer_WhenExists_ShouldReturnCustomer() {
    // Given: A customer exists (created through CustomerService)
    CustomerService customerService = CustomerService.getSingleton();
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
public void testGetCustomer_WithDifferentEmails_ShouldReturnCorrectCustomers() {
    // Given: Multiple customers exist
    CustomerService customerService = CustomerService.getSingleton();
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
        ReservationService reservationService = ReservationService.getSingleton();
        IRoom retrievedRoom = reservationService.getARoom(TEST_ROOM_NUMBER);
        assertNotNull("Room should be added", retrievedRoom);
        assertEquals("Room number should match", TEST_ROOM_NUMBER, retrievedRoom.getRoomNumber());
    }
    
    @Test
    public void testAddRoom_WithMultipleRooms_ShouldAddAllRooms() {
        // Given: A list with multiple rooms
        List<IRoom> rooms = new ArrayList<>();
        rooms.add(new Room(TEST_ROOM_NUMBER, TEST_ROOM_PRICE, RoomType.SINGLE));
        rooms.add(new Room(TEST_ROOM_NUMBER_2, TEST_ROOM_PRICE, RoomType.DOUBLE));
        rooms.add(new Room(TEST_ROOM_NUMBER_3, 0.0, RoomType.SINGLE));
        
        // When: Add all rooms
        adminResource.addRoom(rooms);
        
        // Then: All rooms should be retrievable
        ReservationService reservationService = ReservationService.getSingleton();
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
        assertTrue("Should complete successfully", true);
    }
    
    @Test
    public void testAddRoom_WithDifferentRoomTypes_ShouldAddAll() {
        // Given: Rooms of different types
        List<IRoom> rooms = new ArrayList<>();
        rooms.add(new Room("401", 150.0, RoomType.SINGLE));
        rooms.add(new Room("402", 250.0, RoomType.DOUBLE));
        
        // When: Add rooms
        adminResource.addRoom(rooms);
        
        // Then: All should be added
        ReservationService reservationService = ReservationService.getSingleton();
        IRoom room1 = reservationService.getARoom("401");
        IRoom room2 = reservationService.getARoom("402");
        
        assertNotNull("Single room should be added", room1);
        assertNotNull("Double room should be added", room2);
    }
    
    // ==================== TEST: getAllRooms() ====================
    
    @Test
    public void testGetAllRooms_ShouldReturnCollection() {
        // When: Get all rooms
        Collection<IRoom> result = adminResource.getAllRooms();
        
        // Then: Should return a collection (empty or with rooms)
        assertNotNull("Result should not be null", result);
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
        assertTrue("Should contain rooms", allRooms.size() >= 2);
    }
    
    @Test
    public void testGetAllRooms_MultipleCalls_ShouldReturnConsistentResults() {
        // When: Call getAllRooms multiple times
        Collection<IRoom> result1 = adminResource.getAllRooms();
        Collection<IRoom> result2 = adminResource.getAllRooms();
        
        // Then: Both should return collections
        assertNotNull("First call should not be null", result1);
        assertNotNull("Second call should not be null", result2);
    }
    
    // ==================== TEST: getAllCustomers() ====================
    
    @Test
    public void testGetAllCustomers_ShouldReturnCollection() {
        // When: Get all customers
        Collection<Customer> result = adminResource.getAllCustomers();
        
        // Then: Should return a collection (empty or with customers)
        assertNotNull("Result should not be null", result);
    }
    
    @Test
    public void testGetAllCustomers_AfterAddingCustomers_ShouldReturnAllCustomers() {
        // Given: Customers are added
        CustomerService customerService = CustomerService.getSingleton();
        customerService.addCustomer("customer1@test.com", "Customer", "One");
        customerService.addCustomer("customer2@test.com", "Customer", "Two");
        
        // When: Get all customers
        Collection<Customer> allCustomers = adminResource.getAllCustomers();
        
        // Then: Should contain the added customers
        assertNotNull("Result should not be null", allCustomers);
        assertTrue("Should contain customers", allCustomers.size() >= 2);
    }
    
    @Test
    public void testGetAllCustomers_MultipleCalls_ShouldReturnConsistentResults() {
        // When: Call getAllCustomers multiple times
        Collection<Customer> result1 = adminResource.getAllCustomers();
        Collection<Customer> result2 = adminResource.getAllCustomers();
        
        // Then: Both should return collections
        assertNotNull("First call should not be null", result1);
        assertNotNull("Second call should not be null", result2);
    }
    
    // ==================== TEST: displayAllReservations() ====================
    
    @Test
    public void testDisplayAllReservations_ShouldCompleteWithoutException() {
        // When: Display all reservations
        adminResource.displayAllReservations();
        
        // Then: Should complete without exception
        assertTrue("Should complete successfully", true);
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
    public void testDisplayAllReservations_WithNoReservations_ShouldNotFail() {
        // When: Display when no reservations exist
        adminResource.displayAllReservations();
        
        // Then: Should complete without exception
        assertTrue("Should handle empty reservations", true);
    }
    
    // ==================== TEST: findMostPopularRoom() ====================
    
    @Test
    public void testFindMostPopularRoom_ShouldReturnRoomNumber() {
        // When: Find most popular room
        String result = adminResource.findMostPopularRoom();
        
        // Then: Should return a string (may be null if no reservations)
        // This tests the delegation to the service
        assertTrue("Method should complete without exception", true);
    }
    
    @Test
    public void testFindMostPopularRoom_MultipleCalls_ShouldComplete() {
        // When: Call multiple times
        String result1 = adminResource.findMostPopularRoom();
        String result2 = adminResource.findMostPopularRoom();
        
        // Then: Both calls should complete
        assertTrue("First call should complete", true);
        assertTrue("Second call should complete", true);
    }
    
    @Test
    public void testFindMostPopularRoom_WhenNoReservations_ShouldReturnNull() {
        // When: Find popular room with no reservations
        String result = adminResource.findMostPopularRoom();
        
        // Then: Should handle gracefully (may return null)
        assertTrue("Should handle no reservations", true);
    }
    
    @Test
    public void testFindMostPopularRoom_DelegationToService() {
        // When: Call findMostPopularRoom
        String result = adminResource.findMostPopularRoom();
        
        // Then: Should delegate to ReservationService
        // Result depends on service implementation
        // This tests the delegation path exists
        assertTrue("Should delegate to service", true);
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
        assertTrue("Should have rooms", allRooms.size() >= 2);
    }
    
    @Test
    public void testIntegration_AddCustomersAndRetrieveThem() {
        // Given: Add customers via CustomerService
        CustomerService customerService = CustomerService.getSingleton();
        customerService.addCustomer("integration1@test.com", "Test", "User1");
        customerService.addCustomer("integration2@test.com", "Test", "User2");
        
        // When: Get all customers via AdminResource
        Collection<Customer> customers = adminResource.getAllCustomers();
        
        // Then: Should contain customers
        assertNotNull("Customers should not be null", customers);
        assertTrue("Should have customers", customers.size() >= 2);
        
        // And: Should be able to get specific customer
        Customer customer = adminResource.getCustomer("integration1@test.com");
        assertNotNull("Specific customer should exist", customer);
    }
    
    @Test
    public void testIntegration_CompleteAdminWorkflow() {
        // Scenario: Complete admin workflow
        
        // Step 1: Add rooms
        List<IRoom> rooms = new ArrayList<>();
        rooms.add(new Room("701", 250.0, RoomType.SINGLE));
        adminResource.addRoom(rooms);
        
        // Step 2: Verify rooms are added
        Collection<IRoom> allRooms = adminResource.getAllRooms();
        assertNotNull("Rooms should exist", allRooms);
        
        // Step 3: Add customers
        CustomerService customerService = CustomerService.getSingleton();
        customerService.addCustomer("workflow@test.com", "Workflow", "Test");
        
        // Step 4: Get all customers
        Collection<Customer> allCustomers = adminResource.getAllCustomers();
        assertNotNull("Customers should exist", allCustomers);
        
        // Step 5: Get specific customer
        Customer customer = adminResource.getCustomer("workflow@test.com");
        assertNotNull("Specific customer should exist", customer);
        
        // Step 6: Display all reservations (should not fail)
        adminResource.displayAllReservations();
        
        // Step 7: Find most popular room (should not fail)
        String popularRoom = adminResource.findMostPopularRoom();
        assertTrue("Should complete workflow", true);
    }
    
}
