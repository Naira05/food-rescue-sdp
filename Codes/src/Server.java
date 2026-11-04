import MVC.controller.DonationController;
import MVC.controller.UserController;
import MVC.database.Database;
import MVC.database.DonationRepository;
import MVC.database.NotificationRepository;
import MVC.database.UserRepository;
import MVC.donation.Donation;
import MVC.donation.DonationModel;
import MVC.view.DonationView;
import MVC.user.*;
import ObserverDP.AdminNotification;
import ObserverDP.DonorNotification;
import StrategyDP.APPStrategy;
import StrategyDP.EmailStrategy;
import StrategyDP.CommunicationManager;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.Executors;

/**
 * Tiny web layer: serves the ./web folder and exposes the existing controllers as JSON.
 *   run:  java -cp "out:lib/sqlite-jdbc.jar" Server        (then open http://localhost:8080)
 */
public class Server {
    static final String ADMIN_EMAIL = "sdpproject41@gmail.com";
    static final String ADMIN_PASSWORD = "admin123";

    static UserController users;
    static DonationController donations;
    static final Map<String, Integer> sessions = new HashMap<>();   // token -> user id
    static final Path WEB = Path.of(System.getProperty("web.dir", "web"));

    public static void main(String[] args) throws Exception {
        Database.open(System.getProperty("db.url", "jdbc:sqlite:foodrescue.db"));
        DonationModel model = new DonationModel(new DonationRepository());
        users = new UserController(new UserRepository());
        donations = new DonationController(model, new DonationView());
        CommunicationManager donorComms = new CommunicationManager(new APPStrategy());
        CommunicationManager adminComms = new CommunicationManager(new EmailStrategy());
        model.attach(new DonorNotification(donorComms, users::findById));
        model.attach(new AdminNotification(adminComms, ADMIN_EMAIL, users::findById));
        if (users.findByEmail(ADMIN_EMAIL).isEmpty()) {
            users.createAdmin("System Admin", ADMIN_EMAIL, ADMIN_PASSWORD);
            System.out.println("Admin created: " + ADMIN_EMAIL + " / " + ADMIN_PASSWORD);
        }

        int port = Integer.parseInt(System.getProperty("port", "8080"));
        HttpServer http = HttpServer.create(new InetSocketAddress(port), 0);
        http.createContext("/", Server::handle);
        // one thread only: the project shares a single SQLite connection
        http.setExecutor(Executors.newSingleThreadExecutor());
        http.start();
        Runtime.getRuntime().addShutdownHook(new Thread(Database::close));
        System.out.println("Food Rescue is running -> http://localhost:" + port);
    }

    // ------------------------------------------------------------------ routing
    static void handle(HttpExchange ex) throws IOException {
        try {
            String path = ex.getRequestURI().getPath();
            if (path.startsWith("/api/")) api(ex, ex.getRequestMethod(), path.substring(5));
            else staticFile(ex, path);
        } catch (HttpError e) {
            send(ex, e.code, "{\"error\":" + q(e.getMessage()) + "}");
        } catch (IllegalArgumentException | IllegalStateException e) {
            send(ex, 400, "{\"error\":" + q(e.getMessage()) + "}");
        } catch (Exception e) {
            e.printStackTrace();
            send(ex, 500, "{\"error\":" + q("Server error: " + e.getMessage()) + "}");
        }
    }

    static void api(HttpExchange ex, String m, String p) throws IOException {
        String[] s = p.split("/");
        // /donations/5 -> "donation", /donations/5/claim -> "claim"
        String key = s[0].equals("donations") && s.length > 1 ? (s.length > 2 ? s[2] : "donation") : s[0];
        String route = m + " " + key;

        // ---- public
        switch (route) {
            case "POST register" -> { register(ex); return; }
            case "POST login" -> { login(ex); return; }
            case "GET stats" -> { stats(ex); return; }
            case "GET types" -> { send(ex, 200, strList(donations.getDonationTypes())); return; }
        }

        User me = auth(ex);
        switch (route) {
            case "GET me" -> send(ex, 200, user(me));
            case "POST logout" -> {
                sessions.values().removeIf(id -> id == me.getUserID());
                send(ex, 200, "{\"ok\":true}");
            }
            case "PUT me" -> {
                Map<String, Object> b = body(ex);
                users.updateProfile(me, str(b, "name"), str(b, "email"));
                send(ex, 200, user(me));
            }
            case "GET notifications" -> send(ex, 200, strList(new NotificationRepository().findByRecipient(me.getEmail())));
            case "GET donations" -> {
                List<Donation> l = me instanceof Donor ? donations.getDonationsByDonor(me.getUserID())
                        : me instanceof Admin ? donations.getDonations()
                        : donations.getAvailableDonations();
                send(ex, 200, donationList(l));
            }
            case "POST donations" -> {
                need(me instanceof Donor, "Only donors can create donations");
                Map<String, Object> b = body(ex);
                Donation d = donations.createDonation(me.getUserID(), str(b, "type"), str(b, "description"),
                        num(b, "quantity"), str(b, "unit"), date(str(b, "expiryDate")));
                send(ex, 201, donation(d));
            }
            case "GET receipt" -> {
                int id = Integer.parseInt(s[1]);
                Donation d = donations.findDonation(id)
                        .orElseThrow(() -> new HttpError(404, "Donation not found"));
                need(me instanceof Admin || d.getDonorID() == me.getUserID(), "This is not your donation");
                send(ex, 200, receipt(d));
            }
            case "POST claim" -> {
                need(me instanceof Volunteer || me instanceof Beneficiary, "Only volunteers or beneficiaries can claim");
                int id = Integer.parseInt(s[1]);
                Donation d = donations.claimDonation(id);
                if (me instanceof Volunteer v) users.assignTask(v, "Pick up donation #" + id + " (" + d.getDescription() + ")");
                send(ex, 200, donation(d));
            }
            case "PUT donation" -> {
                int id = Integer.parseInt(s[1]);
                Donation.Status st = Donation.Status.of(str(body(ex), "status"));
                need(me instanceof Admin || (me instanceof Volunteer && st == Donation.Status.PICKED_UP), "Not allowed");
                send(ex, 200, donation(donations.updateDonation(id, null, null, st)));
            }
            case "DELETE donation" -> {
                need(me instanceof Admin, "Admins only");
                send(ex, 200, "{\"deleted\":" + donations.deleteDonation(Integer.parseInt(s[1])) + "}");
            }
            case "GET assignments" -> {
                need(me instanceof Volunteer, "Volunteers only");
                send(ex, 200, strList(((Volunteer) me).viewAssignment()));
            }
            case "GET requests" -> {
                need(me instanceof Beneficiary, "Beneficiaries only");
                StringBuilder sb = new StringBuilder("[");
                for (Beneficiary.FoodRequest r : ((Beneficiary) me).getRequests()) {
                    if (sb.length() > 1) sb.append(',');
                    sb.append("{\"item\":").append(q(r.item())).append(",\"quantity\":").append(r.quantity())
                            .append(",\"requestedOn\":").append(q(r.requestedOn().toString())).append('}');
                }
                send(ex, 200, sb.append(']').toString());
            }
            case "POST requests" -> {
                need(me instanceof Beneficiary, "Beneficiaries only");
                Map<String, Object> b = body(ex);
                users.requestFood((Beneficiary) me, str(b, "item"), num(b, "quantity"));
                send(ex, 201, "{\"ok\":true}");
            }
            // ---- admin
            case "GET users" -> {
                need(me instanceof Admin, "Admins only");
                StringBuilder sb = new StringBuilder("[");
                for (User u : users.getAllUsers()) { if (sb.length() > 1) sb.append(','); sb.append(user(u)); }
                send(ex, 200, sb.append(']').toString());
            }
            case "DELETE users" -> {
                need(me instanceof Admin, "Admins only");
                int id = Integer.parseInt(s[1]);
                if (id == me.getUserID()) throw new IllegalArgumentException("You cannot remove your own account");
                send(ex, 200, "{\"deleted\":" + users.removeUser(id) + "}");
            }
            case "POST types" -> {
                need(me instanceof Admin, "Admins only");
                send(ex, 200, "{\"added\":" + donations.addDonationType(str(body(ex), "name")) + "}");
            }
            case "PUT types" -> {
                need(me instanceof Admin, "Admins only");
                String name = java.net.URLDecoder.decode(s[1], StandardCharsets.UTF_8);
                send(ex, 200, "{\"renamed\":" + donations.renameDonationType(name, str(body(ex), "name")) + "}");
            }
            case "DELETE types" -> {
                need(me instanceof Admin, "Admins only");
                String name = java.net.URLDecoder.decode(s[1], StandardCharsets.UTF_8);
                send(ex, 200, "{\"removed\":" + donations.removeDonationType(name) + "}");
            }
            default -> throw new HttpError(404, "Unknown endpoint");
        }
    }

    // ------------------------------------------------------------------ handlers
    static void register(HttpExchange ex) throws IOException {
        Map<String, Object> b = body(ex);
        String role = String.valueOf(str(b, "role")).toUpperCase();
        String name = str(b, "name"), email = str(b, "email"), pw = str(b, "password");
        User u = switch (role) {
            case "DONOR" -> users.registerDonor(name, email, pw, str(b, "address"));
            case "VOLUNTEER" -> {
                List<String> skills = new ArrayList<>();
                for (String x : String.valueOf(str(b, "skills") == null ? "" : str(b, "skills")).split(",")) {
                    if (!x.isBlank()) skills.add(x.trim());
                }
                yield users.registerVolunteer(name, email, pw, skills, str(b, "availability"));
            }
            case "BENEFICIARY" -> users.registerBeneficiary(name, email, pw, str(b, "needs"));
            default -> throw new IllegalArgumentException("Choose a valid role");
        };
        startSession(ex, u);
    }

    static void login(HttpExchange ex) throws IOException {
        Map<String, Object> b = body(ex);
        User u = users.login(str(b, "email"), str(b, "password"))
                .orElseThrow(() -> new HttpError(401, "Wrong e-mail or password"));
        startSession(ex, u);
    }

    static void startSession(HttpExchange ex, User u) throws IOException {
        String token = UUID.randomUUID().toString();
        sessions.put(token, u.getUserID());
        send(ex, 200, "{\"token\":" + q(token) + ",\"user\":" + user(u) + "}");
    }

    static void stats(HttpExchange ex) throws IOException {
        List<Donation> all = donations.getDonations();
        long available = all.stream().filter(d -> d.getSt() == Donation.Status.AVAILABLE).count();
        long delivered = all.stream().filter(d -> d.getSt() == Donation.Status.PICKED_UP).count();
        send(ex, 200, "{\"donations\":" + all.size() + ",\"available\":" + available
                + ",\"delivered\":" + delivered + ",\"members\":" + users.getAllUsers().size() + "}");
    }

    // ------------------------------------------------------------------ helpers
    static User auth(HttpExchange ex) {
        String h = ex.getRequestHeaders().getFirst("Authorization");
        Integer id = h == null ? null : sessions.get(h.replace("Bearer ", "").trim());
        if (id == null) throw new HttpError(401, "Please log in");
        return users.findById(id).orElseThrow(() -> new HttpError(401, "Account no longer exists"));
    }

    static void need(boolean ok, String msg) { if (!ok) throw new HttpError(403, msg); }

    static String str(Map<String, Object> m, String k) {
        Object v = m.get(k);
        return v == null ? null : v.toString();
    }

    static double num(Map<String, Object> m, String k) {
        try { return Double.parseDouble(String.valueOf(m.get(k))); }
        catch (NumberFormatException e) { throw new IllegalArgumentException("Quantity must be a number"); }
    }

    static LocalDate date(String s) {
        try { return LocalDate.parse(s); }
        catch (Exception e) { throw new IllegalArgumentException("Expiry date must look like 2026-12-31"); }
    }

    static String user(User u) {
        StringBuilder sb = new StringBuilder("{\"id\":").append(u.getUserID())
                .append(",\"name\":").append(q(u.getName()))
                .append(",\"email\":").append(q(u.getEmail()))
                .append(",\"role\":").append(q(u.getRole().toUpperCase()));
        if (u instanceof Donor d) sb.append(",\"address\":").append(q(d.getAddress()));
        if (u instanceof Volunteer v) sb.append(",\"skills\":").append(strList(v.getSkills()))
                .append(",\"availability\":").append(q(v.getAvailability()));
        if (u instanceof Beneficiary b) sb.append(",\"needs\":").append(q(b.getNeeds()));
        return sb.append('}').toString();
    }

    static String donation(Donation d) {
        return "{\"id\":" + d.getDonationID() + ",\"donorId\":" + d.getDonorID()
                + ",\"type\":" + q(d.getType()) + ",\"description\":" + q(d.getDescription())
                + ",\"quantity\":" + d.getQuantity() + ",\"unit\":" + q(d.getUnit())
                + ",\"expiryDate\":" + q(d.getExpiryDate().toString())
                + ",\"donationDate\":" + q(d.getDonationDate().toString())
                + ",\"status\":" + q(d.getSt().name()) + "}";
    }
    static String receipt(Donation d) {
        User donor = users.findById(d.getDonorID()).orElse(null);
        String address = donor instanceof Donor dn ? dn.getAddress() : null;
        return "{\"receiptNo\":" + q(String.format("FR-%06d", d.getDonationID()))
                + ",\"organization\":" + q(SingletonDP.SystemConfig.getInstance().getOrganizationName())
                + ",\"donorName\":" + q(donor == null ? "Unknown donor" : donor.getName())
                + ",\"donorEmail\":" + q(donor == null ? null : donor.getEmail())
                + ",\"donorAddress\":" + q(address)
                + ",\"donation\":" + donation(d) + "}";
    }

    static String donationList(List<Donation> l) {
        StringBuilder sb = new StringBuilder("[");
        for (Donation d : l) { if (sb.length() > 1) sb.append(','); sb.append(donation(d)); }
        return sb.append(']').toString();
    }

    static String strList(List<String> l) {
        StringBuilder sb = new StringBuilder("[");
        for (String s : l) { if (sb.length() > 1) sb.append(','); sb.append(q(s)); }
        return sb.append(']').toString();
    }

    static String q(String s) {
        if (s == null) return "null";
        StringBuilder sb = new StringBuilder("\"");
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> { if (c < 0x20) sb.append(String.format("\\u%04x", (int) c)); else sb.append(c); }
            }
        }
        return sb.append('"').toString();
    }

    static void send(HttpExchange ex, int code, String json) throws IOException {
        sendBytes(ex, code, "application/json; charset=utf-8", json.getBytes(StandardCharsets.UTF_8));
    }

    static void sendBytes(HttpExchange ex, int code, String type, byte[] data) throws IOException {
        ex.getResponseHeaders().set("Content-Type", type);
        ex.getResponseHeaders().set("Cache-Control", "no-cache");
        ex.sendResponseHeaders(code, data.length == 0 ? -1 : data.length);
        if (data.length > 0) ex.getResponseBody().write(data);
        ex.close();
    }

    static void staticFile(HttpExchange ex, String path) throws IOException {
        if (path.equals("/")) path = "/index.html";
        Path file = WEB.resolve(path.substring(1)).normalize();
        if (!file.startsWith(WEB.normalize()) || !Files.isRegularFile(file)) file = WEB.resolve("index.html");
        String n = file.getFileName().toString();
        String type = n.endsWith(".css") ? "text/css; charset=utf-8"
                : n.endsWith(".js") ? "text/javascript; charset=utf-8"
                : n.endsWith(".svg") ? "image/svg+xml"
                : "text/html; charset=utf-8";
        sendBytes(ex, 200, type, Files.readAllBytes(file));
    }

    static class HttpError extends RuntimeException {
        final int code;
        HttpError(int code, String msg) { super(msg); this.code = code; }
    }

    // ------------------------------------------------------------------ minimal JSON object reader
    static Map<String, Object> body(HttpExchange ex) throws IOException {
        String t = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8).trim();
        if (t.isEmpty()) return new HashMap<>();
        int[] i = {0};
        Object v = readValue(t, i);
        if (!(v instanceof Map)) throw new IllegalArgumentException("Bad request body");
        @SuppressWarnings("unchecked") Map<String, Object> m = (Map<String, Object>) v;
        return m;
    }

    static void ws(String t, int[] i) { while (i[0] < t.length() && Character.isWhitespace(t.charAt(i[0]))) i[0]++; }

    static Object readValue(String t, int[] i) {
        ws(t, i);
        char c = t.charAt(i[0]);
        if (c == '{') {
            Map<String, Object> m = new HashMap<>();
            i[0]++; ws(t, i);
            if (t.charAt(i[0]) == '}') { i[0]++; return m; }
            while (true) {
                ws(t, i);
                String k = readString(t, i);
                ws(t, i); i[0]++;                       // ':'
                m.put(k, readValue(t, i));
                ws(t, i);
                if (t.charAt(i[0]++) == '}') return m;  // ',' or '}'
            }
        }
        if (c == '[') {
            List<Object> l = new ArrayList<>();
            i[0]++; ws(t, i);
            if (t.charAt(i[0]) == ']') { i[0]++; return l; }
            while (true) {
                l.add(readValue(t, i)); ws(t, i);
                if (t.charAt(i[0]++) == ']') return l;
            }
        }
        if (c == '"') return readString(t, i);
        int s = i[0];
        while (i[0] < t.length() && ",}] \n\r\t".indexOf(t.charAt(i[0])) < 0) i[0]++;
        String lit = t.substring(s, i[0]);
        return lit.equals("null") ? null : lit;         // numbers/booleans stay as text, parsed on demand
    }

    static String readString(String t, int[] i) {
        StringBuilder sb = new StringBuilder();
        i[0]++;                                         // opening quote
        while (t.charAt(i[0]) != '"') {
            char c = t.charAt(i[0]++);
            if (c == '\\') {
                char e = t.charAt(i[0]++);
                switch (e) {
                    case 'n' -> sb.append('\n');
                    case 't' -> sb.append('\t');
                    case 'r' -> sb.append('\r');
                    case 'u' -> { sb.append((char) Integer.parseInt(t.substring(i[0], i[0] + 4), 16)); i[0] += 4; }
                    default -> sb.append(e);
                }
            } else sb.append(c);
        }
        i[0]++;
        return sb.toString();
    }
}
