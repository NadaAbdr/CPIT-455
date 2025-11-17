import api.AdminResource;
import java.io.PrintStream;
import java.io.PrintWriter;
import model.customer.Customer;
import model.room.IRoom;
import model.room.Room;
import model.room.enums.RoomType;

import java.util.Collection;
import java.util.Collections;
import java.util.Scanner;

/**
 * @author joseneto
 *
 */
public class AdminMenu {

    private static final AdminResource adminResource = AdminResource.getSingleton();

   public static void adminMenu() {
        final Scanner scanner = new Scanner(System.in);
         PrintWriter out = new PrintWriter(System.out, true);
        adminMenu(scanner, out);
    }

  
static void adminMenu(Scanner scanner, PrintWriter out) {  //done
    String line = "";

    printMenu(out);

    try {
        do {
            // هنا نضيف الحماية
            if (!scanner.hasNextLine()) {
                out.println("No input received. Exiting admin menu...");
                break;
            }

            line = scanner.nextLine();

            if (line.length() == 1) {
                switch (line.charAt(0)) {
                    case '1':
                        displayAllCustomers();
                        break;
                    case '2':
                        displayAllRooms();
                        break;
                    case '3':
                        displayAllReservations();
                        break;
                    case '4':
                        addRoom();
                        break;
                    case '5':
                        findMostPopularRoom();
                        break;
                    case '6':
                        MainMenu.printMainMenu();
                        break;
                    default:
                        out.println("Unknown action\n");
                        break;
                }
            } else {
                out.println("Error: Invalid action\n");
            }
        } while (line.charAt(0) != '6' || line.length() != 1);
    } catch (StringIndexOutOfBoundsException ex) {
        out.println("Empty input received. Exiting program...");
    }
}


    // عدّلتها تستقبل out عشان نقدر نتحكم في الـ output في الاختبار
     static void printMenu(PrintWriter out) {
    out.println("Admin Menu");
    out.println("1. Display all customers");
    out.println("2. Display all rooms");
    out.println("3. Display all reservations");
    out.println("4. Add a room");
    out.println("5. Find most popular room");
    out.println("6. Back to main menu");
}


    

     static void addRoom() {
        final Scanner scanner = new Scanner(System.in);

        System.out.println("Enter room number:");
        final String roomNumber = scanner.nextLine();

        System.out.println("Enter price per night:");
        final double roomPrice = enterRoomPrice(scanner);

        System.out.println("Enter room type: 1 for single bed, 2 for double bed:");
        final RoomType roomType = enterRoomType(scanner);

        final Room room = new Room(roomNumber, roomPrice, roomType);

        adminResource.addRoom(Collections.singletonList(room));
        System.out.println("Room added successfully!");

        System.out.println("Would like to add another room? Y/N");
        addAnotherRoom();
    }

     static double enterRoomPrice(final Scanner scanner) {
        try {
            return Double.parseDouble(scanner.nextLine());
        } catch (NumberFormatException exp) {
            System.out.println("Invalid room price! Please, enter a valid double number. " +
                    "Decimals should be separated by point (.)");
            return enterRoomPrice(scanner);
        }
    }

    private static RoomType enterRoomType(final Scanner scanner) {
        try {
            return RoomType.valueOfLabel(scanner.nextLine());
        } catch (IllegalArgumentException exp) {
            System.out.println("Invalid room type! Please, choose 1 for single bed or 2 for double bed:");
            return enterRoomType(scanner);
        }
    }

   // تستخدم في البرنامج
static void addAnotherRoom() {
    Scanner scanner = new Scanner(System.in);
    PrintWriter out = new PrintWriter(System.out, true);
    addAnotherRoom(scanner, out);
}

// تستخدم في الاختبار
static void addAnotherRoom(Scanner scanner, PrintWriter out) {
    String anotherRoom;

    // لو ما فيه أي إدخال
    if (!scanner.hasNextLine()) {
        out.println("No input received. Returning to menu.");
        printMenu(out);
        return;
    }

    anotherRoom = scanner.nextLine();

    // تكرار لين يصير الإدخال حرف واحد Y أو N
    while (anotherRoom.length() != 1 ||
          (anotherRoom.charAt(0) != 'Y' && anotherRoom.charAt(0) != 'N')) {

        out.println("Please enter Y (Yes) or N (No)");

        if (!scanner.hasNextLine()) {
            out.println("No input received. Returning to menu.");
            printMenu(out);
            return;
        }

        anotherRoom = scanner.nextLine();
    }

    if (anotherRoom.charAt(0) == 'Y') {
        // تكملة إضافة غرف
        addRoom();
    } else { // N
        // رجوع للقائمة
        printMenu(out);
    }
}


    private static void displayAllRooms() {
        Collection<IRoom> rooms = adminResource.getAllRooms();

        if(rooms.isEmpty()) {
            System.out.println("No rooms found.");
        } else {
            adminResource.getAllRooms().forEach(System.out::println);
        }
    }

    private static void displayAllCustomers() {
        Collection<Customer> customers = adminResource.getAllCustomers();

        if (customers.isEmpty()) {
            System.out.println("No customers found.");
        } else {
            adminResource.getAllCustomers().forEach(System.out::println);
        }
    }

    private static void displayAllReservations() {
        adminResource.displayAllReservations();
    }
    

    // Displays the most frequently booked room based on existing reservations
    private static void findMostPopularRoom() {
        final String popularRoom = adminResource.findMostPopularRoom();

        if (popularRoom == null) {
            System.out.println("No reservations found to determine the most popular room.");
        } else {
            System.out.println("The most popular room is: " + popularRoom);
        }
    }

}
