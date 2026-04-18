package com.srg.smartexpenseapi.entity;

public enum RepaymentType {
    EMI,               // Principal + Interest monthly
    MONTHLY_INTEREST,  // Only interest monthly, principal at end
    BULLET            // One lump sum at end
}
