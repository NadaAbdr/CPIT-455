/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import api.AdminResource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Scanner;
import model.room.IRoom;
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
public class AdminMenuComponentTest {
    
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
        // تنظيف الكابتشر بعد كل اختبار
        outputStreamCaptor.reset(); 
    }

    private void setInput(String data) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));
        System.setIn(inputStream);
    }
    /**
     * Test of adminMenu method, of class AdminMenu.
     */
    @Test
    public void testComponent_AddRoomThroughMenuAndVerifyPersistence() {
        
        final String roomNumber = "999";
        final String roomPrice = "250.0";
        final String roomType = "2"; // 2 for Double

      
        String inputSequence = 
            "4\r\n" + 
            roomNumber + "\r\n" + 
            roomPrice + "\r\n" + 
            roomType + "\r\n" + 
            "N\r\n" + 
            "6\r\n";

        setInput(inputSequence);

        Scanner scanner = new Scanner(System.in);
        PrintWriter out = new PrintWriter(System.out, true);
        
        AdminResource adminResource = AdminResource.getSingleton();
      
       
        AdminMenu.adminMenu(scanner, out);

       
        out.flush();
        String output = outputStreamCaptor.toString();
        
        assertTrue("Output should confirm room was added.", 
                   output.contains("Room added successfully!"));
        assertTrue("Output should confirm return to main menu.", 
                   output.contains("Back to main menu"));

        Collection<IRoom> rooms = adminResource.getAllRooms();
        
        IRoom addedRoom = rooms.stream()
                .filter(room -> room.getRoomNumber().equals(roomNumber))
                .findFirst()
                .orElse(null);

        assertNotNull("The component test failed: Room " + roomNumber + " was not found in the AdminResource.", addedRoom);
        assertEquals("The room price is incorrect.", 
                     Double.parseDouble(roomPrice), addedRoom.getRoomPrice(), 0.001);
        
     
    }
}