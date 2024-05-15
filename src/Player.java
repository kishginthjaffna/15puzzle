public class Player {
    private String name;
    private int timeTaken;

    public Player(String name, int timeTaken) {
        this.name = name;
        this.timeTaken = timeTaken;
    }

    public String getName() {
        return name;
    }

    public int getTimeTaken() {
        return timeTaken;
    }

    @Override
    public String toString() {
        return name + " - " + formatTime(timeTaken);
    }

    private String formatTime(int seconds) {
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int secs = seconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, secs);
    }
}
