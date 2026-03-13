package com.networkwatcher.network_watcher.repository;

import com.networkwatcher.network_watcher.model.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/*
 * ═══════════════════════════════════════════════════════════════════
 * WHAT IS A REPOSITORY?
 * ═══════════════════════════════════════════════════════════════════
 * 
 * A repository is the layer that talks to the database.
 * It's the ONLY place where SQL-like operations happen.
 * 
 * Think of it as a librarian:
 * - You ask: "Find me books by author 'Smith'"
 * - Librarian searches the database
 * - Librarian returns the books
 * 
 * Similarly:
 * - You ask: "Find me devices with IP '192.168.1.1'"
 * - Repository searches the database
 * - Repository returns the devices
 * 
 * ═══════════════════════════════════════════════════════════════════
 * WHY IS THIS AN INTERFACE NOT A CLASS?
 * ═══════════════════════════════════════════════════════════════════
 * 
 * Spring Data JPA magic: You write the METHOD SIGNATURE,
 * Spring GENERATES the implementation automatically!
 * 
 * You write:
 *   Optional<Device> findByIpAddress(String ipAddress);
 * 
 * Spring generates (behind the scenes):
 *   public Optional<Device> findByIpAddress(String ipAddress) {
 *       return entityManager
 *           .createQuery("SELECT d FROM Device d WHERE d.ipAddress = :ip")
 *           .setParameter("ip", ipAddress)
 *           .getResultStream()
 *           .findFirst();
 *   }
 * 
 * You NEVER see this code — Spring does it automatically!
 * 
 * ═══════════════════════════════════════════════════════════════════
 * ANNOTATIONS EXPLAINED
 * ═══════════════════════════════════════════════════════════════════
 * 
 * @Repository
 * Purpose: Marks this as a Spring-managed repository component
 * What it does:
 *   - Spring creates this bean at startup
 *   - Enables exception translation (converts DB errors to Spring errors)
 *   - Makes it available for @Autowired injection
 * 
 * ═══════════════════════════════════════════════════════════════════
 * EXTENDS JpaRepository<Device, Long>
 * ═══════════════════════════════════════════════════════════════════
 * 
 * What this means:
 * - Device → This repository manages Device entities
 * - Long → The primary key type (Device.id is Long)
 * 
 * What you get FOR FREE:
 * - save(device) → INSERT or UPDATE
 * - findById(id) → SELECT by ID
 * - findAll() → SELECT all
 * - deleteById(id) → DELETE by ID
 * - count() → COUNT(*)
 * - existsById(id) → Check if exists
 * - and many more...
 * 
 * You don't write ANY of these — they're inherited from JpaRepository!
 */
@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {

    /*
     * ═══════════════════════════════════════════════════════════════════
     * QUERY METHOD: Find by IP Address
     * ═══════════════════════════════════════════════════════════════════
     * 
     * METHOD NAME PARSING:
     * Spring breaks down the method name into parts:
     * 
     * findBy + IpAddress
     *    ↓        ↓
     *  action   field name
     * 
     * Spring generates:
     * SELECT * FROM devices WHERE ip_address = ?
     * 
     * How Spring knows field name:
     * - Looks at Device class
     * - Finds field: private String ipAddress;
     * - Matches method name "IpAddress" to field "ipAddress"
     * - Database column: "ip_address" (JPA converts camelCase to snake_case)
     * 
     * ───────────────────────────────────────────────────────────────────
     * RETURN TYPE: Optional<Device>
     * ───────────────────────────────────────────────────────────────────
     * 
     * What is Optional?
     * - A container that either holds a value OR is empty
     * - Safer than returning null
     * 
     * Old way (using null):
     * Device device = repository.findByIpAddress("192.168.1.1");
     * if (device != null) {  // Easy to forget this check!
     *     device.getHostname();
     * }
     * 
     * New way (using Optional):
     * Optional<Device> deviceOpt = repository.findByIpAddress("192.168.1.1");
     * if (deviceOpt.isPresent()) {
     *     Device device = deviceOpt.get();
     *     device.getHostname();
     * }
     * 
     * Or more elegantly:
     * deviceOpt.ifPresent(device -> System.out.println(device.getHostname()));
     * 
     * Why Optional?
     * - Forces you to handle "not found" case
     * - No NullPointerException surprises
     * - Functional programming style
     * 
     * ───────────────────────────────────────────────────────────────────
     * USAGE EXAMPLE:
     * ───────────────────────────────────────────────────────────────────
     * 
     * Optional<Device> result = deviceRepository.findByIpAddress("192.168.1.1");
     * 
     * if (result.isPresent()) {
     *     Device device = result.get();
     *     System.out.println("Found: " + device.getHostname());
     * } else {
     *     System.out.println("Device not found");
     * }
     */
    Optional<Device> findByIpAddress(String ipAddress);

    /*
     * ═══════════════════════════════════════════════════════════════════
     * QUERY METHOD: Find by MAC Address
     * ═══════════════════════════════════════════════════════════════════
     * 
     * Why find by MAC?
     * - IP addresses can change (DHCP networks)
     * - MAC addresses are permanent (hardware-level)
     * 
     * Use case:
     * - Device had IP 192.168.1.100 yesterday
     * - Today it has IP 192.168.1.150 (router reassigned it)
     * - But MAC is still 00:1A:2B:3C:4D:5E
     * - We can find it by MAC and update its IP
     * 
     * Generated SQL:
     * SELECT * FROM devices WHERE mac_address = ?
     */
    Optional<Device> findByMacAddress(String macAddress);

    /*
     * ═══════════════════════════════════════════════════════════════════
     * QUERY METHOD: Find by Status
     * ═══════════════════════════════════════════════════════════════════
     * 
     * METHOD NAME PARSING:
     * findBy + Status
     * 
     * RETURN TYPE: List<Device>
     * - Not Optional because we can have MULTIPLE devices with same status
     * - If no devices found, returns empty list [] (never null)
     * 
     * Generated SQL:
     * SELECT * FROM devices WHERE status = ?
     * 
     * ───────────────────────────────────────────────────────────────────
     * PARAMETER TYPE: Device.DeviceStatus
     * ───────────────────────────────────────────────────────────────────
     * 
     * This is the enum we defined in Device class
     * 
     * Usage:
     * List<Device> onlineDevices = repository.findByStatus(DeviceStatus.ONLINE);
     * List<Device> offlineDevices = repository.findByStatus(DeviceStatus.OFFLINE);
     * 
     * Why this works:
     * - We used @Enumerated(EnumType.STRING) in Device class
     * - Database stores "ONLINE", "OFFLINE", "UNKNOWN" as strings
     * - JPA converts enum to string automatically
     * 
     * Use cases:
     * - Dashboard: "Show me all online devices"
     * - Monitoring: "Alert if more than 10 devices go offline"
     * - Reports: "Generate list of offline devices for investigation"
     */
    List<Device> findByStatus(Device.DeviceStatus status);

    /*
     * ═══════════════════════════════════════════════════════════════════
     * QUERY METHOD: Find Vulnerable Devices
     * ═══════════════════════════════════════════════════════════════════
     * 
     * METHOD NAME PARSING:
     * findBy + IsVulnerable + True
     *    ↓          ↓          ↓
     * action   field name   value
     * 
     * Generated SQL:
     * SELECT * FROM devices WHERE is_vulnerable = true
     * 
     * ───────────────────────────────────────────────────────────────────
     * SPECIAL NAMING CONVENTION: Boolean Fields
     * ───────────────────────────────────────────────────────────────────
     * 
     * For boolean fields, Spring recognizes special keywords:
     * - findBy[Field]True → WHERE field = true
     * - findBy[Field]False → WHERE field = false
     * 
     * Examples:
     * findByIsVulnerableTrue() → WHERE is_vulnerable = true
     * findByIsVulnerableFalse() → WHERE is_vulnerable = false
     * 
     * Alternative syntax:
     * findByIsVulnerable(Boolean value) → WHERE is_vulnerable = ?
     * Usage: findByIsVulnerable(true)
     * 
     * ───────────────────────────────────────────────────────────────────
     * USE CASES:
     * ───────────────────────────────────────────────────────────────────
     * 
     * Security Dashboard:
     * List<Device> vulnerable = repository.findByIsVulnerableTrue();
     * System.out.println("⚠️ " + vulnerable.size() + " vulnerable devices!");
     * 
     * Email Alerts:
     * if (!vulnerable.isEmpty()) {
     *     emailService.sendAlert("Vulnerable devices detected", vulnerable);
     * }
     * 
     * Reports:
     * Generate PDF listing all vulnerable devices with CVE details
     */
    List<Device> findByIsVulnerableTrue();

    /*
     * ═══════════════════════════════════════════════════════════════════
     * QUERY METHOD: Search by Hostname (Partial Match)
     * ═══════════════════════════════════════════════════════════════════
     * 
     * METHOD NAME PARSING:
     * findBy + Hostname + Containing + IgnoreCase
     *    ↓        ↓          ↓            ↓
     * action   field    LIKE '%?%'   case-insensitive
     * 
     * Generated SQL:
     * SELECT * FROM devices WHERE LOWER(hostname) LIKE LOWER(CONCAT('%', ?, '%'))
     * 
     * ───────────────────────────────────────────────────────────────────
     * SPECIAL KEYWORDS IN METHOD NAMES:
     * ───────────────────────────────────────────────────────────────────
     * 
     * Containing
     * - Adds wildcards on BOTH sides: %value%
     * - Finds partial matches anywhere in the string
     * 
     * Examples:
     * findByHostnameContaining("laptop")
     * Matches:
     *   ✓ "Johns-Laptop"
     *   ✓ "LAPTOP-01"
     *   ✓ "my-laptop-pc"
     *   ✗ "Desktop-PC"
     * 
     * IgnoreCase
     * - Makes search case-insensitive
     * - Uses LOWER() in SQL
     * 
     * Examples:
     * findByHostnameContainingIgnoreCase("laptop")
     * Matches:
     *   ✓ "LAPTOP"
     *   ✓ "Laptop"
     *   ✓ "laptop"
     *   ✓ "LaPtOp"
     * 
     * ───────────────────────────────────────────────────────────────────
     * OTHER USEFUL KEYWORDS:
     * ───────────────────────────────────────────────────────────────────
     * 
     * StartingWith → hostname LIKE 'value%'
     * EndingWith → hostname LIKE '%value'
     * Containing → hostname LIKE '%value%'
     * 
     * IgnoreCase → case-insensitive comparison
     * 
     * ───────────────────────────────────────────────────────────────────
     * USE CASE: Search Feature
     * ───────────────────────────────────────────────────────────────────
     * 
     * User types in search box: "printer"
     * 
     * Backend:
     * List<Device> results = repository.findByHostnameContainingIgnoreCase("printer");
     * 
     * Returns:
     * - "Office-Printer-1"
     * - "Color-Printer-Main"
     * - "HP-PRINTER-2"
     * - "backup-printer"
     */
    List<Device> findByHostnameContainingIgnoreCase(String hostname);

    /*
     * ═══════════════════════════════════════════════════════════════════
     * QUERY METHOD: Check if IP Exists
     * ═══════════════════════════════════════════════════════════════════
     * 
     * METHOD NAME PARSING:
     * existsBy + IpAddress
     *    ↓          ↓
     * action    field name
     * 
     * Generated SQL:
     * SELECT COUNT(*) > 0 FROM devices WHERE ip_address = ?
     * 
     * ───────────────────────────────────────────────────────────────────
     * RETURN TYPE: boolean
     * ───────────────────────────────────────────────────────────────────
     * 
     * Not Optional<Boolean> — just plain boolean
     * - true → device with this IP exists
     * - false → no device with this IP
     * 
     * ───────────────────────────────────────────────────────────────────
     * WHY USE existsBy INSTEAD OF findBy?
     * ───────────────────────────────────────────────────────────────────
     * 
     * More efficient when you only need to know if something exists
     * 
     * Less efficient:
     * Optional<Device> device = repository.findByIpAddress("192.168.1.1");
     * boolean exists = device.isPresent();
     * // Problem: Loads ENTIRE device from database (all fields)
     * // Just to check if it exists
     * 
     * More efficient:
     * boolean exists = repository.existsByIpAddress("192.168.1.1");
     * // Only runs COUNT query, doesn't load device data
     * 
     * ───────────────────────────────────────────────────────────────────
     * USE CASES:
     * ───────────────────────────────────────────────────────────────────
     * 
     * Before creating device:
     * if (repository.existsByIpAddress("192.168.1.1")) {
     *     throw new Exception("Device with this IP already exists!");
     * }
     * 
     * Validation:
     * if (!repository.existsByIpAddress(ipAddress)) {
     *     return "Error: Device not found";
     * }
     * 
     * Decision logic:
     * if (repository.existsByIpAddress(ip)) {
     *     // Update existing
     * } else {
     *     // Create new
     * }
     */
    boolean existsByIpAddress(String ipAddress);
}

/*
 * ═══════════════════════════════════════════════════════════════════
 * SPRING DATA JPA METHOD NAME PATTERNS (REFERENCE)
 * ═══════════════════════════════════════════════════════════════════
 * 
 * BASIC PATTERNS:
 * findBy[Field]                → WHERE field = ?
 * findBy[Field]And[Field2]     → WHERE field = ? AND field2 = ?
 * findBy[Field]Or[Field2]      → WHERE field = ? OR field2 = ?
 * 
 * COMPARISONS:
 * findBy[Field]LessThan        → WHERE field < ?
 * findBy[Field]GreaterThan     → WHERE field > ?
 * findBy[Field]Between         → WHERE field BETWEEN ? AND ?
 * 
 * STRING OPERATIONS:
 * findBy[Field]Like            → WHERE field LIKE ?
 * findBy[Field]Containing      → WHERE field LIKE %?%
 * findBy[Field]StartingWith    → WHERE field LIKE ?%
 * findBy[Field]EndingWith      → WHERE field LIKE %?
 * findBy[Field]IgnoreCase      → WHERE LOWER(field) = LOWER(?)
 * 
 * COLLECTIONS:
 * findBy[Field]In              → WHERE field IN (?, ?, ?)
 * findBy[Field]NotIn           → WHERE field NOT IN (?, ?, ?)
 * 
 * NULL CHECKS:
 * findBy[Field]IsNull          → WHERE field IS NULL
 * findBy[Field]IsNotNull       → WHERE field IS NOT NULL
 * 
 * BOOLEAN:
 * findBy[Field]True            → WHERE field = true
 * findBy[Field]False           → WHERE field = false
 * 
 * EXISTENCE:
 * existsBy[Field]              → SELECT COUNT(*) > 0 WHERE field = ?
 * 
 * COUNTING:
 * countBy[Field]               → SELECT COUNT(*) WHERE field = ?
 * 
 * DELETION:
 * deleteBy[Field]              → DELETE FROM table WHERE field = ?
 * 
 * ORDERING:
 * findBy[Field]OrderBy[Field2]Asc  → WHERE field = ? ORDER BY field2 ASC
 * findBy[Field]OrderBy[Field2]Desc → WHERE field = ? ORDER BY field2 DESC
 * 
 * LIMITING:
 * findTop10By[Field]           → WHERE field = ? LIMIT 10
 * findFirst5By[Field]          → WHERE field = ? LIMIT 5
 */