package kjd.reactnative.bluetooth;

public enum BondState {
    BOND_NONE(10),
    BOND_BONDING(11),
    BOND_BONDED(12);

    public final int value;
    BondState(int value) {
        this.value = value;
    }

    public static final BondState[] VALUES = BondState.values();

    public static BondState fromValue(int value) {
        for (BondState state : VALUES) {
            if (value == state.value) {
                return state;
            }
        }

        throw new EnumConstantNotPresentException(BondState.class, Integer.toString(value));
    }
}
