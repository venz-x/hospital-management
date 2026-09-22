package service;

import exceptions.NotFoundException;
import model.Hospital;
import model.Medicine;
import storage.DataStore;
import util.Validator;

import java.util.List;

public class MedicineService extends BaseService<Medicine> {

    public MedicineService(Hospital hospital, DataStore store) {
        super(hospital, store);
    }

    @Override
    public String entityName() {
        return "Medicine";
    }

    @Override
    public List<Medicine> findAll() {
        return hospital.getMedicines();
    }

    public Medicine getMedicine(int id) {
        return require(hospital.findMedicine(id), id);
    }

    public Medicine createMedicine(
            String name,
            String manufacturer,
            String quantity,
            String price) {

        Medicine medicine = new Medicine(
                hospital.nextMedicineId(),
                Validator.requireText(name, "Name"),
                Validator.requireText(manufacturer, "Manufacturer"),
                Validator.requirePositive(quantity, "Quantity"),
                Validator.requirePrice(price)
        );

        hospital.addMedicine(medicine);
        persist();

        return medicine;
    }

    /**
     * Name, manufacturer and price can be edited freely. Stock is not
     * overwritten here: it only moves through addStock / reduceStock so
     * the pharmacy count always matches what was handed out.
     */
    public Medicine updateMedicine(
            int id,
            String name,
            String manufacturer,
            String price) {

        Medicine medicine = getMedicine(id);

        String validName = Validator.requireText(name, "Name");
        String validManufacturer =
                Validator.requireText(manufacturer, "Manufacturer");
        double validPrice = Validator.requirePrice(price);

        medicine.setName(validName);
        medicine.setManufacturer(validManufacturer);
        medicine.setPrice(validPrice);

        persist();

        return medicine;
    }

    public Medicine addStock(int id, String amount) {

        Medicine medicine = getMedicine(id);

        medicine.addStock(Validator.requireInt(amount, "Quantity"));
        persist();

        return medicine;
    }

    public void deleteMedicine(int id) {

        if (!hospital.removeMedicine(id)) {
            throw new NotFoundException("Medicine", id);
        }

        persist();
    }
}
