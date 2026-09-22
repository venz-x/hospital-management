# Hospital Management System — Java Swing

An OOP course project. Pure Java: **Swing** for the GUI, **plain text files** for
storage, **custom exceptions** for the business rules. No database, no
framework, no external library.

## Running it

Requires JDK 17 or newer (JDK 21 recommended).

**IntelliJ IDEA** — `File > Open` and pick this folder. `src` is already
marked as the sources root and a run configuration named `Main` is included,
so you can press Run straight away. If the run configuration does not appear,
right-click `src/Main.java` and choose *Run 'Main.main()'*.

**Command line**

```bash
./run.sh          # Linux / macOS
run.bat           # Windows
```

Or by hand, from the project root:

```bash
javac -d out $(find src -name "*.java")
java -cp out Main
```

The first time it runs, a few sample doctors, nurses, patients and medicines
are created so the tables are not empty. After that the program always starts
with whatever is in the `data` folder.

To start somewhere else (useful while testing), pass a folder name:
`java -cp out Main demo-data`.

## What the application does

Five tabs:

| Tab | What you can do |
| --- | --- |
| **Dashboard** | Live counters, a full text report, export the report to a `.txt` file, reload the data files |
| **Doctors** | Add / edit / delete / search doctors |
| **Nurses** | Add / edit / delete / search nurses |
| **Patients** | Add / edit / delete / search patients, assign a nurse, prescribe medicine, return medicine, discharge |
| **Pharmacy** | Add / edit / delete / search medicines, add stock |

Every panel has a search box that filters as you type, sortable columns
(click a header), and double-click on a row to edit it.

Business rules enforced by the model, not by the GUI:

* prescribing a medicine reduces the pharmacy stock, and is refused with an
  `OutOfStockException` if there are not enough units
* returning a medicine, deleting the patient, or deleting the medicine puts
  the unused units back into stock
* deleting a nurse detaches her from every patient she was looking after
* discharging a patient releases the nurse and sets the status to *Discharged*
* stock can never be typed over; it only moves through `addStock()` and
  `reduceStock()`

## Folder structure

```
HospitalManagementSystem/
├── run.sh / run.bat
├── src/
│   ├── Main.java          entry point, look and feel, first-run sample data
│   ├── enums/             Gender, DepartmentType, Shift, BloodGroup, PatientStatus
│   ├── exceptions/        HospitalException + 4 subclasses
│   ├── interfaces/        Identifiable, Searchable, Persistable
│   ├── model/             Person, Doctor, Nurse, Patient, Medicine,
│   │                      Repository<T>, Hospital
│   ├── service/           BaseService<T>, DoctorService, NurseService,
│   │                      PatientService, MedicineService, ReportService,
│   │                      SampleData
│   ├── storage/           DataStore  (all file reading and writing)
│   ├── util/              Validator  (static input rules)
│   └── ui/                MainFrame, EntityPanel<T>, DashboardPanel,
│                          DoctorPanel, NursePanel, PatientPanel,
│                          MedicinePanel, FormPanel, UiTheme, Refreshable
└── data/                  created on first run (see below)
```

## How a click travels through the layers

```
User clicks "Add" on the Doctors tab
   ↓
DoctorPanel          builds the form, reads the text fields
   ↓
DoctorService        validates through Validator, builds new Doctor(...)
   ↓
Hospital             stores it in Repository<Doctor>
   ↓
DataStore            writes every record back to data/doctors.txt
   ↓
DoctorPanel.refresh()  redraws the JTable
```

If anything goes wrong on the way down — an empty name, a bad phone number, a
missing id, not enough stock, an unwritable file — a `HospitalException`
subclass is thrown. `EntityPanel.runSafely(...)` catches it in **one**
try-catch and shows a `JOptionPane` with the right heading, because the
exception carries its own title.

## Data files

Records are saved as pipe separated lines in the `data` folder, immediately
after every add, edit and delete (and again when you close the window).

```
data/doctors.txt     id|name|phone|gender|specialization|department
data/nurses.txt      id|name|phone|gender|shift|qualification
data/medicines.txt   id|name|manufacturer|quantity|price
data/patients.txt    id|name|phone|gender|disease|bloodGroup|status|nurseId|medicines
```

A patient's medicines live in the last column as `4001:6;4003:2`
(medicine id : quantity). The first line of each file is a `#` comment
describing the columns, and is skipped when reading.

Because `|` is the separator, `Validator` refuses any text field containing
it. A line that cannot be parsed is **skipped, not fatal**: the loader collects
a warning for it and the application starts anyway, then shows the list of
skipped lines in a dialog.

Ids start at 1001 (doctors), 2001 (nurses), 3001 (patients) and 4001
(medicines), so you can tell from an id what it refers to. After loading, each
`Repository` continues counting above the highest id it read.

## Where each OOP concept lives

| Concept | Where to point during the viva |
| --- | --- |
| **Encapsulation** | every model class: private fields, access through getters/setters. `Medicine.quantity` has no setter at all — it only changes through `reduceStock()` / `addStock()`, so the stock can never go negative |
| **Abstraction** | `Person` is abstract with abstract `getRole()` and `extraFields()`. `BaseService<T>` and `EntityPanel<T>` are abstract too: they define *what* happens, subclasses define *what with* |
| **Inheritance** | `Doctor`, `Nurse`, `Patient` extend `Person`; the four services extend `BaseService`; the four tabs extend `EntityPanel` |
| **Polymorphism** | `DataStore.writeAll(Path, List<? extends Persistable>, String)` saves all four entity types through one method. `runSafely()` handles every `HospitalException` subclass without a single `instanceof` |
| **Method overriding** | `getRole()`, `matches()` (subclasses call `super.matches(...)` first), `toRecord()` via `extraFields()`, `toString()`, and every abstract method of `EntityPanel` |
| **Template method** | `Person.toRecord()` writes the common columns once and asks the subclass for its own |
| **Interfaces** | `Identifiable`, `Searchable`, `Persistable`. `Medicine` gets all three without extending `Person` |
| **Generics** | `Repository<T extends Identifiable>`, `BaseService<T extends Identifiable & Searchable>`, `EntityPanel<T>`, `Validator.requireEnum(..., Class<T>, ...)` |
| **Enums** | 5 enums, each with a constructor, a private field, `getLabel()` and an overridden `toString()` so they drop straight into a `JComboBox`. `PatientStatus.needsBed()` adds behaviour |
| **Collections** | `ArrayList` in `Repository`, `LinkedHashMap<Medicine, Integer>` for a patient's chart, streams for search and for the dashboard counters |
| **Object relationships** | `Patient` → `Nurse` (association), `Patient` → `Medicine` with a quantity, `Hospital` → four `Repository` objects (composition) |
| **Custom exceptions** | `HospitalException` carries a dialog title; `ValidationException`, `NotFoundException`, `OutOfStockException`, `DataAccessException` extend it |
| **try-catch** | `DataStore` catches the checked `IOException` and rethrows it as `DataAccessException`; `EntityPanel.runSafely()` catches everything the GUI triggers; `Main` catches a broken data file at startup; `MainFrame.confirmExit()` still offers to exit if the final save fails |
| **File handling** | `DataStore` with `Files.newBufferedReader` / `newBufferedWriter` inside try-with-resources; `ReportService.exportReport(File)` for the report |
| **Static utilities** | `Validator`, `SampleData` — final classes with a private constructor |

## For the report (task 4)

Screenshots worth taking: the dashboard with its counters, the Doctors tab,
an add dialog, a validation error box (type a one-letter name), the
out-of-stock error (prescribe more units than the pharmacy has), the exported
report file, and `data/patients.txt` opened in a text editor to show the
persistence format.

Challenges worth writing about: keeping the stock correct when a prescription
is saved and then loaded again (the saved stock already has the units
subtracted, so the loader restores the chart **without** calling
`reduceStock()` a second time), keeping objects linked after a reload when a
file only stores ids, and reducing four nearly identical CRUD screens to one
generic `EntityPanel<T>`.
