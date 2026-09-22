package model;

import exceptions.OutOfStockException;
import exceptions.ValidationException;
import interfaces.Identifiable;
import interfaces.Persistable;
import interfaces.Searchable;

/**
 * Medicine is not a Person, but it is still stored, searched and saved
 * the same way. It gets those abilities from interfaces instead of from
 * a common base class.
 */
public class Medicine implements Identifiable, Searchable, Persistable {

    private final int id;
    private String name;
    private String manufacturer;
    private int quantity;
    private double price;

    public Medicine(
            int id,
            String name,
            String manufacturer,
            int quantity,
            double price) {

        this.id = id;
        this.name = name;
        this.manufacturer = manufacturer;
        this.quantity = quantity;
        this.price = price;
    }

    @Override
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {

        if (price < 0) {
            throw new ValidationException("Price cannot be negative");
        }

        this.price = price;
    }

    public boolean isAvailable() {
        return quantity > 0;
    }

    /**
     * Business rule that belongs to the object itself: nobody outside
     * can set quantity directly, so the stock can never go negative.
     */
    public void reduceStock(int amount) {

        if (amount <= 0) {
            throw new ValidationException("Quantity must be at least 1");
        }

        if (amount > quantity) {
            throw new OutOfStockException(name, quantity);
        }

        quantity -= amount;
    }

    public void addStock(int amount) {

        if (amount <= 0) {
            throw new ValidationException("Quantity must be at least 1");
        }

        quantity += amount;
    }

    @Override
    public boolean matches(String keyword) {

        if (keyword == null || keyword.isBlank()) {
            return true;
        }

        String needle = keyword.toLowerCase();

        return name.toLowerCase().contains(needle)
                || manufacturer.toLowerCase().contains(needle);
    }

    @Override
    public String toRecord() {

        return id + Person.SEP
                + name + Person.SEP
                + manufacturer + Person.SEP
                + quantity + Person.SEP
                + price;
    }

    @Override
    public String toString() {
        return "#" + id + " " + name + " (" + quantity + " in stock)";
    }
}
