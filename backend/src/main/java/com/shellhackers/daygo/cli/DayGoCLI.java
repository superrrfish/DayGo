package com.shellhackers.daygo.cli;

import com.shellhackers.daygo.model.Event;
import com.shellhackers.daygo.model.EventResponse;
import com.shellhackers.daygo.service.RoutingService;
import com.shellhackers.daygo.model.TransportMode;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;

@Component
@Profile("cli")
public class DayGoCLI implements CommandLineRunner {

    private final Map<String, Event> events;
    private final RoutingService routingService;
    private final Scanner scanner = new Scanner(System.in); // single shared scanner

    private static final String DEFAULT_ORIGIN = "Miami Lakes, FL";
    private static final String ANSI_RESET  = "\u001B[0m";
    private static final String ANSI_CYAN   = "\u001B[36m";
    private static final String ANSI_GREEN  = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_RED    = "\u001B[31m";
    private static final String ANSI_BOLD   = "\u001B[1m";

    public DayGoCLI(Map<String, Event> eventMap, RoutingService routingService) {
        this.events = eventMap;
        this.routingService = routingService;
    }

    @Override
    public void run(String @NonNull ... args) {
        printBanner();

        while (true) {
            System.out.print(ANSI_CYAN + "daygo> " + ANSI_RESET);
            String line = scanner.nextLine().trim();
            if (line.isBlank()) continue;

            String[] parts = splitArgs(line);
            String cmd = parts[0].toLowerCase();

            try {
                switch (cmd) {
                    case "ping"   -> cmdPing();
                    case "add"    -> cmdAdd();
                    case "list"   -> cmdList();
                    case "get"    -> cmdGet(parts);
                    case "delete" -> cmdDelete(parts);
                    case "plan"   -> cmdPlan(parts, 0);
                    case "chaos"  -> cmdChaos(parts);
                    case "help"   -> cmdHelp();
                    case "exit", "quit" -> {
                        System.out.println("Bye!");
                        return;
                    }
                    default -> System.out.println(ANSI_RED + "Unknown command: " + cmd + ". Type 'help'." + ANSI_RESET);
                }
            } catch (Exception e) {
                System.out.println(ANSI_RED + "Error: " + e.getMessage() + ANSI_RESET);
            }
        }
    }

    // ── Commands ────────────────────────────────────────────────────────────────

    private void cmdPing() {
        System.out.println(ANSI_GREEN + "pong ✓" + ANSI_RESET);
    }

    private void cmdAdd() {
        System.out.print("  Title: ");
        String title = scanner.nextLine().trim();

        System.out.print("  Destination: ");
        String destination = scanner.nextLine().trim();

        System.out.print("  Start time (HH:mm): ");
        String startTime = scanner.nextLine().trim();

        System.out.print("  End time (HH:mm, optional — press Enter to skip): ");
        String endTimeInput = scanner.nextLine().trim();
        String endTime = endTimeInput.isBlank() ? null : endTimeInput;

        System.out.print("  Mode (CAR/RIDESHARE/TRANSIT/WALK/BIKE): ");
        String modeInput = scanner.nextLine().trim().toUpperCase();

        TransportMode mode;
        try {
            mode = TransportMode.valueOf(modeInput);
        } catch (IllegalArgumentException e) {
            System.out.println(ANSI_RED + "Invalid mode. Choose: CAR RIDESHARE TRANSIT WALK BIKE" + ANSI_RESET);
            return;
        }

        try {
            LocalTime.parse(startTime, DateTimeFormatter.ofPattern("HH:mm"));
            if (endTime != null) LocalTime.parse(endTime, DateTimeFormatter.ofPattern("HH:mm"));
        } catch (Exception e) {
            System.out.println(ANSI_RED + "Invalid time format. Use HH:mm (24h), e.g. 14:30" + ANSI_RESET);
            return;
        }

        String id = UUID.randomUUID().toString().substring(0, 8);
        Event event = new Event(id, title, destination, startTime, endTime, mode);
        events.put(id, event);

        System.out.println(ANSI_GREEN + "Created event [" + id + "] ✓" + ANSI_RESET);
        printEvent(EventResponse.from(event));
    }

    private void cmdList() {
        if (events.isEmpty()) {
            System.out.println(ANSI_YELLOW + "No events. Use 'add' to create one." + ANSI_RESET);
            return;
        }
        System.out.println(ANSI_BOLD + "\n── Events ──────────────────────────────" + ANSI_RESET);
        events.values().stream()
                .sorted(Comparator.comparing(Event::startTime))
                .map(EventResponse::from)
                .forEach(this::printEvent);
    }

    private void cmdGet(String[] parts) {
        if (parts.length < 2) {
            System.out.println(ANSI_YELLOW + "Usage: get <id>" + ANSI_RESET);
            return;
        }
        Event event = requireEvent(parts[1]);
        printEvent(EventResponse.from(event));
    }

    private void cmdDelete(String[] parts) {
        if (parts.length < 2) {
            System.out.println(ANSI_YELLOW + "Usage: delete <id>" + ANSI_RESET);
            return;
        }
        Event event = requireEvent(parts[1]);
        events.remove(parts[1]);
        System.out.println(ANSI_GREEN + "Deleted \"" + event.title() + "\" [" + parts[1] + "]" + ANSI_RESET);
    }

    private void cmdPlan(String[] parts, int extraDelay) {
        if (parts.length < 2) {
            System.out.println(ANSI_YELLOW + "Usage: plan <id> [origin]" + ANSI_RESET);
            return;
        }
        Event event   = requireEvent(parts[1]);
        String origin = parts.length >= 3 ? parts[2] : DEFAULT_ORIGIN;

        System.out.println("Calculating route" + (extraDelay > 0 ? " (+" + extraDelay + " min chaos)" : "") + "...");

        int travelMinutes = routingService.getTravelMinutes(origin, event.location(), event.mode());
        int totalMinutes  = travelMinutes + extraDelay;

        LocalTime arrival   = LocalTime.parse(event.startTime(), DateTimeFormatter.ofPattern("HH:mm"));
        LocalTime departure = arrival.minusMinutes(totalMinutes);
        String departureStr = departure.format(DateTimeFormatter.ofPattern("HH:mm"));

        String color = extraDelay > 0 ? ANSI_RED : ANSI_GREEN;
        System.out.println(color + ANSI_BOLD);
        System.out.println("  Event    : " + event.title());
        System.out.println("  Arrive by: " + event.startTime());
        System.out.println("  Travel   : " + travelMinutes + " min (" + event.mode() + ")");
        if (extraDelay > 0) System.out.println("  Chaos    : +" + extraDelay + " min delay");
        System.out.println("  LEAVE AT : " + departureStr);
        System.out.println(ANSI_RESET);
    }

    private void cmdChaos(String[] parts) {
        if (parts.length < 2) {
            System.out.println(ANSI_YELLOW + "Usage: chaos <id> [delayMinutes]" + ANSI_RESET);
            return;
        }
        int delay = 15;
        if (parts.length >= 3) {
            try {
                delay = Integer.parseInt(parts[2]);
            } catch (NumberFormatException e) {
                System.out.println(ANSI_RED + "delayMinutes must be a number." + ANSI_RESET);
                return;
            }
        }
        System.out.println(ANSI_RED + "🔥 CHAOS MODE: +" + delay + " min" + ANSI_RESET);
        cmdPlan(parts, delay);
    }

    private void cmdHelp() {
        System.out.println(ANSI_BOLD + "\n── DayGo CLI Commands ──────────────────────────────────────────" + ANSI_RESET);
        System.out.println("  ping                              Health check");
        System.out.println("  add                               Create an event (interactive)");
        System.out.println("  list                              List all events");
        System.out.println("  get <id>                          Show one event");
        System.out.println("  delete <id>                       Delete an event");
        System.out.println("  plan <id> [origin]                Calculate departure time");
        System.out.println("  chaos <id> [delayMinutes]         Plan with delay (default 15 min)");
        System.out.println("  help                              Show this message");
        System.out.println("  exit / quit                       Exit CLI");
        System.out.println();
        System.out.println("  Modes: CAR  RIDESHARE  TRANSIT  WALK  BIKE");
        System.out.println("  Times: 24h format, e.g. 14:30");
        System.out.println();
    }

    // ── Helpers ─────────────────────────────────────────────────────────────────

    private Event requireEvent(String id) {
        Event event = events.get(id);
        if (event == null) throw new IllegalArgumentException("No event with id: " + id);
        return event;
    }

    private void printEvent(EventResponse e) {
        String statusColor = switch (e.status()) {
            case "active"   -> ANSI_GREEN;
            case "upcoming" -> ANSI_CYAN;
            case "past"     -> ANSI_YELLOW;
            default         -> ANSI_RESET;
        };
        System.out.printf("  [%s] %s%-8s%s %s | %s→%s | %s%n",
                e.id(),
                statusColor, e.status(), ANSI_RESET,
                e.title(),
                e.startTime(), e.endTime() != null ? e.endTime() : "?",
                e.location());
    }

    private void printBanner() {
        System.out.println(ANSI_CYAN + ANSI_BOLD);
        System.out.println("  ██████╗  █████╗ ██╗   ██╗ ██████╗  ██████╗ ");
        System.out.println("  ██╔══██╗██╔══██╗╚██╗ ██╔╝██╔════╝ ██╔═══██╗");
        System.out.println("  ██║  ██║███████║ ╚████╔╝ ██║  ███╗██║   ██║");
        System.out.println("  ██║  ██║██╔══██║  ╚██╔╝  ██║   ██║██║   ██║");
        System.out.println("  ██████╔╝██║  ██║   ██║   ╚██████╔╝╚██████╔╝");
        System.out.println("  ╚═════╝ ╚═╝  ╚═╝   ╚═╝    ╚═════╝  ╚═════╝ ");
        System.out.println(ANSI_RESET);
        System.out.println("  ShellHacks 2026 — Type 'help' to get started.");
        System.out.println();
    }

    private String[] splitArgs(String line) {
        java.util.List<String> tokens = new java.util.ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (char c : line.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ' ' && !inQuotes) {
                if (!current.isEmpty()) {
                    tokens.add(current.toString());
                    current.setLength(0);
                }
            } else {
                current.append(c);
            }
        }
        if (!current.isEmpty()) tokens.add(current.toString());
        return tokens.toArray(new String[0]);
    }
}