import java.io.*;
import java.util.Scanner;

public class FileManager {
    public static void save(String filename, GenericLinkedList<Medicine> meds) {
        try (PrintWriter out = new PrintWriter(new FileWriter(filename))) {
            for (int i = 0; i < meds.size(); i++) {
                out.println(meds.get(i).toDataString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static GenericLinkedList<Medicine> load(String filename) {
        GenericLinkedList<Medicine> list = new GenericLinkedList<>();
        try (Scanner scanner = new Scanner(new File(filename))) {
            while (scanner.hasNextLine()) {
                list.add(Medicine.fromDataString(scanner.nextLine()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }
}
