package ggr.urbanadventure;

public enum LocationType {
    WPIFACILITY, FOOD, CULTURE;

    @Override
    public String toString() {
        switch(this) {
            case WPIFACILITY: return "WPI Facility";
            case FOOD: return "Food";
            case CULTURE: return "Culture";
            default: throw new IllegalArgumentException();
        }
    }
}
