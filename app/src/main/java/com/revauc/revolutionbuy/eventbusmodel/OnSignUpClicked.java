package com.revauc.revolutionbuy.eventbusmodel;

/**
 * Created by nishant on 26/09/17.
 */

public class OnSignUpClicked {

    boolean isPositive;

    public OnSignUpClicked(boolean isPositive) {
        this.isPositive = isPositive;
    }

    public boolean isPositive() {
        return isPositive;
    }

    public void setPositive(boolean positive) {
        isPositive = positive;
    }
}
