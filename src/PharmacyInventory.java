import java.util.Comparator;

public class PharmacyInventory {
    private GenericLinkedList<Medicine> medicines = new GenericLinkedList<>();

    public void addMedicine(Medicine med) {
        medicines.add(med);
    }

    public GenericLinkedList<Medicine> getMedicines() {
        return medicines;
    }

    // Може да се махне, ако не се ползва (сега сортирането е в MainFrame)
    public void sortMedicines(Comparator<Medicine> comp) {
        int n = medicines.size();
        for (int i = 0; i < n - 1; i++) {
            int minIdx = i;
            for (int j = i + 1; j < n; j++) {
                if (comp.compare(medicines.get(j), medicines.get(minIdx)) < 0) {
                    minIdx = j;
                }
            }
            Medicine temp = medicines.get(i);
            medicines.set(i, medicines.get(minIdx));
            medicines.set(minIdx, temp);
        }
    }
}
