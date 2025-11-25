package api;

import model.customer.Customer;
import model.room.IRoom;
import model.room.Room;
import model.room.enums.RoomType;
import service.customer.CustomerService;
import service.reservation.ReservationService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Component Test for AdminResource using Mockito
 * @author Nada Alsulami
 */
@RunWith(MockitoJUnitRunner.class)
public class AdminResourceComponentTest {
    
    @Mock
    private CustomerService mockCustomerService;
    
    @Mock
    private ReservationService mockReservationService;
    
    @Mock
    private Customer mockCustomer;
    
    @Mock
    private IRoom mockRoom;
    
    @Captor
    private ArgumentCaptor<IRoom> roomCaptor;
    
    private AdminResource adminResource;
    
    // Test data constants
    private static final String TEST_EMAIL = "admin@example.com";
    private static final String TEST_EMAIL_2 = "admin2@example.com";
    private static final String TEST_FIRST_NAME = "Admin";
    private static final String TEST_LAST_NAME = "User";
    private static final String TEST_ROOM_NUMBER = "301";
    private static final String TEST_ROOM_NUMBER_2 = "302";
    private static final String POPULAR_ROOM_NUMBER = "101";
    private static final Double TEST_ROOM_PRICE = 200.0;
    
    @Before
    public void setUp() throws Exception {
        // Get singleton instance
        adminResource = AdminResource.getSingleton();
        
        // Inject mocks into the singleton
        injectMockService("customerService", mockCustomerService);
        injectMockService("reservationService", mockReservationService);
        
        // Setup default mock behaviors
        setupDefaultMockBehaviors();
    }
    
    /**
     * Helper method to inject mock services using reflection
     */
    private void injectMockService(String fieldName, Object mockService) throws Exception {
        Field field = AdminResource.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(adminResource, mockService);
    }
    
    /**
     * Setup default behaviors for mocks
     */
    private void setupDefaultMockBehaviors() {
        // Default: customer has valid email
        when(mockCustomer.getEmail()).thenReturn(TEST_EMAIL);
        
        // Default: room has valid room number
        when(mockRoom.getRoomNumber()).thenReturn(TEST_ROOM_NUMBER);
    }
    
    // ==================== TEST: getSingleton() ====================
    
    @Test
    public void testGetSingleton_ShouldReturnSameInstance() {
        // When: Call getSingleton twice
        AdminResource instance1 = AdminResource.getSingleton();
        AdminResource instance2 = AdminResource.getSingleton();
        
        // Then: Should return same instance
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
        // Given: Customer exists in service
        when(mockCustomerService.getCustomer(TEST_EMAIL)).thenReturn(mockCustomer);
        
        // When: Get customer
        Customer result = adminResource.getCustomer(TEST_EMAIL);
        
        // Then: Should return the customer
        assertNotNull("Customer should not be null", result);
        assertSame("Should return mock customer", mockCustomer, result);
        
        // And: Should have called service exactly once
        verify(mockCustomerService, times(1)).getCustomer(TEST_EMAIL);
    }
    
    @Test
    public void testGetCustomer_WhenNotExists_ShouldReturnNull() {
        // Given: Customer does not exist
        when(mockCustomerService.getCustomer(TEST_EMAIL)).thenReturn(null);
        
        // When: Get customer
        Customer result = adminResource.getCustomer(TEST_EMAIL);
        
        // Then: Should return null
        assertNull("Customer should be null", result);
        
        // And: Should have called service
        verify(mockCustomerService, times(1)).getCustomer(TEST_EMAIL);
    }
    
    @Test
    public void testGetCustomer_WithDifferentEmails_ShouldCallServiceWithCorrectEmail() {
        // Given: Different customers
        Customer mockCustomer2 = mock(Customer.class);
        when(mockCustomerService.getCustomer(TEST_EMAIL)).thenReturn(mockCustomer);
        when(mockCustomerService.getCustomer(TEST_EMAIL_2)).thenReturn(mockCustomer2);
        
        // When: Get both customers
        Customer customer1 = adminResource.getCustomer(TEST_EMAIL);
        Customer customer2 = adminResource.getCustomer(TEST_EMAIL_2);
        
        // Then: Should return correct customers
        assertSame("First customer should match", mockCustomer, customer1);
        assertSame("Second customer should match", mockCustomer2, customer2);
        
        // And: Should call service for each email
        verify(mockCustomerService, times(1)).getCustomer(TEST_EMAIL);
        verify(mockCustomerService, times(1)).getCustomer(TEST_EMAIL_2);
    }
    
    @Test
    public void testGetCustomer_VerifyParameterPassed() {
        // Given: Customer exists
        when(mockCustomerService.getCustomer(TEST_EMAIL)).thenReturn(mockCustomer);
        
        // When: Get customer
        adminResource.getCustomer(TEST_EMAIL);
        
        // Then: Should pass exact email to service
        verify(mockCustomerService).getCustomer(eq(TEST_EMAIL));
    }
    
    // ==================== TEST: addRoom() ====================
    
    @Test
    public void testAddRoom_WithSingleRoom_ShouldCallServiceOnce() {
        // Given: List with one room
        List<IRoom> rooms = new ArrayList<>();
        rooms.add(mockRoom);
        
        // When: Add rooms
        adminResource.addRoom(rooms);
        
        // Then: Should call service for the room
        verify(mockReservationService, times(1)).addRoom(mockRoom);
    }
    
    @Test
    public void testAddRoom_WithMultipleRooms_ShouldCallServiceForEachRoom() {
        // Given: List with multiple rooms
        IRoom room1 = mock(IRoom.class);
        IRoom room2 = mock(IRoom.class);
        IRoom room3 = mock(IRoom.class);
        
        List<IRoom> rooms = new ArrayList<>();
        rooms.add(room1);
        rooms.add(room2);
        rooms.add(room3);
        
        // When: Add rooms
        adminResource.addRoom(rooms);
        
        // Then: Should call service for each room
        verify(mockReservationService, times(1)).addRoom(room1);
        verify(mockReservationService, times(1)).addRoom(room2);
        verify(mockReservationService, times(1)).addRoom(room3);
        verify(mockReservationService, times(3)).addRoom(any(IRoom.class));
    }
    
    @Test
    public void testAddRoom_WithEmptyList_ShouldNotCallService() {
        // Given: Empty list
        List<IRoom> rooms = new ArrayList<>();
        
        // When: Add empty list
        adminResource.addRoom(rooms);
        
        // Then: Should not call service at all
        verify(mockReservationService, never()).addRoom(any(IRoom.class));
    }
    
    @Test
    public void testAddRoom_VerifyCorrectRoomsPassedToService() {
        // Given: Specific rooms
        IRoom room1 = new Room(TEST_ROOM_NUMBER, TEST_ROOM_PRICE, RoomType.SINGLE);
        IRoom room2 = new Room(TEST_ROOM_NUMBER_2, TEST_ROOM_PRICE, RoomType.DOUBLE);
        
        List<IRoom> rooms = new ArrayList<>();
        rooms.add(room1);
        rooms.add(room2);
        
        // When: Add rooms
        adminResource.addRoom(rooms);
        
        // Then: Should pass exact rooms to service
        verify(mockReservationService).addRoom(eq(room1));
        verify(mockReservationService).addRoom(eq(room2));
    }
    
    @Test
    public void testAddRoom_WithLargeList_ShouldCallServiceMultipleTimes() {
        // Given: Large list of rooms
        List<IRoom> rooms = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            rooms.add(mock(IRoom.class));
        }
        
        // When: Add rooms
        adminResource.addRoom(rooms);
        
        // Then: Should call service 10 times
        verify(mockReservationService, times(10)).addRoom(any(IRoom.class));
    }
    
    @Test
    public void testAddRoom_UsingArgumentCaptor_VerifyAllRoomsAdded() {
        // Given: Multiple rooms
        IRoom room1 = mock(IRoom.class);
        IRoom room2 = mock(IRoom.class);
        
        List<IRoom> rooms = new ArrayList<>();
        rooms.add(room1);
        rooms.add(room2);
        
        // When: Add rooms
        adminResource.addRoom(rooms);
        
        // Then: Capture and verify all rooms
        verify(mockReservationService, times(2)).addRoom(roomCaptor.capture());
        List<IRoom> capturedRooms = roomCaptor.getAllValues();
        
        assertEquals("Should capture 2 rooms", 2, capturedRooms.size());
        assertTrue("Should contain room1", capturedRooms.contains(room1));
        assertTrue("Should contain room2", capturedRooms.contains(room2));
    }
    
    // ==================== TEST: getAllRooms() ====================
    
    @Test
    public void testGetAllRooms_ShouldDelegateToService() {
        // Given: Service has rooms
        Collection<IRoom> expectedRooms = new ArrayList<>();
        expectedRooms.add(mockRoom);
        
        when(mockReservationService.getAllRooms()).thenReturn(expectedRooms);
        
        // When: Get all rooms
        Collection<IRoom> result = adminResource.getAllRooms();
        
        // Then: Should return rooms from service
        assertNotNull("Result should not be null", result);
        assertEquals("Should have 1 room", 1, result.size());
        assertTrue("Should contain mock room", result.contains(mockRoom));
        
        // And: Should call service exactly once
        verify(mockReservationService, times(1)).getAllRooms();
    }
    
    @Test
    public void testGetAllRooms_WhenNoRooms_ShouldReturnEmptyCollection() {
        // Given: Service has no rooms
        when(mockReservationService.getAllRooms()).thenReturn(Collections.emptyList());
        
        // When: Get all rooms
        Collection<IRoom> result = adminResource.getAllRooms();
        
        // Then: Should return empty collection
        assertNotNull("Result should not be null", result);
        assertTrue("Result should be empty", result.isEmpty());
        
        // And: Should have called service
        verify(mockReservationService, times(1)).getAllRooms();
    }
    
    @Test
    public void testGetAllRooms_MultipleCalls_ShouldCallServiceEachTime() {
        // Given: Service returns rooms
        Collection<IRoom> rooms = new ArrayList<>();
        rooms.add(mockRoom);
        when(mockReservationService.getAllRooms()).thenReturn(rooms);
        
        // When: Call multiple times
        adminResource.getAllRooms();
        adminResource.getAllRooms();
        adminResource.getAllRooms();
        
        // Then: Should call service 3 times
        verify(mockReservationService, times(3)).getAllRooms();
    }
    
    @Test
    public void testGetAllRooms_WithMultipleRooms_ShouldReturnAll() {
        // Given: Service has multiple rooms
        IRoom room1 = mock(IRoom.class);
        IRoom room2 = mock(IRoom.class);
        IRoom room3 = mock(IRoom.class);
        
        Collection<IRoom> expectedRooms = new ArrayList<>();
        expectedRooms.add(room1);
        expectedRooms.add(room2);
        expectedRooms.add(room3);
        
        when(mockReservationService.getAllRooms()).thenReturn(expectedRooms);
        
        // When: Get all rooms
        Collection<IRoom> result = adminResource.getAllRooms();
        
        // Then: Should return all rooms
        assertNotNull("Result should not be null", result);
        assertEquals("Should have 3 rooms", 3, result.size());
        assertTrue("Should contain all rooms", result.containsAll(expectedRooms));
    }
    
    // ==================== TEST: getAllCustomers() ====================
    
    @Test
    public void testGetAllCustomers_ShouldDelegateToService() {
        // Given: Service has customers
        Collection<Customer> expectedCustomers = new ArrayList<>();
        expectedCustomers.add(mockCustomer);
        
        when(mockCustomerService.getAllCustomers()).thenReturn(expectedCustomers);
        
        // When: Get all customers
        Collection<Customer> result = adminResource.getAllCustomers();
        
        // Then: Should return customers from service
        assertNotNull("Result should not be null", result);
        assertEquals("Should have 1 customer", 1, result.size());
        assertTrue("Should contain mock customer", result.contains(mockCustomer));
        
        // And: Should call service exactly once
        verify(mockCustomerService, times(1)).getAllCustomers();
    }
    
    @Test
    public void testGetAllCustomers_WhenNoCustomers_ShouldReturnEmptyCollection() {
        // Given: Service has no customers
        when(mockCustomerService.getAllCustomers()).thenReturn(Collections.emptyList());
        
        // When: Get all customers
        Collection<Customer> result = adminResource.getAllCustomers();
        
        // Then: Should return empty collection
        assertNotNull("Result should not be null", result);
        assertTrue("Result should be empty", result.isEmpty());
        
        // And: Should have called service
        verify(mockCustomerService, times(1)).getAllCustomers();
    }
    
    @Test
    public void testGetAllCustomers_MultipleCalls_ShouldCallServiceEachTime() {
        // Given: Service returns customers
        Collection<Customer> customers = new ArrayList<>();
        customers.add(mockCustomer);
        when(mockCustomerService.getAllCustomers()).thenReturn(customers);
        
        // When: Call multiple times
        adminResource.getAllCustomers();
        adminResource.getAllCustomers();
        
        // Then: Should call service 2 times
        verify(mockCustomerService, times(2)).getAllCustomers();
    }
    
    @Test
    public void testGetAllCustomers_WithMultipleCustomers_ShouldReturnAll() {
        // Given: Service has multiple customers
        Customer customer1 = mock(Customer.class);
        Customer customer2 = mock(Customer.class);
        Customer customer3 = mock(Customer.class);
        
        Collection<Customer> expectedCustomers = new ArrayList<>();
        expectedCustomers.add(customer1);
        expectedCustomers.add(customer2);
        expectedCustomers.add(customer3);
        
        when(mockCustomerService.getAllCustomers()).thenReturn(expectedCustomers);
        
        // When: Get all customers
        Collection<Customer> result = adminResource.getAllCustomers();
        
        // Then: Should return all customers
        assertNotNull("Result should not be null", result);
        assertEquals("Should have 3 customers", 3, result.size());
        assertTrue("Should contain all customers", result.containsAll(expectedCustomers));
    }
    
    // ==================== TEST: displayAllReservations() ====================
    
    @Test
    public void testDisplayAllReservations_ShouldCallService() {
        // When: Display all reservations
        adminResource.displayAllReservations();
        
        // Then: Should call service exactly once
        verify(mockReservationService, times(1)).printAllReservation();
    }
    
    @Test
    public void testDisplayAllReservations_MultipleCalls_ShouldCallServiceEachTime() {
        // When: Call multiple times
        adminResource.displayAllReservations();
        adminResource.displayAllReservations();
        adminResource.displayAllReservations();
        
        // Then: Should call service 3 times
        verify(mockReservationService, times(3)).printAllReservation();
    }
    

    @Test
    public void testDisplayAllReservations_VerifyOnlyPrintAllReservationCalled() {
        // When: Display reservations
        adminResource.displayAllReservations();
        
        // Then: Should only call printAllReservation, no other methods
        verify(mockReservationService, times(1)).printAllReservation();
        verify(mockReservationService, never()).getAllRooms();
        verify(mockReservationService, never()).getARoom(anyString());
    }
    
    // ==================== TEST: findMostPopularRoom() ====================
    
    @Test
    public void testFindMostPopularRoom_ShouldReturnRoomNumber() {
        // Given: Service returns popular room
        when(mockReservationService.findMostPopularRoom()).thenReturn(POPULAR_ROOM_NUMBER);
        
        // When: Find most popular room
        String result = adminResource.findMostPopularRoom();
        
        // Then: Should return the room number
        assertNotNull("Result should not be null", result);
        assertEquals("Should return popular room number", POPULAR_ROOM_NUMBER, result);
        
        // And: Should call service exactly once
        verify(mockReservationService, times(1)).findMostPopularRoom();
    }
    
    @Test
    public void testFindMostPopularRoom_WhenNoReservations_ShouldReturnNull() {
        // Given: Service returns null (no reservations)
        when(mockReservationService.findMostPopularRoom()).thenReturn(null);
        
        // When: Find most popular room
        String result = adminResource.findMostPopularRoom();
        
        // Then: Should return null
        assertNull("Result should be null when no reservations", result);
        
        // And: Should call service
        verify(mockReservationService, times(1)).findMostPopularRoom();
    }
    
    @Test
    public void testFindMostPopularRoom_MultipleCalls_ShouldCallServiceEachTime() {
        // Given: Service returns room number
        when(mockReservationService.findMostPopularRoom()).thenReturn(POPULAR_ROOM_NUMBER);
        
        // When: Call multiple times
        adminResource.findMostPopularRoom();
        adminResource.findMostPopularRoom();
        
        // Then: Should call service 2 times
        verify(mockReservationService, times(2)).findMostPopularRoom();
    }
    
    
    @Test
    public void testFindMostPopularRoom_WithDifferentRoomNumbers() {
        // Given: Different room numbers over time
        when(mockReservationService.findMostPopularRoom())
            .thenReturn("101")
            .thenReturn("202")
            .thenReturn("303");
        
        // When: Call multiple times
        String room1 = adminResource.findMostPopularRoom();
        String room2 = adminResource.findMostPopularRoom();
        String room3 = adminResource.findMostPopularRoom();
        
        // Then: Should return different room numbers
        assertEquals("First call should return 101", "101", room1);
        assertEquals("Second call should return 202", "202", room2);
        assertEquals("Third call should return 303", "303", room3);
    }
    
    // ==================== COMPONENT TEST: Integration Scenarios ====================
    
    @Test
    public void testComponent_CompleteAdminWorkflow() {
        // Scenario: Complete admin workflow with all operations
        
        // Step 1: Add rooms
        List<IRoom> rooms = new ArrayList<>();
        rooms.add(mockRoom);
        adminResource.addRoom(rooms);
        
        // Step 2: Get all rooms
        Collection<IRoom> roomCollection = new ArrayList<>();
        roomCollection.add(mockRoom);
        when(mockReservationService.getAllRooms()).thenReturn(roomCollection);
        
        Collection<IRoom> allRooms = adminResource.getAllRooms();
        assertNotNull("Rooms should be retrieved", allRooms);
        
        // Step 3: Get customer
        when(mockCustomerService.getCustomer(TEST_EMAIL)).thenReturn(mockCustomer);
        Customer customer = adminResource.getCustomer(TEST_EMAIL);
        assertNotNull("Customer should be retrieved", customer);
        
        // Step 4: Get all customers
        Collection<Customer> customerCollection = new ArrayList<>();
        customerCollection.add(mockCustomer);
        when(mockCustomerService.getAllCustomers()).thenReturn(customerCollection);
        
        Collection<Customer> allCustomers = adminResource.getAllCustomers();
        assertNotNull("Customers should be retrieved", allCustomers);
        
        // Step 5: Display reservations
        adminResource.displayAllReservations();
        
        // Step 6: Find popular room
        when(mockReservationService.findMostPopularRoom()).thenReturn(POPULAR_ROOM_NUMBER);
        String popularRoom = adminResource.findMostPopularRoom();
        assertNotNull("Popular room should be found", popularRoom);
        
        // Verify all service calls
        verify(mockReservationService, times(1)).addRoom(mockRoom);
        verify(mockReservationService, times(1)).getAllRooms();
        verify(mockCustomerService, times(1)).getCustomer(TEST_EMAIL);
        verify(mockCustomerService, times(1)).getAllCustomers();
        verify(mockReservationService, times(1)).printAllReservation();
        verify(mockReservationService, times(1)).findMostPopularRoom();
    }
    
    
    @Test
    public void testComponent_AddRoomsAndVerifyRetrieval() {
        // Given: Add multiple rooms
        List<IRoom> roomsToAdd = new ArrayList<>();
        IRoom room1 = mock(IRoom.class);
        IRoom room2 = mock(IRoom.class);
        roomsToAdd.add(room1);
        roomsToAdd.add(room2);
        
        adminResource.addRoom(roomsToAdd);
        
        // When: Get all rooms
        Collection<IRoom> retrievedRooms = new ArrayList<>();
        retrievedRooms.add(room1);
        retrievedRooms.add(room2);
        when(mockReservationService.getAllRooms()).thenReturn(retrievedRooms);
        
        Collection<IRoom> result = adminResource.getAllRooms();
        
        // Then: Should have added and retrieved rooms
        verify(mockReservationService, times(1)).addRoom(room1);
        verify(mockReservationService, times(1)).addRoom(room2);
        verify(mockReservationService, times(1)).getAllRooms();
        
        assertEquals("Should retrieve both rooms", 2, result.size());
    }
}