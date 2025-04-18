public enum RecordType {
    SINGLE, MEAN, AVERAGE, MULTI;

    @Override
    public String toString() {
        return name().charAt(0) + name().substring(1).toLowerCase();
    }
}