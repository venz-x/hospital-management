package ui;

import model.Medicine;
import service.MedicineService;

import javax.swing.JButton;
import javax.swing.JTextField;
import java.util.ArrayList;
import java.util.List;

/**
 * The Pharmacy tab. It adds one button of its own, because stock is
 * never typed over: it is only ever increased through addStock().
 */
public class MedicinePanel extends EntityPanel<Medicine> {

    private final MedicineService medicineService;

    public MedicinePanel(MedicineService medicineService) {

        super(medicineService,
                "Pharmacy",
                "Medicine stock. Prescribing a medicine reduces this stock automatically");

        this.medicineService = medicineService;

        refresh();
    }

    @Override
    protected String[] columnNames() {
        return new String[] {
                "ID", "Name", "Manufacturer", "Quantity", "Price (BDT)", "Status"
        };
    }

    @Override
    protected Object[] toRow(Medicine medicine) {

        String status = !medicine.isAvailable()
                ? "Out of stock"
                : medicine.getQuantity() < 10 ? "Low stock" : "Available";

        return new Object[] {
                medicine.getId(),
                medicine.getName(),
                medicine.getManufacturer(),
                medicine.getQuantity(),
                String.format("%.2f", medicine.getPrice()),
                status
        };
    }

    @Override
    protected String describe(Medicine medicine) {
        return "medicine " + medicine.getName();
    }

    @Override
    protected List<JButton> extraButtons() {

        List<JButton> buttons = new ArrayList<>();

        JButton addStock = UiTheme.button("Add stock", UiTheme.PRIMARY_DARK);
        addStock.addActionListener(event -> openAddStockDialog());

        buttons.add(addStock);

        return buttons;
    }

    @Override
    protected void openCreateDialog() {

        FormPanel form = new FormPanel();

        JTextField name = form.addText("Name", "");
        JTextField manufacturer = form.addText("Manufacturer", "");
        JTextField quantity = form.addText("Quantity", "0");
        JTextField price = form.addText("Price (BDT)", "0.00");

        if (!form.showDialog(this, "Add medicine")) {
            return;
        }

        Medicine created = medicineService.createMedicine(
                name.getText(),
                manufacturer.getText(),
                quantity.getText(),
                price.getText());

        refresh();
        info("Medicine " + created.getName()
                + " was added with id " + created.getId() + ".");
    }

    @Override
    protected void openEditDialog(Medicine medicine) {

        FormPanel form = new FormPanel();

        form.addReadOnly("Medicine id", String.valueOf(medicine.getId()));
        form.addReadOnly("Current stock",
                medicine.getQuantity() + " unit(s)  (use \"Add stock\" to change)");

        JTextField name = form.addText("Name", medicine.getName());
        JTextField manufacturer =
                form.addText("Manufacturer", medicine.getManufacturer());
        JTextField price = form.addText(
                "Price (BDT)", String.format("%.2f", medicine.getPrice()));

        if (!form.showDialog(this, "Edit medicine")) {
            return;
        }

        medicineService.updateMedicine(
                medicine.getId(),
                name.getText(),
                manufacturer.getText(),
                price.getText());

        refresh();
        info("Medicine " + medicine.getName() + " was updated.");
    }

    @Override
    protected void deleteItem(Medicine medicine) {
        medicineService.deleteMedicine(medicine.getId());
    }

    private void openAddStockDialog() {

        Medicine medicine = requireSelected();

        if (medicine == null) {
            return;
        }

        runSafely(() -> {

            FormPanel form = new FormPanel();

            form.addReadOnly("Medicine", medicine.getName());
            form.addReadOnly("Current stock",
                    String.valueOf(medicine.getQuantity()));

            JTextField amount = form.addText("Units to add", "10");

            if (!form.showDialog(this, "Add stock")) {
                return;
            }

            Medicine updated =
                    medicineService.addStock(medicine.getId(), amount.getText());

            refresh();
            info(updated.getName() + " now has "
                    + updated.getQuantity() + " unit(s) in stock.");
        });
    }
}
